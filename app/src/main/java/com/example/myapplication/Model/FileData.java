package com.example.myapplication.Model;

public class FileData {
    private String name;
    private String path;
    private long size;
    private boolean isDirectory;

    public FileData(String name, String path, long size, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.size = size;
        this.isDirectory = isDirectory;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }
}
