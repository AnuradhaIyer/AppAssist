package edu.sjsu.seekers.appassist.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.sjsu.seekers.appassist.model.Apps;

public class DB extends SQLiteOpenHelper {

    private static final String TAG = "AppAssist";
    private static String DB_NAME = "appassist.db";
    private static String RecommendationsTableName = "Recommendations";
    private static String DB_PATH = "";
    private static final int DB_VERSION = 1;
    public static final int MAX_APPS_DISPLAY = 15;
    protected static int COUNT_OF_APPS = 11045;

    private SQLiteDatabase mDataBase;
    private Context mContext = null;

    Set<Integer> randomAppsIdSet = null;

    private static DB db;

    public static DB getInstance(Context context){
        if(db == null)
            db = new DB(context);

        return db;
    }

    public DB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        else
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;

        copyDataBase();

        this.getWritableDatabase();
    }

    public void updateDataBase() throws IOException {

        File dbFile = new File(DB_PATH + DB_NAME);
        if (dbFile.exists())
            dbFile.delete();

        //copyDataBase();

    }
    private boolean checkDataBase() {
        File dbFile = new File(DB_PATH + DB_NAME);
        return dbFile.exists();
    }

    private void copyDataBase() {
        if (!checkDataBase()) {
            this.getWritableDatabase();
            this.close();
            try {
                copyDBFile();
            } catch (IOException mIOException) {
                throw new Error("ErrorCopyingDataBase");
            }
        }
    }

    private void copyDBFile() throws IOException {
        InputStream mInput = mContext.getAssets().open(DB_NAME);
        //InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
        OutputStream mOutput = new FileOutputStream(DB_PATH + DB_NAME);
        byte[] mBuffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(mBuffer)) > 0)
            mOutput.write(mBuffer, 0, mLength);
        mOutput.flush();
        mOutput.close();
        mInput.close();
    }


    public boolean openDataBase() throws SQLException {

        if(mDataBase == null)
            mDataBase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READWRITE);

        return mDataBase != null;
    }

    @Override
    public synchronized void close() {
        if (mDataBase != null)
            mDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) { }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }


    public List<Apps> retierieveRandomApps()
    {
        Apps app = null;
        List<Apps> lstApps = new ArrayList<>();
        String apps = getRandomApps();
        Log.i(TAG, "random apps selected : " + apps);
        return getAppsFromDB(apps);
    }

    public List<Apps> getAppsFromDB(String apps) {
        Log.i(TAG, "getting apps from db");
        List<Apps> lstApps = new ArrayList<>();
        Apps app;Cursor c = mDataBase.rawQuery("Select * from Apps where appid IN (" + apps +")", null);
        while(c.moveToNext())
        {
            //Log.d(TAG, "details : " + c.getString(1));
            app = new Apps(Integer.parseInt(c.getString(0)),c.getString(1),c.getString(2),c.getString(3),
                    c.getString(4),c.getString(5));
            lstApps.add(app);
        }
        return lstApps;
    }

    public String getAppIdString(List<Integer> lstApps){
        Log.i(TAG, "getting top results app id strings");
        String finalApps = "";
        for(Integer i: lstApps){
            finalApps +=  "'"+ i + "',";
        }
        return finalApps.substring(0,finalApps.length()-1);
    }

    String getRandomApps(){
        String finalApps = "";
        randomAppsIdSet = new HashSet<>();
        int next = -1;
        while(true){
            next = randomWithRange(1,COUNT_OF_APPS);
            if(!randomAppsIdSet.contains(next)) {
                randomAppsIdSet.add(next);
                finalApps +=  "'"+ next + "',";
            }
            if(randomAppsIdSet.size() == MAX_APPS_DISPLAY)
                return finalApps.substring(0,finalApps.length()-1);
        }
    }

    int randomWithRange(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }


    public HashMap<Integer,Float> getRecommendationApps()
    {
        Log.i(TAG, "getting already stored recommendation apps");
        Cursor c = mDataBase.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + RecommendationsTableName + "'", null);
        c.moveToNext();
        if(c.getCount() == 0)
        {

           Log.i(TAG, "Creating  Recommendations table in DB");

           mDataBase.execSQL("CREATE TABLE IF NOT EXISTS " + RecommendationsTableName + " (appid INTEGER PRIMARY KEY, Ratings REAL)");
//            mDataBase.setTransactionSuccessful();
            return null;
        }

        HashMap<Integer,Float> mapRecommendations = new HashMap<>();


        Log.i(TAG, "Previously recommended apps to be selected from DB");
        c = mDataBase.rawQuery("Select * from " + RecommendationsTableName, null);
        while(c.moveToNext())
        {
            Log.i(TAG, "recommendation for app id: " + c.getString(0) + " is: " + c.getString(1));
            mapRecommendations.put(c.getInt(0),c.getFloat(1));
        }
        return mapRecommendations;
    }


    public void saveRecommendations(HashMap<Integer,Float> mapRecommendations) {

        ContentValues values;
        Cursor c;

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            for (Integer i : mapRecommendations.keySet()) {

                values = new ContentValues();
                values.put("appid", i);
                values.put("Ratings", mapRecommendations.get(i));

                c = db.rawQuery("Select * from " + RecommendationsTableName + " where appid= " + i, null);

                if (c.getCount() == 0) {
                    db.insert(RecommendationsTableName, null, values);
                    Log.i(TAG, "inserting new recommendation for app id: " + i + " is: " + values.get("Ratings"));
                } else {
                    c.moveToNext();
                    if(c.getFloat(1) != mapRecommendations.get(i)) {
                        String selection = "appid= " + i;
                        db.update(RecommendationsTableName, values, selection, null);
                        Log.i(TAG, "updating  recommendation for app id: " + i + " is: " + values.get("Ratings"));
                    }
                    Log.i(TAG, "No update in recommendation for app id: " + i + " still: " + values.get("Ratings"));
                }
            }
            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
            db.close();
        }

    }

}
