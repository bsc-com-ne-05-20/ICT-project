package com.example.ssmsprojectapp.databasehelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ssmsprojectapp.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "agrichat.db";
    private static final int DATABASE_VERSION = 1;

    // Table name and columns
    private static final String TABLE_MESSAGES = "messages";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CONTENT = "content";
    private static final String COLUMN_SENDER_NAME = "sender_name";
    private static final String COLUMN_SENDER_ID = "sender_id";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_IS_USER = "is_user";

    public ChatDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CONTENT + " TEXT,"
                + COLUMN_SENDER_NAME + " TEXT,"
                + COLUMN_SENDER_ID + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER,"
                + COLUMN_IS_USER + " INTEGER)";
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void addMessage(Message message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTENT, message.getContent());
        values.put(COLUMN_SENDER_NAME, message.getSenderName());
        values.put(COLUMN_SENDER_ID, message.getSenderId());
        values.put(COLUMN_TIMESTAMP, message.getTimestamp());
        values.put(COLUMN_IS_USER, message.isUser() ? 1 : 0);

        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES + " ORDER BY " + COLUMN_TIMESTAMP + " ASC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Message message = new Message(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER_ID)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_USER)) == 1
                );
                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messages;
    }

    public void clearAllMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MESSAGES, null, null);
        db.close();
    }
}
