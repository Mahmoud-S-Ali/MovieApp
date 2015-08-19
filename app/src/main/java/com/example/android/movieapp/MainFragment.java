package com.example.android.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

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
import java.util.HashMap;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private static final String MOVIEAPI_KEY = "INSERT YOUR MOVIE API KEY HERE";
    private static final String SI_MOVIE_KEY = "SI_MOVIE_KEY";    //saved instance movie key
    private static final String SI_POS_KEY = "SI_POS_KEY";

    private int mPosition = GridView.INVALID_POSITION;
    private GridView mGridView;

    private MovieImageAdapter mMovieAdapter;
    private ArrayList<MyMovie> mListOfMovies;



    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mListOfMovies != null) {
            outState.putParcelableArrayList(SI_MOVIE_KEY,
                    (ArrayList<? extends Parcelable>) mListOfMovies);
        }

        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SI_POS_KEY, mPosition);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(R.menu.movie_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        //String LOG_TAG = getActivity().getClass().getName();

        mGridView = (GridView)view.findViewById(R.id.gridView_thumbnail);
        mMovieAdapter = new MovieImageAdapter(getActivity(), mGridView);
        if(getActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            mGridView.setNumColumns(5);
        }

        //Setting a listener for the grid view when clicking on any item
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                mPosition= position;
                mListOfMovies = mMovieAdapter.getAllItems();

                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("selectedMovie", (MyMovie) mMovieAdapter.getItem(position));
                startActivity(intent);
            }
        });

        //Setting a listener for the retry button when there is no internet connection
        Button retryButton = (Button)view.findViewById(R.id.main_retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateMovies();
            }
        });


        //Return the last saved state if there is one
        if (savedInstanceState != null && savedInstanceState.containsKey(SI_POS_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SI_POS_KEY);
        }
        return view;
    }


    public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<MyMovie>> {
        private final String LOG_TAG = FetchMovieTask.class.getName();

        @Override
        protected ArrayList<MyMovie> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //This string will hold the json response as a string
            String movieJsonStr = "";

            String sortOrder = params[0];
            //Subtitute the string "key" with your real key
            String key = MOVIEAPI_KEY;
            String pageNum = "1";  //params[1];

            try {
                //Construct the url for the openWeatherMap using a URI builder
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
                final String PAGES_PARAM = "page";
                final String KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendEncodedPath(sortOrder)
                        .appendQueryParameter(PAGES_PARAM, (pageNum))
                        .appendQueryParameter(KEY_PARAM, key)
                        .build();

                URL url = new URL(builtUri.toString());
                //Make sure that the url is built properly
                //Log.v(LOG_TAG, " Built URL: " + url.toString());

                // Create the request to themoviedatabase, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                //Read the input stream into string
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    //nothing to do
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }

                movieJsonStr += buffer.toString();
                //Log.v(LOG_TAG, "Movie string: " + movieJsonStr);

            } catch (IOException e) {
                //Log.e(LOG_TAG, "Error", e);
                //If the code didn't successfully get weather data, no need to parse it
                movieJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }


            return null;
        }

        private ArrayList<MyMovie> getMoviesDataFromJson(String movieJsonStr)
                throws JSONException {

            if (movieJsonStr == null)
                return null;

            final String MDB_RESULTS     = getString(R.string.data_results);
            final String MDB_TITLE       = getString(R.string.data_title);
            final String MDB_RELEASEDATE = getString(R.string.data_release_date);
            final String MDB_VOTEAVG     = getString(R.string.data_vote_average);
            final String MDB_OVERVIEW    = getString(R.string.data_overview);
            final String MDB_BG          = getString(R.string.data_background_url);
            final String MDB_POSTER      = getString(R.string.data_poster_url);
            final String POSTER_SIZE     = getString(R.string.poster_size);
            final String BG_SIZE         = getString(R.string.background_size);

            JSONObject movieJson = new JSONObject(movieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

            int totalNumOfMovies = movieArray.length();
            ArrayList<MyMovie> movieList = new ArrayList<>();
            String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

            for (int i = 0; i < totalNumOfMovies; i++) {
                // Get the JSON object representing the movie data
                HashMap<String, String> movieDataMap = new HashMap<>();
                JSONObject movieData = movieArray.getJSONObject(i);

                movieDataMap.put(MDB_TITLE, movieData.getString(MDB_TITLE));
                movieDataMap.put(MDB_RELEASEDATE, movieData.getString(MDB_RELEASEDATE));
                movieDataMap.put(MDB_VOTEAVG, movieData.getString(MDB_VOTEAVG));
                movieDataMap.put(MDB_OVERVIEW, movieData.getString(MDB_OVERVIEW));

                String posterUniqueUrl     = movieData.getString(MDB_POSTER);
                String backgroundUniqueUrl = movieData.getString(MDB_BG);
                Uri moviePosterUri;
                Uri movieBackgroundUri;

                //Make sure that the movie has an available poster
                if (posterUniqueUrl != "null") {
                    moviePosterUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                            .appendEncodedPath(POSTER_SIZE)
                            .appendEncodedPath(posterUniqueUrl)
                            .build();

                    movieDataMap.put(MDB_POSTER, moviePosterUri.toString());
                }

                if (backgroundUniqueUrl != "null") {
                    movieBackgroundUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                            .appendEncodedPath(BG_SIZE)
                            .appendEncodedPath(backgroundUniqueUrl)
                            .build();

                    movieDataMap.put(MDB_BG, movieBackgroundUri.toString());
                }

                movieList.add(new MyMovie(movieDataMap));
            }

            return movieList;
        }

        @Override
        protected void onPostExecute(ArrayList<MyMovie> result) {
            if (result != null) {
                mMovieAdapter.clearAll();
                mMovieAdapter.addAll(result);
            }
        }
    }

    //This will update the gridView with the new data
    private void updateMovies() {
        if (mListOfMovies != null) {
            mGridView.setVisibility(View.INVISIBLE);

            mMovieAdapter.clearAll();
            mMovieAdapter.addAll(mListOfMovies);

            if (mPosition != GridView.INVALID_POSITION) {
                mGridView.smoothScrollToPosition(mPosition);
            }

            mListOfMovies = null;
            mPosition = GridView.INVALID_POSITION;
            mGridView.setVisibility(View.VISIBLE);
        }

        else if (isNetworkAvailable()) {
            visualizeViews(true);
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String sortType = pref.getString(getString(R.string.pref_sort_key),
                    getString(R.string.pref_sort_default));
            new FetchMovieTask().execute(sortType);
        }
        else
            visualizeViews(false);
    }

    //This method is used to show or hide the main page views depedning on connection
    private void visualizeViews(boolean isConnected) {
        if (isConnected) {
            getActivity().findViewById(R.id.main_noConnection_text).setVisibility(View.INVISIBLE);
            getActivity().findViewById(R.id.main_retry_button).setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        }
        else {
            getActivity().findViewById(R.id.gridView_thumbnail).setVisibility(View.INVISIBLE);
            getActivity().findViewById(R.id.main_noConnection_text).setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}