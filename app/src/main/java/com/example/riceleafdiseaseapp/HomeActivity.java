package com.example.riceleafdiseaseapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int GALLERY_PERMISSION_CODE = 102;
    private static final int CAMERA_REQUEST_CODE = 201;
    private static final int GALLERY_REQUEST_CODE = 202;

    TextView tvWelcome;
    Button btnCaptureImage, btnUploadImage, btnViewAnalysis, btnFeedback, btnLogout, btnViewFeedback, btnAdminApproval;

    String userRole, username;
    ArrayList<Uri> capturedImages = new ArrayList<>();
    int imageCounter = 0;
    Uri photoURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // --- UI உறுப்புகளை இணைத்தல் ---
        tvWelcome = findViewById(R.id.tvWelcome);
        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnViewAnalysis = findViewById(R.id.btnViewAnalysis);
        btnFeedback = findViewById(R.id.btnFeedback);
        btnLogout = findViewById(R.id.btnLogout);
        btnViewFeedback = findViewById(R.id.btnViewFeedback);
        btnAdminApproval = findViewById(R.id.btnAdminApproval);

        // --- லாகின் தகவல்கள் ---
        Intent intent = getIntent();
        userRole = intent.getStringExtra("USER_ROLE");
        username = intent.getStringExtra("USERNAME");
        tvWelcome.setText("Welcome, " + username);

        // --- ரோல் அடிப்படை பட்டன் பார்வை ---
        btnFeedback.setVisibility("FieldExpert".equals(userRole) ? View.VISIBLE : View.GONE);
        btnViewFeedback.setVisibility("Farmer".equals(userRole) || "Admin".equals(userRole) ? View.VISIBLE : View.GONE);
        btnAdminApproval.setVisibility("Admin".equals(userRole) ? View.VISIBLE : View.GONE);

        // --- பட்டன் கிளிக் நிகழ்வுகள் ---
        btnCaptureImage.setOnClickListener(v -> checkCameraPermission());
        btnUploadImage.setOnClickListener(v -> checkGalleryPermission());
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnFeedback.setOnClickListener(v -> {
            Intent i = new Intent(this, FeedbackActivity.class);
            i.putExtra("EXPERT_USERNAME", username);
            startActivity(i);
        });

        btnViewFeedback.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewFeedbackActivity.class);
            i.putExtra("USER_ROLE", userRole);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        btnViewAnalysis.setOnClickListener(v -> {
            Intent i = new Intent(this, ViewAnalysisActivity.class);
            i.putExtra("USER_ROLE", userRole);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });

        btnAdminApproval.setOnClickListener(v -> {
            Intent i = new Intent(this, AdminApprovalActivity.class);
            i.putExtra("USERNAME", username);
            startActivity(i);
        });
    }

    // --- 📷 கேமரா அனுமதி (AlertDialog உடன்) ---
    private void checkCameraPermission() {
        new AlertDialog.Builder(this)
                .setTitle("Camera Permission Request")
                .setMessage("Allow access for camera?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
                    } else {
                        start5PointCapture();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(this, "Camera permission cancelled.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    // --- 📂 கேலரி அனுமதி (AlertDialog உடன்) ---
    private void checkGalleryPermission() {
        new AlertDialog.Builder(this)
                .setTitle("Gallery Permission Request")
                .setMessage("Allow access for gallery?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    String permission;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permission = Manifest.permission.READ_MEDIA_IMAGES;
                    } else {
                        permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                    }

                    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{permission}, GALLERY_PERMISSION_CODE);
                    } else {
                        openGallery();
                    }
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Toast.makeText(this, "Gallery permission cancelled.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .show();
    }

    // --- அனுமதிகள் முடிவு ---
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == CAMERA_PERMISSION_CODE) {
                start5PointCapture();
            } else if (requestCode == GALLERY_PERMISSION_CODE) {
                openGallery();
            }
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    // --- கேலரி திறத்தல் ---
    private void openGallery() {
        Toast.makeText(this, "Select five rice leaf images", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select 5 Images"), GALLERY_REQUEST_CODE);
    }

    // --- 5 படங்களை எடுக்கும் முறை ---
    private void start5PointCapture() {
        imageCounter = 0;
        capturedImages.clear();
        captureNextImage();
    }

    private void captureNextImage() {
        if (imageCounter < 5) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try { photoFile = createImageFile(); } catch (IOException e) { e.printStackTrace(); }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.riceleafdiseaseapp.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        } else {
            navigateToAnalysis();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    // --- கேமரா / கேலரி முடிவுகள் ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                capturedImages.add(photoURI);
                imageCounter++;
                if (imageCounter < 5) showNextPointInstruction();
                else navigateToAnalysis();
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                capturedImages.clear();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    if (count == 5) {
                        for (int i = 0; i < 5; i++) {
                            capturedImages.add(data.getClipData().getItemAt(i).getUri());
                        }
                        navigateToAnalysis();
                    } else {
                        Toast.makeText(this, "Please select exactly 5 images", Toast.LENGTH_SHORT).show();
                    }
                } else if (data.getData() != null) {
                    Toast.makeText(this, "Please long-press to select 5 images", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showNextPointInstruction() {
        new AlertDialog.Builder(this)
                .setMessage("Go to the next corner of the field ➔")
                .setPositiveButton("OK", (dialog, which) -> captureNextImage())
                .setCancelable(false).show();
    }

    private void navigateToAnalysis() {
        Intent intent = new Intent(this, DetectionActivity.class);
        intent.putParcelableArrayListExtra("CAPTURED_IMAGES", capturedImages);
        startActivity(intent);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .setNegativeButton("No", null).show();
    }
}