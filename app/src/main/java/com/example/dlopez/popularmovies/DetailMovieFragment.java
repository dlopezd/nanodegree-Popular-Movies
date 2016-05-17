package com.example.dlopez.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailMovieFragment extends Fragment {

    public DetailMovieFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_detail_movie, container, false);

        Intent intent = getActivity().getIntent();
        String title = intent.getStringExtra(getString(R.string.intent_original_title));
        String posterURL = intent.getStringExtra(getString(R.string.intent_poster_thumbnail));
        String releaseDate = intent.getStringExtra(getString(R.string.intent_release_date));
        String synopsis = intent.getStringExtra(getString(R.string.intent_synopsis));
        String rating = intent.getStringExtra(getString(R.string.intent_user_rating));

        TextView titleTV = (TextView)rootview.findViewById(R.id.movie_name);
        titleTV.setText(title);

        ImageView thumbnail = (ImageView)rootview.findViewById(R.id.movie_thumbnail);
        Picasso.with(getActivity()).load(posterURL).into(thumbnail);

        TextView synopsisTV = (TextView)rootview.findViewById(R.id.movie_synopsis);
        synopsisTV.setText(synopsis);

        TextView yearTV = (TextView)rootview.findViewById(R.id.movie_year);
        yearTV.setText(releaseDate.substring(0,4));

        TextView ratingTV = (TextView)rootview.findViewById(R.id.movie_rating);
        ratingTV.setText(rating + " / 10");

        RatingBar ratingBar = (RatingBar) rootview.findViewById(R.id.movie_ratingbar);
        ratingBar.setRating(Float.valueOf(rating));
        return rootview;
    }
}
