package com.example.riceleafdiseaseapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class AdminApprovalActivity extends AppCompatActivity {

    private RecyclerView rvPendingExperts;
    private ExpertAdapter adapter;
    private List<User> expertList;
    // Using 10.0.2.2 for Android Emulator to access local XAMPP/WAMP
    private final String fetchUrl = APIConfig.BASE_URL + "get_pending_experts.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_approval);

        // 1. Initialize UI
        rvPendingExperts = findViewById(R.id.rvPendingExperts);
        rvPendingExperts.setLayoutManager(new LinearLayoutManager(this));

        // 2. Initialize Data List
        expertList = new ArrayList<>();

        // 3. Start Data Fetching
        loadPendingExperts();
    }

    private void loadPendingExperts() {
        StringRequest request = new StringRequest(Request.Method.GET, fetchUrl,
                response -> {
                    // This block runs when the server responds
                    try {
                        // Clear list to prevent duplicates on refresh
                        expertList.clear();

                        // Convert the string response into a JSON Array
                        JSONArray array = new JSONArray(response);

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);

                            // Create a new User object from JSON data
                            User user = new User(
                                    obj.getString("user_id"),
                                    obj.getString("username"),
                                    obj.getString("email"),
                                    obj.getString("region")
                            );
                            expertList.add(user);
                        }

                        // 4. Initialize Adapter with the correct Activity Context
                        adapter = new ExpertAdapter(AdminApprovalActivity.this, expertList);
                        rvPendingExperts.setAdapter(adapter);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        // If PHP returns an error string instead of JSON, this catches it
                        Log.e("JSON_ERROR", "Error: " + e.getMessage() + " Response: " + response);
                        Toast.makeText(AdminApprovalActivity.this, "Data Error: " + response, Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // This block runs if the server is offline or URL is wrong
                    Log.e("VOLLEY_ERROR", "Error: " + error.toString());
                    Toast.makeText(AdminApprovalActivity.this, "Server Connection Failed", Toast.LENGTH_SHORT).show();
                }
        );

        // --- THE FIX: USE getApplicationContext() ---
        // This ensures the RequestQueue is handled safely by the app system
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
}