package com.example.dlopez.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Daniel on 14-05-2016.
 * REFERENCE: https://github.com/udacity/android-custom-arrayadapter
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();

  // Constructor
    public MovieAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Gets the AndroidFlavor object from the ArrayAdapter at the appropriate position
        Movie movie = getItem(position);

        // Adapters recycle views to AdapterViews.
        // If this is a new View object we're getting, then inflate the layout.
        // If not, this view already has the layout inflated from a previous call to getView,
        // and we modify the View widgets as usual.
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_catalog, parent, false);
        }

        ImageView poster = (ImageView) convertView.findViewById(R.id.movie_poster);
        Picasso.with(getContext()).load(movie.getPosterURL()).into(poster);

        return convertView;
    }
}
