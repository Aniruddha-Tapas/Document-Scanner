package com.myapps.documentscanner;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by ANIRUDDHA
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        Intent configIntent1 = new Intent(context, DocumentScannerActivity.class);
        configIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        configIntent1.putExtra(DocumentScannerActivity.WidgetCameraIntent, true);

        Intent configIntent2 = new Intent(context, OcrCaptureActivity.class);
        configIntent2.putExtra(OcrCaptureActivity.AutoFocus, true);
        configIntent2.putExtra(OcrCaptureActivity.UseFlash, false);
        configIntent2.putExtra(OcrCaptureActivity.WidgetIntent, true);
        Intent configIntent3 = new Intent(context, GalleryGridActivity.class);

        PendingIntent configPendingIntent1 = PendingIntent.getActivity(context, 0, configIntent1, 0);
        PendingIntent configPendingIntent2 = PendingIntent.getActivity(context, 0, configIntent2, 0);
        PendingIntent configPendingIntent3 = PendingIntent.getActivity(context, 0, configIntent3, 0);

        remoteViews.setOnClickPendingIntent(R.id.wb1home, configPendingIntent1);
        remoteViews.setOnClickPendingIntent(R.id.wb2ocr, configPendingIntent2);
        remoteViews.setOnClickPendingIntent(R.id.wb3gallery, configPendingIntent3);

       appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

    }
}