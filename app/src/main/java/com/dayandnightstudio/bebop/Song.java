package com.dayandnightstudio.bebop;

/**
 * Created by matthew on 2/9/16.
 */
public class Song implements Comparable<Song> {
    private String title, id;
    private int score;

    public Song(String title, String id, int score) {
        this.title = title;
        this.id = id;
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public int compareTo(Song another) {
        if(score == another.getScore()) {
            return title.compareTo(another.getTitle());
        } else {
            return another.getScore() - score;
        }
    }
}
