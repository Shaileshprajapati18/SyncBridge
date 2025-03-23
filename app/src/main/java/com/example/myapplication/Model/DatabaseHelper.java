package com.example.myapplication.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "User";
    private static final int DATABASE_VERSION = 3;
    public static final String TABLE_PRODUCTS = "products";

    public static final String COLUMN_ID = "id";  // Changed to public
    public static final String COLUMN_FIRST_NAME = "first_name";  // Changed to public
    public static final String COLUMN_LAST_NAME = "last_name";  // Changed to public
    public static final String COLUMN_PHONE = "phone";  // Changed to public
    public static final String COLUMN_EMAIL = "email";  // Changed to public
    public static final String COLUMN_IMAGE = "image";  // Changed to public

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_FIRST_NAME + " TEXT, " +
                COLUMN_LAST_NAME + " TEXT, " +
                COLUMN_PHONE + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE," +
                COLUMN_IMAGE + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    public boolean insertOrUpdateProduct(String firstName, String lastName, String phone, String email, String image) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COLUMN_EMAIL + " = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FIRST_NAME, firstName);
        contentValues.put(COLUMN_LAST_NAME, lastName);
        contentValues.put(COLUMN_PHONE, phone);
        contentValues.put(COLUMN_EMAIL, email);
        contentValues.put(COLUMN_IMAGE, image);

        long result;
        if (exists) {
            result = db.update(TABLE_PRODUCTS, contentValues, COLUMN_EMAIL + " = ?", new String[]{email});
        } else {
            result = db.insert(TABLE_PRODUCTS, null, contentValues);
        }

        db.close();
        return result != -1;
    }

    public Cursor viewData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PRODUCTS, null, null, null, null, null, null);
    }
}