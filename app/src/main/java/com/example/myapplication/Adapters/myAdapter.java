package com.example.myapplication.Adapters;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.Activities.storageViewer;
import com.example.myapplication.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    private static File fileToCopy = null;
    private Map<File, String> fileSizeCache = new HashMap<>();

    public myAdapter(Context context, File[] files) {
        this.context = context;
        this.files = files != null ? files : new File[0];
        this.currentDirectory = (files != null && files.length > 0) ? files[0].getParentFile() : null;
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
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                Glide.with(context)
                        .load(file)
                        .centerCrop()
                        .placeholder(R.drawable.file)
                        .into(holder.imageView);
            } else if (fileName.endsWith(".pdf")) {
                new LoadPdfThumbnailTask(context, holder.imageView).execute(file);
            } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                holder.imageView.setImageResource(R.drawable.ic_docx);
            } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
                holder.imageView.setImageResource(R.drawable.ic_pptx);
            } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
                holder.imageView.setImageResource(R.drawable.ic_xlsx);
            } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wav") || fileName.endsWith(".aac") || fileName.endsWith(".m4a")) {
                holder.imageView.setImageResource(R.drawable.ic_audio);
            }
            else if (fileName.endsWith(".mp4") || fileName.endsWith(".mkv") || fileName.endsWith(".avi") || fileName.endsWith(".3gp")) {
                holder.imageView.setImageResource(R.drawable.ic_video);
            }
            else if (fileName.endsWith(".apk")) {
                holder.imageView.setImageResource(R.drawable.ic_apk);
            }
            else if (fileName.endsWith(".zip")){
                holder.imageView.setImageResource(R.drawable.ic_zip);
            }
            else if (fileName.endsWith(".rar")){
                holder.imageView.setImageResource(R.drawable.ic_rar);
            }
            else {
                holder.imageView.setImageResource(R.drawable.file);
            }

            holder.itemCountTextView.setText("");
            holder.verticalLine.setVisibility(View.GONE);
            holder.fileInfoLayout.setVisibility(View.VISIBLE);
        }

        if (fileSizeCache.containsKey(file)) {
            holder.fileSizeTextView.setText(fileSizeCache.get(file));
        } else {
            holder.fileSizeTextView.setText("...");
        }

        holder.itemView.setOnClickListener(v -> openFile(file));
        holder.itemView.setOnLongClickListener(v -> {
            showBottomSheet(file, holder, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    public void setFiles(File[] files) {
        this.files = files != null ? files : new File[0];
        this.currentDirectory = (files != null && files.length > 0) ? files[0].getParentFile() : null;
        fileSizeCache.clear();
        loadFileSizesInBackground();
        notifyDataSetChanged();
    }

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

    private static class LoadPdfThumbnailTask extends AsyncTask<File, Void, Bitmap> {
        private final Context context;
        private final ImageView imageView;
        private boolean isCancelled = false;

        LoadPdfThumbnailTask(Context context, ImageView imageView) {
            this.context = context.getApplicationContext();
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(File... files) {
            File file = files[0];
            ParcelFileDescriptor pfd = null;
            PdfRenderer renderer = null;
            try {
                pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                renderer = new PdfRenderer(pfd);
                PdfRenderer.Page page = renderer.openPage(0);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                return bitmap;
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    if (renderer != null) renderer.close();
                    if (pfd != null) pfd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled || context == null || imageView.getContext() instanceof android.app.Activity && ((android.app.Activity) imageView.getContext()).isDestroyed()) {
                return;
            }

            if (bitmap != null) {
                Glide.with(context)
                        .load(bitmap)
                        .centerCrop()
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.ic_pdf);
            }
        }

        @Override
        protected void onCancelled() {
            isCancelled = true;
        }
    }

    private void showBottomSheet(File file, ViewHolder holder, int position) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_layout, null);

        LinearLayout openItem = bottomSheetView.findViewById(R.id.bottom_sheet_open);
        LinearLayout renameItem = bottomSheetView.findViewById(R.id.bottom_sheet_rename);
        LinearLayout copyItem = bottomSheetView.findViewById(R.id.bottom_sheet_copy);
        LinearLayout pasteItem = bottomSheetView.findViewById(R.id.bottom_sheet_paste);
        LinearLayout deleteItem = bottomSheetView.findViewById(R.id.bottom_sheet_delete);
        LinearLayout shareItem = bottomSheetView.findViewById(R.id.bottom_sheet_share);

        openItem.setOnClickListener(v -> {
            openFile(file);
            bottomSheetDialog.dismiss();
        });

        renameItem.setOnClickListener(v -> {
            renameFile(file, holder);
            bottomSheetDialog.dismiss();
        });

        copyItem.setOnClickListener(v -> {
            fileToCopy = file;
            copyToClipboard(file);
            Toast.makeText(context, "File copied to clipboard", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        pasteItem.setOnClickListener(v -> {
            promptPasteAction(file);
            bottomSheetDialog.dismiss();
        });

        deleteItem.setOnClickListener(v -> {
            deleteFile(file, holder);
            bottomSheetDialog.dismiss();
        });

        shareItem.setOnClickListener(v -> {
            shareFile(file);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
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
        Dialog customDialog = new Dialog(context);
        customDialog.setContentView(R.layout.custom_dialog_layout);

        TextView title = customDialog.findViewById(R.id.dialog_title);
        EditText input = customDialog.findViewById(R.id.dialog_input);
        Button cancel = customDialog.findViewById(R.id.dialog_cancel);
        Button confirm = customDialog.findViewById(R.id.dialog_confirm);

        title.setText("Rename");
        input.setVisibility(View.VISIBLE);
        input.setText(file.getName());

        confirm.setText("Rename");
        confirm.setOnClickListener(v -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                File newFile = new File(file.getParent(), newName);
                if (file.renameTo(newFile)) {
                    Toast.makeText(context, "File renamed", Toast.LENGTH_SHORT).show();
                    refreshCurrentDirectory();
                } else {
                    Toast.makeText(context, "Failed to rename", Toast.LENGTH_SHORT).show();
                }
            }
            customDialog.dismiss();
        });

        cancel.setOnClickListener(v -> customDialog.dismiss());

        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.show();

        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        customDialog.getWindow().setAttributes(params);
    }

    private void promptPasteAction(File destination) {
        if (fileToCopy == null) {
            Toast.makeText(context, "No file to paste", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog customDialog = new Dialog(context);
        customDialog.setContentView(R.layout.custom_dialog_layout);

        TextView title = customDialog.findViewById(R.id.dialog_title);
        TextView message = customDialog.findViewById(R.id.dialog_message);
        Button cancel = customDialog.findViewById(R.id.dialog_cancel);
        Button confirm = customDialog.findViewById(R.id.dialog_confirm);

        title.setText("Paste File");
        message.setVisibility(View.VISIBLE);
        message.setText("Do you want to paste the copied file/directory here?");

        confirm.setText("Yes");
        confirm.setOnClickListener(v -> {
            pasteFile(destination);
            customDialog.dismiss();
        });

        cancel.setText("No");
        cancel.setOnClickListener(v -> customDialog.dismiss());

        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.show();

        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        customDialog.getWindow().setAttributes(params);
    }

    private void pasteFile(File destination) {
        if (fileToCopy == null) {
            Toast.makeText(context, "No file to paste", Toast.LENGTH_SHORT).show();
            return;
        }

        File destinationFolder = destination.isDirectory() ? destination : destination.getParentFile();
        File newFile = new File(destinationFolder, fileToCopy.getName());

        if (fileToCopy.isDirectory()) {
            if (newFile.mkdir()) {
                copyDirectoryContents(fileToCopy, newFile);
                Toast.makeText(context, "Folder pasted", Toast.LENGTH_SHORT).show();
                refreshCurrentDirectory();
            } else {
                Toast.makeText(context, "Failed to paste folder", Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                Files.copy(fileToCopy.toPath(), newFile.toPath());
                Toast.makeText(context, "File pasted", Toast.LENGTH_SHORT).show();
                refreshCurrentDirectory();
            } catch (IOException e) {
                Toast.makeText(context, "Failed to paste file", Toast.LENGTH_SHORT).show();
            }
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
        Dialog customDialog = new Dialog(context);
        customDialog.setContentView(R.layout.custom_dialog_layout);

        TextView title = customDialog.findViewById(R.id.dialog_title);
        TextView message = customDialog.findViewById(R.id.dialog_message);
        Button cancel = customDialog.findViewById(R.id.dialog_cancel);
        Button confirm = customDialog.findViewById(R.id.dialog_confirm);

        title.setText("Delete");
        message.setVisibility(View.VISIBLE);
        message.setText("Are you sure you want to delete this " + (file.isDirectory() ? "folder?" : "file?"));

        confirm.setText("Yes");
        confirm.setOnClickListener(v -> {
            if (file.isDirectory()) {
                if (deleteDirectory(file)) {
                    Toast.makeText(context, "Folder deleted", Toast.LENGTH_SHORT).show();
                    refreshCurrentDirectory();
                } else {
                    Toast.makeText(context, "Failed to delete folder", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (file.delete()) {
                    Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show();
                    refreshCurrentDirectory();
                } else {
                    Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
                }
            }
            customDialog.dismiss();
        });

        cancel.setText("No");
        cancel.setOnClickListener(v -> customDialog.dismiss());

        customDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customDialog.show();

        WindowManager.LayoutParams params = customDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        customDialog.getWindow().setAttributes(params);
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