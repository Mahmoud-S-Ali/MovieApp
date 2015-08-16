package com.example.android.movieapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

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
import java.util.HashMap;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {
    private ImageAdapter mMovieAdapter;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
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

        GridView gridview = (GridView)view.findViewById(R.id.gridView_thumbnail);
        mMovieAdapter = new ImageAdapter(getActivity(), gridview);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position,
                                    long id) {
                //Toast.makeText(getActivity(), "" + position, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("movieData", (HashMap) mMovieAdapter.getItem(position));
                startActivity(intent);
            }
        });

        Button retryButton = (Button)view.findViewById(R.id.main_retry_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                updateMovies();
            }
            });

        return view;
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private HashMap<String, String>[] mMoviesData;
        private GridView gridView;

        public ImageAdapter(Context c, GridView gView) {
            mContext = c;
            mMovieAdapter = null;
            gridView = gView;
        }

        @Override
        public int getCount() {
            return mMoviesData.length;
        }

        @Override
        public Object getItem(int position) {
            return mMoviesData[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        public void clearAll() {
            gridView.removeAllViewsInLayout();
        }

        public void addAll(HashMap<String, String>[] data) {
            mMoviesData = null;
            mMoviesData = data;
            gridView.setAdapter(this);
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                //imageView.setAdjustViewBounds(true);
                imageView.setLayoutParams(new GridView.LayoutParams(gridView.getWidth() / 2,
                        gridView.getWidth() * 3 / 4));
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }

            String posterUrl = mMoviesData[position].get(getString(R.string.data_poster_url));
            if (posterUrl != null)
                Picasso.with(getActivity()).load(posterUrl).into(imageView);

            else
                Picasso.with(getActivity()).load(R.drawable.no_image_available).into(imageView);

            return imageView;
        }
    }

    public class FetchMovieTask extends AsyncTask<String, Void, HashMap<String, String>[]> {
        private final String LOG_TAG = FetchMovieTask.class.getName();

        @Override
        protected HashMap<String, String>[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //This string will hold the json response as a string
            String movieJsonStr = "";

            String sortOrder = params[0];
            //Subtitute the string "key" with your real key
            String key = "Key";
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

        private HashMap<String, String>[] getMoviesDataFromJson(String movieJsonStr)
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
            HashMap<String, String>[] allMoviesData = new HashMap[totalNumOfMovies];
            String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

            for (int i = 0; i < totalNumOfMovies; i++) {
                // Get the JSON object representing the movie data
                JSONObject movieData = movieArray.getJSONObject(i);
                allMoviesData[i] = new HashMap<>();

                allMoviesData[i].put(MDB_TITLE, movieData.getString(MDB_TITLE));
                allMoviesData[i].put(MDB_RELEASEDATE, movieData.getString(MDB_RELEASEDATE));
                allMoviesData[i].put(MDB_VOTEAVG, movieData.getString(MDB_VOTEAVG));
                allMoviesData[i].put(MDB_OVERVIEW, movieData.getString(MDB_OVERVIEW));

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

                    allMoviesData[i].put(MDB_POSTER, moviePosterUri.toString());
                }

                if (backgroundUniqueUrl != "null") {
                    movieBackgroundUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                            .appendEncodedPath(BG_SIZE)
                            .appendEncodedPath(backgroundUniqueUrl)
                            .build();

                    allMoviesData[i].put(MDB_BG, movieBackgroundUri.toString());
                }
            }
            return allMoviesData;
        }

        @Override
        protected void onPostExecute(HashMap<String, String>[] result) {
            if (result != null) {
                mMovieAdapter.clearAll();
                mMovieAdapter.addAll(result);
            }
        }
    }

    //This will update the gridView with the new data
    private void updateMovies() {
        if (isNetworkAvailable()) {
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
            getActivity().findViewById(R.id.gridView_thumbnail).setVisibility(View.VISIBLE);
        }
        else {
            getActivity().findViewById(R.id.gridView_thumbnail).setVisibility(View.INVISIBLE);
            getActivity().findViewById(R.id.main_noConnection_text).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.main_retry_button).setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}