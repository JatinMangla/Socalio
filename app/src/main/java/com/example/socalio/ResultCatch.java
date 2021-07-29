package com.example.socalio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import fragments.ChatListFragment;

public class ResultCatch extends AppCompatActivity {

    int hightScore;
    ImageView medal;
    private ImageButton exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_catch);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        TextView hightScoreLabel = (TextView) findViewById(R.id.hightScoreLabel);
        TextView gamesPlayedLabel = (TextView) findViewById(R.id.gamesPlayedLabel);
        //medal = (ImageView) findViewById(R.id.medal);
        exitBtn = findViewById(R.id.exitBtn);


        int score = getIntent().getIntExtra("SCORE", 0);
        scoreLabel.setText(""+score);

       /* if (score < 50){
            medal.setImageResource(R.drawable.bronze);
        } else  if (score >= 150){
            medal.setImageResource(R.drawable.gold);
        } else {
            medal.setImageResource(R.drawable.silver);
        }*/

        SharedPreferences preferencesScore = getSharedPreferences("HIGHTSCORE", Context.MODE_PRIVATE);
        hightScore = preferencesScore.getInt("HIGHTSCORE", 0);

        if (score > hightScore) {
            hightScoreLabel.setText("Hight Score: " + score);

            SharedPreferences.Editor editor = preferencesScore.edit();
            editor.putInt("HIGHTSCORE", score);
            editor.commit();
        } else {
            hightScoreLabel.setText("Hight Score: "+ hightScore);
        }

        SharedPreferences preferencesGames = getSharedPreferences("GAMES", Context.MODE_PRIVATE);
        int games = preferencesGames.getInt("GAMES", 0);

        gamesPlayedLabel.setText("Games Played: "+ (games+1));

        SharedPreferences.Editor editor = preferencesGames.edit();
        editor.putInt("GAMES", (games+1));
        editor.commit();


        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ResultCatch.this,DashboardActivity.class));
            }
        });
    }

    public void tryAgain(View view){
        startActivity(new Intent(getApplicationContext(), MainScreen.class));
        finish();
    }

    //disable return button
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN){
            switch (event.getKeyCode()){
                case  KeyEvent.KEYCODE_BACK:
                    return true;
            }
        }
        return  super.dispatchKeyEvent(event);
    }
}