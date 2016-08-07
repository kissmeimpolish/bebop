package com.dayandnightstudio.bebop;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;

/**
 * Created by matthew on 2/20/16.
 */
public class Data {
    String userId;
    int nLogins;
    Group group;
    ArrayList<Group> myGroups = new ArrayList<>();
    Jedis jedis = new Jedis("130.211.146.160", 6379, 1000);
    Gson gson = new Gson();
    HashMap<String, Integer> queue = new HashMap<>();
    boolean wasShown;

    final static private Data data = new Data();

    public static Data getData() {
        return data;
    }

    public void initUserInfo(String userId) {
        wasShown = false;
        jedis.auth("0c896a563d48ec728f74d9c31ee732442817af2c");
        this.userId = userId.replace(':', '-');
        myGroups.clear();

        Map<String, String> temp = jedis.hgetAll("user:" + this.userId);
        if(!temp.isEmpty()) {
            nLogins = Integer.valueOf(temp.remove("nLogins"));

            String[] keys = temp.keySet().toArray(new String[temp.size()]);
            for(String g:keys) {
                myGroups.add(gson.fromJson(temp.get(g), Group.class));
            }

            Collections.sort(myGroups);
        } else {
            nLogins = 1;
        }

        setUserInfo();
    }

    public void setUserInfo() {
        jedis.hincrBy("user:" + userId, "nLogins", 1);
    }

    public void initGroup(String groupName) {
        group = gson.fromJson(jedis.hget("user:" + userId, groupName), Group.class);
        if(group == null)
            group = new Group(groupName, new HashMap<String, Integer>(), new ArrayList<Song>(), new Date(), null);
    }

    public void setGroup(HashMap<String, Integer> liked, ArrayList<Song> added, Song lastPlayed) {
        Group g = new Group(group.getGroupName(), liked, added, new Date(), lastPlayed);
        jedis.hset("user:" + userId, group.getGroupName(), gson.toJson(g));

        Integer idx = searchByGroupName(g.getGroupName());
        if(idx != null)
            myGroups.remove(idx.intValue());
        myGroups.add(g);

        Collections.sort(myGroups);
    }

    public Integer searchById(ArrayList<Song> list, String id) {
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).getId().equals(id))
                return i;
        }
        return null;
    }

    public Integer searchByGroupName(String groupName) {
        for(int i = 0; i < myGroups.size(); i++) {
            if(myGroups.get(i).getGroupName().equals(groupName))
                return i;
        }
        return null;
    }
}
