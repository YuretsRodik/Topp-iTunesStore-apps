package com.yuretsrodik.top100apps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DB {

    //class constants

	final String LOG = "mLogs";
	private static final String DB_NAME = "apps.db";
	private final static int VERSION = 1;
	public static final String TABLE_NAME = "applications";
	private static final String CREATE_ENTRIES = "CREATE TABLE applications("
			+ "_id INTEGER PRIMARY KEY, "
			+ "name TEXT" + ");";
	private static final String DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;
	public static final String COL_NAME = "name";
    public static final String COL_ID = "_id";

	private SQLiteDatabase mDB;
	private DBHelper dbHelper;
	static Context mCtx;

    //Database class constructor
	public DB(Context ctx) {
		mCtx = ctx;
	}
    //Method for opening connection and getting writable database
	public void openConnection(){
		dbHelper = new DBHelper(mCtx);
		mDB = dbHelper.getWritableDatabase();
        Log.d(LOG, "Connection is opened");
	}
    //Method for closing connection
	public void closeConnection(){
		if(dbHelper != null) dbHelper.close();
        Log.d(LOG, "Connection is closed");
	}
    //Getting data from database with query, which returns cursor
	public Cursor queryDB(){
		return mDB.query(TABLE_NAME, null, null, null, null, null, null);
	}

    //checking database for existing records
    public Boolean checkData(){
        Boolean bool = false;
        Cursor c = mDB.query(TABLE_NAME, null, null, null, null, null, null);
        if(c.moveToFirst()){
            bool = false;
        }else{
            bool = true;
        }
        return bool;
    }
    //Method for inserting new data
    public void insert(ContentValues cv){
        mDB.insert(TABLE_NAME, null, cv);
        Log.d(LOG, "Inserted!");
    }

    //DataBase Helper class
	private class DBHelper extends SQLiteOpenHelper {

        //constructor
		public DBHelper(Context context){
			super(context, DB_NAME, null, VERSION);
		}	
        //Creating database
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_ENTRIES);
		}

        //upgrading database with new version
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL(DELETE_ENTRIES);
			onCreate(db);
		}
	}

}
