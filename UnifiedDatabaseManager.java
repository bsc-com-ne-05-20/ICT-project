// UnifiedDatabaseManager.java
package com.example.soilhealthy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

public class UnifiedDatabaseManager extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SoilData.db";
    private static final int DATABASE_VERSION = 2; // Version bumped for migration

    // Table and columns
    private static final String TABLE_SOIL_DATA = "soil_data";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_TEMPERATURE = "temperature";
    private static final String COLUMN_SALINITY = "salinity";
    private static final String COLUMN_PH = "ph";
    private static final String COLUMN_MOISTURE = "moisture";
    private static final String COLUMN_NITROGEN = "nitrogen";
    private static final String COLUMN_PHOSPHORUS = "phosphorus";
    private static final String COLUMN_POTASSIUM = "potassium";
    private static final String COLUMN_PERSON_ID = "person_id";
    private static final String COLUMN_SYNC_STATUS = "sync_status";

    public UnifiedDatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_SOIL_DATA + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_TEMPERATURE + " REAL,"
                + COLUMN_SALINITY + " REAL,"
                + COLUMN_PH + " REAL,"
                + COLUMN_MOISTURE + " REAL,"
                + COLUMN_NITROGEN + " REAL,"
                + COLUMN_PHOSPHORUS + " REAL,"
                + COLUMN_POTASSIUM + " REAL,"
                + COLUMN_PERSON_ID + " TEXT,"
                + COLUMN_SYNC_STATUS + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database migration if needed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOIL_DATA);
        onCreate(db);
    }

    public void addSoilData(SoilData data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEMPERATURE, data.getTemperature());
        values.put(COLUMN_SALINITY, data.getSalinity());
        values.put(COLUMN_PH, data.getPh());
        values.put(COLUMN_MOISTURE, data.getMoisture());
        values.put(COLUMN_NITROGEN, data.getNitrogen());
        values.put(COLUMN_PHOSPHORUS, data.getPhosphorus());
        values.put(COLUMN_POTASSIUM, data.getPotassium());
        values.put(COLUMN_PERSON_ID, data.getPersonId());
        values.put(COLUMN_SYNC_STATUS, data.isSyncedWithFirebase() ? 1 : 0);

        db.insert(TABLE_SOIL_DATA, null, values);
        db.close();
    }

    public List<SoilData> getAllSoilData() {
        List<SoilData> dataList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_SOIL_DATA + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                dataList.add(cursorToSoilData(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataList;
    }
    public List<SoilData> getAverageRecords() {
        List<SoilData> averagesList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SOIL_DATA,
                null,
                COLUMN_PERSON_ID + " LIKE ?",
                new String[]{"AVERAGES_%"},
                null, null,
                COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                averagesList.add(cursorToSoilData(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return averagesList;
    }

    public List<SoilData> getDataByPersonId(String personId) {
        List<SoilData> dataList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_SOIL_DATA,
                null,
                COLUMN_PERSON_ID + "=?",
                new String[]{personId},
                null, null,
                COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                dataList.add(cursorToSoilData(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataList;
    }

    private SoilData cursorToSoilData(Cursor cursor) {
        SoilData data = new SoilData();
        data.setId(cursor.getInt(0));
        data.setTimestamp(cursor.getString(1));
        data.setTemperature(cursor.getFloat(2));
        data.setSalinity(cursor.getFloat(3));
        data.setPh(cursor.getFloat(4));
        data.setMoisture(cursor.getFloat(5));
        data.setNitrogen(cursor.getFloat(6));
        data.setPhosphorus(cursor.getFloat(7));
        data.setPotassium(cursor.getFloat(8));
        data.setPersonId(cursor.getString(9));
        data.setSyncedWithFirebase(cursor.getInt(10) == 1);
        return data;
    }
    public int deleteAverageRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SOIL_DATA,
                COLUMN_PERSON_ID + " LIKE ?",
                new String[]{"AVERAGES_%"});
    }

    public boolean deleteSoilData(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SOIL_DATA,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}) > 0;
    }
    public int clearAllSoilData() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_SOIL_DATA, null, null);
    }

}
