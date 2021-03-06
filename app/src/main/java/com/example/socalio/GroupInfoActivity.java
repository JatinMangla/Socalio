package com.example.socalio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import adapters.AdapterParticipantsAdd;
import models.ModelUser;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private ActionBar actionBar;

    private String myGroupRole = "";
    private FirebaseAuth firebaseAuth;

    private TextView descriptionTv,createdByTv,editGroupTv,addParticipantTv,leaveGroupTv,participantsTv;
    private RecyclerView participantRv;
    private ImageView groupIconIv;

    private ArrayList<ModelUser> userList;
    private AdapterParticipantsAdd adapterParticipantsAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        groupId = getIntent().getStringExtra("groupId");

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconIv= findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
         participantRv = findViewById(R.id.participantRv);

         firebaseAuth = FirebaseAuth.getInstance();
         loadGroupInfo();
         loadMyGroupRole();

         addParticipantTv.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(GroupInfoActivity.this,GroupParicipantsAddActivity.class);
                 intent.putExtra("groupId",groupId);
                 startActivity(intent);
             }
         });

    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid").equalTo(firebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    myGroupRole = ""+ds.child("role").getValue();
                    actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail()+"("+myGroupRole+")");

                    if (myGroupRole.equals("participant"))
                    {
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.GONE);
                        leaveGroupTv.setText("Leave Group");
                    }
                    else if (myGroupRole.equals("admin"))
                    {
                        editGroupTv.setVisibility(View.GONE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Leave Group");
                    }
                    else if (myGroupRole.equals("creator"))
                    {
                        editGroupTv.setVisibility(View.VISIBLE);
                        addParticipantTv.setVisibility(View.VISIBLE);
                        leaveGroupTv.setText("Delete Group");
                    }
                }

                loadParticipants();

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void loadParticipants() {
        userList = new ArrayList<>();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String uid = ""+ds.child("uid").getValue();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull  DataSnapshot snapshot) {
                            for (DataSnapshot ds: snapshot.getChildren())
                            {
                                ModelUser modelUser = ds.getValue(ModelUser.class);

                                userList.add(modelUser);
                            }
                            adapterParticipantsAdd = new AdapterParticipantsAdd(GroupInfoActivity.this,userList,groupId,myGroupRole);
                            participantRv.setAdapter(adapterParticipantsAdd);
                            participantsTv.setText("Participants("+userList.size()+")");
                        }

                        @Override
                        public void onCancelled(@NonNull  DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String grpDescription =""+ ds.child("grpDescription").getValue();
                    String groupIcon =""+ ds.child("groupIcon").getValue();
                    String timestamp =""+ ds.child("timestamp").getValue();
                    String createdBy =""+ ds.child("createdBy").getValue();

                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String  dateTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", cal).toString();

                    loadCreatorInfo(dateTime,createdBy);

                    actionBar.setTitle(groupTitle);
                    descriptionTv.setText(grpDescription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_default_image_white).into(groupIconIv);
                    }
                    catch (Exception e)
                    {
                        groupIconIv.setImageResource(R.drawable.ic_default_image_white);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadCreatorInfo(String dateTime, String createdBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String name = ""+ds.child("name").getValue();
                    createdByTv.setText("Created by "+name+ "on "+ dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}