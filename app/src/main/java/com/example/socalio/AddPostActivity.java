package com.example.socalio;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    ActionBar actionBar;
    FirebaseAuth firebaseAuth;
    DatabaseReference userdbref;

    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBt;

    Uri image_rui = null;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissons;
    String[] storagePermissions;

    String name,email,uid,dp;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        cameraPermissons = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        pd = new ProgressDialog(this);

        firebaseAuth = firebaseAuth.getInstance();
        checkUsersStatus();
        actionBar.setSubtitle(email);

        userdbref = FirebaseDatabase.getInstance().getReference("Users");
        Query query = userdbref.orderByChild("email").equalTo(email);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    name = ""+ds.child("name").getValue();
                    email = ""+ds.child("email").getValue();
                    dp = ""+ds.child("image").getValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        uploadBt = findViewById(R.id.pUploadBt);
        imageIv = findViewById(R.id.pTmageIv);

        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickDialog();
            }
        });

        uploadBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
                if (TextUtils.isEmpty(title))
                {
                    Toast.makeText(AddPostActivity.this, "enter title...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(description))
                {
                    Toast.makeText(AddPostActivity.this, "enter description", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (image_rui == null)
                {
                    uploadData(title,description,"noImage");
                }
                else
                {
                    uploadData(title,description,String.valueOf(image_rui));
                }
            }
        });
    }

    private void uploadData(final String title, final String description, String uri) {
        pd.setMessage("Publishing post..");
        pd.show();

        final String timestamp = String.valueOf(System.currentTimeMillis());

        String filePathAndName = "Posts/" + "post..." +timestamp;
        if (!uri.equals("noImage"))
        {
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filePathAndName);
            reference.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!uriTask.isSuccessful());

                    String downloadUri = uriTask.getResult().toString();
                    //String pLikes = "0";

                    if (uriTask.isSuccessful())
                    {
                        HashMap<Object,String > hashMap = new HashMap<>();
                        hashMap.put("uId",uid);
                        hashMap.put("uName",name);
                        hashMap.put("uEmail",email);
                        hashMap.put("uDp",dp);
                        hashMap.put("pTitle",title);
                        hashMap.put("pId",timestamp);
                        hashMap.put("pDescr",description);
                        hashMap.put("pImage",downloadUri);
                        hashMap.put("pTime",timestamp);
                        hashMap.put("pLikes","0");
                        hashMap.put("pComments","0");

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();

                                titleEt.setText("");
                                descriptionEt.setText("");
                                imageIv.setImageURI(null);
                                image_rui = null;
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            HashMap<Object,String >hashMap = new HashMap<>();
            hashMap.put("uId",uid);
            hashMap.put("uName",name);
            hashMap.put("uEmail",email);
            hashMap.put("uDp",dp);
            hashMap.put("pTitle",title);
            hashMap.put("pId",timestamp);
            hashMap.put("pDesc",description);
            hashMap.put("pImage","noImage");
            hashMap.put("pTime",timestamp);
            hashMap.put("pLikes","0");
            hashMap.put("pComments","0");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
            ref.child(timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();

                    titleEt.setText("");
                    descriptionEt.setText("");
                    imageIv.setImageURI(null);
                    image_rui = null;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(AddPostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        checkUsersStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUsersStatus();
    }

    private void checkUsersStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            email = user.getEmail();
            uid = user.getUid();

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
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
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

                imageIv.setImageURI(image_rui);
            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                imageIv.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}