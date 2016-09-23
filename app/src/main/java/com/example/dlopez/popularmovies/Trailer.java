package com.example.dlopez.popularmovies;

/**
 * Created by Daniel on 28-08-2016.
 */
public class Trailer {
    String Url;

    public Trailer(String url)
    {
        this.Url = url;
    }
    public String getText(){
        return "Ver Trailer";
    }
    public String getUrl(){
        return this.Url;
    }
}
