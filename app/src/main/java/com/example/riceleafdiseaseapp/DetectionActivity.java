package com.example.riceleafdiseaseapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetectionActivity extends AppCompatActivity {

    ImageView[] imageViews = new ImageView[5];
    Button btnDetect;
    ProgressBar loadingSpinner;
    LinearLayout resultContainer;
    TextView tvMajorResult, tvAverages;

    private Interpreter tfliteHealthy;
    private Interpreter tfliteDisease;
    private final int IMG_SIZE = 224;
    private ArrayList<Uri> capturedImages;

    // பயனர் ID-ஐ சேமிக்க
    private String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detection);

        // SharedPreferences-லிருந்து லாகின் செய்த பயனர் ID-ஐ எடுத்தல்
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        // "user_id" என்பது உங்கள் லாகின் பக்கத்தில் நீங்கள் கொடுத்த கீ (Key) பெயராக இருக்க வேண்டும்
        loggedInUserId = sharedPreferences.getString("user_id", "0");

        imageViews[0] = findViewById(R.id.iv1);
        imageViews[1] = findViewById(R.id.iv2);
        imageViews[2] = findViewById(R.id.iv3);
        imageViews[3] = findViewById(R.id.iv4);
        imageViews[4] = findViewById(R.id.iv5);
        btnDetect = findViewById(R.id.btnDetect);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        resultContainer = findViewById(R.id.resultContainer);
        tvMajorResult = findViewById(R.id.tvMajorResult);
        tvAverages = findViewById(R.id.tvAverages);

        capturedImages = getIntent().getParcelableArrayListExtra("CAPTURED_IMAGES");

        if (capturedImages != null) {
            for (int i = 0; i < capturedImages.size(); i++) {
                if (i < 5) imageViews[i].setImageURI(capturedImages.get(i));
            }
        }

        try {
            tfliteHealthy = new Interpreter(FileUtil.loadMappedFile(this, "model_healthy.tflite"));
            tfliteDisease = new Interpreter(FileUtil.loadMappedFile(this, "model_disease.tflite"));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Model Load Failed", Toast.LENGTH_SHORT).show();
        }

        btnDetect.setOnClickListener(v -> runRealAnalysis());
    }

    private void runRealAnalysis() {
        if (capturedImages == null || capturedImages.isEmpty()) {
            Toast.makeText(this, "No images to analyze", Toast.LENGTH_SHORT).show();
            return;
        }

        btnDetect.setVisibility(View.GONE);
        loadingSpinner.setVisibility(View.VISIBLE);

        new Thread(() -> {
            float sumHealthy = 0, sumBlast = 0, sumBrown = 0, sumUnknown = 0;

            try {
                for (Uri uri : capturedImages) {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                    TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
                    tensorImage.load(bitmap);
                    ImageProcessor processor = new ImageProcessor.Builder()
                            .add(new ResizeOp(IMG_SIZE, IMG_SIZE, ResizeOp.ResizeMethod.BILINEAR))
                            .add(new NormalizeOp(0f, 255f))
                            .build();
                    tensorImage = processor.process(tensorImage);

                    // Step 1: Healthy vs Diseased
                    float[][] healthyOutput = new float[1][1];
                    tfliteHealthy.run(tensorImage.getBuffer(), healthyOutput);
                    float healthyProb = healthyOutput[0][0];

                    if (healthyProb >= 0.8f) {
                        sumHealthy += healthyProb;
                    } else {
                        // Step 2: Identify Disease
                        float[][] diseaseOutput = new float[1][2];
                        tfliteDisease.run(tensorImage.getBuffer(), diseaseOutput);

                        float brownProb = diseaseOutput[0][0];
                        float blastProb = diseaseOutput[0][1];

                        if (Math.max(brownProb, blastProb) >= 0.7f) {
                            sumBrown += brownProb;
                            sumBlast += blastProb;
                        } else {
                            sumUnknown += Math.max(brownProb, blastProb);
                        }
                    }
                }

                int totalImgs = capturedImages.size();
                float avgHealthy = (sumHealthy / totalImgs) * 100;
                float avgBlast = (sumBlast / totalImgs) * 100;
                float avgBrown = (sumBrown / totalImgs) * 100;
                float avgUnknown = (sumUnknown / totalImgs) * 100;

                String finalResult = "Unknown";
                float maxVal = avgUnknown;

                if (avgHealthy > maxVal) { finalResult = "Healthy"; maxVal = avgHealthy; }
                if (avgBlast > maxVal) { finalResult = "Rice Blast"; maxVal = avgBlast; }
                if (avgBrown > maxVal) { finalResult = "Brown Spot"; maxVal = avgBrown; }

                final String resultText = finalResult;
                final float resultConf = maxVal;
                final String report = String.format("Averages:\n• Blast: %.1f%%\n• Brown: %.1f%%\n• Healthy: %.1f%%\n• Unknown: %.1f%%",
                        avgBlast, avgBrown, avgHealthy, avgUnknown);

                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    resultContainer.setVisibility(View.VISIBLE);
                    tvMajorResult.setText("Result: " + resultText);
                    tvAverages.setText(report);

                    // டேட்டாபேஸில் சேமித்தல்
                    saveToDatabase(resultText, resultConf);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    loadingSpinner.setVisibility(View.GONE);
                    btnDetect.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Analysis Error", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveToDatabase(String disease, float confidence) {
        // உங்கள் சர்வர் IP முகவரியை உறுதி செய்து கொள்ளவும்
        String url = APIConfig.BASE_URL + "save_history.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> Log.d("DB_SUCCESS", "Server Response: " + response),
                error -> Log.e("DB_ERROR", "Network Error: " + error.toString())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // இப்போது loggedInUserId என்பது டைனமிக் ஆக மாறும்
                params.put("user_id", loggedInUserId);
                params.put("disease_name", disease);
                params.put("confidence", String.valueOf(confidence));
                return params;
            }
        };
        queue.add(postRequest);
    }
}