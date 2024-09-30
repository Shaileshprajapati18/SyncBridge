package com.example.myapplication;

public class FileData {
    private String name;
    private boolean isDirectory;
    private long size;

    public FileData(String name, boolean isDirectory, long size) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public long getSize() {
        return size;
    }
}
