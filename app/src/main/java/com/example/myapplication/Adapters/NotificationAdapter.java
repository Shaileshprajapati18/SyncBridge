package com.example.myapplication.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activities.notification_Activity;
import com.example.myapplication.Model.Notification;
import com.example.myapplication.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notificationList;
    private notification_Activity activity; // Reference to activity

    public NotificationAdapter(List<Notification> notificationList, notification_Activity activity) {
        this.notificationList = notificationList;
        this.activity = activity; // Initialize activity reference
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.tvTitle.setText(notification.getTitle());
        holder.tvMessage.setText(notification.getMessage());
        holder.tvTimestamp.setText(notification.getTimestamp());

        // Handle action button click (e.g., delete)
        holder.ivAction.setOnClickListener(v -> {
            notificationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, notificationList.size());
            activity.updateUI(); // Update UI after deletion
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTimestamp;
        ImageView ivAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTimestamp = itemView.findViewById(R.id.tv_notification_timestamp);
            ivAction = itemView.findViewById(R.id.iv_action);
        }
    }
}