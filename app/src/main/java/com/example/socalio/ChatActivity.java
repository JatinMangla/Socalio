package com.example.socalio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import adapters.AdapterChat;
import models.ModelChat;
import models.ModelUser;

public class ChatActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn , attachBtn;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference userDBR;

    String hisUid;
    String myUid;

    String hisImage;


    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissons;
    String[] storagePermissions;

    Uri image_rui = null;

    private FloatingActionButton main , game , whiteboard,catch1;
    private  TextView gametext, whiteboardtext,catch1_text;
    private boolean isopen =false;

    ValueEventListener seenListener;
    DatabaseReference UserRefForSeen;

    List<ModelChat> chatList;
    AdapterChat adapterChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView = findViewById(R.id.chat_recycleView);
        profileIv = findViewById(R.id.profileIv);
        userStatusTv = findViewById(R.id.userStatusTv);
        messageEt = findViewById(R.id.messageEt);
        sendBtn = findViewById(R.id.sendBtn);
        nameTv = findViewById(R.id.nameTv);
        attachBtn = findViewById(R.id.attachbtn);

        main = findViewById(R.id.main);
        game = findViewById(R.id.game);
        whiteboard = findViewById(R.id.whiteboard);
        gametext = findViewById(R.id.gametext);
        whiteboardtext = findViewById(R.id.whiteboardtext);
        catch1= findViewById(R.id.catch1);
        catch1_text = findViewById(R.id.catch1_text);

       /* Bundle data = getIntent().getExtras();
        String score = data.getString("score");
        if (score != null)
        {
            messageEt.setText(score);
        }*/


        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isopen)
                {
                    game.setVisibility(View.INVISIBLE);
                    whiteboard.setVisibility(View.INVISIBLE);
                    gametext.setVisibility(View.INVISIBLE);
                    whiteboardtext.setVisibility(View.INVISIBLE);
                    catch1.setVisibility(View.INVISIBLE);
                    catch1_text.setVisibility(View.INVISIBLE);
                    isopen = false;
                }
                else
                {
                    game.setVisibility(View.VISIBLE);
                    whiteboard.setVisibility(View.VISIBLE);
                    gametext.setVisibility(View.VISIBLE);
                    whiteboardtext.setVisibility(View.VISIBLE);
                    catch1.setVisibility(View.VISIBLE);
                    catch1_text.setVisibility(View.VISIBLE);
                    isopen = true;
                }
            }
        });


        game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 startActivity(new Intent(ChatActivity.this,SpeedUp.class));
            }
        });

        whiteboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this,white_boards.class));
            }
        });

        catch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ChatActivity.this,SplashScreenActivity.class));
            }
        });

        cameraPermissons = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent = getIntent();
        hisUid = intent.getStringExtra("hisUid");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = firebaseDatabase.getInstance();
        userDBR = firebaseDatabase.getReference("Users");

        Query userQuery = userDBR.orderByChild("uid").equalTo(hisUid);

        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    String name =""+ds.child("name").getValue();
                    hisImage = ""+ds.child("image").getValue();
                    String typingStatus = ""+ds.child("typingTo").getValue();

                    if (typingStatus.equals(myUid))
                    {
                        userStatusTv.setText("typing...");

                    }
                    else
                    {
                        String onlineStatus = ""+ds.child("onlineStatus").getValue();

                        if (onlineStatus.equals("online"))
                        {
                            userStatusTv.setText(onlineStatus);
                        }
                        else {
                            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String  dateTime = DateFormat.format("dd/MM/yyyy  hh:mm aa", cal).toString();
                            userStatusTv.setText("Last seen at "+dateTime);

                        }
                    }
                    //userStatusTv.setText();
                    nameTv.setText(name);
                    try {
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_image_white).into(profileIv);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_default_image_white).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String  message = messageEt.getText().toString().trim();
                if (TextUtils.isEmpty(message))
                {
                    Toast.makeText(ChatActivity.this, "cannot send the empty message", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendMessage(message);
                }
                messageEt.setText("");
            }

        });

        attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (s.toString().trim().length()==0)
                {
                    checkTypingStatus("noOne");
                }
                else
                {
                    checkTypingStatus(hisUid);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        readMessages();
        seenMessage();




    }

    private void showImagePickDialog() {
        String[] options = {"Camera","Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("choose Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0)
                {
                    if (!checkCameraPermissions())
                    {
                        requestCameraPermissions();
                    }
                    else
                    {
                        PickFromCamera();
                    }
                }
                if (i==1)
                {
                    if (!checkStoragePermissions())
                    {
                        requestStoragePermissions();
                    }
                    else
                    {
                        PickFromGallery();
                    }
                }
            }
        });
        builder.create().show();
    }

    private void PickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);

    }

    private void PickFromCamera() {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.TITLE,"Temp Pick");
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp description");
        image_rui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,image_rui);
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermissions()
    {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermissions()
    {
        ActivityCompat.requestPermissions(this,storagePermissions,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermissions()
    {
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result&&result1;
    }

    private void requestCameraPermissions()
    {
        ActivityCompat.requestPermissions(this,cameraPermissons,CAMERA_REQUEST_CODE);
    }

    private void seenMessage() {
        UserRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = UserRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid))
                    {
                        HashMap<String, Object> hasSeenHashMap = new HashMap<>();
                        hasSeenHashMap.put("isSeen", true);

                        ds.getRef().updateChildren(hasSeenHashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("Chats");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat.getReceiver().equals(myUid) && chat.getSender().equals(hisUid) ||
                            chat.getReceiver().equals(hisUid) && chat.getSender().equals(myUid))
                    {
                        chatList.add(chat);
                    }
                    adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myUid);
        hashMap.put("receiver", hisUid);
        hashMap.put("message", message);
        hashMap.put("timestamp", timestamp);
        hashMap.put("isSeen", false);
        hashMap.put("type","text");
        databaseReference.child("Chats").push().setValue(hashMap);

        final  DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                Toast.makeText(ChatActivity.this, "gya notification", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);
        chatRef1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {
                    chatRef1.child("id").setValue(hisUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);
        chatRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists())
                {
                    chatRef2.child("id").setValue(myUid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void sendImageMessage(Uri image_rui) throws IOException {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending image ...");
        progressDialog.show();

        String timeStamp = ""+System.currentTimeMillis();

        String fileNameAndPath = "ChatImages/" + "post_"+timeStamp;

        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_rui);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();
        StorageReference ref = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                progressDialog.dismiss();
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());

                String downloadUri = uriTask.getResult().toString();
                if (uriTask.isSuccessful())
                    {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("sender", myUid);
                        hashMap.put("receiver", hisUid);
                        hashMap.put("message", downloadUri);
                        hashMap.put("timestamp", timeStamp);
                        hashMap.put("isSeen",false);
                        hashMap.put("type","image");

                        databaseReference.child("Chats").push().setValue(hashMap);

                        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
                        database.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                ModelUser user = snapshot.getValue(ModelUser.class);

                                Toast.makeText(ChatActivity.this, "gya notification", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist").child(myUid).child(hisUid);
                        chatRef1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists())
                                {
                                    chatRef1.child("id").setValue(hisUid);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("Chatlist").child(hisUid).child(myUid);
                        chatRef2.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (!snapshot.exists())
                                {
                                    chatRef2.child("id").setValue(myUid);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(ChatActivity.this, "ye failed", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void checkUsersStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            myUid = user.getUid();

        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void checkOnlineStatus(String status)
    {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",status);

        dbRef.updateChildren(hashMap);
    }

    private void checkTypingStatus(String typing)
    {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("typingTo",typing);

        dbRef.updateChildren(hashMap);
    }



    @Override
    protected void onStart() {
        checkUsersStatus();
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        String timestamp = String.valueOf(System.currentTimeMillis());
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        UserRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted)
                    {
                        PickFromCamera();
                    } else
                    {
                        Toast.makeText(this, "camera and stoarge permission are necccessary", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        PickFromGallery();
                    } else {
                        Toast.makeText(this, "storage permission ", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {

                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_rui = data.getData();

                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                try {
                    sendImageMessage(image_rui);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUsersStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}