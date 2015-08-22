package com.example.android.movieapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Toty on 8/16/2015.
 */
public class MovieImageAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<MyMovie> mMovieList;
    private GridView mGridView;
    private LayoutInflater mInflater;

    public MovieImageAdapter(Context c, GridView gView) {
        mContext = c;
        mGridView = gView;
        mMovieList = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mMovieList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMovieList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public ArrayList<MyMovie> getAllItems() {
        return mMovieList;
    }

    public void clearAll() {
        mGridView.removeAllViewsInLayout();
    }

    public void addAll(ArrayList<MyMovie> data) {
        mMovieList = null;
        mMovieList = data;
        mGridView.setAdapter(this);
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        ImageView thumbnail;
        Inflater inflater;

        if (v == null) {
            v = mInflater.inflate(R.layout.grid_item, parent, false);
            v.setTag(R.id.grid_thumbnail, v.findViewById(R.id.grid_thumbnail));
        }

        thumbnail = (ImageView) v.getTag(R.id.grid_thumbnail);

        String posterUrl = mMovieList.get(position).getMovieDetails()
                .get(mContext.getString(R.string.data_poster_url));
        if (posterUrl != null)
            Picasso.with(mContext).load(posterUrl).into(thumbnail);

        else
            Picasso.with(mContext).load(R.drawable.no_photo_available).into(thumbnail);

        return v;
    }

    private static class Item {
        public final int drawableId;

        Item(String name, int drawableId) {
            this.drawableId = drawableId;
        }
    }
}
