package com.example.catdog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.widget.Toast.LENGTH_SHORT;


public class SignupActivity extends AppCompatActivity {


    ImageView ImgUserPhoto;
    static int PReqCode = 1 ;
    static int GALLERY_CODE = 1 ;
    static int CAMERA_CODE=2;
    static int NO_PICTURE = 3;
    Uri pickedImgUri ;



    EditText emailText,passwordText,confirmPasswordText,usernameText,bioText;
    Button signupButton;
    ImageView profileImage;
    TextView imageLabel;
    ProgressBar progressBar;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //ini views
        emailText = findViewById(R.id.emailTextfield);
        passwordText = findViewById(R.id.passwordTextfield);
        confirmPasswordText = findViewById(R.id.confirmPasswordTextfield);
        usernameText = findViewById(R.id.usernameTextfield);
        bioText = findViewById(R.id.bioTextfield);
        progressBar = findViewById(R.id.progressBarSignup);
        signupButton = findViewById(R.id.gotoSignupButton);

        progressBar.setVisibility(View.INVISIBLE);


        firebaseAuth = FirebaseAuth.getInstance();

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //signupButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                final String email = emailText.getText().toString();
                final String password = passwordText.getText().toString();
                final String confirmPassword = confirmPasswordText.getText().toString();
                final String username = usernameText.getText().toString();
                final String bio  = bioText.getText().toString();


                if(TextUtils.isEmpty(email)) {
                    emailText.setError("Please enter your email");
                    progressBar.setVisibility(View.INVISIBLE);

                    return;
                }

                if(TextUtils.isEmpty(password)){
                    passwordText.setError("Please enter your password");
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if(TextUtils.isEmpty(confirmPassword)){
                    confirmPasswordText.setError("Please confirm your password");
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if(TextUtils.isEmpty(username)){
                    usernameText.setError("Please enter your username");
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }

                if(TextUtils.isEmpty(bio)){
                    bioText.setError("Please enter your bio");
                    progressBar.setVisibility(View.INVISIBLE);

                    return;
                }

                if(!password.equals(confirmPassword)){
                    confirmPasswordText.setError("Your passwords are not same");
                    progressBar.setVisibility(View.INVISIBLE);
                    return;
                }


                    // everything is ok and all fields are filled now we can start creating user account
                    // CreateUserAccount method will try to create the user if the email is valid
                showMessage("Start creating user");
                CreateUserAccount(email,username,password,bio);


            }
        });

        ImgUserPhoto = findViewById(R.id.profileImageSignup) ;

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items = {"Camera","Gallery"};
                AlertDialog.Builder listDialog =
                        new AlertDialog.Builder(SignupActivity.this);
                listDialog.setTitle("Choose:");
                listDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {



                        switch(which){
                            case 0:
//                                if(Build.VERSION.SDK_INT <=22){
//                                    openCamera();
//
//                                }
//                                else{
//                                    checkCameraPermission();
//                                }
                                openCamera();
                                break;
                            case 1:
//                                if (Build.VERSION.SDK_INT >= 22) {
//                                    checkGalleryPermission();
//                                }
//                                else
//                                {
//                                    openGallery();
//                                }
                                openGallery();
                                break;
                            default:
                                Intent nopictureIntent = new Intent(Intent.ACTION_BUG_REPORT);
                                startActivityForResult(nopictureIntent,NO_PICTURE);



                        }
                    }
                });
                listDialog.show();

            }
        });


    }

    private void CreateUserAccount(final String email, final String username, String password, final String bio) {

        // this method create user account with specific email and password

        if(pickedImgUri == null){
            showMessage("Please upload your profile picture");
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        else{
            firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                // user account created successfully
                                //showMessage("Account created");
                                updateUserInfo( username ,pickedImgUri,bio,email,firebaseAuth.getCurrentUser());

                                userID = firebaseAuth.getCurrentUser().getUid();


                            }
                            else
                            {

                                // account creation failed
                                showMessage("Account creation failed\n" + task.getException().getMessage());
                                signupButton.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);

                            }
                        }
                    });
        }
    }


    // update user photo and name
    private void updateUserInfo(final String username, final Uri pickedImgUri, final String bio,final String email, final FirebaseUser currentUser) {

        // first we need to upload user photo to firebase storage and get url

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {



                // image uploaded successfully
                // now we can get our image url

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) {

                        // uri contain user image url

                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(username)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // user info updated successfully
                                            //showMessage("Register Complete");

                                            userID = firebaseAuth.getCurrentUser().getUid();

                                            if (task.isSuccessful()) {
                                                // user info updated successfully
                                                //showMessage("Register Complete");

                                                userID = firebaseAuth.getCurrentUser().getUid();

                                                User theUser = new User(userID,email, username, bio, uri.toString());


                                                firebaseFirestore = FirebaseFirestore.getInstance();
                                                firebaseFirestore.collection("profile photos").add(theUser).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        showMessage("Firebase OK");
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                                                        finish();
                                                    }
                                                });
                                            }
                                        }

                                    }
                                });

                    }
                });


            }
        });




    }

    private void updateUI() {

        Intent profileActivity = new Intent(getApplicationContext(),ProfileActivity.class);
        startActivity(profileActivity);
        finish();


    }

    // simple method to show toast message
    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();

    }

    private void openGallery() {
        //TODO: open gallery intent and wait for user to pick an image !

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GALLERY_CODE);
    }

    private void openCamera(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent,CAMERA_CODE);

    }

    private void checkGalleryPermission() {


        if (ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SignupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(SignupActivity.this,"Please accept for required permission", LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(SignupActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        }
        else
            openGallery();

    }

    private void checkCameraPermission(){


        if (ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SignupActivity.this, Manifest.permission.CAMERA)) {

                Toast.makeText(SignupActivity.this,"Please accept for required permission", LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(SignupActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        PReqCode);
            }
        }
        if (ContextCompat.checkSelfPermission(SignupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(SignupActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Toast.makeText(SignupActivity.this,"Please accept for required permission", LENGTH_SHORT).show();

            }

            else
            {
                ActivityCompat.requestPermissions(SignupActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PReqCode);
            }
        }
        else
            openCamera();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK && requestCode == GALLERY_CODE && data != null ) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData() ;
            ImgUserPhoto.setImageURI(pickedImgUri);

        }

        if (resultCode == RESULT_OK && requestCode == CAMERA_CODE && data != null ) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            Bundle extras = data.getExtras();
            if (extras != null){
                Bitmap bitmap = (Bitmap) extras.get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                pickedImgUri = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, null,null));
                //ImgUserPhoto.setImageBitmap(bitmap);
                ImgUserPhoto.setImageURI(pickedImgUri);

            }


        }

    }


}
