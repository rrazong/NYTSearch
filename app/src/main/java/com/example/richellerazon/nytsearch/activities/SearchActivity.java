package com.example.richellerazon.nytsearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.richellerazon.nytsearch.Article;
import com.example.richellerazon.nytsearch.ArticleArrayAdapter;
import com.example.richellerazon.nytsearch.EndlessScrollListener;
import com.example.richellerazon.nytsearch.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity {
    GridView gvResults;
    SearchView searchView;

    String sortOrder;
    String beginDate;
    ArrayList<String> newsDesks;
    int maxHits;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

    private final int REQUEST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupViews();

        if (!isNetworkAvailable()) {
            Log.e("Error", "onCreate: Network is not available", new Exception("Network is not available"));
        }

        if (!isOnline()) {
            Log.e("Error", "onCreate: Not online", new Exception("Not online."));
        }
    };

    private boolean isNetworkAvailable() {
        ConnectivityManager mgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = mgr.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process iprocess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = iprocess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) { e.printStackTrace(); }
          catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }

    public void setupViews() {
        gvResults = (GridView) findViewById(R.id.gvResults);
        articles = new ArrayList<>();
        adapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(adapter);
        sortOrder = "newest";
        beginDate = "";
        newsDesks = new ArrayList<String>();

        // hook up item click listener for grid view
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create an intent to display the article
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);

                // get the article to display
                Article article = articles.get(position);

                // pass in that articl into intent
                i.putExtra("article", article);

                // launch the activity
                startActivity(i);
            }
        });

        // hook up scroll listener for grid view
        gvResults.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Log.d("DEBUG", "onLoadMore, page " + page + ", totalItemsCount " + totalItemsCount + ", maxHits " + maxHits);
                if (totalItemsCount >= maxHits) {
                    return false;
                }
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to your AdapterView
                loadNextDataFromApi(page);
                // or loadNextDataFromApi(totalItemsCount);


                return true; // ONLY if more data is actually being loaded; false otherwise.            }
            }
        });
    }

    public void loadNextDataFromApi(final int page) {
        Log.d("DEBUG", "loadNextDataFromApi: page " + page);
        String query = searchView.getQuery().toString();
        String newsDeskValue = "";

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
        RequestParams params = new RequestParams();
        params.put("api-key", "aebb93f60cdf4d8a989931b8f1b32cf4");
        params.put("page", page);
        params.put("q", query);
        params.put("sort", sortOrder);
        if (beginDate.length() > 0) {
            params.put("begin_date", beginDate);
        }
        if (newsDesks.size() > 0) {
            newsDeskValue = "";
            for(int i = 0; i < newsDesks.size(); i++) {
                newsDeskValue = newsDeskValue + "\"" + newsDesks.get(i) + "\" ";
            }
            newsDeskValue = "news_desk:(" + newsDeskValue + ")";
            params.put("fq", newsDeskValue);
        }
        Log.d("DEBUG", params.toString());

        if (page == 0) {
            adapter.clear();
            adapter.notifyDataSetChanged();
        }

        client.get(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;

                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    if (page == 0) {
                        maxHits = response.getJSONObject("response").getJSONObject("meta").getInt("hits");
                    }
                    adapter.addAll(Article.fromJSONArray(articleJsonResults));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //perform query here
                loadNextDataFromApi(0);

                // workaround to avoid issues with some emulators and keyboard devices firing twice if a keyboard enter is used
                // see https://code.google.com/p/android/issues/detail?id=24599
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent i;

        // TODO replace beginDate, sortOrder, and newsDesks with a Filter model
        if (id == R.id.search_filters) {
            i = new Intent(getApplicationContext(), FilterActivity.class);
            i.putExtra("beginDate", beginDate);
            i.putExtra("sortOrder", sortOrder);
            i.putStringArrayListExtra("newsDesks", newsDesks);
            startActivityForResult(i, REQUEST_CODE);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            // Extract name value from result extras
            beginDate = data.getExtras().getString("beginDate");
            sortOrder = data.getExtras().getString("sortOrder");
            newsDesks = data.getStringArrayListExtra("newsDesks");
            Toast.makeText(this, beginDate, Toast.LENGTH_SHORT).show();
        }
    }

}
