package com.example.dlopez.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Daniel on 22-07-2016.
 */
public class Utility {
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String POSTER_SIZE_PHONE = "w342";
    public static final String POSTER_SIZE_THUMBNAIL = "w185";

    public static String getPreferredOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_order_key),
                context.getString(R.string.pref_order_default));
    }

    public static String getPosterURL(String posterSize, String posterImagePath) {
        return BASE_URL + posterSize + posterImagePath;
    }

    public static void setPreferredOrder(String order, Context context){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.pref_order_key), order);
        editor.commit();
    }
}
