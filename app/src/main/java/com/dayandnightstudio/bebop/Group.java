package com.dayandnightstudio.bebop;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by matthew on 2/20/16.
 */
public class Group implements Comparable<Group> {
    private String groupName;
    private HashMap<String, Integer> liked;
    private ArrayList<Song> added;
    private Date date;
    private Song lastPlayed;

    public Group(String groupName, HashMap<String, Integer> liked, ArrayList<Song> added, Date date, Song lastPlayed) {
        this.groupName = groupName;
        this.liked = liked;
        this.added = added;
        this.date = date;
        this.lastPlayed = lastPlayed;
    }

    public Song getLastPlayed() {
        return lastPlayed;
    }

    public Date getDate() {
        return date;
    }

    public ArrayList<Song> getAdded() {
        return added;
    }

    public HashMap<String, Integer> getLiked() {
        return liked;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public int compareTo(Group another) {
        return date.compareTo(another.date);
    }
}
