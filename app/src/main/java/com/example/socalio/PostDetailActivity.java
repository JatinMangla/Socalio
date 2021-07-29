package com.example.socalio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import adapters.AdapterComments;
import models.ModelComment;

public class PostDetailActivity extends AppCompatActivity {

    String hisUid,myUid, myEmail,myName , myDp, postId, pLikes,hisDp,hisName, pImage;

    boolean mProcessComment = false;
    boolean mProcessLike = false;

    ProgressDialog pd;

    ImageView uPictureIv, pImageIv;
    TextView uNameTv, pTimeTv,pTitleTv, pDescriptionTv, pLikesTv,pCommentsTv;
    ImageButton moreBtn;
    Button likeBtn, shareBtn;
    LinearLayout profileLayout;

    RecyclerView recyclerView;

    List<ModelComment> commentList;
    AdapterComments adapterComments;

    EditText commentEt;
    ImageButton sendBtn;
    ImageView cAvatarIv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Post Details");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        uPictureIv = findViewById(R.id.uPictureIv);
        pImageIv = findViewById(R.id.pImageIv);
        uNameTv = findViewById(R.id.uNameTv);
        pTimeTv = findViewById(R.id.pTimeTv);
        pTitleTv = findViewById(R.id.pTitleTv);
        pDescriptionTv = findViewById(R.id.pDescriptionTv);
        pLikesTv = findViewById(R.id.pLikesTv);
        pCommentsTv = findViewById(R.id.pCommentsTv);
        moreBtn = findViewById(R.id.moreBtn);
        likeBtn = findViewById(R.id.likeBtn);
        shareBtn = findViewById(R.id.shareBtn);
        profileLayout = findViewById(R.id.profileLayout);
        recyclerView = findViewById(R.id.recyclerView);

        commentEt = findViewById(R.id.commentEt);
        sendBtn = findViewById(R.id.sendBtnn);
        cAvatarIv = findViewById(R.id.cAvatarIv);

        loadPostInfo();
        checkUserStatus();

        loadUserInfo();

        setLikes();

        actionBar.setSubtitle("Signed as: "+myEmail);

