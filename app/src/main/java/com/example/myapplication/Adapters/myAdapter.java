package com.example.myapplication.Adapters;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Activites.storageViewer;
import com.example.myapplication.R;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private final Context context;
    private File[] files;
    private File currentDirectory;
    private File pastedDirectory;
    private Map<File, String> fileSizeCache = new HashMap<>();
    private static File fileToCopy = null;

    public myAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files;
        this.currentDirectory = files != null && files.length > 0 ? files[0].getParentFile() : null;
        loadFileSizesInBackground();
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
            holder.itemCountTextView.setText(file.listFiles() != null ? file.listFiles().length + " items" : "0 items");
            holder.fileInfoLayout.setVisibility(View.VISIBLE);
            holder.verticalLine.setVisibility(View.VISIBLE);
        } else {
            holder.imageView.setImageResource(R.drawable.file);
            holder.itemCountTextView.setText("");
            holder.verticalLine.setVisibility(View.GONE);
            holder.fileInfoLayout.setVisibility(View.VISIBLE);
        }

        if (fileSizeCache.containsKey(file)) {
            holder.fileSizeTextView.setText(fileSizeCache.get(file));
        } else {
            holder.fileSizeTextView.setText("Loading...");
        }

        holder.itemView.setOnClickListener(v -> openFile(file));

        holder.itemView.setOnLongClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, v);

            popupMenu.getMenuInflater().inflate(R.menu.file_popup_menu, popupMenu.getMenu());
            popupMenu.setForceShowIcon(true);

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_open) {
                    openFile(file);
                } else if (id == R.id.action_rename) {
                    renameFile(file, holder);
                } else if (id == R.id.action_copy) {
                    fileToCopy = file;
                    copyToClipboard(file);
                    Toast.makeText(context, "File copied to clipboard", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.action_paste) {
                    promptPasteAction(holder.getAdapterPosition());
                } else if (id == R.id.action_delete) {
                    deleteFile(file, holder);
                } else if (id == R.id.action_share) {
                    shareFile(file);
                }
                return true;
            });

            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return files != null ? files.length : 0;
    }

    public void setFiles(File[] files) {
        this.files = files;
        this.currentDirectory = files != null && files.length > 0 ? files[0].getParentFile() : null;
        fileSizeCache.clear();
        loadFileSizesInBackground();
        notifyDataSetChanged();
    }

    // New method to refresh the current directory
    public void refreshCurrentDirectory() {
        if (currentDirectory != null) {
            File[] updatedFiles = currentDirectory.listFiles();
            if (updatedFiles != null) {
                setFiles(updatedFiles);
            }
        }
    }

    public File getCurrentDirectory() {
        return currentDirectory;
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

    private void openFile(File file) {
        if (file.isDirectory()) {
            Intent intent = new Intent(context, storageViewer.class);
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

    private void copyToClipboard(File file) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        ClipData clipData = ClipData.newUri(context.getContentResolver(), "Copied File", uri);
        clipboard.setPrimaryClip(clipData);
    }

    private void shareFile(File file) {
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(getMimeType(file.getName()));
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(shareIntent, "Share File"));
    }

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
                    refreshCurrentDirectory(); // Refresh after rename
                } else {
                    Toast.makeText(context, "Failed to rename", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void promptPasteAction(int position) {
        if (fileToCopy == null) {
            Toast.makeText(context, "No file to paste", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Paste File");
        builder.setMessage("Do you want to paste the copied file/directory here?");
        builder.setPositiveButton("Yes", (dialog, which) -> pasteFile(files[position]));
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void pasteFile(File destinationFolder) {
        if (fileToCopy != null && destinationFolder.isDirectory()) {
            File newFile = new File(destinationFolder, fileToCopy.getName());
            File parentDirectory = destinationFolder.getParentFile();
            if (fileToCopy.isDirectory()) {
                boolean directoryCreated = newFile.mkdir();
                newFile.setWritable(true, false);
                if (directoryCreated) {
                    copyDirectoryContents(fileToCopy, newFile);
                    Toast.makeText(context, "Folder pasted", Toast.LENGTH_SHORT).show();
                    File[] updatedFiles = parentDirectory != null ? parentDirectory.listFiles() : null;
                    if (updatedFiles != null) {
                        setFiles(updatedFiles);
                    }
                } else {
                    Toast.makeText(context, "Failed to paste folder", Toast.LENGTH_SHORT).show();
                }
            } else {
                try {
                    Files.copy(fileToCopy.toPath(), newFile.toPath());
                    Toast.makeText(context, "File pasted", Toast.LENGTH_SHORT).show();
                    File[] updatedFiles = parentDirectory != null ? parentDirectory.listFiles() : null;
                    if (updatedFiles != null) {
                        setFiles(updatedFiles);
                    }
                } catch (IOException e) {
                    Toast.makeText(context, "Failed to paste file", Toast.LENGTH_SHORT).show();
                }
            }
            currentDirectory = destinationFolder;
        } else {
            Toast.makeText(context, "Cannot paste here, not a directory", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFilesInDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            setFiles(files);
        }
    }

    private void copyDirectoryContents(File source, File destination) {
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File newFile = new File(destination, file.getName());
                if (file.isDirectory()) {
                    newFile.mkdir();
                    copyDirectoryContents(file, newFile);
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

    private void deleteFile(File file, ViewHolder holder) {
        new AlertDialog.Builder(context)
                .setTitle("Delete")
                .setMessage("Are you sure you want to delete this " + (file.isDirectory() ? "folder?" : "file?"))
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (file.isDirectory()) {
                        if (deleteDirectory(file)) {
                            Toast.makeText(context, "Folder deleted", Toast.LENGTH_SHORT).show();
                            refreshCurrentDirectory(); // Refresh after delete
                        } else {
                            Toast.makeText(context, "Failed to delete folder", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        if (file.delete()) {
                            Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show();
                            refreshCurrentDirectory(); // Refresh after delete
                        } else {
                            Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) {
                        return false;
                    }
                } else {
                    if (!file.delete()) {
                        return false;
                    }
                }
            }
        }
        return directory.delete();
    }

    private void loadFileSizesInBackground() {
        new FileSizeLoaderTask().execute(files);
    }

    private class FileSizeLoaderTask extends AsyncTask<File, Void, Map<File, String>> {
        @Override
        protected Map<File, String> doInBackground(File... files) {
            Map<File, String> sizeMap = new HashMap<>();
            for (File file : files) {
                if (file != null) {
                    long size = file.isDirectory() ? getDirectorySize(file) : file.length();
                    sizeMap.put(file, getReadableFileSize(size));
                }
            }
            return sizeMap;
        }

        @Override
        protected void onPostExecute(Map<File, String> result) {
            fileSizeCache.putAll(result);
            notifyDataSetChanged();
        }
    }

    private String getReadableFileSize(long size) {
        if (size <= 0) return "0 bytes";
        final String[] units = {"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

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

    private String getMimeType(String fileName) {
        String mimeType = "*/*";
        if (fileName.contains(".")) {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            switch (extension) {
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
            }
        }
        return mimeType;
    }

    public static void clearCopiedFile() {
        fileToCopy = null;
    }
}