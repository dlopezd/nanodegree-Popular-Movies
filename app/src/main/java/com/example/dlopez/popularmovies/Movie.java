package com.example.dlopez.popularmovies;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Daniel on 14-05-2016.
 */
public class Movie {
    public String jsonStr;
    private final String LOG_TAG = Movie.class.getSimpleName();
    private final String BASE_URL = "http://image.tmdb.org/t/p/";
    private final String POSTER_SIZE_PHONE = "w342";
    private final String POSTER_SIZE_THUMBNAIL = "w185";

    private final String TAG_POSTER_PATH = "poster_path";
    private final String TAG_OVERVIEW = "overview";
    private final String TAG_RELEASE_DATE = "release_date";
    private final String TAG_TITLE = "original_title";
    private final String TAG_RATING = "vote_average";




    public Movie(String jsonStr){
        this.jsonStr = jsonStr;
    }

    public String getTitle(){
        try {
            JSONObject movieJSON = new JSONObject(jsonStr);
            String value = movieJSON.getString(TAG_TITLE);
            return value;
        } catch (JSONException e){
            Log.e(LOG_TAG, "Error ", e);
            return "";
        }
    }

    public String getSynopsis(){
        try {
            JSONObject movieJSON = new JSONObject(jsonStr);
            String value = movieJSON.getString(TAG_OVERVIEW);
            return value;
        } catch (JSONException e){
            Log.e(LOG_TAG, "Error ", e);
            return "";
        }
    }

    public String getReleaseDate(){
        try {
            JSONObject movieJSON = new JSONObject(jsonStr);
            String value = movieJSON.getString(TAG_RELEASE_DATE);
            return value;
        } catch (JSONException e){
            Log.e(LOG_TAG, "Error ", e);
            return "";
        }
    }

    public String getRating(){
        try {
            JSONObject movieJSON = new JSONObject(jsonStr);
            String value = movieJSON.getString(TAG_RATING);
            return value;
        } catch (JSONException e){
            Log.e(LOG_TAG, "Error ", e);
            return "";
        }
    }

    public String getPosterURL() {
        try {
            JSONObject movieJSON = new JSONObject(jsonStr);
            String imagePath = movieJSON.getString(TAG_POSTER_PATH);
            return BASE_URL + POSTER_SIZE_PHONE + imagePath;
        } catch (JSONException e){
            Log.e(LOG_TAG, "Error ", e);
            return "";
        }
    }

    public String getThumbnailURL() {
        try {
            JSONObject movieJSON = new JSONObject(jsonStr);
            String imagePath = movieJSON.getString(TAG_POSTER_PATH);
            return BASE_URL + POSTER_SIZE_THUMBNAIL + imagePath;
        } catch (JSONException e){
            Log.e(LOG_TAG, "Error ", e);
            return "";
        }
    }
}
