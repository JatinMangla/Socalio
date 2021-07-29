package com.example.socalio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import fragments.ChatListFragment;

public class MainScreen extends AppCompatActivity {

    ImageButton play, more, shop,exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //full screnn

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        play =  findViewById(R.id.play);
        more = findViewById(R.id.more);
        shop =  findViewById(R.id.shop);
        exitBtn = findViewById(R.id.exitBtn);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainNextScreen.class));
                finish();
            }
        });

        shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainScreen.this, ShopActivity.class));
                finish();
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PopupMenu popupMenu = new PopupMenu(MainScreen.this, more);
                popupMenu.getMenuInflater().inflate(R.menu.pop_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        Intent intent, chooser;
                        int id = menuItem.getItemId();
                        if (id == R.id.feedback){
                            intent = new Intent(Intent.ACTION_SEND);
                            intent.setData(Uri.parse("mailto:"));
                            String[] to = {"jatinmangla135@gmail.com"}; // my email
                            intent.putExtra(Intent.EXTRA_EMAIL, to);
                            intent.setType("message/rfc822");
                            chooser = Intent.createChooser(intent, "Send Feedback");
                            startActivity(chooser);
                        }

                        if (id == R.id.share){
                            intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Vermiculi");
                            String sAux = "\n Let me recommend you this Game \n\n";
                            sAux = sAux+ "https://play.google.com/store/apps/details?id=com.angrymonkey.angrymonkey \n\n";
                            intent.putExtra(Intent.EXTRA_TEXT, sAux);
                            startActivity(Intent.createChooser(intent, "Share"));
                        }

                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainScreen.this, ChatListFragment.class));
            }
        });
    }

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