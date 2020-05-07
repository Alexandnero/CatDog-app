package com.example.catdog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PostActivity extends AppCompatActivity {

    ImageView postImageView;
    TextView timeLabel, usernameLabel,captionLabel, hashtagsLabel;
    FirebaseUser currentUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    EditText commentText;
    Button deleteButton;

    String timestampPost;
    Long timestampLong;
    String pictureUrl, caption, hashtags, uid, username;

    RecyclerView mRecyclerView;


    List<Comment> postCommentList;
    Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseFirestore=FirebaseFirestore.getInstance();


        postImageView = findViewById(R.id.postImageView);
        timeLabel = findViewById(R.id.timeLabel);
        usernameLabel = findViewById(R.id.usernameLabel);
        captionLabel = findViewById(R.id.captionLabel);
        hashtagsLabel = findViewById(R.id.hashtagsLabel);
        commentText = findViewById(R.id.commentsText);
        deleteButton = findViewById(R.id.deletePostButton);


        Intent intent = getIntent();
        timestampLong = Long.parseLong(intent.getExtras().getString("Time"));
        timestampPost = intent.getExtras().getString("Time");
        pictureUrl = intent.getExtras().getString("Picture");
        caption = intent.getExtras().getString("Caption");
        hashtags = intent.getExtras().getString("Hashtags");
        uid = intent.getExtras().getString("Uid");
        username = intent.getExtras().getString("Username");


        showComments();

        showPosts();

    }


    public void showPosts(){

        if (timestampLong < 10000000000L) {
            timestampLong = timestampLong * 1000;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date(Long.parseLong(String.valueOf(timestampLong))));

        post = new Post(pictureUrl,username,sd,uid,caption,hashtags);


    }

    public void showComments(){


        firebaseFirestore.collection("comments")
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        postCommentList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()){

                            String currentUid = document.getData().get("uid").toString();
                            String currentUsername = document.getData().get("username").toString();
                            String currentTimestamp = document.getData().get("timestamp").toString();
                            String currentPostTimestamp = document.getData().get("postTimestamp").toString();
                            String currentProfilePhoto = document.getData().get("profilePhoto").toString();
                            String currentComment = document.getData().get("commentText").toString();

                            if(currentPostTimestamp.equals(timestampPost)){
                                Comment currentCommentData = new Comment(
                                        currentUid,
                                        currentUsername,
                                        currentProfilePhoto,
                                        currentTimestamp,
                                        currentPostTimestamp
                                        ,currentComment);
                                postCommentList.add(currentCommentData);
                            }

                        }

                        mRecyclerView = findViewById(R.id.commentsRecycler);
                        CommentsImageAdapter myAdapter = new CommentsImageAdapter(PostActivity.this,postCommentList, post);
                        mRecyclerView.setHasFixedSize(true);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(PostActivity.this));
                        mRecyclerView.setAdapter(myAdapter);


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(e.toString());
            }
        });

    }

    public void submitComment(View view){


        final Map commentData = new HashMap<>();

        firebaseFirestore.collection("profile photos")
                .whereEqualTo("uid",currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String profilePhoto = document.getData().get("photoUrl").toString();
                            String username = document.getData().get("username").toString();

                            String CommentString = commentText.getText().toString();

                            commentData.put("uid", currentUser.getUid());
                            commentData.put("username", username);
                            commentData.put("profilePhoto", profilePhoto);
                            commentData.put("timestamp", Timestamp.now().getSeconds());
                            commentData.put("postTimestamp",timestampPost);
                            commentData.put("commentText", CommentString);


                        }
                        firebaseFirestore.collection("comments").add(commentData)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Intent intent = new Intent(getApplicationContext(),PostActivity.class);

                                        intent.putExtra("Time",timestampPost);
                                        intent.putExtra("Picture",pictureUrl);
                                        intent.putExtra("Caption",caption);
                                        intent.putExtra("Hashtags",hashtags);
                                        intent.putExtra("Uid",uid);
                                        intent.putExtra("Username",username);
                                        // start the activity
                                        startActivity(intent);
                                    }
                                });

                    }
                });



    }

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    public void deletePost(View view){
        firebaseFirestore
                .collection("post photos")
                .whereEqualTo("uid",currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (DocumentSnapshot document : task.getResult()) {
                    String timestamp = document.getData().get("timestamp").toString();
                    if(timestamp.equals(timestampPost)){
                        firebaseFirestore.collection("post photos")
                                .document(document.getId()).delete();
                        showMessage("Deleted!");
                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                        finish();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showMessage(e.toString());
            }
        });
    }

    public void backToProfile(View view){
        showMessage("Going back to profile page");
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        finish();
    }

    public void backToGlobal(View view){
        showMessage("Going back to global page...");
        startActivity(new Intent(getApplicationContext(),GlobalActivity.class));
        finish();
    }


}
