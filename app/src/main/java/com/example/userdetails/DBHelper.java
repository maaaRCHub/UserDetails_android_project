package com.example.userdetails;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context) {
        super(context, "users_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users_table(id INTEGER PRIMARY KEY, name TEXT, email TEXT, birthDate TEXT, username TEXT, address TEXT, location TEXT, phone TEXT, photo TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users_table");
        onCreate(db);
    }

    public List<DBUsersDetails> getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users_table", null);
        List<DBUsersDetails> dbUsersDetailsList = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                DBUsersDetails dbUsersDetails = new DBUsersDetails();
                dbUsersDetails.setId(cursor.getInt(0));
                dbUsersDetails.setName(cursor.getString(1));
                dbUsersDetails.setEmail(cursor.getString(2));
                dbUsersDetails.setBirthDate(cursor.getString(3));
                dbUsersDetails.setUsername(cursor.getString(4));
                dbUsersDetails.setAddress(cursor.getString(5));
                dbUsersDetails.setLocation(cursor.getString(6));
                dbUsersDetails.setPhone(cursor.getString(7));
                dbUsersDetails.setPhoto(cursor.getString(8));
                dbUsersDetailsList.add(dbUsersDetails);
            }
            cursor.close();
        }
        return dbUsersDetailsList;
    }

    public void insertUser(DBUsersDetails dbUsersDetails) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("id", dbUsersDetails.getId());
        contentValues.put("name", dbUsersDetails.getName());
        contentValues.put("email", dbUsersDetails.getEmail());
        contentValues.put("birthDate", dbUsersDetails.getBirthDate());
        contentValues.put("username", dbUsersDetails.getUsername());
        contentValues.put("address", dbUsersDetails.getAddress());
        contentValues.put("location", dbUsersDetails.getLocation());
        contentValues.put("phone", dbUsersDetails.getPhone());
        contentValues.put("photo", dbUsersDetails.getPhoto());

        db.insert("users_table", null, contentValues);
        db.close();
    }

    public void deleteUser() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM users_table");
        db.close();
    }

    public void deleteUser(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM users_table WHERE id = " + id);
        db.close();
    }

    public void updatePhoto(int id, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("photo", imagePath);
        db.update("users_table", contentValues, "id = ?", new String[]{String.valueOf(id)});
    }
}
