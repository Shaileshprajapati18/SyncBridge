package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

public class storageViewer extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noFilesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_viewer);

        recyclerView = findViewById(R.id.recycler_view);
        noFilesTextView = findViewById(R.id.nofiles_textview);

        String path = getIntent().getStringExtra("path");
        File root = new File(path);
        File[] files = root.listFiles();

        if (files == null || files.length == 0) {
            noFilesTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noFilesTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new myAdapter(this, files));
        }
    }
}
