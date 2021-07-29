package com.example.socalio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import adapters.AdapterPosts;
import models.ModelPost;

public class ThereProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ImageView avatarTv,coverIv;
    TextView nameTv,emailTv,phoneTv;
    RecyclerView postRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        avatarTv = findViewById(R.id.avatarIv);
        nameTv =findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        coverIv = findViewById(R.id.coverIv);
        postRecyclerView = findViewById(R.id.recycleview_posts);
        firebaseAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");



        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    String name = ""+ ds.child("name").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String cover = ""+ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try
                    {
                        Picasso.get().load(image).into(avatarTv);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_default_image_white).into(avatarTv);
                    }
                    try
                    {
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e)
                    {
                        //Picasso.get().load(R.drawable.ic_default_image_white).into(coverIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postList = new ArrayList<>();

        checkUsersStatus();
        loadHisPosts();

    }

    private void loadHisPosts() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query =ref.orderByChild("uId").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    postList.add(myPosts);

                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchHisPost(String searchQuery)
    {
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        postRecyclerView.setLayoutManager(layoutManager);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query =ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    assert myPosts != null;
                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) || myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase()))
                    {
                        postList.add(myPosts);
                    }

                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void checkUsersStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
           // uid=user.getUid();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s))
                {
                    searchHisPost(s);
                }
                else
                {
                    loadHisPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s))
                {
                    searchHisPost(s);
                }
                else
                {
                    loadHisPosts();
                }
                return false;
            }
        });

       return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUsersStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}