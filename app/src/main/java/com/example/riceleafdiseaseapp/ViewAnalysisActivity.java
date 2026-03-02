package com.example.riceleafdiseaseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewAnalysisActivity extends AppCompatActivity {

    // UI Elements
    TextView tvHeader, tvStats;
    LinearLayout expertSection;
    Spinner spinnerActiveFarmers;
    ListView lvRecentDetections;

    // Adapters and Lists
    ArrayList<String> detectionList = new ArrayList<>();
    ArrayAdapter<String> detectionAdapter;
    ArrayList<String> farmerList = new ArrayList<>();
    ArrayAdapter<String> farmerAdapter;

    // User Session Data
    String loginUsername, loginRole;
    boolean isSpinnerPopulated = false;

    // API URL (Change to your hosting IP if not local)
    // 👈 இப்படி மாற்றவும்
    String ANALYSIS_URL = APIConfig.BASE_URL + "get_analysis.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_analysis);

        // 1. Initialize UI
        tvHeader = findViewById(R.id.tvHeader);
        tvStats = findViewById(R.id.tvStats);
        expertSection = findViewById(R.id.expertSection);
        spinnerActiveFarmers = findViewById(R.id.spinnerActiveFarmers);
        lvRecentDetections = findViewById(R.id.lvRecentDetections);

        // 2. Get Data from Intent
        loginUsername = getIntent().getStringExtra("USERNAME");
        loginRole = getIntent().getStringExtra("USER_ROLE");

        // 3. Setup ListAdapter
        detectionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, detectionList);
        lvRecentDetections.setAdapter(detectionAdapter);

        // 4. Initial UI Logic
        setupInitialUI();

        // 5. Load Data for the first time
        // Initially, we load data for the logged-in user themselves
        loadAnalysisData(loginUsername);
    }

    private void setupInitialUI() {
        if ("Farmer".equals(loginRole)) {
            tvHeader.setText("Farmer: " + loginUsername);
            expertSection.setVisibility(View.GONE); // Farmers don't see the dropdown
        } else if ("Admin".equals(loginRole)) {
            tvHeader.setText("Admin Dashboard");
            expertSection.setVisibility(View.GONE); // Admins now also don't see the dropdown
        } else {
            tvHeader.setText("FieldExpert Dashboard");
            expertSection.setVisibility(View.VISIBLE); // Experts see the dropdown
        }
    }

    private void loadAnalysisData(final String targetUser) {
        StringRequest request = new StringRequest(Request.Method.POST, ANALYSIS_URL,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        // A. Set Yearly Summary (Multi-line formatted from PHP)
                        // This matches the complex summary you requested
                        tvStats.setText(obj.getString("stats_summary"));

                        // B. Handle Recent Detections List (Last 24h)
                        JSONArray recentArray = obj.getJSONArray("recent_detections");
                        detectionList.clear();
                        for (int i = 0; i < recentArray.length(); i++) {
                            JSONObject d = recentArray.getJSONObject(i);
                            String entry = d.getString("disease_name") + " [" +
                                    d.getString("confidence") + "%]\n" +
                                    d.getString("detected_time");
                            detectionList.add(entry);
                        }
                        detectionAdapter.notifyDataSetChanged();

                        // C. Handle Active Farmers Spinner (Expert/Admin Only)
                        if (!"Farmer".equals(loginRole) && obj.has("active_farmers") && !isSpinnerPopulated) {
                            JSONArray activeFarmers = obj.getJSONArray("active_farmers");

                            farmerList.clear();
                            farmerList.add("-- Select Active Farmer (24h) --");

                            for (int i = 0; i < activeFarmers.length(); i++) {
                                farmerList.add(activeFarmers.getString(i));
                            }

                            farmerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, farmerList);
                            farmerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerActiveFarmers.setAdapter(farmerAdapter);

                            spinnerActiveFarmers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (position > 0) {
                                        String selectedFarmer = farmerList.get(position);
                                        // Load the specific 24h feed for the selected farmer
                                        loadAnalysisData(selectedFarmer);
                                    }
                                }
                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {}
                            });
                            isSpinnerPopulated = true;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Data Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Server Connection Failed", Toast.LENGTH_SHORT).show()) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // targetUser = selected farmer from spinner or logged-in farmer
                params.put("username", targetUser);
                params.put("role", loginRole);
                params.put("expert_name", loginUsername); // Used by PHP to determine region/admin access
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}