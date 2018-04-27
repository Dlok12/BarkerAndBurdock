package empty.barkerandburdock;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by user-pc on 06.03.2018.
 *
 * Контроллер базы данных матчей
 */

public class DbController {

    // Модель базы данных:
    //
    // int id,
    // int barker,          как походил зазывала
    // int burdock,         как походил лопух
    // int compbarker,      сложность зазывалы (random, optimal, human)
    // int compburdock,     сложность лопуха
    // int result           итог матча (от лица зазывалы)
    //
    // Справка на сайте:
    // http://startandroid.ru/ru/uroki/vse-uroki-spiskom/74-urok-34-hranenie-dannyh-sqlite.html

    DbController(Context c) {
        context = c;
    }

    Context context;
    DBHelper dbHelper;

    long dbAddRow(int barker, int burdock, int modeBarker, int modeBurdock, int result) {

        dbHelper = new DBHelper(context);

        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        cv.put("barker", barker);
        cv.put("burdock", burdock);
        cv.put("modebarker", modeBarker);
        cv.put("modeburdock", modeBurdock);
        cv.put("result", result);

        long rowID = db.insert("matches", null, cv);
        dbHelper.close();
        return rowID;
    }

    int getIndex(int modeBarker, int modeBurdock) {
        int[][] res = {
                {0, 1, 2},
                {3, 4, 5},
                {6, 7, 8}
        };

        return res[modeBarker][modeBurdock];
    }

    long[][] refreshStatistic() {

        long[][] result = new long[2][8];

        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.query("matches", null, null,
                null, null, null, null);

        if (c.moveToFirst()) {
            int modeBarkerColIndex = c.getColumnIndex("modebarker");
            int modeBurdockColIndex = c.getColumnIndex("modeburdock");
            int resultColIndex = c.getColumnIndex("result");

            do {
                int modeBarker = c.getInt(modeBarkerColIndex);
                int modeBurdock = c.getInt(modeBurdockColIndex);
                int res = c.getInt(resultColIndex);

                int index = getIndex(modeBarker, modeBurdock);

                result[0][index]++;
                result[1][index] += res;

            } while (c.moveToNext());
        }

        c.close();
        dbHelper.close();

        return result;
    }

    public List<int[]> readTable() {

        List<int[]> result = new List<int[]>() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean contains(Object o) {
                return false;
            }

            @NonNull
            @Override
            public Iterator<int[]> iterator() {
                return null;
            }

            @NonNull
            @Override
            public Object[] toArray() {
                return new Object[0];
            }

            @NonNull
            @Override
            public <T> T[] toArray(@NonNull T[] ts) {
                return null;
            }

            @Override
            public boolean add(int[] ints) {
                return false;
            }

            @Override
            public boolean remove(Object o) {
                return false;
            }

            @Override
            public boolean containsAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean addAll(@NonNull Collection<? extends int[]> collection) {
                return false;
            }

            @Override
            public boolean addAll(int i, @NonNull Collection<? extends int[]> collection) {
                return false;
            }

            @Override
            public boolean removeAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public boolean retainAll(@NonNull Collection<?> collection) {
                return false;
            }

            @Override
            public void clear() {

            }

            @Override
            public int[] get(int i) {
                return new int[0];
            }

            @Override
            public int[] set(int i, int[] ints) {
                return new int[0];
            }

            @Override
            public void add(int i, int[] ints) {

            }

            @Override
            public int[] remove(int i) {
                return new int[0];
            }

            @Override
            public int indexOf(Object o) {
                return 0;
            }

            @Override
            public int lastIndexOf(Object o) {
                return 0;
            }

            @NonNull
            @Override
            public ListIterator<int[]> listIterator() {
                return null;
            }

            @NonNull
            @Override
            public ListIterator<int[]> listIterator(int i) {
                return null;
            }

            @NonNull
            @Override
            public List<int[]> subList(int i, int i1) {
                return null;
            }
        };
        dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor c = db.query("matches", null, null,
                null, null, null, null);

        if (c.moveToFirst()) {

            int idColIndex = c.getColumnIndex("id");
            int barkerColIndex = c.getColumnIndex("barker");
            int burdockColIndex = c.getColumnIndex("burdock");
            int modeBarkerColIndex = c.getColumnIndex("modebarker");
            int modeBurdockColIndex = c.getColumnIndex("modeburdock");
            int resultColIndex = c.getColumnIndex("result");

            do {
                int[] arr = {
                        c.getInt(idColIndex),
                        c.getInt(barkerColIndex),
                        c.getInt(burdockColIndex),
                        c.getInt(modeBarkerColIndex),
                        c.getInt(modeBurdockColIndex),
                        c.getInt(resultColIndex)
                };
                result.add(arr);
            } while (c.moveToNext());
        }

        c.close();
        dbHelper.close();

        return result;
    }

    public int clearTable(String table) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int clearCount = db.delete(table, null, null);
        dbHelper.close();

        return clearCount;
    }

    class DBHelper extends SQLiteOpenHelper {

        DBHelper(Context context) {
            super(context, "matchesDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(
                    "create table matches ("
                    + "id integer primary key autoincrement,"
                    + "barker integer,"
                    + "burdock integer,"
                    + "modebarker integer,"
                    + "modeburdock integer,"
                    + "result integer"
                    + ");"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }
}
