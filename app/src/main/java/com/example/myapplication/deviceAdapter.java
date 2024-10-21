package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Stack;

public class deviceAdapter extends RecyclerView.Adapter<deviceAdapter.ViewHolder> {

    private final Context context;
    private final List<FileData> files;
    private final OnItemClickListener onItemClickListener;
    private final Stack<String> directoryStack = new Stack<>(); // Stack for directory management

    public interface OnItemClickListener {
        void onItemClick(FileData file);
        void onDownload(FileData file);
        void onBackPressed(); // Back press method
    }

    public deviceAdapter(Context context, List<FileData> files, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.files = files;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileData file = files.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        return files.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView fileName;
        private final ImageView fileIcon;
        private FileData file;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            itemView.setOnClickListener(this);
        }

        void bind(FileData file) {
            this.file = file;
            fileName.setText(file.getName());
            fileIcon.setImageResource(file.isDirectory() ? R.drawable.folder : R.drawable.file);
        }

        @Override
        public void onClick(View v) {
            if (file.isDirectory()) {
                onItemClickListener.onItemClick(file); // Handle directory navigation
            } else {
                // Show options for file (download or other options)
                showPopupMenu(v);
            }
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(context, view);
            popupMenu.getMenuInflater().inflate(R.menu.menu_file_options, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.action_download) {
                    onItemClickListener.onDownload(file); // Download the file
                    return true;
                }
                return false;
            });
            popupMenu.show();
        }
    }
}
