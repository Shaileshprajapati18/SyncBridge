package com.example.myapplication;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FtpServer extends AppCompatActivity {

    private SimpleFileServer server;
    private static final int PORT = 8080;
    private static final String ROOT_DIRECTORY = "/storage/emulated/0/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ftp_server); // Ensure this matches your layout file

        TextView serverStatus = findViewById(R.id.serverStatus);
        TextView ipAddressView = findViewById(R.id.ipAddressView); // TextView to display the IP address

        // Start the server
        try {
            server = new SimpleFileServer(PORT);
            server.start();
            serverStatus.setText("Server is running on port " + PORT);
            Toast.makeText(this, "Server started successfully!", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            serverStatus.setText("Failed to start server");
            e.printStackTrace();
            Toast.makeText(this, "Failed to start server!", Toast.LENGTH_LONG).show();
        }

        // Get and display the device IP address
        String ipAddress = getIpAddress();
        ipAddressView.setText("IP Address: " + ipAddress);
    }

    // Method to get the device's IP address
    private String getIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            Toast.makeText(this, "Server stopped", Toast.LENGTH_SHORT).show();
        }
    }

    // Inner class for SimpleFileServer using NanoHTTPD
    private class SimpleFileServer extends NanoHTTPD {

        public SimpleFileServer(int port) {
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            // Show a toast message when the server is accessed
            runOnUiThread(() -> Toast.makeText(FtpServer.this, "Server accessed", Toast.LENGTH_SHORT).show());

            String uri = session.getUri();

            // Handle file listing and navigation
            if (uri.startsWith("/files")) {
                String relativePath = uri.substring("/files".length());
                File directory = new File(ROOT_DIRECTORY + relativePath);

                if (directory.isDirectory()) {
                    File[] files = directory.listFiles();
                    if (files != null && files.length > 0) {
                        StringBuilder fileList = new StringBuilder();
                        for (File file : files) {
                            fileList.append(file.getName()).append("\n");
                        }
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", fileList.toString());
                    } else {
                        return newFixedLengthResponse(Response.Status.OK, "text/plain", "No files found");
                    }
                } else {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Directory not found");
                }
            } else if (uri.startsWith("/download")) {
                String fileName = uri.substring("/download/".length());
                File file = new File(ROOT_DIRECTORY + fileName);
                if (file.exists()) {
                    try {
                        FileInputStream fis = new FileInputStream(file);
                        return newFixedLengthResponse(Response.Status.OK, "application/octet-stream", fis, file.length());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/plain", "Error reading file");
                    }
                } else {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "File not found");
                }
            }

            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Invalid request");
        }
    }
}
