package com.example.riceleafdiseaseapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpertAdapter extends RecyclerView.Adapter<ExpertAdapter.ExpertViewHolder> {

    private Context context;
    private List<User> expertList;
    private final String updateUrl = APIConfig.BASE_URL + "approve_reject_expert.php";

    public ExpertAdapter(Context context, List<User> expertList) {
        this.context = context;
        this.expertList = expertList;
    }

    @NonNull
    @Override
    public ExpertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expert, parent, false);
        return new ExpertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpertViewHolder holder, int position) {
        User user = expertList.get(position);
        holder.tvName.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRegion.setText("Region: " + user.getRegion());

        holder.btnApprove.setOnClickListener(v -> updateStatus(user.getId(), "approve", position));
        holder.btnReject.setOnClickListener(v -> updateStatus(user.getId(), "reject", position));
    }

    private void updateStatus(String userId, String action, int position) {
        StringRequest request = new StringRequest(Request.Method.POST, updateUrl,
                response -> {
                    if (response.trim().equals("success")) {
                        expertList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, expertList.size());
                        Toast.makeText(context, "Expert " + action + "d successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Server Error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                params.put("action", action);
                return params;
            }
        };
        Volley.newRequestQueue(context).add(request);
    }

    @Override
    public int getItemCount() { return expertList.size(); }

    public static class ExpertViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRegion;
        Button btnApprove, btnReject;

        public ExpertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvExpertName);
            tvEmail = itemView.findViewById(R.id.tvExpertEmail);
            tvRegion = itemView.findViewById(R.id.tvExpertRegion);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}