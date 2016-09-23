package com.example.dlopez.popularmovies;

/**
 * Created by Daniel on 06-09-2016.
 */
public class Review {
    String autor;
    String url;
    String review;

    public Review(String autor, String review, String url)
    {
        this.autor = autor;
        this.review = review;
        this.url = url;
    }
    public String getAutor(){
        return this.autor;
    }
    public String getReview(){
        return this.review;
    }
    public String getUrl()
    {
        return this.url;
    }
}
