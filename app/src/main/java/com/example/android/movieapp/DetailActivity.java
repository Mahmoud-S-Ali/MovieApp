package com.example.android.movieapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * Created by Toty on 8/12/2015.
 */
public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment2())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment2 extends Fragment {

        public DetailFragment2() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                MyMovie movie = intent.getExtras().getParcelable("selectedMovie");
                HashMap<String, String> movieData = movie.getMovieDetails();
                setViews(rootView, movieData);
            }

            return rootView;
        }

        private void setViews(View rootView, HashMap<String, String> movieData) {
            String bgUrl = movieData.get(getString(R.string.data_background_url));
            String posterUrl = movieData.get(getString(R.string.data_poster_url));

            //Setting views in first layout that has a TextView and an ImageView
            ImageView bgImageView = ((ImageView) rootView.findViewById(R.id.detail_background));
            Picasso.with(getActivity()).load(bgUrl).into(bgImageView);


            ((TextView) rootView.findViewById(R.id.detail_main_title))
                    .setText(movieData.get(getString(R.string.data_title)));

            //Setting views in middle layout that has 3 TextViews and an ImageView
            ImageView posterImageView = ((ImageView) rootView.findViewById(R.id.detail_poster));
            Picasso.with(getActivity()).load(posterUrl).into(posterImageView);

            ((TextView) rootView.findViewById(R.id.detail_title_input))
                    .setText(movieData.get(getString(R.string.data_title)));

            ((TextView) rootView.findViewById(R.id.detail_release_date_input))
                    .setText(movieData.get(getString(R.string.data_release_date)));

            ((TextView) rootView.findViewById(R.id.detail_rating_input))
                    .setText(movieData.get(getString(R.string.data_vote_average)));

            //Setting the overview in the last layout
            ((TextView) rootView.findViewById(R.id.detail_overview_input))
                    .setText(movieData.get(getString(R.string.data_overview)));
        }
    }
}
