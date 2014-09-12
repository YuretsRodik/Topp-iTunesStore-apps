package com.yuretsrodik.top100apps;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

public class MyCursorLoader extends CursorLoader {
	
	DB mDB;

    //constructor of the CursorLoader class
	public MyCursorLoader(Context context, DB mDB) {
		super(context);
		this.mDB = mDB;
	}

    //Loading data in background
	@Override
	public Cursor loadInBackground() {
		Cursor cursor = mDB.queryDB();
		return cursor;
	}

}
