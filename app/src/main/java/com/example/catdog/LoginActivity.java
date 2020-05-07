package com.example.catdog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText emailText, passwordText;
    Button loginButton,gotoSignupButton;

    ProgressBar progressBar;
    FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.emailLogin);
        passwordText = findViewById(R.id.passwordLogin);
        progressBar = findViewById(R.id.progressBar2);

        loginButton = findViewById(R.id.loginButton);
        gotoSignupButton = findViewById(R.id.gotoSignupButton);
        firebaseAuth = FirebaseAuth.getInstance();


        gotoSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignupActivity.class));
            }
        });

        if(firebaseAuth.getCurrentUser()!=null){
            startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
            progressBar.setVisibility(View.INVISIBLE);

        }

        else{
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = emailText.getText().toString().trim();
                    String password = passwordText.getText().toString().trim();

                    if(TextUtils.isEmpty(email)){
                        emailText.setError("Please enter your email");
                        return;
                    }

                    if(TextUtils.isEmpty(password)){
                        passwordText.setError("Please enter your password");
                        return;
                    }

                    progressBar.setVisibility(View.VISIBLE);

                    //To authenticate user

                    firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this,"Successfully login",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(),ProfileActivity.class));
                            }
                            else{
                                Toast.makeText(LoginActivity.this,"Error!"+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });



                }
            });
        }



    }
}
