package com.eshakorps.tateti_firebase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.eshakorps.tateti_firebase.Commons;
import com.eshakorps.tateti_firebase.R;
import com.eshakorps.tateti_firebase.app.My_Constants;
import com.eshakorps.tateti_firebase.model.Game;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

public class FindGameActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView loadingScreenText;
    private ImageView principalImage;
    private Button findGameButton, viewRankingButton;
    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String myUserID, gameID;
    private ScrollView layoutProgressBar, layoutMenu;
    private ListenerRegistration listenerRegistration = null;
    private LottieAnimationView lottieAnimationView;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        layoutMenu = findViewById(R.id.layoutMenu);

        principalImage = findViewById(R.id.imageViewPrincipal);
        findGameButton = findViewById(R.id.buttonFindGame);
        viewRankingButton = findViewById(R.id.buttonSeeRanking);

        initProgressBar();
        initFirebase();
        events();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(gameID!=""){
            changeVisibility(false);
            waitNewPlayer();
        } else {
            changeVisibility(true);
        }
    }

    @Override
    protected void onStop() {
        if(listenerRegistration!=null){
            listenerRegistration.remove();
        }
        if(gameID!=""){
            db.collection("game")
                    .document(gameID)
                    .delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            gameID="";
                        }
                    });
        }

        super.onStop();
    }

    private void events() {
        findGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeVisibility(false);
                lookingGame();
            }
        });

        viewRankingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void lookingGame(){
        loadingScreenText.setText("Looking game");
        lottieAnimationView.playAnimation();
        db.collection("games")
                .whereEqualTo("player2", "")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.getResult().size()==0){
                            //TODO there's none avaible game, create a new one
                            Toast.makeText(FindGameActivity.this, "No hay nada asi que se crea nueva partida", Toast.LENGTH_LONG);
                            loadingScreenText.setText("Creating a new game");
                            Game newGame = new Game(myUserID);
                            db.collection("games")
                                    .add(newGame)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            gameID = documentReference.getId();
                                            //JUST WAIT UNTIL A NEW PLAYER IS AVAIBLE TO PLAY
                                            waitNewPlayer();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    changeVisibility(true);
                                    Toast.makeText(FindGameActivity.this, "Error when creating a new game", Toast.LENGTH_LONG);
                                }
                            });
                        } else {
                            boolean gameFound = false;

                            for (DocumentSnapshot docGame : task.getResult().getDocuments()) {
                                if(docGame.get("player1").equals(myUserID)){
                                    gameFound=true;

                                gameID = docGame.getId();
                                Game newGame = docGame.toObject(Game.class);
                                newGame.setPlayer2(myUserID);

                                db.collection("games")
                                        .document(gameID)
                                        .set(newGame)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                loadingScreenText.setText("Game avaible, ready to start!");
                                                lottieAnimationView.setRepeatCount(0);
                                                lottieAnimationView.setAnimation("checked_animation.json");
                                                lottieAnimationView.playAnimation();

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        changeVisibility(true);
                                        Toast.makeText(FindGameActivity.this, "Error at starting an avaible game", Toast.LENGTH_LONG);
                                    }

                                });
                                    break;
                                }
                                 if(!gameFound){
                                      startGame();
                                 }
                            }
                        }
                    }
                });
    }

    private void waitNewPlayer() {
        loadingScreenText.setText("Waiting for player 2");
        listenerRegistration = db.collection("games")
                .document(gameID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(!documentSnapshot.get("player2").equals("")){
                            loadingScreenText.setText("Player 2 is ready to start!");
                            lottieAnimationView.setRepeatCount(0);
                            lottieAnimationView.setAnimation("checked_animation.json");
                            lottieAnimationView.playAnimation();

                            final Handler handler = new Handler();
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    startGame();
                                }
                            };
                            handler.postDelayed(r, 1500);
                        }
                    }
                });
    }

    private void startGame(){
        if(listenerRegistration!=null){
            listenerRegistration.remove();
        }
        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra(My_Constants.EXTRA_GAME_ID, gameID);
        startActivity(i);
        gameID="";
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        myUserID= firebaseUser.getUid();

    }

    private void initProgressBar() {
        progressBar = findViewById(R.id.progressBar4);
        loadingScreenText = findViewById(R.id.textgameLoading);
        lottieAnimationView = findViewById(R.id.animation_view);

        progressBar.setIndeterminate(true);
        loadingScreenText.setText("Loading...");

        changeVisibility(true);
    }

    private void changeVisibility(boolean state){
        layoutProgressBar.setVisibility(state ? View.GONE : View.VISIBLE);
        layoutMenu.setVisibility(state ? View.VISIBLE : View.GONE);
    }
}