package com.example.dlopez.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.dlopez.popularmovies.data.MoviesContract;
import com.example.dlopez.popularmovies.data.MoviesContract.MovieEntry;
import com.example.dlopez.popularmovies.data.MoviesContract.FavoriteMovieEntry;
import com.squareup.picasso.Picasso;

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
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailMovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DetailMovieFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    static boolean mIsFavorite;
    private ShareActionProvider mShareActionProvider;
    private Uri mUri;
    private Cursor mData;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_ID_API,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RATING,
            MovieEntry.COLUMN_POSTER_PATH,
            MovieEntry.COLUMN_OVERVIEW
    };
    private static final String[] DETAIL_COLUMNS_FAV = {
            FavoriteMovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            FavoriteMovieEntry.COLUMN_ID_API,
            FavoriteMovieEntry.COLUMN_RELEASE_DATE,
            FavoriteMovieEntry.COLUMN_TITLE,
            FavoriteMovieEntry.COLUMN_RATING,
            FavoriteMovieEntry.COLUMN_POSTER_PATH,
            FavoriteMovieEntry.COLUMN_OVERVIEW
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIE_ID_API = 1;
    public static final int COL_MOVIE_RELEASE_DATE = 2;
    public static final int COL_MOVIE_TITLE = 3;
    public static final int COL_MOVIE_RATING = 4;
    public static final int COL_MOVIE_POSTER_URL = 5;
    public static final int COL_MOVIE_OVERVIEW = 6;

    private TextView mSynopsisLabel;
    private TextView mTrailersLabel;
    private TextView mReviewsLabel;
    private ImageView mThumbnail;
    private TextView mTitleTV;
    private TextView mSynopsisTV;
    private TextView mYearTV;
    private TextView mRatingTV;
    private RatingBar mRatingBar;
    private FloatingActionButton mFavorite;
    public static TrailerAdapter mTrailerAdapter;
    private static ListView mTrailerList;
    public static ReviewAdapter mReviewAdapter;
    private static ListView mReviewList;

    public DetailMovieFragment() {    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailMovieFragment.DETAIL_URI);
        }

        View rootview = inflater.inflate(R.layout.fragment_detail_movie, container, false);

        mReviewsLabel = (TextView)rootview.findViewById(R.id.reviews_label);
        mSynopsisLabel = (TextView)rootview.findViewById(R.id.synopsis_label);
        mTrailersLabel = (TextView)rootview.findViewById(R.id.trailers_label);
        mTitleTV = (TextView)rootview.findViewById(R.id.movie_name);
        mThumbnail = (ImageView)rootview.findViewById(R.id.movie_thumbnail);
        mSynopsisTV = (TextView)rootview.findViewById(R.id.movie_synopsis);
        mYearTV = (TextView)rootview.findViewById(R.id.movie_year);
        mRatingTV = (TextView)rootview.findViewById(R.id.movie_rating);
        mRatingBar = (RatingBar) rootview.findViewById(R.id.movie_ratingbar);
        mFavorite = (FloatingActionButton)rootview.findViewById(R.id.btn_favorite);

        ArrayList<Trailer> listaTrailer = new ArrayList<Trailer>();
        mTrailerAdapter = new TrailerAdapter(getActivity(), listaTrailer);
        mTrailerList = (ListView) rootview.findViewById(R.id.movie_trailer_list);
        mTrailerList.setAdapter(mTrailerAdapter);
        mTrailerList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                String url = mTrailerAdapter.getItem(position).getUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        });

        ArrayList<Review> listaReview = new ArrayList<Review>();
        mReviewAdapter = new ReviewAdapter(getActivity(), listaReview);
        mReviewList = (ListView) rootview.findViewById(R.id.movie_reviews_list);
        mReviewList.setAdapter(mReviewAdapter);

        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
   }

    private void setIconFavorite(FloatingActionButton favorite, boolean isFavorite) {
        if(isFavorite){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                favorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_black_48dp, getContext().getTheme()));
            } else {
                favorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_black_48dp));
            }
        }
        else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                favorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_black_48dp, getContext().getTheme()));
            } else {
                favorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_border_black_48dp));
            }
        }
    }

    private boolean isFavorite(String title) {
        Cursor c =
                getActivity().getContentResolver().query(MoviesContract.FavoriteMovieEntry.buildMovieWithTitle(title),
                        new String[]{MovieEntry.COLUMN_TITLE},
                        null,
                        null,
                        null);
        if (c.getCount() == 0){
            return  false;
        }
        return  true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            String[] cols;
            if(Utility.getPreferredOrder(getActivity()).equals(getString(R.string.pref_order_favorite))){
                cols = DETAIL_COLUMNS_FAV;
            }
            else{
                cols = DETAIL_COLUMNS;
            }

            CursorLoader cursorLoader = new CursorLoader(
                    getActivity(),
                    mUri,
                    cols,
                    null,
                    null,
                    null
            );

            return cursorLoader;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mData = data;

            FetchTrailerListTask task = new FetchTrailerListTask();
            task.execute(mData.getString(COL_MOVIE_ID_API));

            FetchReviewListTask task2 = new FetchReviewListTask();
            task2.execute(mData.getString(COL_MOVIE_ID_API));

            mSynopsisLabel.setText("Synopsis");
            mTrailersLabel.setText("Trailers");
            mReviewsLabel.setText("Reviews");
            mFavorite.setVisibility(View.VISIBLE);

            String title = data.getString(COL_MOVIE_TITLE);
            mTitleTV.setText(title);

            String posterURL = Utility.getPosterURL(Utility.POSTER_SIZE_THUMBNAIL, data.getString(COL_MOVIE_POSTER_URL));
            Picasso.with(getActivity()).load(posterURL).into(mThumbnail);

            String synopsis = data.getString(COL_MOVIE_OVERVIEW);
            mSynopsisTV.setText(synopsis);

            String releaseDate = data.getString(COL_MOVIE_RELEASE_DATE);
            mYearTV.setText(releaseDate.substring(0,4));

            String rating = data.getString(COL_MOVIE_RATING);
            mRatingTV.setText(rating + " / 10");
            mRatingBar.setRating(Float.valueOf(rating));

            mIsFavorite = isFavorite(mData.getString(COL_MOVIE_TITLE));
            setIconFavorite(mFavorite, mIsFavorite);
            mFavorite.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(!mIsFavorite) {
                        ContentValues cv = new ContentValues();
                        cv.put(MoviesContract.FavoriteMovieEntry.COLUMN_ID_API, mData.getString(COL_MOVIE_ID_API));
                        cv.put(MoviesContract.FavoriteMovieEntry.COLUMN_OVERVIEW, mData.getString(COL_MOVIE_OVERVIEW));
                        cv.put(MoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH, mData.getString(COL_MOVIE_POSTER_URL));
                        cv.put(MoviesContract.FavoriteMovieEntry.COLUMN_RATING, mData.getString(COL_MOVIE_RATING));
                        cv.put(MoviesContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE, mData.getString(COL_MOVIE_RELEASE_DATE));
                        cv.put(MoviesContract.FavoriteMovieEntry.COLUMN_TITLE, mData.getString(COL_MOVIE_TITLE));

                        Uri uri = getContext().getContentResolver().insert(MoviesContract.FavoriteMovieEntry.CONTENT_URI,cv);
                        if(uri != null) {
                            mIsFavorite = true;
                        }
                    }
                    else{
                        getContext().getContentResolver().delete(MoviesContract.FavoriteMovieEntry.buildMovieWithTitle(
                                mData.getString(COL_MOVIE_TITLE)),null, null);
                        mIsFavorite = false;
                    }

                    setIconFavorite(mFavorite,mIsFavorite);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

    public static void setTrailerListViewHeightBasedOnChildren() {
        if (mTrailerAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < mTrailerAdapter.getCount(); i++) {
            View listItem = mTrailerAdapter.getView(i, null, mTrailerList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = mTrailerList.getLayoutParams();
        params.height = totalHeight + (mTrailerList.getDividerHeight() * (mTrailerAdapter.getCount() - 1));
        mTrailerList.setLayoutParams(params);
        mTrailerList.requestLayout();
    }

    public static void setReviewListViewHeightBasedOnChildren() {
        if (mReviewAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < mReviewAdapter.getCount(); i++) {
            View listItem = mReviewAdapter.getView(i, null, mTrailerList);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = mReviewList.getLayoutParams();
        params.height = totalHeight + (mReviewList.getDividerHeight() * (mReviewAdapter.getCount() - 1));
        mReviewList.setLayoutParams(params);
        mReviewList.requestLayout();
    }

    public void reloadMovie(Uri uri){
        if (null != uri) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}