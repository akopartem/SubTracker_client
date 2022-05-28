package xyz.akopartem.subtracker;

import java.time.LocalDate;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {

    private Context context;
    private String DB_NAME = "subs.db";

    private SQLiteDatabase db;

    static DBManager dbManager;

    public static DBManager getInstance(Context context) {
        if (dbManager == null) {
            dbManager = new DBManager(context);
        }
        return dbManager;
    }

    private DBManager(Context context) {
        this.context = context;
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        createTablesIfNeedBe();
    }

    void addResult(String name, int price, LocalDate date) {
        db.execSQL(String.format("INSERT INTO SUBS VALUES ('%s', %d, 's', '%s')", name, price, date.toString()));
    }

    void exec(String o) {
        db.execSQL(o);
    }

    ArrayList<Sub> getAllResults() {
        ArrayList<Sub> data = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM SUBS;", null);
        boolean hasMoreData = cursor.moveToFirst();

        while (hasMoreData) {
            String name = cursor.getString(cursor.getColumnIndex("NAME"));
            int price = Integer.parseInt(cursor.getString(cursor.getColumnIndex("PRICE")));
            String lastTimestamp = cursor.getString(cursor.getColumnIndex("DATE"));
            data.add(new Sub(name, price, LocalDate.parse(lastTimestamp)));
            hasMoreData = cursor.moveToNext();
        }

        return data;
    }

    private void createTablesIfNeedBe() {
        db.execSQL("CREATE TABLE IF NOT EXISTS SUBS (NAME TEXT, PRICE INTEGER, CURRENCY TEXT, DATE TEXT);");
    }


}
