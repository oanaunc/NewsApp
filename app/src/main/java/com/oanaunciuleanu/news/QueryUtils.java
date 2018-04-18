package com.oanaunciuleanu.news;


import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();
    private static final String response = App.getContext().getResources().getString(R.string.response);
    private static final String results = App.getContext().getResources().getString(R.string.results);
    private static final String sectionName = App.getContext().getResources().getString(R.string.section_name);
    private static final String webPublicationDate = App.getContext().getResources().getString(R.string.web_publication_date);
    private static final String webTitle = App.getContext().getResources().getString(R.string.web_title);
    private static final String webUrl = App.getContext().getResources().getString(R.string.web_url);
    private static final String tags = App.getContext().getResources().getString(R.string.tags);
    private static final String authorWebTitle = App.getContext().getResources().getString(R.string.web_title);

    //Constructor
    private QueryUtils() {
    }


    public static List<News> fetchNewsData(String requestUrl) throws InterruptedException {

        URL url = returnUrl(requestUrl);

        // Receive a JSON response
        String jsonResponse = null;
        try {
            jsonResponse = httpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, App.getContext().getResources().getString(R.string.http_request_failed), e);
        }

        // Extracting the needed fields from JSON
        List<News> listFromJson = extractFromJason(jsonResponse);
        return listFromJson;
    }

    private static URL returnUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, App.getContext().getResources().getString(R.string.url_bulding_problem), e);
        }
        return url;
    }


    //HTTP request
    private static String httpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        // Initialize variables
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod(App.getContext().getResources().getString(R.string.get));
            urlConnection.connect();
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readStream(inputStream);
            } else {
                Log.e(LOG_TAG,  App.getContext().getResources().getString(R.string.error_response_code) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, App.getContext().getResources().getString(R.string.retrieving_json) , e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }

            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    // A string with the JSON response
    private static String readStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();

        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(App.getContext().getResources().getString(R.string.utf8)));
            BufferedReader reader = new BufferedReader(inputStreamReader);

            // Reading the data
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }

        return output.toString();
    }

    //Returning the News object list
    private static List<News> extractFromJason(String newsJson) {
        if (TextUtils.isEmpty(newsJson)) {
            return null;
        }
        List<News> newsList = new ArrayList<>();
        try {

            JSONObject baseJsonResponse = new JSONObject(newsJson);
            JSONObject responseJsonNews = baseJsonResponse.getJSONObject(response);
            JSONArray newsArray = responseJsonNews.getJSONArray(results);

            // Create objects based on the articles information
            for (int i = 0; i < newsArray.length(); i++) {
                JSONObject currentArticle = newsArray.getJSONObject(i);
                String articleSectionName = currentArticle.getString(sectionName);
                String newsDate = App.getContext().getResources().getString(R.string.date_not_available);

                if (currentArticle.has(webPublicationDate)) {
                    newsDate = currentArticle.getString(webPublicationDate);
                }
                String newsTitle = currentArticle.getString(webTitle);
                String newsUrl = currentArticle.getString(webUrl);
                JSONArray currentAuthorArray = currentArticle.getJSONArray(tags);

                String articleAuthor = App.getContext().getResources().getString(R.string.author_not_available);
                int length = currentAuthorArray.length();
                if (length == 1) {
                    JSONObject currentArticleAuthor = currentAuthorArray.getJSONObject(0);
                    String author = currentArticleAuthor.getString(authorWebTitle);
                    articleAuthor = "Author: " + author;
                }

                // A new object with the details from Json
                News newsObject = new News(newsTitle, articleSectionName, articleAuthor, newsDate, newsUrl);
                newsList.add(newsObject);
            }

        } catch (JSONException e) {
            Log.e(App.getContext().getResources().getString(R.string.query_utils), App.getContext().getResources().getString(R.string.problem_parsing_json));
        }
        return newsList;
    }
}