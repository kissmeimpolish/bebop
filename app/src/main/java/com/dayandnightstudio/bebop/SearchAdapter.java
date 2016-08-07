package com.dayandnightstudio.bebop;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by matthew on 2/21/16.
 */
public class SearchAdapter extends ArrayAdapter<Song> {
    Data data = Data.getData();
    private Context context;
    private ArrayList<Song> searchResults, playlist;
    public ArrayList<Song> added;
    Typeface tfLight;
    Toast toast;

    public SearchAdapter(Context context, ArrayList<Song> searchResults, ArrayList<Song> playlist, ArrayList<Song> added) {
        super(context, -1, searchResults);

        this.context = context;

        this.searchResults = new ArrayList<>();
        this.searchResults.addAll(searchResults);

        this.playlist = new ArrayList<>();
        this.playlist.addAll(playlist);

        this.added = new ArrayList<>();
        this.added.addAll(added);

        tfLight = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View item = layoutInflater.inflate(R.layout.item_search, parent, false);

        final Song s = searchResults.get(position);

        TextView tvTitle = (TextView) item.findViewById(R.id.tvTitle);
        tvTitle.setTypeface(tfLight);
        tvTitle.setText(s.getTitle());

        final ImageButton ibAdd = (ImageButton) item.findViewById(R.id.ibAdd);
        final Integer idx = data.searchById(added, s.getId());
        if(idx != null) {
            ibAdd.setImageResource(R.mipmap.s_add);
            ibAdd.setBackgroundResource(R.drawable.s_add_shape);
        }

        ibAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(data.searchById(playlist, s.getId()) != null) {
                    toast.makeText(context.getApplicationContext(), R.string.song_exists, Toast.LENGTH_SHORT).show();
                } else if(idx != null) {
                    ibAdd.setImageResource(R.mipmap.ns_add);
                    ibAdd.setBackgroundResource(R.drawable.ns_add_shape);

                    added.remove(idx.intValue());
                    notifyDataSetChanged();
                } else if(added.size() == 3) {
                    Toast.makeText(context.getApplicationContext(), R.string.wait, Toast.LENGTH_LONG).show();
                } else {
                    ibAdd.setImageResource(R.mipmap.s_add);
                    ibAdd.setBackgroundResource(R.drawable.s_add_shape);

                    s.setScore(1);
                    added.add(s);
                    notifyDataSetChanged();
                }
            }
        });

        return item;
    }

    public void update(ArrayList<Song> searchResults, ArrayList<Song> playlist) {
        this.searchResults.clear();
        this.searchResults.addAll(searchResults);

        this.playlist.clear();
        this.playlist.addAll(playlist);

        notifyDataSetChanged();
    }
}
