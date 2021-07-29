package com.example.socalio;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SpeedUp extends AppCompatActivity {

    TextView score , time;
    Button play;
    RelativeLayout field;

    private CountDownTimer countDownTimer;
    private long timemillisecond = 10000;

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_up);

        score = findViewById(R.id.score);
        time = findViewById(R.id.time);
        play = findViewById(R.id.play);
        field = findViewById(R.id.field);

        score.setText("0");
        time.setText("10sec");

        field.setEnabled(false);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        field.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                score.setText(""+count);
            }
        });
    }

    public void play()
    {
        play.setVisibility(View.GONE);
        field.setEnabled(true);
        count = 0;
        startTime();
    }

    private void startTime()
    {
        countDownTimer = new CountDownTimer(timemillisecond, 1000) {
            @Override
            public void onTick(long l) {
                timemillisecond = 1;
                update();
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(SpeedUp.this,ResultActivity.class);
                intent.putExtra("score",count);
                startActivity(intent);
                finish();

            }
        }.start();
    }

    private  void update()
    {
        int seconds = (int) timemillisecond/1000;
        time.setText(""+seconds+"sec");
    }
}