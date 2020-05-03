package com.eshakorps.tateti_firebase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.eshakorps.tateti_firebase.Commons;
import com.eshakorps.tateti_firebase.R;
import com.eshakorps.tateti_firebase.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText Ename, Eemail, Epassword;
    private String username, email, password;
    private Button buttonSingin;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private ScrollView formRegister;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        Ename=findViewById(R.id.loginUsernameRegister);
        Eemail=findViewById(R.id.loginEmailRegister);
        Epassword=findViewById(R.id.loginPasswordRegister);
        formRegister=findViewById(R.id.formSingin);
        buttonSingin = findViewById(R.id.loginButtonRegister);
        progressBar=findViewById(R.id.progressBarRegister);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        changeVisibility(true);
        events();
    }

    private void events(){
        buttonSingin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeVisibility(false);
                username = Ename.getText().toString();
                email = Eemail.getText().toString();
                password = Epassword.getText().toString();
                Map<EditText, String> inputs = new HashMap<EditText, String>();
                inputs.put(Ename, username);
                inputs.put(Eemail, email);
                inputs.put(Epassword, password);
                Commons c = new Commons();
                if(c.checkIfNotVoid(inputs)){
                    //We start introducing a new user to firebase auth
                    createNewUser();
                }
            }
        });
    }

    private Boolean creacionBuena = false;
    private FirebaseUser user;
    private void createNewUser(){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            creacionBuena= true;
                            Toast.makeText(RegisterActivity.this, "Creación exitosa", Toast.LENGTH_SHORT).show();
                            user = firebaseAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Log.w("TAG", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
        if(creacionBuena){
        }
    }

    private void updateUI(FirebaseUser user){
        if(user != null) {
            // SAVE THE USER'S INFO AT FIRESTORE
            User newUser = new User(username, 0, 0);
            db.collection("user")
                    .document(user.getUid())
                    .set(newUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
//                            changeVisibility(true);
                            finish();
                            Intent i = new Intent(RegisterActivity.this, FindGameActivity.class);
                            startActivity(i);
                        }
                    });
            //GO TO QUEUE


        } else {
            changeVisibility(true);
            Epassword.setError("Email y/o contraseña incorrecta");
            Epassword.requestFocus();
        }
    }

    private void changeVisibility(boolean state){
        progressBar.setVisibility(state ? View.GONE : View.VISIBLE);
        formRegister.setVisibility(state ? View.VISIBLE : View.GONE);
    }

}
