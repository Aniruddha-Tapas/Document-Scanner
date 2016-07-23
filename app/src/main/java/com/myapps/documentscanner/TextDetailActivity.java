package com.myapps.documentscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapps.documentscanner.data.TextContract;
import com.myapps.documentscanner.data.TextContract.TextEntry;
import com.myapps.documentscanner.helpers.Utils;
import com.myapps.documentscanner.views.TagEditorFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TextDetailActivity extends AppCompatActivity {
    private final static String LOG_TAG = "TextDetailActivity";
    //private Spinner mCategory;
    private AlertDialog.Builder deleteConfirmBuilder;
    private EditText mTitleText;
    private EditText mBodyText;
    private Button mEditButton;
    private Button mClear;

    private Uri textUri;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.text_edit);

        DocumentScannerApplication.getInstance().trackScreenView("OCR Text Details");

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(getString(R.string.edit_text_detail));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

        //mCategory = (Spinner) findViewById(R.id.category);
        mTitleText = (EditText) findViewById(R.id.text_edit_summary);
        mBodyText = (EditText) findViewById(R.id.text_edit_description);
        mEditButton = (Button) findViewById(R.id.text_edit_button);
        mClear = (Button) findViewById(R.id.mclear);


        //Used to get Passed param from SavedTextsActivity
        Bundle extras = getIntent().getExtras();

        //Use URI from saved instance
        if (bundle != null) {
            textUri = bundle.getParcelable(TextEntry.CONTENT_ITEM_TYPE);
        }

        //Use Uri from Passed param
        if (extras != null) {
            textUri = extras.getParcelable(TextEntry.CONTENT_ITEM_TYPE);   //getParcelableExtra to get URI from extra
            getTextListDetail(textUri);
        }

        mEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTextList();
            }
        });

        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBodyText.getText().toString().trim().length() != 0) {
                    AlertDialog.Builder clearDialog = new AlertDialog.Builder(v.getContext());

                    clearDialog.setTitle("Confirm");
                    clearDialog.setMessage("Do you want to clear the text?");

                    clearDialog.setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mBodyText.setText("");
                            dialog.dismiss();
                        }
                    });

                    clearDialog.setNegativeButton(getString(R.string.answer_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    clearDialog.create().show();
                }
            }

        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TextEntry.CONTENT_ITEM_TYPE, textUri);
    }

    // Grab textlist from database and display it to UI.
    public void getTextListDetail(Uri uri) {
        String[] projection = {TextEntry._ID, TextEntry.COLUMN_SUMMARY, TextEntry.COLUMN_DESCRIPTION};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            // Get the first result  with cursor
            cursor.moveToFirst();
            String textTitle = cursor.getString(cursor.getColumnIndexOrThrow(TextEntry.COLUMN_SUMMARY));
            String textBody = cursor.getString(cursor.getColumnIndexOrThrow(TextEntry.COLUMN_DESCRIPTION));

            mTitleText.setText(textTitle);
            mBodyText.setText(textBody);

            // always close the cursor
            cursor.close();
        }
    }

    // Insert or Update TextList item
    public void createTextList() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        if (title.length() > 0 && body.length() > 0) {
            // Create a new map of values, where column names are the key.
            ContentValues values = new ContentValues();
            values.put(TextEntry.COLUMN_SUMMARY, title);
            values.put(TextEntry.COLUMN_DESCRIPTION, body);

            if (textUri == null) {
                textUri = getContentResolver().insert(TextEntry.CONTENT_URI, values);
            } else {
                getContentResolver().update(textUri, values, null, null);
            }

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

            String ocrtextfile = Environment.getExternalStorageDirectory().toString()
                    + "/DocumentScanner/OCR-Texts/"
                    + title
                    + ".txt";

            String oldocrtextfile = Environment.getExternalStorageDirectory().toString()
                    + "/DocumentScanner/OCR-Texts/"
                    + textTitle
                    + ".txt";

            final File oldtextFile = new File(oldocrtextfile);
            oldtextFile.delete();

            File textFile = new File(ocrtextfile);

            try {
                textFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(textFile);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.write(body);
                myOutWriter.close();
                fOut.close();

                Toast.makeText(getBaseContext(), "Done writing to " + ocrtextfile, Toast.LENGTH_LONG).show();

                ExifInterface exif = null;

                exif = new ExifInterface(ocrtextfile);
                exif.setAttribute("UserComment", "Generated using DocumentScanner");
                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                String nowFormatted = mDateFormat.format(new Date().getTime());
                exif.setAttribute(ExifInterface.TAG_DATETIME, nowFormatted);
                exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, nowFormatted);
                exif.setAttribute("Software", "DocumentScanner " + BuildConfig.VERSION_NAME);
                exif.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Close the edit and back to main.
            this.finish();
        } else

        {
            Toast.makeText(this, "Please enter filename and text", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_text_edit, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.share_text:
                shareText();
                return true;

            case R.id.delete_text:

                deleteConfirmBuilder = new AlertDialog.Builder(this);
                deleteConfirmBuilder.setTitle(getString(R.string.confirm_title));
                deleteConfirmBuilder.setMessage(getString(R.string.confirm_delete));

                deleteConfirmBuilder.setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

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
                                + "/DocumentScanner/OCR-Texts/"
                                + textTitle
                                + ".txt";

                        final File textFile = new File(ocrtextfile);
                        textFile.delete();
                        Toast.makeText(getApplicationContext(), " Item Deleted ", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), SavedTextsList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

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

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public void shareText() {

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String title = mTitleText.getText().toString();
        String ocrtextfile = Environment.getExternalStorageDirectory().toString()
                + "/DocumentScanner/OCR-Texts/"
                + title
                + ".txt";


        final File textFile = new File(ocrtextfile);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(textFile));
        Log.d("Text_Share", "uri " + Uri.fromFile(textFile));
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_snackbar)));
    }
}
