package com.myapps.documentscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.myapps.documentscanner.data.TextContract;
import com.myapps.documentscanner.data.TextContract.TextEntry;

import java.io.File;


public class SavedTextsList extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = "SavedTextsList";
    // private Cursor cursor;
    private SimpleCursorAdapter mTextAdapter;
    private AlertDialog.Builder deleteConfirmBuilder;

    private static final int TEXT_LOADER = 0;
    private static final int ACTIVITY_EDIT = 0;
    private static final int ACTIVITY_SHARE = 1;
    private static final int ACTIVITY_DELETE = 2;
    private ListView listViewText;

    @Override
    protected void onResume() {
        super.onResume();
        // todo: refresh listview with newer data when update / insert done
        getLoaderManager().restartLoader(TEXT_LOADER, null, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.texts_list);

        DocumentScannerApplication.getInstance().trackScreenView("Saved OCR Texts");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.drawable.view_headline);
        actionBar.setTitle("  Saved Texts");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

        getTextList();

    }

    private void getTextList(){
        // Fields from the database (projection)
        String[] from = new String[] {TextEntry.COLUMN_SUMMARY};

        // Fields on the UI to which we map
        int[] to = new int[] { R.id.label };

        listViewText = (ListView) findViewById(R.id.listText);

        //Init loader for first time use.
        getLoaderManager().initLoader(TEXT_LOADER, null, this);
        mTextAdapter = new SimpleCursorAdapter(this, R.layout.text_row, null, from,  to, 0);
        listViewText.setEmptyView(findViewById(R.id.txvEmptyList));         //Set view if listview is empty
        listViewText.setAdapter(mTextAdapter);
        listViewText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(view.getContext(), TextDetailActivity.class);
                // content://com.com.myapps.documentscanner/todos/2
                Uri textUri = TextEntry.buildTextUri(id);
                i.putExtra(TextEntry.CONTENT_ITEM_TYPE, textUri);
                startActivity(i);
            }
        });

        //Initialize context menu for ListView, Long press to show it.
        registerForContextMenu(listViewText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_texts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        int id = v.getId();
        if (id == R.id.listText) {
            menu.setHeaderTitle("Manage Saved Texts");
            String[] menuItems = getResources().getStringArray(R.array.manage_text);
            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        long id = item.getItemId();
        final AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();      //Used to get id from the selected item

        if(id == ACTIVITY_EDIT){
            Intent intent = new Intent(this, TextDetailActivity.class);
            Uri textUri = TextEntry.buildTextUri(info.id);
            intent.putExtra(TextEntry.CONTENT_ITEM_TYPE, textUri);
            startActivity(intent);
        }

        if(id == ACTIVITY_SHARE){
            Uri textUri = TextEntry.buildTextUri(info.id);
            String[] projection = {TextEntry._ID, TextEntry.COLUMN_SUMMARY, TextEntry.COLUMN_DESCRIPTION};
            Cursor cursor = getContentResolver().query(textUri, projection, null, null, null);

            String textTitle = null;
            if (cursor != null) {
                // Get the first result  with cursor
                cursor.moveToFirst();
                textTitle = cursor.getString(cursor.getColumnIndexOrThrow(TextEntry.COLUMN_SUMMARY));
                // always close the cursor
                cursor.close();
            }

            shareText(textTitle);
        }

        if(id == ACTIVITY_DELETE){

            deleteConfirmBuilder = new AlertDialog.Builder(this);
            deleteConfirmBuilder.setTitle(getString(R.string.confirm_title));
            deleteConfirmBuilder.setMessage(getString(R.string.confirm_delete));

            deleteConfirmBuilder.setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {

                    Uri textUri = TextEntry.buildTextUri(info.id);

                    String[] projection = {TextEntry._ID, TextEntry.COLUMN_SUMMARY, TextEntry.COLUMN_DESCRIPTION};
                    Cursor cursor = getContentResolver().query(textUri, projection, null, null, null);

                    String textTitle = null;
                    if (cursor != null) {
                        // Get the first result  with cursor
                        cursor.moveToFirst();
                        textTitle = cursor.getString(cursor.getColumnIndexOrThrow(TextEntry.COLUMN_SUMMARY));
                        // always close the cursor
                        cursor.close();
                    }

                    getContentResolver().delete(textUri, null, null);

                    String ocrtextfile = Environment.getExternalStorageDirectory().toString()
                            + "/ConTextScanner/OCR-Texts/"
                            + textTitle
                            + ".txt";
                    final File textFile = new File(ocrtextfile);
                    textFile.delete();

                    Toast.makeText(getApplicationContext(), " Item Deleted ", Toast.LENGTH_SHORT).show();

                    getLoaderManager().restartLoader(TEXT_LOADER, null,SavedTextsList.this);      // Refresh listview to show the latest item from db after delete.

                    dialog.dismiss();

                }

            });

            deleteConfirmBuilder.setNegativeButton(getString(R.string.answer_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            deleteConfirmBuilder.create().show();
        }

        return super.onContextItemSelected(item);
    }

    public void shareText(String title) {

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String ocrtextfile = Environment.getExternalStorageDirectory().toString()
                + "/ConTextScanner/OCR-Texts/"
                + title
                + ".txt";

        final File textFile = new File(ocrtextfile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(textFile));
        Log.d("Text_Share","uri "+Uri.fromFile(textFile));
        startActivity(Intent.createChooser(shareIntent, "Share text file through:"));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { TextEntry._ID, TextEntry.COLUMN_SUMMARY };
        String sortOrder = TextEntry._ID + " DESC ";

        CursorLoader cursorLoader = new CursorLoader(this, TextEntry.CONTENT_URI, projection, null, null, sortOrder);

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTextAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        mTextAdapter.swapCursor(null);
    }
}
