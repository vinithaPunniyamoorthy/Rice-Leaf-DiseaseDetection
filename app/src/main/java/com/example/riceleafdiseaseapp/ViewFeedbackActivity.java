package com.example.riceleafdiseaseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewFeedbackActivity extends AppCompatActivity {

    ListView lvFeedback;
    TextView tvEmptyMessage;
    Button btnBack;
    ArrayList<String> feedbackList;
    ArrayAdapter<String> adapter;

    String username, userRole;
    private static final String FETCH_URL = APIConfig.BASE_URL + "get_feedback.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_feedback);

        lvFeedback = findViewById(R.id.lvFeedback);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        btnBack = findViewById(R.id.btnBack);

        username = getIntent().getStringExtra("USERNAME");
        userRole = getIntent().getStringExtra("USER_ROLE");

        feedbackList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, feedbackList);
        lvFeedback.setAdapter(adapter);

        fetchFeedback();

        btnBack.setOnClickListener(v -> finish());
    }

    private void fetchFeedback() {
        StringRequest request = new StringRequest(Request.Method.POST, FETCH_URL,
                response -> {
                    try {
                        JSONArray array = new JSONArray(response);
                        if (array.length() == 0) {
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                        } else {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject obj = array.getJSONObject(i);
                                String msg = "From: " + obj.getString("sender_username") +
                                        "\nMessage: " + obj.getString("message") +
                                        "\nDate: " + obj.getString("created_at");
                                feedbackList.add(msg);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "No feedback data available", Toast.LENGTH_SHORT).show();
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                    }
                },
                error -> Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("role", userRole);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}