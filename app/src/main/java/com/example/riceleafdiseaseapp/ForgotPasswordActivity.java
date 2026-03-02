package com.example.riceleafdiseaseapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnSendOtp;

    // எமுலேட்டருக்கு 10.0.2.2 என்பது சரியான முகவரி
    String url = APIConfig.BASE_URL + "forgot_password.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendOtp = findViewById(R.id.btnSendOtp);

        btnSendOtp.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {
        final String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        // முன்னேற்றத்தைக் காட்ட ஒரு மெசேஜ்
        Toast.makeText(this, "Sending OTP...", Toast.LENGTH_SHORT).show();

        StringRequest request = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    // சர்வர் பதிலில் உள்ள இடைவெளிகளை நீக்குதல்
                    String cleanResponse = response.trim();
                    Log.d("SERVER_RES", "Response: " + cleanResponse);

                    if (cleanResponse.contains("otp_sent")) {
                        Toast.makeText(this, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, VerifyOtpActivity.class);
                        intent.putExtra("EMAIL", email);
                        startActivity(intent);
                    } else if (cleanResponse.contains("email_not_found")) {
                        Toast.makeText(this, "Email not registered!", Toast.LENGTH_SHORT).show();
                    } else if (cleanResponse.contains("mail_error")) {
                        Toast.makeText(this, "Failed to send email. Check Internet!", Toast.LENGTH_SHORT).show();
                    } else {
                        // வேறு ஏதேனும் மெசேஜ் வந்தால் அதைத் திரையில் காட்டும் (Debug செய்ய உதவும்)
                        Toast.makeText(this, "Server Response: " + cleanResponse, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Log.e("VOLLEY_ERROR", error.toString());
                    Toast.makeText(this, "Server/Network Error. Try again!", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}