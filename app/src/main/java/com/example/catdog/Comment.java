package com.example.catdog;

import androidx.annotation.NonNull;

public class Comment {
    String uid;
    String timestamp;
    String commentText;
    String username;
    String profilePhoto;
    String postTimestamp;

    public Comment(String uid, String username, String profilePhoto, String timestamp, String postTimestamp, String commentText){
        this.uid = uid;
        this.username = username;
        this.profilePhoto = profilePhoto;
        this.timestamp = timestamp;
        this.postTimestamp = postTimestamp;
        this.commentText = commentText;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPostTimestamp() {
        return timestamp;
    }


    public String getCommentText() {
        return commentText;
    }

    @NonNull
    @Override
    public String toString() {
        return uid+ timestamp;
    }
}
