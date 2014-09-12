package com.yuretsrodik.top100apps;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>{

    final String LOG = "mLogs";

    private static final int LOADER_ID = 1;
    ListView lvMain;

    DB mDB;
    AsyncTaskParseJson task;
    SimpleCursorAdapter scAdapter;
    //variables for adapter
    String[] from = null;
    int[] to = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);
        //initializing database and open connection
        mDB = new DB(this);
        mDB.openConnection();
        //finding ListView
        lvMain = (ListView)findViewById(R.id.lvMain);

        //checking database if has records
        if(mDB.checkData()){
            //AsyncTask subclass for parsing data in background
            task = new AsyncTaskParseJson();
            task.execute();
            Log.d(LOG, "DD is empty. AsyncTask is started");
        }else{
            //apps already in database, just load
            onInitApps();
            Log.d(LOG, "DB already filled");
        }
    }

    //initializing ListView
    public void onInitApps() {
        //forming variables for inserting to adapter
        from = new String[]{DB.COL_ID, DB.COL_NAME};
        to = new int[]{R.id.tvNumber, R.id.tvName};
        //init adapter
        scAdapter = new SimpleCursorAdapter(this, R.layout.item, null, from, to, 0);
        //setting adapter
        lvMain.setAdapter(scAdapter);
        //initializing CursorLoader
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    //creating menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //close application if selected MenuItem "Exit"
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.exit:
                finish();
                return true;
            default:
                return false;
        }
    }

    //initializing loader class
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bndl) {
        return new MyCursorLoader(this, mDB);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //putting data from cursor to adapter
        scAdapter.swapCursor(data);
    }
    //clearing adapter before recall CursorLoader
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        scAdapter.swapCursor(null);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        //disconnecting database
        if(mDB != null){
            mDB.closeConnection();
        }
    }

    private class AsyncTaskParseJson extends AsyncTask<Void, Boolean, Boolean> {

        // our url
        String url = "https://itunes.apple.com/br/rss/topfreeapplications/limit=100/json";

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            //initializing ProgressDialog before loading data
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Loading...");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(LOG, "AsyncTask. Do in background");
            ContentValues cv = new ContentValues();

            try {//Making a request to server getting entities
                JSONObject json = new JSONObject(EntityUtils.toString(
                        new DefaultHttpClient().execute(
                                new HttpGet(url)).getEntity()));
                //getting json root object
                JSONObject feedObject = json.getJSONObject("feed");
                //getting needed array in root json object
                JSONArray entryArray = feedObject.getJSONArray("entry");

                //moving though the array
                for(int i = 0; i < entryArray.length(); i++){
                    //getting all objects in array
                    JSONObject entryObjects = entryArray.getJSONObject(i);
                    //taking objects with needed key
                    JSONObject nameObject = entryObjects.getJSONObject("im:name");
                    //getting string name
                    String name = nameObject.getString("label");
                    //putting data into ContentValues - name and id (for making a numbers
                    // next to records in ListView)
                    cv.put(DB.COL_NAME, name);
                    cv.put(DB.COL_ID, i+1);
                    mDB.insert(cv);
                    //just controlling in log
                    Log.d(LOG, "" + name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean boolFromBackground){
            //closing ProgressDialog
            if(dialog.isShowing()){
                dialog.dismiss();
            }
            //calling for the method for loading data from database
            onInitApps();
        }
    }
}
