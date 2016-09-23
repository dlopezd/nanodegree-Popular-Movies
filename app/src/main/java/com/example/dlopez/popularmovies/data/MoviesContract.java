package com.example.dlopez.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Daniel on 19-07-2016.
 */
public class MoviesContract {

    public static final String CONTENT_AUTHORITY = "com.example.dlopez.popularmovies.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static final class MovieEntry implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "movie";

        // column name
        public static final String COLUMN_ID_API = "id_api";
        public static final String COLUMN_TITLE = "original_title";
        public static String COLUMN_POSTER_PATH = "poster_path";
        public static String COLUMN_OVERVIEW = "overview";
        public static String COLUMN_RELEASE_DATE = "release_date";
        public static String COLUMN_RATING = "vote_average";

        // create content uri
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;


        public static Uri buildMovieWithIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class FavoriteMovieEntry implements BaseColumns {

        // table name
        public static final String TABLE_NAME = "favorite_movie";

        // column name
        public static final String COLUMN_ID_API = "id_api";
        public static final String COLUMN_TITLE = "original_title";
        public static String COLUMN_POSTER_PATH = "poster_path";
        public static String COLUMN_OVERVIEW = "overview";
        public static String COLUMN_RELEASE_DATE = "release_date";
        public static String COLUMN_RATING = "vote_average";

        // create content uri
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;

        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + TABLE_NAME;


        public static Uri buildMovieWithIdUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithTitle(String title) {
            return CONTENT_URI.buildUpon().appendPath(title).build();
        }
    }
}
