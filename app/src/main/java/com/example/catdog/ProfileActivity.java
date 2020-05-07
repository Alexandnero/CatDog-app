package com.example.catdog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.LogDescriptor;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.value.TimestampValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.grpc.Server;

import static android.widget.Toast.LENGTH_SHORT;

public class ProfileActivity extends AppCompatActivity {

    //Uri pickedImgUri ;
    ImageView profileImageSignup;
    FirebaseUser currentUser ;
    FirebaseAuth firebaseAuth;
    TextView usernameDisplay,userbioDisplay;
    Uri postImageUri ;
    FloatingActionButton addButton;
    ProgressBar progressBar;
    // For uploading posts

    StorageReference storageReference;
    StorageTask storageTask;
    //DatabaseReference databaseReference;
    FirebaseFirestore firebaseFirestore;

    RecyclerView mRecyclerView;
    ImageAdapter mImageAdapter;

    List<Post> postList;


    static int PReqCode = 1 ;
    static int GALLERY_CODE = 1 ;
    static int CAMERA_CODE=2;
    static int NO_PICTURE = 3;

    String currentPhotoPath;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();

        currentUser = firebaseAuth.getCurrentUser();

        usernameDisplay = findViewById(R.id.usernameDisplay);
        userbioDisplay = findViewById(R.id.userbioDisplay);
        profileImageSignup = findViewById(R.id.profileImageSignup);
        addButton = findViewById(R.id.addButton);
        progressBar = findViewById(R.id.progressBarProfile);
        progressBar.bringToFront();


        showPosts();
        showProfileInfo();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();

            }
        });
    }




    public void showProfileInfo(){

        String userID = currentUser.getUid();

        Glide.with(ProfileActivity.this).load(currentUser.getPhotoUrl()).into(profileImageSignup);

        firebaseFirestore=FirebaseFirestore.getInstance();

        firebaseFirestore.collection("profile photos")
                .whereEqualTo("uid",currentUser.getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                if(e!=null){
//
//                }
//                userList  = new ArrayList<>();
                User theUser = null;

                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {

                    String currentUid = documentSnapshot.getData().get("uid").toString();
                    String currentBio = documentSnapshot.getData().get("bio").toString();
                    String currentEmail = documentSnapshot.getData().get("email").toString();
                    String currentUsername = documentSnapshot.getData().get("username").toString();
                    String currentUrl = documentSnapshot.getData().get("photoUrl").toString();

                    theUser = new User(currentUid,currentEmail, currentUsername, currentBio, currentUrl);

                }
                usernameDisplay.setText(theUser.getUsername());
                userbioDisplay.setText(theUser.getBio());


            }
        });



}

    public void getPicture(){
        final String[] items = {"Camera","Gallery"};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(ProfileActivity.this);
        listDialog.setTitle("Choose:");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch(which){
                    case 0:
//                        if(Build.VERSION.SDK_INT < 22){
//                            openCamera();
//                        }
//                        else{
//                            checkCameraPermission();
//                        }
                        openCamera();
                        break;
                    case 1:
//                        if (Build.VERSION.SDK_INT >= 22) {
//                            checkGalleryPermission();
//                        }
//                        else
//                        {
//                            openGallery();
//                        }
                        openGallery();
                        break;
                }
            }
        });
        listDialog.show();

    }

    public void storePicture(){

        progressBar.setVisibility(View.VISIBLE);

        Bitmap srcBmp = null;
        try {
            srcBmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap dstBmp = null;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        Bitmap bitmap  = Bitmap.createScaledBitmap(dstBmp, 1024, 1024, true);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        postImageUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));

        startActivity(new Intent(getApplicationContext(),CaptionActivity.class).putExtra("Post",postImageUri.toString()));

    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_CODE);
    }

    private void openCamera(){
//        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//        startActivityForResult(cameraIntent,CAMERA_CODE);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                showMessage(ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_CODE);
            }
        }

    }

    private void checkGalleryPermission() {

        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(ProfileActivity.this,"Please accept for required permission", LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(ProfileActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        }
        else
            openGallery();
            //storePicture();

    }

    private void checkCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PReqCode);
        }
        else{
            openCamera();
            //storePicture();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == GALLERY_CODE && data != null ) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            postImageUri = data.getData() ;

            storePicture();

        }

        if (resultCode == RESULT_OK && requestCode == CAMERA_CODE && data != null ) {

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(currentPhotoPath);
            postImageUri = Uri.fromFile(f);
            mediaScanIntent.setData(postImageUri);
            this.sendBroadcast(mediaScanIntent);

            storePicture();

        }

    }

    public void showPosts(){

        firebaseFirestore=FirebaseFirestore.getInstance();

        firebaseFirestore.collection("post photos")
                .whereEqualTo("uid",currentUser.getUid())
                .orderBy("timestamp")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        postList  = new ArrayList<>();

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String currentUri = document.getData().get("storageRef").toString();
                                String currentUsername = document.getData().get("username").toString();
                                String currentTimestamp = document.getData().get("timestamp").toString();
                                String currentUid = document.getData().get("uid").toString();
                                String currentCaption = document.getData().get("caption").toString();
                                String currentHashtags = document.getData().get("hashtags").toString();

                                Post currentPost = new Post(currentUri, currentUsername,currentTimestamp,currentUid,currentCaption,currentHashtags);
                                postList.add(currentPost);

                            }

                            Collections.reverse(postList);
                            mRecyclerView = findViewById(R.id.postsRecycleView);
                            ImageAdapter myAdapter = new ImageAdapter(ProfileActivity.this,postList);
                            mRecyclerView.setLayoutManager(new GridLayoutManager(ProfileActivity.this,3));
                            mRecyclerView.setAdapter(myAdapter);


                        }
                        else {
//                            Log.d(TAG, "Error getting documents: ", task.getException());
                            System.out.println(task.getException());

                        }
                    }
                });



        progressBar.setVisibility(View.INVISIBLE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        finish();
        showMessage("Successfully log out");
    }

    public void changeFeed(View view){
        startActivity(new Intent(getApplicationContext(),GlobalActivity.class));
        finish();
    }

}


