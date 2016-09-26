package com.example.dlopez.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Daniel on 07-09-2016.
 */
public class FetchReviewListTask extends AsyncTask<String, Void, ArrayList<Review>> {

    private final String LOG_TAG = FetchTrailerListTask.class.getSimpleName();

    private ArrayList<Review> getReviewsDataFromJson(String catalogJsonStr) throws JSONException {

        final String OWM_RESULT = "results";

        JSONObject forecastJson = new JSONObject(catalogJsonStr);
        JSONArray catalogArray = forecastJson.getJSONArray(OWM_RESULT);

        ArrayList<Review> result = new ArrayList<Review>();
        for (int i = 0; i < catalogArray.length(); i++) {
            JSONObject object = catalogArray.getJSONObject(i);
            String autor = object.getString("author");
            String url = object.getString("url");
            String review = object.getString("content");
            result.add(new Review(autor, review, url));
        }
        return result;
    }


    @Override
    protected ArrayList<Review> doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String trailersJsonStr = null;

        try {

            final String URL_BASE = "http://api.themoviedb.org/3/movie/";
            final String API_KEY_STR = "api_key";
            final String ID_MOVIE = params[0];
            final String API_KEY_VALUE = BuildConfig.THE_MOVIE_DB_API_KEY;

            Uri builtUri = Uri.parse(URL_BASE).buildUpon()
                    .appendEncodedPath(ID_MOVIE)
                    .appendPath("reviews")
                    .appendQueryParameter(API_KEY_STR, API_KEY_VALUE)
                    .build();

            URL url = new URL(builtUri.toString());
            Log.v(LOG_TAG, "Built URI " + builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            trailersJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getReviewsDataFromJson(trailersJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }


        // This will only happen if there was an error getting or parsing the forecast.
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Review> result) {
        if (result != null) {
            DetailMovieFragment.mReviewAdapter.clear();
            for(Review review : result) {
                DetailMovieFragment.mReviewAdapter.add(review);
            }
            DetailMovieFragment.setReviewListViewHeightBasedOnChildren();
        }
    }
}
