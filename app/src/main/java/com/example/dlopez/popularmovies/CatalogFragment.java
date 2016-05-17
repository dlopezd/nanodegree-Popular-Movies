package com.example.dlopez.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Toast;

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
 * A placeholder fragment containing a simple view.
 */
public class CatalogFragment extends Fragment {
    MovieAdapter mCatalogAdapter;


    public CatalogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.catalogfragment, container, false);

        mCatalogAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_catalog);

        // if the device is portrait, the gridview shows 2 columns
        // if the device is landscape, the gridview shows 3 columns
        int rotation = getResources().getConfiguration().orientation;
        if(rotation == Configuration.ORIENTATION_LANDSCAPE){
            gridView.setNumColumns(3);
        }
        else{
            gridView.setNumColumns(2);
        }

        gridView.setAdapter(mCatalogAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Movie movie = mCatalogAdapter.getItem(position);

                Intent intent =  new Intent(getActivity(), DetailMovie.class);
                intent.putExtra(getString(R.string.intent_original_title), movie.getTitle());
                intent.putExtra(getString(R.string.intent_poster_thumbnail), movie.getThumbnailURL());
                intent.putExtra(getString(R.string.intent_release_date), movie.getReleaseDate());
                intent.putExtra(getString(R.string.intent_synopsis), movie.getSynopsis());
                intent.putExtra(getString(R.string.intent_user_rating), movie.getRating());

                startActivity(intent);
            }
        });

        return rootView;
    }


    @Override
    public void onStart(){
        super.onStart();
        updateCatalog();
    }

    private void updateCatalog(){
        FetchCatalogTask task = new FetchCatalogTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String orderBy = prefs.getString(getString(R.string.pref_order_key), getString(R.string.pref_order_default));
        task.execute(orderBy);
    }

    public class FetchCatalogTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchCatalogTask.class.getSimpleName();

        private String[] getMoviesDataFromJson(String catalogJsonStr) throws JSONException {

            final String OWM_RESULT = "results";

            JSONObject forecastJson = new JSONObject(catalogJsonStr);
            JSONArray catalogArray = forecastJson.getJSONArray(OWM_RESULT);

            String[] resultStrs = new String[catalogArray.length()];
            for (int i = 0; i < catalogArray.length(); i++) {
                resultStrs[i] = catalogArray.getJSONObject(i).toString();
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String catalogJsonStr = null;

            try {
                final String URL_BASE = "http://api.themoviedb.org/3/movie/";
                final String API_KEY_STR = "api_key";
                final String API_KEY_VALUE = "<YOUR_API_KEY>";

                Uri builtUri = Uri.parse(URL_BASE).buildUpon()
                        .appendEncodedPath(params[0])
                        .appendQueryParameter(API_KEY_STR, API_KEY_VALUE)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                catalogJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                return getMoviesDataFromJson(catalogJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate() {
        }

        protected void onPostExecute(String[] data) {
            mCatalogAdapter.clear();
            for (String movie : data) {
                mCatalogAdapter.add(new Movie(movie));
            }
        }
    }
}
