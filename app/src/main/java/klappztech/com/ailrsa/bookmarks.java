package klappztech.com.ailrsa;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class bookmarks extends ActionBarActivity {


    public static final String  ACTION_BROADCAST = "URL_BROADCAST", EXTRA_URL_TEXT = "URL";
    ListView bookmarkLV;

    //String[] links = {"USA", "UK","CHINA","FRANCE","RUSSIA","INDIA","pak","Kerala","tamil nadu","andhra","madhyaparadae"};
    DBAdapter  myDb;
    List<Long> selected_list_items = new ArrayList<Long>();
    int selected_count=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

        //fullscreen
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);



        //database
        openDB();
        registerListClickCallback();

        //list saved URL
        populateListViewFromDB();



    }

    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }

    private void closeDB() {
        myDb.close();
    }
    public void onClick_AddRecord(View v) {

        Toast.makeText(getApplicationContext(), "Clicked add record!", Toast.LENGTH_SHORT).show();

        long newId = myDb.insertRow("Jenny", "555", 14);

        // Query for the record we just added.
        // Use the ID:
        //Cursor cursor = myDb.getRow(newId);
        //displayRecordSet(cursor);
    }

    public void onClick_ClearAll(View v) {
        Toast.makeText(getApplicationContext(), "Clicked clear all!", Toast.LENGTH_SHORT).show();
        myDb.deleteAll();
        populateListViewFromDB();
    }

    public void populateListViewFromDB() {
        // Set the adapter for the list view
        final ListView myList = (ListView) findViewById(R.id.listView);
        View empty = findViewById(R.id.textViewNoBookmark);

        myList.setEmptyView(empty);

        Cursor cursor = myDb.getAllRows();

        // Allow activity to manage lifetime of the cursor.
        // DEPRECATED! Runs on the UI thread, OK for small/short queries.
        startManagingCursor(cursor);

        // Setup mapping from cursor to view fields:
        String[] fromFieldNames = new String[]
                {DBAdapter.KEY_TITLE, DBAdapter.KEY_URL};
        int[] toViewIDs = new int[]
                {R.id.item_name,     R.id.item_favcolour};

        // Create adapter to may columns of the DB onto elemesnt in the UI.
        SimpleCursorAdapter myCursorAdapter =
                new SimpleCursorAdapter(
                        this,		// Context
                        R.layout.item_layout,	// Row layout template
                        cursor,					// cursor (set of DB records to map)
                        fromFieldNames,			// DB Column names
                        toViewIDs				// View IDs to put information in
                );


        myList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        myList.setItemsCanFocus(false);
        myList.setAdapter(myCursorAdapter);
        myList.setMultiChoiceModeListener( new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(b) {
                    selected_count++;
                    actionMode.setTitle(selected_count+" items selected");
                    selected_list_items.add(l);
                }else {
                    selected_count--;
                    actionMode.setTitle(selected_count+" items selected");
                    selected_list_items.remove(l);
                }

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_bookmarks,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

                switch (menuItem.getItemId())
                {
                    case R.id.delete_id:
                        if(selected_list_items.isEmpty()) {
                            Toast.makeText(getApplicationContext(),"Select items to delete!", Toast.LENGTH_LONG);
                        }
                        for(long id : selected_list_items)
                        {


                            Cursor cursor = myDb.getRow(id);
                            if (cursor.moveToFirst()) {
                                long idDB = cursor.getLong(DBAdapter.COL_ROWID);
                                // String name = cursor.getString(DBAdapter.COL_TITLE);
                                String url = cursor.getString(DBAdapter.COL_URL);
                                //String favColour = cursor.getString(DBAdapter.COL_FAVCOLOUR);

                                    myDb.deleteRow(idDB);
                            }
                            cursor.close();



                        }
                        populateListViewFromDB();
                        Toast.makeText(getApplicationContext(),selected_count + "items deleted", Toast.LENGTH_SHORT);

                        actionMode.finish();
                        return true;
//                        break;
                    default:

                        return false;
                }
//                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                selected_count=0;
                selected_list_items.clear();
            }
        });
    }
    private void registerListClickCallback() {
        ListView myList = (ListView) findViewById(R.id.listView);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,
                                    int position, long idInDB) {

                Toast.makeText(getApplicationContext(), "Clicked on Item:"+idInDB, Toast.LENGTH_SHORT).show();


                ActionOnClick(idInDB);
            }
        });
    }

    private void ActionOnClick(long idInDB) {
        View v=null;
        Cursor cursor = myDb.getRow(idInDB);
        if (cursor.moveToFirst()) {
            long idDB = cursor.getLong(DBAdapter.COL_ROWID);
            String url = cursor.getString(DBAdapter.COL_URL);

             sendBroadcastMessage(url);
        }
        cursor.close();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_bookmarks,menu);
        return true;


    }

    /********************************************************************************************/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDB();
    }

    private void sendBroadcastMessage(String url) {

        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_URL_TEXT, url);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        Log.e("mahc", "==========sent broadcast"+url);

        // close this activity
        this.finish();
    }


}
