package com.eshakorps.tateti_firebase.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.eshakorps.tateti_firebase.Commons;
import com.eshakorps.tateti_firebase.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private boolean trylogin=false;
    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginButton, singinButton;
    private ProgressBar progressBar;
    private ScrollView formLogin;

    private String email, password;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // Create a new user with a first and last name
   /*     Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);*/

        loginEmail = findViewById(R.id.loginEmail);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        singinButton = findViewById(R.id.loginSingIn);
        formLogin = findViewById(R.id.formLogin);
        progressBar = findViewById(R.id.progressBar);

        changeVisibility(true);
        events();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Check if the user has already logged in in this device
        //if TRUE, just continue
//        FirebaseUser user = firebaseAuth.getCurrentUser();
//        updateUI(user, trylogin);
    }

    private void events(){
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = loginEmail.getText().toString();
                password = loginPassword.getText().toString();
                Map<EditText, String> mapInputs = new HashMap<EditText, String>();
                mapInputs.put(loginEmail, email);
                mapInputs.put(loginPassword, password);
                trylogin = true;
                Commons c = new Commons();
                if(c.checkIfNotVoid(mapInputs)){
                    //Start Game or wait until a new opponent in.
                    Toast.makeText(MainActivity.this,"LOGIN BUTTON",
                            Toast.LENGTH_LONG);

                    changeVisibility(false);
                    loginUser();
                } else {
                    //ERROR
                }
            }
        });

        singinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    private void loginUser(){
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //CORRECT
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user, trylogin);
                        } else {
                            Log.w("TAG", "Sign in error: ", task.getException());
                            updateUI(null, trylogin);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user, boolean trylogin){
        if(user != null) {
            // SAVE THE USER'S INFO AT FIRESTORE
            //GO TO QUEUE
            Intent i = new Intent(MainActivity.this, FindGameActivity.class);
            changeVisibility(true);
            startActivity(i);
        } else{
            changeVisibility(true);
            if(trylogin){
                loginPassword.setError("Email y/o contraseña incorrectas");
                loginPassword.requestFocus();
            }
        }
    }

    private void changeVisibility(boolean state){
        progressBar.setVisibility(state ? View.GONE : View.VISIBLE);
        formLogin.setVisibility(state ? View.VISIBLE : View.GONE);
    }



}
