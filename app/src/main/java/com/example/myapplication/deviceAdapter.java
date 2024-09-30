package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class deviceAdapter extends RecyclerView.Adapter<deviceAdapter.ViewHolder> {

    private final Context context;
    private final List<FileData> files;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(FileData file);
    }

    public deviceAdapter(Context context, List<FileData> files, OnItemClickListener listener) {
        this.context = context;
        this.files = files;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.show_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileData file = files.get(position);
        holder.textView.setText(file.getName());

        if (file.isDirectory()) {
            holder.imageView.setImageResource(R.drawable.folder);
            holder.fileSizeTextView.setText("Directory");
        } else {
            holder.imageView.setImageResource(R.drawable.file);
            holder.fileSizeTextView.setText(getReadableFileSize(file.getSize()));
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(file);
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView, fileSizeTextView;
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name_text_view);
            fileSizeTextView = itemView.findViewById(R.id.file_size_text_view);
            imageView = itemView.findViewById(R.id.icon_view);
        }
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) return "0 bytes";
        final String[] units = {"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
