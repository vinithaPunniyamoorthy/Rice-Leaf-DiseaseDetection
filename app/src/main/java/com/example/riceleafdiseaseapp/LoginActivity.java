package com.example.riceleafdiseaseapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    private static final String loginUrl = APIConfig.BASE_URL + "login.php";

    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvForgotPassword, tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });
    }

    private void loginUser() {
        String login = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(
                Request.Method.POST,
                loginUrl,
                response -> {
                    response = response.trim();

                    // 🔴 FIELD EXPERT PENDING CASE
                    if (response.equals("pending")) {
                        Toast.makeText(this,
                                "Your Field Expert account is awaiting admin approval.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // ✅ SUCCESS CASE
                    if (response.startsWith("success")) {

                        // PHP இப்போது "success|username|role|user_id" என்று அனுப்புகிறது
                        String[] parts = response.split("\\|");

                        if (parts.length == 4) {
                            String username = parts[1];
                            String role = parts[2];
                            String userId = parts[3]; // இதுதான் முக்கியம்

                            // 🔹 முக்கியமான மாற்றம்: user_id-ஐ SharedPreferences-ல் சேமித்தல்
                            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_id", userId);
                            editor.apply();

                            // ஹோம் ஸ்கிரீனுக்கு செல்லுதல்
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("USERNAME", username);
                            intent.putExtra("USER_ROLE", role);
                            startActivity(intent);
                            finish();
                        }

                    } else {
                        // ❌ INVALID LOGIN
                        Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Server error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("login", login);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}