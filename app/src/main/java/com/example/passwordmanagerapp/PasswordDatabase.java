package com.example.passwordmanagerapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Password.class}, version = 4)
public abstract class PasswordDatabase extends RoomDatabase {

    private static PasswordDatabase instance;

    public abstract PasswordDao passwordDao();

    public static synchronized PasswordDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            PasswordDatabase.class,
                            "password_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}