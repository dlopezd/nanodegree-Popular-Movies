package com.example.dlopez.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.dlopez.popularmovies.sync.MoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements CatalogFragment.Callback {
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane = false;
    private String mOrder;
    private Uri uriMovieSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailMovieFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        MoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String order = Utility.getPreferredOrder( this );

        if(order.equals(getString(R.string.pref_order_popular))){
            setTitle(getString(R.string.pref_order_popular_label));
        }
        else if(order.equals(getString(R.string.pref_order_favorite))){
            setTitle(getString(R.string.pref_order_favorite_label));
        }
        else{
            setTitle(getString(R.string.pref_order_top_rated_label));
        }

        // update the movie in our second pane using the fragment manager
        if (order != null && !order.equals(mOrder)) {
            CatalogFragment cf = (CatalogFragment) getSupportFragmentManager().findFragmentById(R.id.catalog_fragment);
            if ( null != cf ) {
                cf.onOrderChanged();
                DetailMovieFragment df = (DetailMovieFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                if ( null != df ) {
                    df.reloadMovie(uriMovieSelected);
                }
            }
            mOrder = order;
        }
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            uriMovieSelected = contentUri;
            Bundle args = new Bundle();
            args.putParcelable(DetailMovieFragment.DETAIL_URI, contentUri);

            DetailMovieFragment fragment = new DetailMovieFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailMovie.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
