
package com.example.studentassignmenttracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AssignmentDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Assignments";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "subject TEXT NOT NULL, " +
                "dueDate TEXT NOT NULL, " +
                "status TEXT DEFAULT 'Pending')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        // Preserve existing records during upgrades.
    }

    public boolean addAssignment(String title, String subject,
                                 String dueDate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("subject", subject);
        values.put("dueDate", dueDate);
        values.put("status", "Pending");

        long result = db.insert(TABLE_NAME, null, values);

        return result != -1;
    }

    public Cursor getAllAssignments() {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT id, title, subject, dueDate, status " +
                        "FROM " + TABLE_NAME + " ORDER BY id DESC",
                null
        );
    }

    public boolean updateAssignment(long id, String title,
                                    String subject, String dueDate) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("subject", subject);
        values.put("dueDate", dueDate);

        int rowsUpdated = db.update(
                TABLE_NAME,
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        Log.d("AssignmentDB", "Rows updated: " + rowsUpdated);

        return rowsUpdated > 0;
    }

    public boolean deleteAssignment(long id) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {
            int rowsDeleted = db.delete(
                    TABLE_NAME,
                    "id = ?",
                    new String[]{String.valueOf(id)}
            );

            Log.d("AssignmentDB",
                    "Deleting ID: " + id +
                            ", rows deleted: " + rowsDeleted);

            if (rowsDeleted > 0) {
                db.setTransactionSuccessful();
                return true;
            }

            return false;

        } finally {
            db.endTransaction();
        }
    }
}
