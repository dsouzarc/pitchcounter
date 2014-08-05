package com.ryan.pitchcounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SQLitePitcherDatabase extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "PitcherDB";
    private static final String TABLE_NAME = "pitcher";
    private static final String KEY_PITCHER_NAME = "PitcherName";
    private static final String KEY_NUM_PITCHES = "NumPitches";
    private final String KEY_ID = "id";

    private final String[] COLUMNS = {KEY_ID, KEY_PITCHER_NAME, KEY_NUM_PITCHES};

    public SQLitePitcherDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        final String CREATE_PITCHER_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_PITCHER_NAME + " TEXT, "+
                KEY_NUM_PITCHES + " SHORT )";

        // create books table
        db.execSQL(CREATE_PITCHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public void addPitcher(final Pitcher thePitcher) {
        final SQLiteDatabase db = this.getWritableDatabase();

        final ContentValues values = new ContentValues();
        values.put(KEY_PITCHER_NAME, thePitcher.getName());
        values.put(KEY_NUM_PITCHES, thePitcher.getNumPitches());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public List<Pitcher> getAllPitchers(final Context theC) {
        final Set<Pitcher> thePitchers = new HashSet<Pitcher>();
        final List<String> theNames = new ArrayList<String>();

        final SQLiteGamesDatabase theGDB = new SQLiteGamesDatabase(theC);
        final Game[] theG = getArray(theGDB.getAllGames());
        final String query = "SELECT  * FROM " + TABLE_NAME;

        final SQLiteDatabase db = this.getWritableDatabase();
        final Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                theNames.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        final  short[] numPer = new short[theNames.size()];

        for(int i = 0; i < theNames.size(); i++) {
            for (int y = 0; y < theG.length; y++) {
                if (theNames.get(i).equals(theG[y].getPitcherName())) {
                    numPer[i] += theG[y].getTotalPitches();
                }
            }
        }

        for(int i = 0; i < theNames.size(); i++)
            thePitchers.add(new Pitcher(theNames.get(i), numPer[i]));

        db.close();
        return new ArrayList<Pitcher>(thePitchers);
    }

    public void deletePitcher(final Pitcher thePitcher) {
        final SQLiteDatabase db = this.getWritableDatabase();

        try
        {
            db.delete(TABLE_NAME,
                    KEY_PITCHER_NAME +" = ?",
                    new String[] {thePitcher.getName()});
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
    }

    //Delete everything
    public void deleteAllPitchers() {
        final SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.delete(TABLE_NAME, null, null);
        theDB.close();
    }

    public void addPitchers(final Pitcher[] thePitchers) {
        final SQLiteDatabase theDB = this.getWritableDatabase();
        theDB.beginTransaction();

        for(Pitcher thePitcher : thePitchers) {
            final ContentValues theVals = new ContentValues();
            theVals.put(KEY_PITCHER_NAME, thePitcher.getName());
            theVals.put(KEY_NUM_PITCHES, thePitcher.getNumPitches());
            theDB.insert(TABLE_NAME, null, theVals);
        }

        theDB.setTransactionSuccessful();
        theDB.endTransaction();
        theDB.close();
    }

    public Game[] getArray(final List<Game> theGames) {
        return theGames.toArray(new Game[theGames.size()]);
    }

    public void log(final String message) {
        Log.e("com.ryan.pitchcounter", message);
    }
}
