package com.example.dlopez.popularmovies;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.example.dlopez.popularmovies.data.MoviesContract;
import com.example.dlopez.popularmovies.sync.MoviesSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class CatalogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private MovieAdapter mCatalogAdapter;
    GridView mGridView;
    private int mPosition = ListView.INVALID_POSITION;

    private static final String SELECTED_KEY = "selected_position";
    private static final int CATALOG_LOADER = 0;

    public interface Callback {
        public void onItemSelected(Uri MovieUri);
    }

    public CatalogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.catalog_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateCatalog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        Uri moviesUri = MoviesContract.MovieEntry.CONTENT_URI;
        Cursor cur = getActivity().getContentResolver().query(moviesUri, null, null, null, null);
        mCatalogAdapter = new MovieAdapter(getActivity(), cur, 0);

        View rootView = inflater.inflate(R.layout.catalogfragment, container, false);
        mGridView = (GridView) rootView.findViewById(R.id.gridview_catalog);

        // if the device is portrait, the gridview shows 2 columns
        // if the device is landscape, the gridview shows 3 columns
        //int rotation = getResources().getConfiguration().orientation;
        //if (rotation == Configuration.ORIENTATION_LANDSCAPE) {
        //    mGridView.setNumColumns(3);
        //} else {
        //    mGridView.setNumColumns(2);
        //}
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            mGridView.setNumColumns(1);
        }

        mGridView.setAdapter(mCatalogAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    if (Utility.getPreferredOrder(getActivity()).equals(getString(R.string.pref_order_favorite))) {
                        int idx = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE);
                        String title = cursor.getString(idx);

                        ((Callback) getActivity())
                                .onItemSelected(MoviesContract.FavoriteMovieEntry.buildMovieWithTitle(title));
                    } else {
                        int idx = cursor.getColumnIndex(MoviesContract.MovieEntry._ID);
                        long id = cursor.getLong(idx);
                        ((Callback) getActivity())
                                .onItemSelected(MoviesContract.MovieEntry.buildMovieWithIdUri(id));
                    }
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
            Log.i("CATALOG","RECUPERADA POSICION "+mPosition);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(CATALOG_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onOrderChanged() {
        updateCatalog();
        getLoaderManager().restartLoader(CATALOG_LOADER, null, this);
    }

    private void updateCatalog() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        String order = Utility.getPreferredOrder(getActivity());
        if (isConnected && !order.equals(getString(R.string.pref_order_favorite))) {
            MoviesSyncAdapter.syncImmediately(getActivity());
        } else {
            Utility.setPreferredOrder(getString(R.string.pref_order_favorite), getContext());
        }
        if (!isConnected && !order.equals(getString(R.string.pref_order_favorite))) {
            getActivity().setTitle(getString(R.string.pref_order_favorite_label));
            Snackbar.make(getView(), "Internet Connection failed. Showing Favorites", Snackbar.LENGTH_INDEFINITE)
                    //.setActionTextColor(Color.CYAN)
                    .setActionTextColor(getResources().getColor(R.color.primary))
                    //.setAction("Reintent", new View.OnClickListener() {
                    //    @Override
                    //   public void onClick(View view) {
                    //        Log.i("Snackbar", "Reintent download information.");
                    //        updateCatalog();
                    //    }
                    //})
                    .show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
            Log.i("CATALOG", "POSICION GUARDADA "+mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String order = Utility.getPreferredOrder(getActivity());
        Uri moviesUri;
        if (order.equals(getString(R.string.pref_order_favorite))) {
            moviesUri = MoviesContract.FavoriteMovieEntry.CONTENT_URI;
        } else {
            moviesUri = MoviesContract.MovieEntry.CONTENT_URI;
        }

        return new CursorLoader(getActivity(),
                moviesUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mCatalogAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCatalogAdapter.swapCursor(null);
    }
}