        loadComments();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        likeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                likePost();
            }
        });

        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions();
            }
        });

    }

    private void loadComments() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                commentList.clear();
                for (DataSnapshot ds: snapshot.getChildren())
                {

                    ModelComment modelComment = ds.getValue(ModelComment.class);

                    commentList.add(modelComment);

                    adapterComments = new AdapterComments(getApplicationContext(),commentList);

                    recyclerView.setAdapter(adapterComments);
                }
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    private void showMoreOptions() {
        PopupMenu popupMenu  = new PopupMenu(this,moreBtn, Gravity.END);

        if (hisUid.equals(myUid))
        {
            popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
        }


        //popupMenu.getMenu().add(Menu.NONE,0,0,"Delete");
        popupMenu.getMenu().add(Menu.NONE,2,0,"View details");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id==0)
                {
                    beginDelete();
                }

                return false;
            }
        });
        popupMenu.show();
    }

    private void beginDelete() {
        if (pImage.equals("noImage"))
        {
            deleteWithoutImage();
        }
        else
        {
            deleteWithImage();
        }
    }

    private void deleteWithImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting....");

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Query fquery =FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
                fquery.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren() )
                        {
                            ds.getRef().removeValue();
                        }
                        Toast.makeText(PostDetailActivity.this, "Deleted succesfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();;
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Deleting....");

        Query fquery =FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren() )
                {
                    ds.getRef().removeValue();
                }
                Toast.makeText(PostDetailActivity.this, "Deleted succesfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setLikes() {
        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postId).hasChild(myUid))
                {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_liked,0,0,0);
                    likeBtn.setText("Liked");
                }
                else
                {
                    likeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_black,0,0,0);
                    likeBtn.setText("Like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likePost() {
        mProcessLike = true;

        DatabaseReference likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessLike)
                {
                    if (snapshot.child(postId).hasChild(myUid))
                    {
                        postsRef.child(postId).child("pLikes").setValue(""+ (Integer.parseInt(pLikes)-1));
                        likesRef.child(postId).child(myUid).removeValue();
                        mProcessLike = false;

                    }
                    else
                    {
                        postsRef.child(postId).child("pLikes").setValue(""+(Integer.parseInt(pLikes)+1));
                        likesRef.child(postId).child(myUid).setValue("Liked");
                        mProcessLike = false;

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void postComment() {
        pd = new ProgressDialog(this);
        pd.setMessage("Adding Comment...");

        String comment = commentEt.getText().toString().trim();
        if (TextUtils.isEmpty(comment))
        {
            Toast.makeText(this, "comment is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        String timeStamp = String.valueOf(System.currentTimeMillis());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId).child("Comments");

        HashMap<String , Object> hashMap = new HashMap<>();
        hashMap.put("cId", timeStamp);
        hashMap.put("comment",comment);
        hashMap.put("timestamp", timeStamp);
        hashMap.put("uid",myUid);
        hashMap.put("uEmail",myEmail);
        hashMap.put("uDp",myDp);
        hashMap.put("uName",myName);

        ref.child(timeStamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                pd.dismiss();
                Toast.makeText(PostDetailActivity.this, "comment added...", Toast.LENGTH_SHORT).show();
                commentEt.setText("");
                updateCommentCount();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
               pd.dismiss();
                Toast.makeText(PostDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void updateCommentCount() {
        mProcessComment = true;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mProcessComment)
                {
                    String comments = ""+snapshot.child("pComments").getValue();
                    int newCommentVal = Integer.parseInt(comments) + 1;
                    ref.child("pComments").setValue(""+newCommentVal);
                    mProcessComment = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadUserInfo() {
        Query myRef = FirebaseDatabase.getInstance().getReference("Users");
        myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    myName = ""+ds.child("name").getValue();
                    myDp = ""+ds.child("image").getValue();

                    try {
                        Picasso.get().load(myDp).placeholder(R.drawable.ic_default_image_white).into(cAvatarIv);

                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_default_image_white).into(cAvatarIv);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadPostInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        Query query = ref.orderByChild("pId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren())
                {
                    String pTitle = ""+ds.child("pTitle").getValue();
                    String pDescr = ""+ds.child("pDescr").getValue();
                    pLikes = ""+ds.child("pLikes").getValue();
                    String pTimeStamp = ""+ds.child("pTime").getValue();
                    pImage =""+ds.child("pImage").getValue();
                    hisDp =""+ds.child("uDp").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String uEmail = ""+ds.child("uEamil").getValue();
                    hisName = ""+ds.child("uName").getValue();
                    String commentCount = ""+ds.child("pComments").getValue();

                    Calendar calendar = Calendar.getInstance(Locale.getDefault());
                    calendar.setTimeInMillis(Long.parseLong(pTimeStamp));
                    String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

                    pTitleTv.setText(pTime);
                    pDescriptionTv.setText(pDescr);
                    pLikesTv.setText(pLikes+" Likes");
                    pTimeTv.setText(pTime);
                    pCommentsTv.setText(commentCount+" Comments");

                    uNameTv.setText(hisName);

                    if (pImage.equals("noImage"))
                    {
                        pImageIv.setVisibility(View.GONE);
                    }
                    else
                    {
                        pImageIv.setVisibility(View.VISIBLE);

                        try {
                            Picasso.get().load(pImage).into(pImageIv);
                        }
                        catch (Exception e)
                        {


                        }
                    }

                    try {
                        Picasso.get().load(hisDp).placeholder(R.drawable.ic_default_image_white).into(uPictureIv);
                    }
                    catch (Exception e)
                    {
                        Picasso.get().load(R.drawable.ic_default_image_white).into(uPictureIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!= null)
        {
            myEmail = user.getEmail();
            myUid = user.getUid();

        }
        else
        {
            startActivity(new Intent(this,MainActivity.class));
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

        getMenuInflater().inflate(R.menu.menu_main,menu);
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_groupInfo).setVisible(false);
        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}