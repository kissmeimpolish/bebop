package com.dayandnightstudio.bebop;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.StrictMode;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.lifestreet.android.lsmsdk.SlotView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import redis.clients.jedis.Transaction;

public class GroupActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {
    Data data = Data.getData();
    Typeface tfLight;
    TextView tvGroupName;
    PlaylistAdapter playlistAdapter;
    SearchAdapter searchAdapter;
    ListView lvPlaylist;
    ImageButton ibBack, ibRefresh, ibSearch;
    boolean isRefreshed = false;
    Toast toast;
    Song lastPlayed = data.group.getLastPlayed();
    ArrayList<Song> playlist = new ArrayList<>();

    //youtube
    YoutubeConnector youtubeConnector;
    YouTubePlayer youTubePlayer;
    YouTubePlayerView youTubePlayerView;

    //dialog
    TextView tvTitle;
    EditText etKeyword;
    ListView lvSearch;
    Button bSubmit;
    ImageButton ibSearch2;
    ArrayList<Song> searchResults = new ArrayList<>();

    //ads
    SlotView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_group);

        tfLight = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Light.ttf");

        youtubeConnector = new YoutubeConnector(getApplicationContext());
        youTubePlayerView = (YouTubePlayerView) findViewById(R.id.playerView);
        youTubePlayerView.initialize(youtubeConnector.API_KEY, this);

        ibBack = (ImageButton) findViewById(R.id.ibBack);
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adView != null)
                    adView.destroy();

                youTubePlayer.release();
                data.jedis.hincrBy("group:" + data.group.getGroupName(), "nUsers", -1);
                data.setGroup(playlistAdapter.liked, searchAdapter.added, lastPlayed);
                startActivity(new Intent(GroupActivity.this, MainActivity.class));
            }
        });

        if(!isNetworkConnected())
            ibBack.performClick();

        playlistAdapter = new PlaylistAdapter(getApplicationContext(), playlist, data.group.getLiked());
        getPlaylist();
        playlistAdapter.update(playlist);

        searchAdapter = new SearchAdapter(getApplicationContext(), searchResults, playlist, data.group.getAdded());

        lvPlaylist = (ListView) findViewById(R.id.lvPlaylist);
        lvPlaylist.setAdapter(playlistAdapter);

        tvGroupName = (TextView) findViewById(R.id.tvGroupName);
        tvGroupName.setTypeface(tfLight);
        tvGroupName.setText(data.group.getGroupName());

        ibRefresh = (ImageButton) findViewById(R.id.ibRefresh);
        ibRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isNetworkConnected())
                    ibBack.performClick();

                if (isRefreshed)
                    return;

                boolean wasEmpty = playlist.isEmpty();
                getPlaylist();
                playlistAdapter.update(playlist);
                if (wasEmpty && !playlist.isEmpty())
                    youTubePlayer.loadVideo(playlist.get(0).getId());

                toast.makeText(getApplicationContext(), R.string.refresh, Toast.LENGTH_SHORT).show();

                //cool-down
                isRefreshed = true;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                        }
                        isRefreshed = false;
                    }
                });
                thread.setPriority(Thread.NORM_PRIORITY);
                thread.start();
            }
        });

        ibSearch = (ImageButton) findViewById(R.id.ibSearch);
        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(GroupActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_search);

                if (!isNetworkConnected())
                    ibBack.performClick();

                searchResults.clear();

                tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
                tvTitle.setTypeface(tfLight);

                etKeyword = (EditText) dialog.findViewById(R.id.etKeyword);
                etKeyword.setTypeface(tfLight);
                etKeyword.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().isEmpty()) {
                            ibSearch2.setImageResource(R.mipmap.ns_search2);
                        } else {
                            ibSearch2.setImageResource(R.mipmap.s_search2);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                ibSearch2 = (ImageButton) dialog.findViewById(R.id.ibSearch);
                ibSearch2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String keywords = etKeyword.getText().toString();
                        if (!keywords.isEmpty()) {
                            searchResults.clear();
                            searchResults.addAll(youtubeConnector.searchByKeyword(keywords));
                            searchAdapter.update(searchResults, playlist);

                            //set height
                            if (searchResults.size() > 5) {
                                View item = searchAdapter.getView(0, null, lvSearch);
                                item.measure(0, 0);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (5.5 * item.getMeasuredHeight()));
                                lvSearch.setLayoutParams(params);
                            }

                            bSubmit.setVisibility(View.VISIBLE);
                        }
                    }
                });

                lvSearch = (ListView) dialog.findViewById(R.id.lvSearch);
                lvSearch.setAdapter(searchAdapter);

                bSubmit = (Button) dialog.findViewById(R.id.bSubmit);
                bSubmit.setTypeface(tfLight);
                bSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!searchAdapter.added.isEmpty()) {
                            boolean wasEmpty = playlist.isEmpty();

                            for (Song s : searchAdapter.added) {
                                if (data.searchById(playlist, s.getId()) == null) {
                                    playlistAdapter.liked.put(s.getId(), 1);
                                    playlist.add(s);

                                    String json = data.gson.toJson(new Song(s.getTitle(), s.getId(), 0));
                                    data.queue.put(json, 1);
                                }
                            }

                            getPlaylist();
                            playlistAdapter.update(playlist);
                            if (wasEmpty)
                                youTubePlayer.loadVideo(playlist.get(0).getId());
                        }
                    }
                });

                dialog.show();
            }
        });

        adView = (SlotView) findViewById(R.id.adView);
        adView.setSlotTag("http://mobile-android.lfstmedia.com/m2/slot132649?adkey=eb8&exact_match=1");
        adView.loadAd();
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        toast.makeText(getApplicationContext(), R.string.youtube_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean isRestored) {
        youTubePlayer.setPlayerStateChangeListener(pscl);
        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
        this.youTubePlayer = youTubePlayer;

        if(!isRestored && !playlist.isEmpty())
            youTubePlayer.loadVideo(playlist.get(0).getId());
    }

    private YouTubePlayer.PlayerStateChangeListener pscl = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {

        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {
            lastPlayed = playlist.get(0);
        }

        @Override
        public void onVideoEnded() {
            String json = data.gson.toJson(new Song(lastPlayed.getTitle(), lastPlayed.getId(), 0));
            data.jedis.hdel("playlist:" + data.group.getGroupName(), json);

            Integer idx = data.searchById(searchAdapter.added, lastPlayed.getId());
            if(idx != null)
                searchAdapter.added.remove(idx.intValue());

            lastPlayed = null;

            getPlaylist();
            playlistAdapter.update(playlist);

            if(!playlist.isEmpty())
                youTubePlayer.loadVideo(playlist.get(0).getId());
        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {
            toast.makeText(getApplicationContext(), errorReason.toString(), Toast.LENGTH_LONG).show();
        }
    };

    public void setPlaylist() {
        if(data.queue.isEmpty())
            return;

        Transaction t = data.jedis.multi();
        String[] keys = data.queue.keySet().toArray(new String[data.queue.size()]);
        for(String json:keys)
            t.hincrBy("playlist:" + data.group.getGroupName(), json, data.queue.get(json));
        t.exec();

        data.queue.clear();
    }

    public void getPlaylist() {
        setPlaylist();

        Map<String, String> temp = data.jedis.hgetAll("playlist:" + data.group.getGroupName());
        String[] keys = temp.keySet().toArray(new String[temp.size()]);

        playlist.clear();
        for(String json:keys) {
            Song s = data.gson.fromJson(json, Song.class);
            int score = Integer.valueOf(temp.get(json));
            s.setScore(score);
            playlist.add(s);
        }

        Collections.sort(playlist);
        if(lastPlayed != null) {
            Integer idx = data.searchById(playlist, lastPlayed.getId());
            if(idx == null) {
                lastPlayed = null;
            } else if(idx != 0) {
                playlist.remove(idx.intValue());
                playlist.add(0, lastPlayed);
            }
        }

        if(skipSong() && youTubePlayer != null) {
            toast.makeText(getApplicationContext(), R.string.skip_song, Toast.LENGTH_SHORT).show();
            int duration = youTubePlayer.getDurationMillis();
            youTubePlayer.seekToMillis(duration);
        }
    }

    public boolean skipSong() {
        if(lastPlayed == null)
            return false;

        int nUsers = Integer.valueOf(data.jedis.hget("group:" + data.group.getGroupName(), "nUsers"));
        Integer idx = data.searchById(playlist, lastPlayed.getId());
        if(idx != null) {
            int score = playlist.get(idx).getScore();
            if(score < Math.ceil(-0.51 * nUsers))
                return true;
        }

        return false;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(adView != null)
            adView.destroy();

        youTubePlayer.release();
        data.jedis.hincrBy("group:" + data.group.getGroupName(), "nUsers", -1);
        data.setGroup(playlistAdapter.liked, searchAdapter.added, lastPlayed);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(adView != null)
            adView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adView != null)
            adView.resume();
    }
}
