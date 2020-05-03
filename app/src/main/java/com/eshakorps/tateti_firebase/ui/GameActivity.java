package com.eshakorps.tateti_firebase.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.airbnb.lottie.LottieAnimationView;
import com.eshakorps.tateti_firebase.R;
import com.eshakorps.tateti_firebase.app.My_Constants;
import com.eshakorps.tateti_firebase.model.Game;
import com.eshakorps.tateti_firebase.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    List<ImageView> boxes;
    TextView player1, player2;
    FirebaseFirestore db;
    FirebaseAuth auth;
    String uid, gameId, playerName1="", playerName2="", winnerDI = "";;
    ListenerRegistration listenerGame=null;
    FirebaseUser firebaseUser;
    Game game;
    String playerName;
    User user1, user2;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.the_game_activity);
        init();
        initGame();
    }

    @Override
    protected void onStart() {
        super.onStart();
        gameListener();
    }

    @Override
    protected void onStop() {
        if (listenerGame != null) {
            listenerGame.remove();
        }

        super.onStop();
    }

    private void gameListener(){
        listenerGame = db.collection("game")
                .document()
                .addSnapshotListener(GameActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(e !=null){
                            //ERROR
                            Toast.makeText(GameActivity.this, "Error with the game and firebase DB", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String source = documentSnapshot!=null && documentSnapshot.getMetadata().hasPendingWrites()?"Local":"Server";

                        if(documentSnapshot.exists() && source.equals("Server")){
                            game = documentSnapshot.toObject(Game.class);
                            if(playerName1.isEmpty()||playerName2.isEmpty()){
                                getNameOfPlayers();
                            }
                            updateUI();
                        }
                        changeColorPlayers();

                    }
                });
    }

    private void changeColorPlayers(){
        if(game.isPlayer1Turn()){
            player1.setTextColor(getResources().getColor(R.color.colorPasivePlayer));
            player2.setTextColor(getResources().getColor(R.color.colorPlayer2));
            game.setPlayer1Turn(false);
        } else {
            player2.setTextColor(getResources().getColor(R.color.colorPasivePlayer));
            player1.setTextColor(getResources().getColor(R.color.colorPlayer1));
            game.setPlayer1Turn(true);
        }
    }

    private void updateUI(){
        for(int i=0; i<9; i++){
            int box = game.getSelectedCells().get(i);
            ImageView actualBox = boxes.get(i);
            if(box==0){
                actualBox.setImageResource(R.drawable.ic_empty_square);
            } else if(box==1){
                actualBox.setImageResource(R.drawable.ic_player_one);
            } else {
                actualBox.setImageResource(R.drawable.ic_player_two);
            }
        }
    }


    /*
            x x x
            0 0 0
            x x x
     */
    private boolean isAnySolution(){
        boolean exist = false;

        boolean canNotContinue = false;
        for (int i = 0; i<9;i++){
            if(game.getSelectedCells().get(i)==0){
                canNotContinue=true;
                break;
            }
        }

        if(!canNotContinue) {
            exist=true;
        } else {
            List<Integer> selectedCells = game.getSelectedCells();
            if(selectedCells.get(0) == selectedCells.get(1)
                    && selectedCells.get(1) == selectedCells.get(2)
                    && selectedCells.get(2) != 0) { // 0 - 1 - 2
                exist = true;
            } else if(selectedCells.get(3) == selectedCells.get(4)
                    && selectedCells.get(4) == selectedCells.get(5)
                    && selectedCells.get(5) != 0) { // 3 - 4 - 5
                exist = true;
            } else if(selectedCells.get(6) == selectedCells.get(7)
                    && selectedCells.get(7) == selectedCells.get(8)
                    && selectedCells.get(8) != 0) { // 6 - 7 - 8
                exist = true;
            } else if(selectedCells.get(0) == selectedCells.get(3)
                    && selectedCells.get(3) == selectedCells.get(6)
                    && selectedCells.get(6) != 0) { // 0 - 3 - 6
                exist = true;
            } else if(selectedCells.get(1) == selectedCells.get(4)
                    && selectedCells.get(4) == selectedCells.get(7)
                    && selectedCells.get(7) != 0) { // 1 - 4 - 7
                exist = true;
            } else if(selectedCells.get(2) == selectedCells.get(5)
                    && selectedCells.get(5) == selectedCells.get(8)
                    && selectedCells.get(8) != 0) { // 2 - 5 - 8
                exist = true;
            } else if(selectedCells.get(0) == selectedCells.get(4)
                    && selectedCells.get(4) == selectedCells.get(8)
                    && selectedCells.get(8) != 0) { // 0 - 4 - 8
                exist = true;
            } else if(selectedCells.get(2) == selectedCells.get(4)
                    && selectedCells.get(4) == selectedCells.get(6)
                    && selectedCells.get(6) != 0) { // 2 - 4 - 6
                exist = true;
            }
        }

        return exist;
    }

    private void getNameOfPlayers(){
        getNamePlayer1();
        getNamePlayer2();


    }

    private void getNamePlayer1(){
        db.collection("users")
                .document(game.getPlayer1())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        playerName1 = documentSnapshot.get("name").toString();
                        player1.setText(playerName1);
                        if(game.getPlayer1().equals(uid)) {
                            playerName = playerName1;
                        }
                    }
                });
    }

    private void getNamePlayer2(){
        db.collection("users")
                .document(game.getPlayer2())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        playerName2 = documentSnapshot.get("name").toString();
                        player2.setText(playerName2);
                        if(game.getPlayer2().equals(uid)) {
                            playerName = playerName2;
                        }
                    }
                });
    }

    private void boxSelected(View view){
        if(!game.getWinnerID().isEmpty()){
            Toast.makeText(this, "Game OVER", Toast.LENGTH_SHORT);
        } else {
            if(game.isPlayer1Turn() && game.getPlayer1().equals(uid)){
                updateGame(view.getTag().toString());
            } else if(!game.isPlayer1Turn() && game.getPlayer2().equals(uid)){
                updateGame(view.getTag().toString());
            } else {
                Toast.makeText(this, "It's not your turn yet", Toast.LENGTH_SHORT).show();
            }
        }
        //UODATE FIRESTORE

        if(isAnySolution()) {
            game.setWinnerID(uid);
            Toast.makeText(this, "Hay solución", Toast.LENGTH_SHORT).show();
        } else if(isTie()) {
            game.setWinnerID("EMPATE");
            Toast.makeText(this, "Hay empate", Toast.LENGTH_SHORT).show();
        } else {
            changeTurn();
        }


        db.collection("game")
                .document(gameId)
                .set(game)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                }).addOnFailureListener(GameActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ERROR", "Erro after saving game on Firestore");
                    }
                });
    }

    private boolean isTie(){
            boolean existe = false;

            // Empate
            boolean hayCasillaLibre = false;
            for(int i=0; i<9; i++) {
                if(game.getSelectedCells().get(i) == 0) {
                    hayCasillaLibre = true;
                    break;
                }
            }

            if(!hayCasillaLibre) {
                existe = true;
            }
            return existe;
    }

    private void changeTurn(){
        if(game.isPlayer1Turn()){
            player1.setTextColor(getResources().getColor(R.color.colorPasivePlayer));
            player2.setTextColor(getResources().getColor(R.color.colorActivePlayer));
            game.setPlayer1Turn(false);
        } else {
            player2.setTextColor(getResources().getColor(R.color.colorPasivePlayer));
            player1.setTextColor(getResources().getColor(R.color.colorActivePlayer));
            game.setPlayer1Turn(true);
        }
    }

    private void updateGame(String nroBox) {
        int boxPosition = Integer.parseInt(nroBox);
        if(game.getSelectedCells().get(boxPosition)!=0){
            Toast.makeText(this, "Select an empty box", Toast.LENGTH_SHORT);
        } else {
            if(game.isPlayer1Turn()){
                boxes.get(boxPosition).setImageResource(R.drawable.ic_player_one);
                game.getSelectedCells().set(boxPosition, 1);
            } else {
                boxes.get(boxPosition).setImageResource(R.drawable.ic_player_two);
                game.getSelectedCells().set(boxPosition, 2);
            }

            if(isAnySolution()){

            }
        }



    }

    private void initGame(){
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = auth.getCurrentUser();
        uid = firebaseUser.getUid();

        Bundle extras = getIntent().getExtras();
        gameId = extras.getString(My_Constants.EXTRA_GAME_ID);
    }

    private void init(){
        player1 = findViewById(R.id.player1);
        player2 = findViewById(R.id.player2);

        boxes = new ArrayList<>();
        boxes.add((ImageView) findViewById(R.id.place1));
        boxes.add((ImageView) findViewById(R.id.place2));
        boxes.add((ImageView) findViewById(R.id.place3));
        boxes.add((ImageView) findViewById(R.id.place4));
        boxes.add((ImageView) findViewById(R.id.place5));
        boxes.add((ImageView) findViewById(R.id.place6));
        boxes.add((ImageView) findViewById(R.id.place7));
        boxes.add((ImageView) findViewById(R.id.place8));
        boxes.add((ImageView) findViewById(R.id.place9));
    }

    public void mostrarDialogoGameOver() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = getLayoutInflater().inflate(R.layout.dialog_game_over, null);
        // Obtenemos las referencias a los View components de nuestro layout
        TextView tvPuntos = v.findViewById(R.id.textViewPoints);
        TextView tvInformacion = v.findViewById(R.id.textViewInformation);
        LottieAnimationView gameOverAnimation = v.findViewById(R.id.animation_view);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Game Over");
        builder.setCancelable(false);
        builder.setView(v);

        if(winnerDI.equals("EMPATE")) {
            tvInformacion.setText("¡" + playerName + " has empatado!");
            tvPuntos.setText("+1 punto");
            updatePoints(uid, 1);
        } else if(winnerDI.equals(uid)) {
            tvInformacion.setText("¡" + playerName + " has ganado!");
            tvPuntos.setText("+3 puntos");
            updatePoints(uid, 3);
        } else {
            tvInformacion.setText("¡" + playerName + " has perdido!");
            tvPuntos.setText("0 puntos");
            gameOverAnimation.setAnimation("thumbs_down_animation.json");
            updatePoints(uid, 0);
        }

        builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updatePoints(String uid, int gained){
        User userToUpdate = null;
        if(playerName.equals(player1)) {
            user1.setPoints(user1.getPoints() + gained);
            user1.setPlayable_games(user1.getPlayable_games() + 1);
            userToUpdate = user1;
        } else {
            user2.setPoints((user2.getPoints() + gained));
            user2.setPlayable_games(user2.getPlayable_games() + 1);
            userToUpdate = user2;
        }
        db.collection("users")
                .document(uid)
                .set(gained)
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(GameActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}
