package com.example.riceleafdiseaseapp;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    EditText etRecipientUsername, etFeedback;
    Button btnSubmit, btnCancel;

    String expertUsername;

    private static final String FEEDBACK_URL = APIConfig.BASE_URL + "send_feedback.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        etRecipientUsername = findViewById(R.id.etRecipientUsername);
        etFeedback = findViewById(R.id.etFeedback);
        btnSubmit = findViewById(R.id.btnSubmitFeedback);
        btnCancel = findViewById(R.id.btnCancel);


        expertUsername = getIntent().getStringExtra("EXPERT_USERNAME");

        // Safety check (VERY IMPORTANT)
        if (expertUsername == null || expertUsername.isEmpty()) {
            Toast.makeText(this, "Expert login missing", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Debug (you can remove later)
        Toast.makeText(this, "Logged in as: " + expertUsername, Toast.LENGTH_SHORT).show();


        btnSubmit.setOnClickListener(v -> submitFeedback());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void submitFeedback() {
        String recipientUsername =
                etRecipientUsername.getText().toString().trim();
        String message =
                etFeedback.getText().toString().trim();

        if (recipientUsername.isEmpty() || message.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                FEEDBACK_URL,
                response -> {
                    response = response.trim();

                    if (response.equalsIgnoreCase("success")) {
                        Toast.makeText(this, "Feedback sent successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (response.equalsIgnoreCase("user_not_found")) {
                        Toast.makeText(this, "Recipient user not found", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed: " + response, Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    Toast.makeText(this, "Server error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }

        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("recipient_username", recipientUsername);
                params.put("sender_username", expertUsername);
                params.put("message", message);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
