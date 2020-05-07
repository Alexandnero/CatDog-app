package com.example.catdog;
import androidx.annotation.NonNull;

import java.sql.Time;
import java.util.List;


public class Post {
    String uri;
    String timestamp;
    String uid;
    String caption;
    String hashtags;
    String username;


    public Post(String uri, String username, String timestamp, String uid, String caption, String hashtags){
        this.uri = uri;
        this.username = username;
        this.timestamp = timestamp;
        this.uid = uid;
        this.caption = caption;
        this.hashtags = hashtags;


    }
    public String getUri(){
        return uri;
    }
    public String getUsername(){ return username;}
    public String getTimestamp(){
        return timestamp;
    }
    public String getUid(){
        return uid;
    }
    public String getCaption() { return caption; }
    public String getHashtags() { return hashtags; }

    @NonNull
    @Override
    public String toString() {
        return uri+timestamp+uid;
    }
}
