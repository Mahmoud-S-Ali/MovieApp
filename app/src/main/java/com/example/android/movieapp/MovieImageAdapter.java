package com.example.android.movieapp;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Toty on 8/16/2015.
 */
public class MovieImageAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<MyMovie> mMovieList;
    private GridView mGridView;

    public MovieImageAdapter(Context c, GridView gView) {
        mContext = c;
        mGridView = gView;
        mMovieList = new ArrayList<>();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setAdjustViewBounds(true);
            /*imageView.setLayoutParams(new GridView.LayoutParams(mGridView.getWidth() / 2,
                    mGridView.getWidth() * 3 / 4));*/
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(4, 4, 4, 4);
        } else {
            imageView = (ImageView) convertView;
        }

        String posterUrl = mMovieList.get(position).getMovieDetails()
                .get(mContext.getString(R.string.data_poster_url));
        if (posterUrl != null)
            Picasso.with(mContext).load(posterUrl).into(imageView);

        else
            Picasso.with(mContext).load(R.drawable.no_photo_available).into(imageView);

        return imageView;
    }
}
