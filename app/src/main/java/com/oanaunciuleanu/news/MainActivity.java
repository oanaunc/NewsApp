package com.oanaunciuleanu.news;


import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {

    // My url for searching the The Guardian

    private static final String USGS_REQUEST_URL = App.getContext().getResources().getString(R.string.request);
    private static final String API_KEY =App.getContext().getResources().getString(R.string.my_key);
    private static final String KEY = App.getContext().getResources().getString(R.string.apy_key);



    private static final int NEWS_LOADER_ID = 1;
    private NewsAdapter newsAdapter;
    private String warningMessage;
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list_item);

        //find ListView in news_list_item.xml
        ListView newsListView = findViewById(R.id.articleList);

        //if there are no articles found, display a message
        mEmptyStateTextView = findViewById(R.id.noArticles);
        newsListView.setEmptyView(mEmptyStateTextView);

        // new adapter
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());
        newsListView.setAdapter(newsAdapter);


        //onItemClick listener to open web page for articles
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                News clickedNews = newsAdapter.getItem(position);
                assert clickedNews != null;
                Uri newsURI = Uri.parse(clickedNews.getWebUrl());
                Intent foodIntent = new Intent(Intent.ACTION_VIEW, newsURI);
                PackageManager packageManager = getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(foodIntent,
                        PackageManager.MATCH_DEFAULT_ONLY);
                boolean isIntentSafe = activities.size() > 0;

                if (isIntentSafe) {
                    startActivity(foodIntent);
                } else {
                    String message = getString(R.string.browser_not_found);
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
                }

            }
        });


        // Check the state of network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        assert connectivityManager != null;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading);
            loadingIndicator.setVisibility(View.GONE);
            warningMessage = (String) getText(R.string.no_internet_connection);
            WarningMessage(warningMessage);
        }
    }

    @Override
    // This method initialize the contents of the Activity's options menu.
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the Options Menu we specified in XML
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Loader

    @Override
    // onCreateLoader instantiates and returns a new Loader for the given ID
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String myTopicChoice = sharedPreferences.getString(
                getString(R.string.settings_search_by),
                getString(R.string.settings_title));
        //  default value for this preference.


        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(USGS_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value. For example, the `format=geojson`

        uriBuilder.appendQueryParameter((String) getText(R.string.q), myTopicChoice);
        uriBuilder.appendQueryParameter((String) getText(R.string.show_tags),(String) getText(R.string.contributor));
        uriBuilder.appendQueryParameter(KEY, API_KEY);

        return new NewsLoader(this, uriBuilder.toString());

    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> newsList) {
        View loadingIndicator = findViewById(R.id.loading);
        loadingIndicator.setVisibility(View.GONE);
        newsAdapter.clear();

        if (newsList != null && !newsList.isEmpty()) {
            newsAdapter.addAll(newsList);

            if (newsList.isEmpty()) {
                warningMessage = (String) getText(R.string.no_news);
                WarningMessage(warningMessage);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.clear();
    }

    //Display a warning message
    private void WarningMessage(String message) {
        View loadingIndicator = findViewById(R.id.loading);
        loadingIndicator.setVisibility(View.GONE);
        mEmptyStateTextView.setVisibility(View.VISIBLE);
        mEmptyStateTextView.setText(message);
    }
}