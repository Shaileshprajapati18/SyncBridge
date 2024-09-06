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
import android.net.Uri;
import java.io.File;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    Context context;
    File[] files;

    public myAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.show_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File selectFile = files[position];
        holder.textView.setText(selectFile.getName());

        if (selectFile.isDirectory()) {
            holder.imageView.setImageResource(R.drawable.folder);

            File[] subFiles = selectFile.listFiles();
            int itemCount = subFiles != null ? subFiles.length : 0;
            long totalSize = 0;
            for (File file : subFiles != null ? subFiles : new File[0]) {
                totalSize += file.length();
            }

            holder.fileSizeTextView.setText(getReadableFileSize(totalSize));
            holder.itemCountTextView.setText(itemCount + " items");

            // Show the file info layout for directories
            holder.fileInfoLayout.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setImageResource(R.drawable.file);
            holder.fileSizeTextView.setText(getReadableFileSize(selectFile.length()));

            // Hide the item count text and vertical line for files
            holder.itemCountTextView.setText("");
            holder.verticalLine.setVisibility(View.GONE);
            holder.fileInfoLayout.setVisibility(View.VISIBLE); // Show file size only
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectFile.isDirectory()) {
                Intent intent = new Intent(context, MainActivity2.class);
                String path = selectFile.getAbsolutePath();
                intent.putExtra("path", path);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);

                    // Get the file URI using FileProvider
                    Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", selectFile);

                    // Get the file extension and set MIME type
                    String fileName = selectFile.getName();
                    String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
                    String type;

                    // Determine the MIME type based on the file extension
                    switch (fileExtension.toLowerCase()) {
                        case "jpg":
                        case "jpeg":
                        case "png":
                        case "gif":
                        case "bmp":
                            type = "image/*";  // Images
                            break;
                        case "pdf":
                            type = "application/pdf";  // PDF
                            break;
                        case "txt":
                            type = "text/plain";  // Text files
                            break;
                        case "mp4":
                        case "mkv":
                        case "avi":
                            type = "video/*";  // Videos
                            break;
                        case "mp3":
                        case "wav":
                        case "ogg":
                            type = "audio/*";  // Audio
                            break;
                        case "doc":
                        case "docx":
                            type = "application/msword";  // Word documents
                            break;
                        case "xls":
                        case "xlsx":
                            type = "application/vnd.ms-excel";  // Excel files
                            break;
                        case "ppt":
                        case "pptx":
                            type = "application/vnd.ms-powerpoint";  // PowerPoint files
                            break;
                        case "zip":
                        case "rar":
                            type = "application/zip";  // Compressed files
                            break;
                        default:
                            type = "*/*";  // Fallback for any other type
                            break;
                    }

                    // Set the URI and MIME type
                    intent.setDataAndType(fileUri, type);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);  // Grant read permission for the URI

                    // Start the intent to open the file
                    context.startActivity(intent);

                } catch (Exception e) {
                    // Handle exceptions if no suitable application is found
                    Toast.makeText(context, "No application found to open this file", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private String getReadableFileSize(long size) {
        if (size <= 0) return "0 bytes";
        final String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
