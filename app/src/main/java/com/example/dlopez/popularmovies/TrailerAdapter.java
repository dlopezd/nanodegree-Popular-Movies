package com.example.dlopez.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Daniel on 31-08-2016.
 */
public class TrailerAdapter  extends ArrayAdapter<Trailer> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();


    public static class ViewHolder {
        public final TextView text;

        public ViewHolder(View view) {
            text = (TextView) view.findViewById(R.id.list_item_trailer_textview);
        }
    }

    // Constructor
    public TrailerAdapter(Activity context, List<Trailer> trailers) {
        super(context, 0, trailers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.text.setText(this.getItem(position).getText());
        view.setTag(viewHolder);
        return view;
    }
}
