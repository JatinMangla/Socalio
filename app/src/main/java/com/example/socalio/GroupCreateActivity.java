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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;

public class GroupCreateActivity extends AppCompatActivity {

    private  static final int CAMERA_REQUEST_CODE = 100;
    private  static final int STORAGE_REQUEST_CODE = 200;

    private static final int IMAGE_PICK_CAMERA_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;

    String[] cameraPermissons;
    String[] storagePermissions;
    ProgressDialog pd;

    Uri image_rui = null;

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    private ImageView grpIconIv;
    private EditText grpTitleEt, grpDescriptionEt;
    private FloatingActionButton createGrpBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Create Group");

        grpIconIv = findViewById(R.id.grpIconIv);
        grpTitleEt = findViewById(R.id.grpTitleEt);
        grpDescriptionEt = findViewById(R.id.grpDescriptionEt);
        createGrpBtn = findViewById(R.id.createGrpBtn);

        cameraPermissons = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();

        grpIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });

        createGrpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCreatingGrp();
            }
        });
    }

    private void startCreatingGrp() {
        pd  = new ProgressDialog(this);
        pd.setMessage("Creating Group");

        final String grpTitle = grpTitleEt.getText().toString().trim();
        final String grpDescription = grpDescriptionEt.getText().toString().trim();

        if (TextUtils.isEmpty(grpTitle))
        {
            Toast.makeText(this, "please enter thr group title", Toast.LENGTH_SHORT).show();
            return;
        }

        pd.show();
        String g_timestamp = ""+System.currentTimeMillis();
        if (image_rui == null)
        {
            createGroup(""+g_timestamp,""+grpTitle,""+grpDescription,"");
        }
        else
        {
            String fileNameAndPath = "Group_Imgs/"+"image"+g_timestamp;
            StorageReference reference = FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
            reference.putFile(image_rui).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Task<Uri> p_uriTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!p_uriTask.isSuccessful());
                    Uri p_downloadUri = p_uriTask.getResult();


                    if (p_uriTask.isSuccessful())
                    {
                        createGroup(""+g_timestamp, ""+grpTitle, ""+grpDescription, ""+p_downloadUri);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createGroup(String g_timestamp , String grpTitle , String grpDescription, String grpIcon) {
        HashMap<String, String > hashMap = new HashMap<>();
        hashMap.put("groupId",""+g_timestamp);
        hashMap.put("grpDescription",""+grpDescription);
        hashMap.put("groupTitle",""+grpTitle);
        hashMap.put("groupIcon",""+grpIcon);
        hashMap.put("timestamp",g_timestamp);
        hashMap.put("createdBy",""+firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(g_timestamp).setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused)
            {


                HashMap<String, String> hashMap1 = new HashMap<>();
                hashMap1.put("uid",firebaseAuth.getUid());
                hashMap1.put("role","creator");
                hashMap1.put("timestamp",g_timestamp);

                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");
                ref1.child(g_timestamp).child("Participants").child(firebaseAuth.getUid()).setValue(hashMap1)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                pd.dismiss();
                                Toast.makeText(GroupCreateActivity.this, "grp created", Toast.LENGTH_SHORT).show();
                                //startActivity(new Intent(GroupCreateActivity.this,GroupChatActivity.class));
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private void checkUser() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null)
        {
            actionBar.setSubtitle(user.getEmail());

        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
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
                grpIconIv.setImageURI(image_rui);

            }
            else if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                grpIconIv.setImageURI(image_rui);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}