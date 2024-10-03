package com.example.myapplication;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private final Context context;
    private File[] files;
    private File fileToCopy = null;
    private File currentDirectory; // Current directory
    private File pastedDirectory; // Directory of the pasted item
// Store the file for copy-paste functionality

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

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.getMenuInflater().inflate(R.menu.file_popup_menu, popupMenu.getMenu());
            popupMenu.setForceShowIcon(true); // Force show icons in the popup menu

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();

                if (id == R.id.action_open) {
                    openFile(file);
                } else if (id == R.id.action_rename) {
                    renameFile(file, holder);
                } else if (id == R.id.action_copy) {
                    fileToCopy = file;
                    copyToClipboard(file);  // Copy file or folder to clipboard
                    Toast.makeText(context, "File copied to clipboard", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.action_paste) {
                    promptPasteAction(holder.getAdapterPosition());
                } else if (id == R.id.action_delete) {
                    deleteFile(file, holder);
                } else if (id == R.id.action_share) {
                    shareFile(file);
                }
                return true; // Indicates that the click was handled
            });

            popupMenu.show();
            return true; // Indicates that the long click was handled
        });
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public void setFiles(File[] files) {
        this.files = files;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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

    // Method to open file or directory
    private void openFile(File file) {
        if (file.isDirectory()) {
            // Open the selected directory and reset the copy state
            Intent intent = new Intent(context, storageViewer.class);
            intent.putExtra("path", file.getAbsolutePath());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            fileToCopy = null; // Reset copy state
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

    // Copy file or folder to clipboard
    private void copyToClipboard(File file) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        ClipData clipData = ClipData.newUri(context.getContentResolver(), "Copied File", uri);
        clipboard.setPrimaryClip(clipData);
    }

    // Share file method
    private void shareFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(getMimeType(file.getName()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share File"));
    }

    // Rename file method
    private void renameFile(File file, ViewHolder holder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename");

        final EditText input = new EditText(context);
        input.setText(file.getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                File newFile = new File(file.getParent(), newName);
                boolean renamed = file.renameTo(newFile);
                if (renamed) {
                    Toast.makeText(context, "File renamed", Toast.LENGTH_SHORT).show();

                    // Update the files array and refresh the adapter
                    File[] updatedFiles = newFile.getParentFile().listFiles(); // Get updated file list
                    if (updatedFiles != null) {
                        // Update the adapter's data
                        setFiles(updatedFiles); // Update the files in the adapter
                    }
                } else {
                    Toast.makeText(context, "Failed to rename", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    // Paste file method
    // Prompt the user to paste the file
    private void promptPasteAction(int position) {
        if (fileToCopy == null) {
            Toast.makeText(context, "No file to paste", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Paste File");
        builder.setMessage("Do you want to paste the copied file/directory here?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            File destinationFolder = files[position]; // Get the folder to paste into
            pasteFile(destinationFolder);
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
    // Paste file method
    // Paste file method
    private void pasteFile(File destinationFolder) {
        if (fileToCopy != null) {
            // Ensure the destination is a directory
            if (destinationFolder.isDirectory()) {
                File newFile = new File(destinationFolder, fileToCopy.getName());

                // Check if the copied item is a directory
                if (fileToCopy.isDirectory()) {
                    boolean directoryCreated = newFile.mkdir();
                    if (directoryCreated) {
                        copyDirectoryContents(fileToCopy, newFile);
                        Toast.makeText(context, "Folder pasted", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to paste folder", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        Files.copy(fileToCopy.toPath(), newFile.toPath());
                        Toast.makeText(context, "File pasted", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Failed to paste file", Toast.LENGTH_SHORT).show();
                    }
                }

                // Update the current directory and pasted directory
                currentDirectory = destinationFolder; // Update to the current folder
                pastedDirectory = newFile; // Set the pasted directory

                // Navigate to the pasted folder
                showFilesInDirectory(pastedDirectory); // Method to update the view
                fileToCopy = null; // Reset copy state
            } else {
                Toast.makeText(context, "Cannot paste here, not a directory", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showFilesInDirectory(File directory) {
        // Load and display files in the specified directory
        File[] files = directory.listFiles();
        if (files != null) {
            setFiles(files); // Update the adapter with the new files
        }
    }
    // Method to copy contents of a directory
    private void copyDirectoryContents(File source, File destination) {
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File newFile = new File(destination, file.getName());
                if (file.isDirectory()) {
                    newFile.mkdir();
                    copyDirectoryContents(file, newFile); // Recursive call
                } else {
                    try {
                        Files.copy(file.toPath(), newFile.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    // Method to delete files or directories
    private void deleteFile(File file, ViewHolder holder) {
        new AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this " + (file.isDirectory() ? "folder?" : "file?"))
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Check if it's a directory
                    if (file.isDirectory()) {
                        // Recursively delete the folder and its contents
                        if (deleteDirectory(file)) {
                            Toast.makeText(context, "Folder deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete folder", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // If it's a file, delete the file
                        if (file.delete()) {
                            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
                        }
                    }

                    // Update the files array and refresh the adapter
                    File[] updatedFiles = file.getParentFile().listFiles(); // Get updated file list
                    if (updatedFiles != null) {
                        setFiles(updatedFiles); // Update the files in the adapter
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // Recursive method to delete a directory and its contents
    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // Recursively delete subdirectories
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                } else {
                    // Delete files
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }
        // Finally, delete the directory itself
        return directory.delete();
    }

    // Get readable file size
    private String getReadableFileSize(long size) {
        if (size <= 0) return "0 bytes";

        final String[] units = {"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    // Get directory size
    private long getDirectorySize(File directory) {
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += getDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }

    // Get MIME type for a file
    private String getMimeType(String fileName) {
        String mimeType = "*/*"; // Default to all types
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                mimeType = "image/jpeg";
                break;
            case "png":
                mimeType = "image/png";
                break;
            case "gif":
                mimeType = "image/gif";
                break;
            case "pdf":
                mimeType = "application/pdf";
                break;
            case "txt":
                mimeType = "text/plain";
                break;
            case "doc":
            case "docx":
                mimeType = "application/msword";
                break;
            case "xls":
            case "xlsx":
                mimeType = "application/vnd.ms-excel";
                break;
            case "ppt":
            case "pptx":
                mimeType = "application/vnd.ms-powerpoint";
                break;
            case "mp3":
                mimeType = "audio/mpeg";
                break;
            case "mp4":
                mimeType = "video/mp4";
                break;
            // Add more file types as needed
        }
        return mimeType;
    }

}
