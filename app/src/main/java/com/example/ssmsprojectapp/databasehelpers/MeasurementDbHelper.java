package com.example.ssmsprojectapp.databasehelpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ssmsprojectapp.datamodels.Measurement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MeasurementDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "measurements.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_MEASUREMENTS = "measurements";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FARM_ID = "farm_id";
    private static final String COLUMN_SALINITY = "salinity";
    private static final String COLUMN_MOISTURE = "moisture";
    private static final String COLUMN_TEMPERATURE = "temperature";
    private static final String COLUMN_PH = "ph";
    private static final String COLUMN_NITROGEN = "nitrogen";
    private static final String COLUMN_PHOSPHORUS = "phosphorus";
    private static final String COLUMN_POTASSIUM = "potassium";
    private static final String COLUMN_METALS = "metals";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    // Create table SQL query
    private static final String CREATE_TABLE_MEASUREMENTS =
            "CREATE TABLE " + TABLE_MEASUREMENTS + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_FARM_ID + " TEXT,"
                    + COLUMN_SALINITY + " REAL,"
                    + COLUMN_MOISTURE + " REAL,"
                    + COLUMN_TEMPERATURE + " REAL,"
                    + COLUMN_PH + " REAL,"
                    + COLUMN_NITROGEN + " REAL,"
                    + COLUMN_PHOSPHORUS + " REAL,"
                    + COLUMN_POTASSIUM + " REAL,"
                    + COLUMN_METALS + " TEXT,"
                    + COLUMN_TIMESTAMP + " INTEGER"
                    + ")";

    public MeasurementDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MEASUREMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEASUREMENTS);
        onCreate(db);
    }

    // Insert a measurement
    public void insertMeasurement(Measurement measurement) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FARM_ID, measurement.getFarmId());
        values.put(COLUMN_SALINITY, measurement.getSalinity());
        values.put(COLUMN_MOISTURE, measurement.getMoisture());
        values.put(COLUMN_TEMPERATURE, measurement.getTemperature());
        values.put(COLUMN_PH, measurement.getPh());
        values.put(COLUMN_NITROGEN, measurement.getNitrogen());
        values.put(COLUMN_PHOSPHORUS, measurement.getPhosphorus());
        values.put(COLUMN_POTASSIUM, measurement.getPotassium());
        values.put(COLUMN_METALS, measurement.getMetals());
        values.put(COLUMN_TIMESTAMP, measurement.getTimestamp().getTime());

        long id = db.insert(TABLE_MEASUREMENTS, null, values);
        db.close();
    }

    // Get all measurements
    @SuppressLint("Range")
    public List<Measurement> getAllMeasurements() {
        List<Measurement> measurements = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_MEASUREMENTS + " ORDER BY " + COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Measurement measurement = new Measurement();
                measurement.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                measurement.setFarmId(cursor.getString(cursor.getColumnIndex(COLUMN_FARM_ID)));
                measurement.setSalinity(cursor.getDouble(cursor.getColumnIndex(COLUMN_SALINITY)));
                measurement.setMoisture(cursor.getDouble(cursor.getColumnIndex(COLUMN_MOISTURE)));
                measurement.setTemperature(cursor.getDouble(cursor.getColumnIndex(COLUMN_TEMPERATURE)));
                measurement.setPh(cursor.getDouble(cursor.getColumnIndex(COLUMN_PH)));
                measurement.setNitrogen(cursor.getDouble(cursor.getColumnIndex(COLUMN_NITROGEN)));
                measurement.setPhosphorus(cursor.getDouble(cursor.getColumnIndex(COLUMN_PHOSPHORUS)));
                measurement.setPotassium(cursor.getDouble(cursor.getColumnIndex(COLUMN_POTASSIUM)));
                measurement.setMetals(cursor.getString(cursor.getColumnIndex(COLUMN_METALS)));
                measurement.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP))));

                measurements.add(measurement);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return measurements;
    }

    // Get measurements by farm ID
    @SuppressLint("Range")
    public List<Measurement> getMeasurementsByFarmId(String farmId) {
        List<Measurement> measurements = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MEASUREMENTS,
                null,
                COLUMN_FARM_ID + " = ?",
                new String[]{farmId},
                null, null,
                COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Measurement measurement = new Measurement();
                measurement.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
                measurement.setFarmId(cursor.getString(cursor.getColumnIndex(COLUMN_FARM_ID)));
                measurement.setSalinity(cursor.getDouble(cursor.getColumnIndex(COLUMN_SALINITY)));
                measurement.setMoisture(cursor.getDouble(cursor.getColumnIndex(COLUMN_MOISTURE)));
                measurement.setTemperature(cursor.getDouble(cursor.getColumnIndex(COLUMN_TEMPERATURE)));
                measurement.setPh(cursor.getDouble(cursor.getColumnIndex(COLUMN_PH)));
                measurement.setNitrogen(cursor.getDouble(cursor.getColumnIndex(COLUMN_NITROGEN)));
                measurement.setPhosphorus(cursor.getDouble(cursor.getColumnIndex(COLUMN_PHOSPHORUS)));
                measurement.setPotassium(cursor.getDouble(cursor.getColumnIndex(COLUMN_POTASSIUM)));
                measurement.setMetals(cursor.getString(cursor.getColumnIndex(COLUMN_METALS)));
                measurement.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP))));

                measurements.add(measurement);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return measurements;
    }

    // Get a single measurement by ID
    @SuppressLint("Range")
    public Measurement getMeasurement(String id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MEASUREMENTS,
                null,
                COLUMN_ID + " = ?",
                new String[]{id},
                null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        Measurement measurement = new Measurement();
        measurement.setId(cursor.getString(cursor.getColumnIndex(COLUMN_ID)));
        measurement.setFarmId(cursor.getString(cursor.getColumnIndex(COLUMN_FARM_ID)));
        measurement.setSalinity(cursor.getDouble(cursor.getColumnIndex(COLUMN_SALINITY)));
        measurement.setMoisture(cursor.getDouble(cursor.getColumnIndex(COLUMN_MOISTURE)));
        measurement.setTemperature(cursor.getDouble(cursor.getColumnIndex(COLUMN_TEMPERATURE)));
        measurement.setPh(cursor.getDouble(cursor.getColumnIndex(COLUMN_PH)));
        measurement.setNitrogen(cursor.getDouble(cursor.getColumnIndex(COLUMN_NITROGEN)));
        measurement.setPhosphorus(cursor.getDouble(cursor.getColumnIndex(COLUMN_PHOSPHORUS)));
        measurement.setPotassium(cursor.getDouble(cursor.getColumnIndex(COLUMN_POTASSIUM)));
        measurement.setMetals(cursor.getString(cursor.getColumnIndex(COLUMN_METALS)));
        measurement.setTimestamp(new Date(cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP))));

        cursor.close();
        db.close();
        return measurement;
    }

    // Update a measurement
    public int updateMeasurement(Measurement measurement) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FARM_ID, measurement.getFarmId());
        values.put(COLUMN_SALINITY, measurement.getSalinity());
        values.put(COLUMN_MOISTURE, measurement.getMoisture());
        values.put(COLUMN_TEMPERATURE, measurement.getTemperature());
        values.put(COLUMN_PH, measurement.getPh());
        values.put(COLUMN_NITROGEN, measurement.getNitrogen());
        values.put(COLUMN_PHOSPHORUS, measurement.getPhosphorus());
        values.put(COLUMN_POTASSIUM, measurement.getPotassium());
        values.put(COLUMN_METALS, measurement.getMetals());
        values.put(COLUMN_TIMESTAMP, measurement.getTimestamp().getTime());

        return db.update(TABLE_MEASUREMENTS, values, COLUMN_ID + " = ?",
                new String[]{measurement.getId()});
    }

    // Delete a measurement
    public void deleteMeasurement(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MEASUREMENTS, COLUMN_ID + " = ?",
                new String[]{id});
        db.close();
    }

    // Clear all measurements
    public void clearAllMeasurements() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_MEASUREMENTS);
        db.close();
    }

    // Get measurements count
    public int getMeasurementsCount() {
        String countQuery = "SELECT * FROM " + TABLE_MEASUREMENTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    /**
     * Checks if the measurements table is empty
     * @return true if the table is empty, false otherwise
     */
    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_MEASUREMENTS, null);

        boolean isEmpty = true;
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            isEmpty = (count == 0);
            cursor.close();
        }

        db.close();
        return isEmpty;
    }
}
