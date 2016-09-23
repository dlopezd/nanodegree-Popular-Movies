package com.example.dlopez.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dlopez.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

public class MovieAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final ImageView posterView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.movie_poster);
        }
    }


    public MovieAdapter(Context context, Cursor c, int flags)
    {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item_catalog, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int idx_Url = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER_PATH);
        String posterPath = cursor.getString(idx_Url);
        String url = Utility.getPosterURL(Utility.POSTER_SIZE_PHONE, posterPath);

        Picasso.with(context).load(url).into(viewHolder.posterView);
    }
}