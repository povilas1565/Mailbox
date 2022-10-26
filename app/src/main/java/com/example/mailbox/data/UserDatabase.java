package com.example.mailbox.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UserDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "mailbox_users";
    public static final int DATABASE_VERSION = 1;
    private final Context context;
    private static UserDatabase instance;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DATABASE_NAME +
                    " (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL, " +
                    "email TEXT, " +
                    "jwt TEXT NOT NULL, " +
                    "mailbox_ids TEXT )";

    private static final String SQL_DELETE_ENTRIES =
                    "DROP TABLE IF EXISTS " + DATABASE_NAME;

    private static final String SQL_INSERT_EMPTY_ENTRY =
                    "INSERT INTO " + DATABASE_NAME +
                    " (username,jwt) VALUES ('','')";

    public UserDatabase(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_INSERT_EMPTY_ENTRY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void resetDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_INSERT_EMPTY_ENTRY);
        db.close();
    }

    public static synchronized UserDatabase getInstance(final Context c) {
        if (instance == null) {
            instance = new UserDatabase(c.getApplicationContext());
        }
        return instance;
    }

    public String getJwtToken(){
        Cursor cursor = getReadableDatabase()
                .query(DATABASE_NAME,
                        new String[]{"jwt"},
                        "id = 1",
                        null,
                        null,
                        null,
                        null);
        cursor.moveToFirst();
        String retrievedToken = "";

        retrievedToken = cursor.getString(cursor.getColumnIndexOrThrow("jwt"));
        cursor.close();
        return retrievedToken.isEmpty() ? null : retrievedToken;
    }

    public void saveJWT(String username, String jwt){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username",username);
        cv.put("jwt", jwt);
        db.update(DATABASE_NAME, cv, "id=1", null);
        db.close();
    }

    public boolean isUserLoggedIn() {
        String token = getJwtToken();

        return token != null;
    }

    public String getUsername() {
        Cursor cursor = getReadableDatabase()
                .query(DATABASE_NAME,
                        new String[]{"username"},
                        "id = 1",
                        null,
                        null,
                        null,
                        null);
        cursor.moveToFirst();
        String username = "";
        username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
        cursor.close();
        return username.isEmpty() ? null : username;
    }

    public void saveUser(String username, String email, List<Long> mailboxIds) {
        String jsonIds = new Gson().toJson(mailboxIds);
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username",username);
        cv.put("email", email);
        cv.put("mailbox_ids", jsonIds);
        db.update(DATABASE_NAME, cv, "id=1", null);
        db.close();
    }

    public List<Long> getMailboxIds(){
        Cursor cursor = getReadableDatabase()
                .query(DATABASE_NAME,
                        new String[]{"mailbox_ids"},
                        "id = 1",
                        null,
                        null,
                        null,
                        null);
        cursor.moveToFirst();
        String jsonIds = "";
        jsonIds = cursor.getString(cursor.getColumnIndexOrThrow("mailbox_ids"));
        cursor.close();

        if (jsonIds == null)
            return null;

        Type listType = new TypeToken<List<Long>>(){}.getType();

        return new Gson().fromJson(jsonIds, listType);
    }

    public String getEmail() {
        Cursor cursor = getReadableDatabase()
                .query(DATABASE_NAME,
                        new String[]{"email"},
                        "id = 1",
                        null,
                        null,
                        null,
                        null);
        cursor.moveToFirst();
        String username = "";
        username = cursor.getString(cursor.getColumnIndexOrThrow("email"));
        cursor.close();
        return username;
    }
}
