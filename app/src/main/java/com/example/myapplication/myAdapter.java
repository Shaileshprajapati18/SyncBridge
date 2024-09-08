package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private final Context context;
    private final File[] files;

    public myAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.show_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File file = files[position];
        holder.textView.setText(file.getName());

        if (file.isDirectory()) {
            holder.imageView.setImageResource(R.drawable.folder);

            long totalSize = getDirectorySize(file);
            int itemCount = file.listFiles() != null ? file.listFiles().length : 0;

            holder.fileSizeTextView.setText(getReadableFileSize(totalSize));
            holder.itemCountTextView.setText(itemCount + " items");

            holder.fileInfoLayout.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setImageResource(R.drawable.file);
            holder.fileSizeTextView.setText(getReadableFileSize(file.length()));

            holder.itemCountTextView.setText("");
            holder.verticalLine.setVisibility(View.GONE);
            holder.fileInfoLayout.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> openFile(file));
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView, fileSizeTextView, itemCountTextView;
        ImageView imageView;
        LinearLayout fileInfoLayout;
        View verticalLine;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.file_name_text_view);
            fileSizeTextView = itemView.findViewById(R.id.file_size_text_view);
            itemCountTextView = itemView.findViewById(R.id.item_count_text_view);
            imageView = itemView.findViewById(R.id.icon_view);
            fileInfoLayout = itemView.findViewById(R.id.file_info_layout);
            verticalLine = itemView.findViewById(R.id.vertical_line);
        }
    }

    private void openFile(File file) {
        if (file.isDirectory()) {
            Intent intent = new Intent(context, MainActivity2.class);
            intent.putExtra("path", file.getAbsolutePath());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

                String type = getMimeType(file.getName());
                intent.setDataAndType(fileUri, type);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                context.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(context, "No application found to open this file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private long getDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += getDirectorySize(file);  // Recursively add sizes
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) return "0 bytes";

        final String[] units = {"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private String getMimeType(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (fileExtension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return "image/*";
            case "pdf":
                return "application/pdf";
            case "txt":
                return "text/plain";
            case "mp4":
            case "mkv":
            case "avi":
                return "video/*";
            case "mp3":
            case "wav":
            case "ogg":
                return "audio/*";
            case "doc":
            case "docx":
                return "application/msword";
            case "xls":
            case "xlsx":
                return "application/vnd.ms-excel";
            case "ppt":
            case "pptx":
                return "application/vnd.ms-powerpoint";
            case "zip":
            case "rar":
                return "application/zip";
            default:
                return "*/*";
        }
    }
}
