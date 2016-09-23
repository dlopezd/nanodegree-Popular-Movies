package com.example.dlopez.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.dlopez.popularmovies.R;
import com.example.dlopez.popularmovies.Utility;
import com.example.dlopez.popularmovies.data.MoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Daniel on 22-07-2016.
 */
public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Start sync.");
        String order = Utility.getPreferredOrder(getContext());
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String catalogJsonStr = null;

        try {
            final String URL_BASE = "http://api.themoviedb.org/3/movie/";
            final String API_KEY_STR = "api_key";
            final String API_KEY_VALUE = "<YOUR_API_KEY>";


            Uri builtUri = Uri.parse(URL_BASE).buildUpon()
                    .appendEncodedPath(order)
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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return;
            }
            catalogJsonStr = buffer.toString();
            saveMoviesFromJson(catalogJsonStr);

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } catch (JSONException e) {
        Log.e(LOG_TAG, e.getMessage(), e);
        e.printStackTrace();
        }finally {
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

    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

        }
        return newAccount;
    }

    private void saveMoviesFromJson(String catalogJsonStr)throws JSONException{

        final String OWM_RESULT = "results";

        JSONObject forecastJson = new JSONObject(catalogJsonStr);
        JSONArray catalogArray = forecastJson.getJSONArray(OWM_RESULT);

        final String TAG_ID = "id";
        final String TAG_POSTER_PATH = "poster_path";
        final String TAG_OVERVIEW = "overview";
        final String TAG_RELEASE_DATE = "release_date";
        final String TAG_TITLE = "original_title";
        final String TAG_RATING = "vote_average";


        Vector<ContentValues> cVVector = new Vector<ContentValues>(catalogArray.length());
        for (int i = 0; i < catalogArray.length(); i++) {
            JSONObject movieJSON = catalogArray.getJSONObject(i);
            String id = movieJSON.getString(TAG_ID);
            String title = movieJSON.getString(TAG_TITLE);
            String poster_path = movieJSON.getString(TAG_POSTER_PATH);
            String release_date = movieJSON.getString(TAG_RELEASE_DATE);
            String rating = movieJSON.getString(TAG_RATING);
            String overview = movieJSON.getString(TAG_OVERVIEW);

            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MovieEntry.COLUMN_ID_API, id);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, title);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, poster_path);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, release_date);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_RATING, rating);
            movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);

            cVVector.add(movieValues);
        }
            // add to database
        int inserted = 0;
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            getContext().getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI,null,null);
            getContext().getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);
        }

        Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}