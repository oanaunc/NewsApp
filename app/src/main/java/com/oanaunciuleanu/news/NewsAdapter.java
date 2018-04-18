package com.oanaunciuleanu.news;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class NewsAdapter extends ArrayAdapter<News> {

    public NewsAdapter(Context context, ArrayList<News> news) {
        super(context, 0, news);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.news_activity, parent, false);
        }

        // The object at this position in the list
        News currentNewsClass = getItem(position);

        // For title
        TextView newsTitleTextView = listItemView.findViewById(R.id.articleTitle);
        assert currentNewsClass != null;
        newsTitleTextView.setText(currentNewsClass.getWebTitle());

        // For category
        TextView newsCategoryTextView = listItemView.findViewById(R.id.articleCategory);
        newsCategoryTextView.setText(currentNewsClass.getSectionName());

        // For author
        TextView newsAuthorTextView = listItemView.findViewById(R.id.articleAuthor);
        newsAuthorTextView.setText(currentNewsClass.getAuthor());

        // For date
        TextView newsDateTextView = listItemView.findViewById(R.id.articleDate);
        SimpleDateFormat dateFormatJSON = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat dateFormat2 = new SimpleDateFormat(App.getContext().getResources().getString(R.string.pattern_two), Locale.ENGLISH);

        try {
            Date dateNews = dateFormatJSON.parse(currentNewsClass.getWebPublicationDate());
            String date = dateFormat2.format(dateNews);
            newsDateTextView.setText(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //Return the list
        return listItemView;
    }
}