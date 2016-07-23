package com.myapps.documentscanner.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class TextContract {
    //todo 1: create ContentProvider property
    public static final String CONTENT_AUTHORITY = "com.myapps.documentscanner";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_TEXT = "texts";

    public static final class TextEntry implements BaseColumns {
        //todo 2: content provider URI for specific Table

        //content://com.myapps.documentscanner/texts
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEXT).build();

        //vnd.android.cursor.dir/texts
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + PATH_TEXT;

        //vnd.android.cursor.item/texts
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + PATH_TEXT;

        // Database table
        public static final String TABLE_NAME = "text";
        public static final String COLUMN_SUMMARY = "summary";
        public static final String COLUMN_DESCRIPTION = "description";

        //todo 3: build table URI
        //content://com.myapps.documentscanner/texts/10
        public static Uri buildTextUri(Long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
