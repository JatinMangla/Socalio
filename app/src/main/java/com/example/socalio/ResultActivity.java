package com.example.socalio;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import fragments.ChatListFragment;


public class ResultActivity extends AppCompatActivity {

    Button playagain, exit;
    TextView score , bestscore;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);


        playagain = findViewById(R.id.playagain);
        score = findViewById(R.id.score);
        bestscore = findViewById(R.id.bestscore);
        exit = findViewById(R.id.exit);

        playagain.setEnabled(false);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                playagain.setEnabled(true);

            }
        };
        handler.postDelayed(runnable,2000);

        Intent intent = getIntent();
        final int scores = intent.getIntExtra("score",0);

        score.setText(""+scores);

        SharedPreferences prefs = getSharedPreferences("PREPS", MODE_PRIVATE);
        int bestscores = prefs.getInt("bestscore",0);

        if (scores>bestscores)
        {
            SharedPreferences.Editor editor = getSharedPreferences("PREPS",MODE_PRIVATE).edit();
            editor.putInt("bestscore",scores);
            editor.apply();
            bestscore.setText("ur score"+scores);
        }
        else
        {
            bestscore.setText("world best score"+bestscores +"/n"+"ur score"+scores);

        }
        playagain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ResultActivity.this,SpeedUp.class));
                finish();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ResultActivity.this, ChatListFragment.class));
               /* Intent intent = new Intent(getApplicationContext(), MessageAdapter.class);
                String hua = String.valueOf(scores);
                intent.putExtra("full",hua  );
                startActivity(intent);*/
            }
        });
    }
}