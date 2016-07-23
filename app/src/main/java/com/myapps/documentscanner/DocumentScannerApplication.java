package com.myapps.documentscanner;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by ANIRUDDHA
 */
public class DocumentScannerApplication extends Application {
    private SharedPreferences mSharedPref;
    private boolean mOptOut;

    /*

    //To get the app-level opt out setting, use:

    // Get singleton.
    GoogleAnalytics myInstance = GoogleAnalytics.getInstance(this);

    // Get the app opt out preference using an AppOptOutCallback.
    myInstance.requestAppOptOut(new AppOptOutCallback() {
        @Override
        public void reportAppOptOut(boolean optOut) {
            if (optOut) {
                ... // Alert the user that they've opted out.
            }
        });
    }

    //To set the app-level opt out flag, use:
    //myInstance.setAppOptOut(appPreferences.userOptOut);
    */


    //GoogleAnalytics thisInstance = GoogleAnalytics.getInstance(this);

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("usage_stats")) {

                mOptOut = !sharedPreferences.getBoolean("usage_stats", true);

                GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(mOptOut);

                Log.e("usage_stats", Boolean.toString(mOptOut));

            }
        }
    };

    public static final String TAG = DocumentScannerApplication.class
            .getSimpleName();

    private static DocumentScannerApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
    }

    public static synchronized DocumentScannerApplication getInstance() {
        return mInstance;
    }

    public synchronized Tracker getGoogleAnalyticsTracker() {
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.get(AnalyticsTrackers.Target.APP);
    }

    /***
     * Tracking screen view
     *
     * @param screenName screen name to be displayed on GA dashboard
     */
    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();

        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());

        GoogleAnalytics.getInstance(this).dispatchLocalHits();


    }

    /***
     * Tracking event
     *
     * @param category event category
     * @param action   action of the event
     * @param label    label
     */
    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }
}
