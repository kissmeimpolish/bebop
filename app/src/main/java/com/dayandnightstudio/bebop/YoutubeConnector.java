package com.dayandnightstudio.bebop;

import android.content.Context;
import android.widget.Toast;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthew on 1/9/16.
 */
public class YoutubeConnector {
    Data data = Data.getData();
    Toast toast;
    private Context context;
    private YouTube youtube;
    private YouTube.Search.List query;
    private long maxResults = 25;
    private SearchListResponse response;
    private ArrayList<Song> songs;

    public static final String API_KEY = "AIzaSyBcMqq1KwbPszI0IRFkKO0SqUmKdzrMDSg";

    public YoutubeConnector(Context context) {
        this.context = context;
        songs = new ArrayList<>();

        youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try {
            query = youtube.search().list("id,snippet");
            query.setKey(API_KEY);
            query.setType("video");
            query.setFields("items(id/videoId,snippet/title)");
            query.setMaxResults(maxResults);
            query.setTopicId("/en/music");
        } catch (IOException e) {
            toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public ArrayList<Song> searchByKeyword(String keywords) {
        songs.clear();
        query.setQ(keywords);

        //run query on thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    response = query.execute();
                } catch (IOException e) {
                    toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        List<SearchResult> results = response.getItems();
        for(SearchResult result:results)
            songs.add(new Song(result.getSnippet().getTitle(), result.getId().getVideoId(), 0));

        return songs;
    }
}
