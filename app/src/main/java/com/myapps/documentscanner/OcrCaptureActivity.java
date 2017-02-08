package com.myapps.documentscanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.CheckBox;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.myapps.documentscanner.data.TextContract;
import com.myapps.documentscanner.uicamera.CameraSource;
import com.myapps.documentscanner.uicamera.CameraSourcePreview;
import com.myapps.documentscanner.uicamera.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Activity for the OCR Capture.  This activity detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String WidgetIntent = "WidgetIntent";
    public static final String WidgetOCRIntent = "WidgetOCRIntent";
    public static final String TextBlockObject = "String";
    public static int block;
    public String ocrtext, textfilename;
    private Uri textUri;

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private EditText ocr_text;
    private Button save_text;
    private Button clear;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    private SharedPreferences mSharedPref;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {

        try {

            super.onCreate(icicle);
            block = 1;

            mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            DocumentScannerApplication.getInstance().trackScreenView("OCR Capture Activity");

            setContentView(R.layout.ocr_capture);

            ActionBar actionBar = getSupportActionBar();
            assert actionBar != null;
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(R.string.ocr_capture_screen);
            actionBar.setIcon(R.drawable.ic_camera_icon);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);

            final boolean widgetIntent = getIntent().getBooleanExtra(WidgetIntent, false);
            final boolean widgetcameraIntent = getIntent().getBooleanExtra(DocumentScannerActivity.WidgetCameraIntent, false);

            ocr_text = (EditText) findViewById(R.id.ocr_text);
            assert ocr_text != null;
            ocr_text.setClickable(false);


            save_text = (Button) findViewById(R.id.save_text);
            clear = (Button) findViewById(R.id.clear);

            save_text.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(final View v) {

                    ocrtext = ocr_text.getText().toString();

                    if (ocrtext.trim().length() == 0) {
                        Toast.makeText(v.getContext(), R.string.no_text_detected, Toast.LENGTH_SHORT).show();
                    } else {
                        try {

                            AlertDialog.Builder renameDialog = new AlertDialog.Builder(v.getContext());

                            renameDialog.setTitle(getString(R.string.rename));
                            renameDialog.setMessage(getString(R.string.enter_file_name));

                            final EditText input = new EditText(v.getContext());
                            textfilename = "DOC-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
                            input.setText(textfilename);
                            input.selectAll();
                            renameDialog.setView(input);


                            renameDialog.setPositiveButton(getString(R.string.save_file), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String ocrtextfile;
                                    final Intent data;
                                    String savedfilename = input.getText().toString();

                                    //Write to external storage
                                    try {
                                        File ocrfolder = new File(Environment.getExternalStorageDirectory().toString()
                                                + "/ConTextScanner/OCR-Texts");
                                        if (!ocrfolder.exists()) {
                                            ocrfolder.mkdir();
                                            Log.d(TAG, "wrote: created folder " + ocrfolder.getPath());
                                        }
                                        ocrtextfile = Environment.getExternalStorageDirectory().toString()
                                                + "/ConTextScanner/OCR-Texts/"
                                                + savedfilename
                                                + ".txt";

                                        File myFile = new File(ocrtextfile);
                                        myFile.createNewFile();
                                        FileOutputStream fOut = new FileOutputStream(myFile);
                                        OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                                        myOutWriter.append(ocrtext);
                                        myOutWriter.close();
                                        fOut.close();

                                        createTextList(savedfilename,ocrtext);

                                        Toast.makeText(getBaseContext(), "Done writing to " + ocrtextfile, Toast.LENGTH_LONG).show();

                                        ExifInterface exif = new ExifInterface(ocrtextfile);
                                        exif.setAttribute("UserComment", "Generated using ConTextScanner");
                                        String nowFormatted = mDateFormat.format(new Date().getTime());
                                        exif.setAttribute(ExifInterface.TAG_DATETIME, nowFormatted);
                                        exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, nowFormatted);
                                        exif.setAttribute("Software", "ConTextScanner " + BuildConfig.VERSION_NAME);
                                        exif.saveAttributes();

                                        data = new Intent(v.getContext(), DocumentScannerActivity.class);

                                        if(widgetcameraIntent){
                                            setIntent(data);
                                        }

                                        data.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                        data.putExtra(TextBlockObject, ocrtext);
                                        data.putExtra(WidgetOCRIntent, true);
                                        startActivity(data);
                                        finish();

                                    } catch (Exception e) {
                                        Toast.makeText(getBaseContext(), e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    Log.d(TAG, "Text read: " + ocrtext);
                                    // Record goal "OCR Text Saved"
                                    DocumentScannerApplication.getInstance().trackEvent("Event", "OCR Text Saved", "OCR Capture Activity");

                                    dialog.dismiss();

                                }
                            });

                            renameDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            renameDialog.create().show();

                        } catch (Exception e) {
                            Dialog d = new Dialog(v.getContext());
                            d.setTitle("Error saving text!");
                            TextView tv = new TextView(v.getContext());
                            tv.setText(e.toString());
                            d.setContentView(tv);
                            d.show();
                        }
                    }
                }
            });

            clear.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    ocr_text.setText(" ");
                }
            });


            mPreview = (CameraSourcePreview) findViewById(R.id.preview);
            mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

            // read parameters from the intent used to launch the activity.
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

            // Check for the camera permission before accessing the camera.  If the
            // permission is not granted yet, request permission.
            int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (rc == PackageManager.PERMISSION_GRANTED) {
                createCameraSource(autoFocus, useFlash);
            } else {
                requestCameraPermission();
            }

            gestureDetector = new GestureDetector(this, new CaptureGestureListener());
            scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

            View.OnClickListener onclicklistener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(mGraphicOverlay, R.string.snackbar_tap,
                            Snackbar.LENGTH_LONG).dismiss();
                }
            };

            Snackbar.make(mGraphicOverlay, R.string.snackbar_tap,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.got_it, onclicklistener)
                    .show();

        } catch (Exception e) {
            Dialog d = new Dialog(this);
            d.setTitle(R.string.error_saving_text);
            TextView tv = new TextView(this);
            tv.setText(e.toString());
            d.setContentView(tv);
            d.show();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void createTextList(String savedfilename, String ocrtext) {

        String title =  savedfilename;
        String body = ocrtext;

        if(title.length() > 0 && body.length() > 0) {
            // Create a new map of values, where column names are the key.
            ContentValues values = new ContentValues();
            values.put(TextContract.TextEntry.COLUMN_SUMMARY, title);
            values.put(TextContract.TextEntry.COLUMN_DESCRIPTION, body);

            if(textUri == null) {
                textUri = getContentResolver().insert(TextContract.TextEntry.CONTENT_URI, values);
            } else{
                getContentResolver().update(textUri, values, null, null);
            }

            //Close the edit and back to main.
            this.finish();
        } else{
            Toast.makeText(this, "Empty Content can't be saved", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(2.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                        .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.ocr)
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * onTap is called to capture the first TextBlock under the tap location and return it to
     * the Initializing Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();

            if (text != null && text.getValue() != null) {
                ocr_text.setClickable(true);

                String ocrtext = ocr_text.getText().toString();

                if (!ocrtext.isEmpty()) {
                    ocrtext = ocrtext + "\n" + text.getValue();
                    ocr_text.setText(ocrtext);
                } else {
                    ocr_text.setText(text.getValue());
                }

            } else {
                Log.d(TAG, "text data is null");
            }
        } else {
            Log.d(TAG, "no text detected");
        }
        return text != null;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "OcrCapture Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.myapps.documentscanner/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "OcrCapture Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.myapps.documentscanner/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    public int getBlock() {
        return block;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ocr, menu);
        return true;
    }


    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    Log.e(TAG, "onPrepareOptionsPanel", e);
                } catch (Exception e) {
                    Log.e(TAG, "Menu Icons Error", e);
                }
            }
        }

        return super.onPrepareOptionsPanel(view, menu);
    }

    //  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {

            case R.id.saved_texts:
                Intent intent = new Intent(this, SavedTextsList.class);
                startActivity(intent);
                return true;

            case R.id.tblocks:
                block = 0;
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                break;

            case R.id.tlines:
                block = 1;
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                break;

            case R.id.twords:
                block = 2;
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                break;

            default:
                block = 1;
                break;
        }

        mSharedPref.edit().putInt("block", block);

        return false;
    }
}
