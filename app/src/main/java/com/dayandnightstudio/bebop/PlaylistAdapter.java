package com.dayandnightstudio.bebop;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by matthew on 2/21/16.
 */
public class PlaylistAdapter extends ArrayAdapter<Song> {
    Data data = Data.getData();
    private Context context;
    private ArrayList<Song> playlist;
    public HashMap<String, Integer> liked;
    Typeface tfLight;
    int incr;

    public PlaylistAdapter(Context context, ArrayList<Song> playlist, HashMap<String, Integer> liked) {
        super(context, -1, playlist);

        this.context = context;

        this.playlist = new ArrayList<>();
        this.playlist.addAll(playlist);

        this.liked = new HashMap<>();
        this.liked.putAll(liked);

        tfLight = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View item = layoutInflater.inflate(R.layout.item_song, parent, false);

        final Song s = playlist.get(position);

        incr = 0;

        TextView tvScore = (TextView) item.findViewById(R.id.tvScore);
        tvScore.setTypeface(tfLight);
        tvScore.setText(String.valueOf(s.getScore()));

        TextView tvTitle = (TextView) item.findViewById(R.id.tvTitle);
        tvTitle.setTypeface(tfLight);
        tvTitle.setText(s.getTitle());

        final ImageButton ibLike = (ImageButton) item.findViewById(R.id.ibLike);
        final ImageButton ibDislike = (ImageButton) item.findViewById(R.id.ibDislike);

        if (liked.get(s.getId()) == 1) {
            ibLike.setImageResource(R.mipmap.s_like);
        } else if (liked.get(s.getId()) == -1) {
            ibDislike.setImageResource(R.mipmap.s_dislike);
        }

        ibLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liked.get(s.getId()) == 1) {
                    ibLike.setImageResource(R.mipmap.ns_like);

                    incr = -1;
                    liked.put(s.getId(), 0);

                    String json = data.gson.toJson(new Song(s.getTitle(), s.getId(), 0));
                    data.queue.put(json, incr);

                    playlist.get(position).setScore(s.getScore() + incr);
                } else {
                    ibLike.setImageResource(R.mipmap.s_like);
                    ibDislike.setImageResource(R.mipmap.ns_dislike);

                    incr = 1 - liked.get(s.getId());
                    liked.put(s.getId(), 1);

                    String json = data.gson.toJson(new Song(s.getTitle(), s.getId(), 0));
                    data.queue.put(json, incr);

                    playlist.get(position).setScore(s.getScore() + incr);
                }

                notifyDataSetChanged();
            }
        });

        ibDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (liked.get(s.getId()) == -1) {
                    ibDislike.setImageResource(R.mipmap.ns_dislike);

                    incr = 1;
                    liked.put(s.getId(), 0);

                    String json = data.gson.toJson(new Song(s.getTitle(), s.getId(), 0));
                    data.queue.put(json, incr);

                    playlist.get(position).setScore(s.getScore() + incr);
                } else {
                    ibDislike.setImageResource(R.mipmap.s_dislike);
                    ibLike.setImageResource(R.mipmap.ns_like);

                    incr = -1 - liked.get(s.getId());
                    liked.put(s.getId(), -1);

                    String json = data.gson.toJson(new Song(s.getTitle(), s.getId(), 0));
                    data.queue.put(json, incr);

                    playlist.get(position).setScore(s.getScore() + incr);
                }

                notifyDataSetChanged();
            }
        });

        return item;
    }

    public void update(ArrayList<Song> playlist) {
        this.playlist.clear();
        this.playlist.addAll(playlist);

        String [] keys = liked.keySet().toArray(new String[liked.size()]);
        for(String id:keys) {
            if(data.searchById(playlist, id) == null)
                liked.remove(id);
        }

        for(Song s:playlist) {
            if(!liked.containsKey(s.getId()))
                liked.put(s.getId(), 0);
        }

        notifyDataSetChanged();
    }
}
