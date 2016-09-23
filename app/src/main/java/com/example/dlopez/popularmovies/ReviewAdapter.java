package com.example.dlopez.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Daniel on 06-09-2016.
 */
public class ReviewAdapter   extends ArrayAdapter<Review> {
    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();


    public static class ViewHolder {
        public final TextView autor;
        public final TextView content;

        public ViewHolder(View view) {
            autor = (TextView) view.findViewById(R.id.list_item_review_autor);
            content = (TextView) view.findViewById(R.id.list_item_review_content);
        }
    }

    // Constructor
    public ReviewAdapter(Activity context, List<Review> reviews) {
        super(context, 0, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.autor.setText(this.getItem(position).getAutor());
        viewHolder.content.setText(this.getItem(position).getReview());
        view.setTag(viewHolder);
        return view;
    }
}
