package com.example.richellerazon.nytsearch.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.example.richellerazon.nytsearch.Article;
import com.example.richellerazon.nytsearch.ArticleArrayAdapter;
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
    EditText etQuery;
    GridView gvResults;
    Button btnSearch;

    String sortOrder;
    String beginDate;
    ArrayList<String> newsDesks;

    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

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

        Log.i("RRR", "Finished onCreate()");
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
        etQuery = (EditText) findViewById(R.id.etQuery);
        gvResults = (GridView) findViewById(R.id.gvResults);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        articles = new ArrayList<>();
        adapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(adapter);
        sortOrder = "newest";
        beginDate = "";
        newsDesks = new ArrayList<String>();
        //newsDesks.add("Sports");
        //newsDesks.add("Cars");

        // hook up listner for grid click
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search_filters) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onArticleSearch(View view) {
        String query = etQuery.getText().toString();
        String newsDeskValue = "";

        //Toast.makeText(this, "Searching for " + query, Toast.LENGTH_SHORT).show();

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";
        RequestParams params = new RequestParams();
        params.put("api-key", "aebb93f60cdf4d8a989931b8f1b32cf4");
        params.put("page", 0);
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

        client.get(url, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;

                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    adapter.addAll(Article.fromJSONArray(articleJsonResults));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });

    }
}
