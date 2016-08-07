package com.dayandnightstudio.bebop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by matthew on 2/21/16.
 */
public class GroupAdapter extends ArrayAdapter<Group> {
    Data data = Data.getData();
    private Context context;
    private ArrayList<Group> myGroups;
    Typeface tfLight;
    DateFormat dateFormat;

    public GroupAdapter(Context context, ArrayList<Group> myGroups) {
        super(context, -1, myGroups);

        this.context = context;

        this.myGroups = new ArrayList<>();
        this.myGroups.addAll(myGroups);

        tfLight = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
        dateFormat = new SimpleDateFormat("yyy/MM/dd");
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View item = layoutInflater.inflate(R.layout.item_group, parent, false);

        TextView tvGroupName = (TextView) item.findViewById(R.id.tvGroupName);
        tvGroupName.setTypeface(tfLight);
        tvGroupName.setText(myGroups.get(position).getGroupName());

        TextView tvDate = (TextView) item.findViewById(R.id.tvDate);
        tvDate.setTypeface(tfLight);
        tvDate.setText(dateFormat.format(myGroups.get(position).getDate()));

        item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                data.initGroup(myGroups.get(position).getGroupName());
                Toast.makeText(context.getApplicationContext(), "Joined " + myGroups.get(position).getGroupName(), Toast.LENGTH_SHORT).show();
                context.startActivity(intent);
            }
        });

        return item;
    }
}
