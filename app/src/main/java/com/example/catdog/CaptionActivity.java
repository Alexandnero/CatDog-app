package com.example.catdog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaptionActivity extends AppCompatActivity {

    ImageView postImageView;
    EditText captionText;
    Switch autoHashtagsSwitch;
    Button postButton;
    FirebaseUser currentUser;
    Uri postImageUri;
    ProgressBar progressBar;
    Bitmap bitmap;
    List<String> hashtagsList;
    String hashtagsString="";
    String caption;
    TextView hashtagsTextview;


    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);

        FirebaseAuth firebaseAuth;
        FirebaseFirestore firebaseFirestore;

        postImageView = findViewById(R.id.postCaption);
        captionText = findViewById(R.id.captionText);
        autoHashtagsSwitch = findViewById(R.id.autoHashtagsSwitch);
        postButton = findViewById(R.id.postButton);
        hashtagsTextview = findViewById(R.id.hashtagsLabel);
        progressBar = findViewById(R.id.progressBarCaption);
        progressBar.setVisibility(View.INVISIBLE);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();


        Intent intent = getIntent();
        String postImageString = intent.getExtras().getString("Post");
        postImageUri = Uri.parse(postImageString);
        postImageView.setImageURI(postImageUri);

        autoHashtagsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    generateHashtags();
                }
                else{
                    hashtagsString="";
                    hashtagsTextview.setText("");
                }
            }
        });

    }

    public void submitPost(View view){
        caption = captionText.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        storageReference = FirebaseStorage.getInstance().getReference().child("post photos");
        final StorageReference imageFilePath = storageReference.child(postImageUri.getLastPathSegment());

        imageFilePath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Map postData = new HashMap<>();
                        postData.put("uid",currentUser.getUid());
                        postData.put("username",currentUser.getDisplayName());
                        postData.put("timestamp", Timestamp.now().getSeconds());
                        postData.put("storageRef",uri.toString());
                        postData.put("caption",caption);
                        postData.put("hashtags",hashtagsString);
                        showMessage("Uploading new post..");



                        firebaseFirestore = FirebaseFirestore.getInstance();
                        firebaseFirestore.collection("post photos")
                                .add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                progressBar.setVisibility(View.INVISIBLE);
                                startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                                finish();
                            }
                        });

                    }
                });

            }
        });

        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        finish();
    }


    public void generateHashtags(){


        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), postImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();

        labeler.processImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {

                        hashtagsList  = new ArrayList<>();
                        // Task completed successfully
                        for (FirebaseVisionImageLabel label: labels) {
                            String text = label.getText();
                            String entityId = label.getEntityId();
                            float confidence = label.getConfidence();
                            hashtagsList.add(text);
                        }

                        for (String hashtag : hashtagsList ){
                            hashtagsString+="# "+hashtag+" ";
                        }

                        hashtagsTextview.setText(hashtagsString);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        showMessage(e.toString());
                    }
                });
    }

    public void cancelPost(View view){
        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
        finish();
    }

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_Cat:
                if (checked)
                    Toast.makeText(CaptionActivity.this, "Tag: Cat",
                            Toast.LENGTH_SHORT).show();
                break;
            case R.id.radio_Dog:
                if (checked)
                    Toast.makeText(CaptionActivity.this, "Tag: Dog",
                            Toast.LENGTH_SHORT).show();
                break;
            case R.id.radio_Others:
                if (checked)
                    Toast.makeText(CaptionActivity.this, "Tag: Others",
                            Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
