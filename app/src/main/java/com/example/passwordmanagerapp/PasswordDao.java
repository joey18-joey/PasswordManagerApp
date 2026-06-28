package com.example.passwordmanagerapp;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PasswordDao {

    @Insert
    void insert(Password password);

    @Update
    void update(Password password);

    @Delete
    void delete(Password password);

    @Query("SELECT * FROM password_table ORDER BY updatedAt DESC, id DESC")
    List<Password> getAllPasswords();

    @Query("SELECT * FROM password_table ORDER BY updatedAt DESC, id DESC")
    List<Password> getAllPasswordsNewest();

    @Query("SELECT * FROM password_table ORDER BY updatedAt ASC, id ASC")
    List<Password> getAllPasswordsOldest();

    @Query("SELECT * FROM password_table ORDER BY siteName COLLATE NOCASE ASC, id DESC")
    List<Password> getAllPasswordsAZ();

    @Query("SELECT * FROM password_table ORDER BY siteName COLLATE NOCASE DESC, id DESC")
    List<Password> getAllPasswordsZA();
}