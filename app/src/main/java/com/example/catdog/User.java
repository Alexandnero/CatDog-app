package com.example.catdog;

public class User {

    String uid;
    String email;
    String username;
    String bio;
    String photoUrl;


    public User(String uid, String email, String username, String bio,String photoUrl){
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.bio = bio;
        this.photoUrl = photoUrl;
    }

    public String getUid(){ return uid;}
    public String getEmail(){
        return email;
    }

    public String getUsername(){
        return username;
    }

    public String getBio(){
        return bio;
    }

    public String getPhotoUrl(){
        return photoUrl;
    }

}
