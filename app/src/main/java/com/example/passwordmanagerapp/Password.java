package com.example.passwordmanagerapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "password_table")
public class Password {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String siteName;
    private String username;
    private String password;
    private String extraNote;
    private String category;
    private long createdAt;
    private long updatedAt;

    public Password(String siteName, String username, String password, String extraNote, String category) {
        this.siteName = siteName;
        this.username = username;
        this.password = password;
        this.extraNote = extraNote;
        this.category = category;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getExtraNote() {
        return extraNote;
    }

    public String getCategory() {
        return category;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}