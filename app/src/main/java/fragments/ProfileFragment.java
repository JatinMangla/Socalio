package fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socalio.AddPostActivity;
import com.example.socalio.MainActivity;
import com.example.socalio.R;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import adapters.AdapterPosts;
import models.ModelPost;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    StorageReference storageReference;
    String storagePath = "Users_Profile_Cover_Imgs/";

    ImageView avatarTv,coverIv;
    TextView nameTv,emailTv,phoneTv;
    FloatingActionButton fab;

    RecyclerView postRecyclerView;

    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    String[] cameraPermission;
    String[] storagePermission;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    Uri image_uri;

    String profileOrCoverPhoto;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        avatarTv = view.findViewById(R.id.avatarIv);
        nameTv =view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        coverIv = view.findViewById(R.id.coverIv);
        fab =view.findViewById(R.id.fab);
        postRecyclerView =view.findViewById(R.id.recycleviewposts);

        pd = new ProgressDialog(getActivity());


        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
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

        fab.setOnClickListener(view1 -> showEditProfileDialog());


        postList = new ArrayList<>();

        checkUsersStatus();
        loadMyPosts();


        return view;
    }

    private void loadMyPosts() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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

                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void searchMyPosts(final String searchQuery) {


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
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

                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    postRecyclerView.setAdapter(adapterPosts);
                }
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private  boolean checkStoragePermission()
    {
        return ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
    }

    private void requestStoragePermission()
    {
        requestPermissions(storagePermission,STORAGE_REQUEST_CODE);
    }

    private  boolean checkCameraPermission()
    {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return  result && result1;
    }

    private void requestCameraPermission()
    {
        requestPermissions(cameraPermission,CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        String options[] = {"Edit propfile picture","Edit Cover Photo","Edit Name","Edit phone"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose action");

        builder.setItems(options, (dialogInterface, i) -> {
            if (i == 0)
            {
                pd.setMessage("uploading profile picture");
                profileOrCoverPhoto =  "image";
                showImagePicDialog();
            }
            else if (i==1)
            {
                pd.setMessage("\n updating cover photo");
                profileOrCoverPhoto= "cover";
                showImagePicDialog();
            }
            else if (i==2)
            {
                pd.setMessage("updating Name");
                showNamePhoneUpdateDialog("name");
            }
            else if (i==3)
            {
                pd.setMessage("updating Phone");
                showNamePhoneUpdateDialog("phone");
            }
        });
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String Key) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("update "+ Key);
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        EditText editText = new EditText(getActivity());
        editText.setHint("enter "+Key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("update", (dialogInterface, i) -> {
            String value = editText.toString().trim();
            if (!TextUtils.isEmpty(value))
            {
                pd.show();
                HashMap<String, Object> result = new HashMap<>();
                result.put(Key,value);

                databaseReference.child(user.getUid()).updateChildren(result).addOnSuccessListener(aVoid -> {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "upadte....", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
                    if (Key.equals("name"))
                    {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren())
                                {
                                    String child = ds.getKey();
                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren())
                                {
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments"))
                                    {
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds: snapshot.getChildren())
                                                {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull  DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull  DatabaseError error) {

                            }
                        });
                    }
            }
            else {
                Toast.makeText(getActivity(), "enter "+Key, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("cancel", (dialog, i) -> dialog.dismiss());
        builder.create().show();
    }

    private void showImagePicDialog() {

        String options[] = {"Camera","Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");

        builder.setItems(options, (dialogInterface, i) -> {
            if (i == 0)
            {
                if (!checkCameraPermission())
                {
                    requestCameraPermission();
                }
                else
                {
                    pickFromCamera();
                }
            }
            else if (i==1)
            {
                if (!checkStoragePermission())
                {
                    requestStoragePermission();
                }
                else
                {
                    pickFromGallery();
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case  CAMERA_REQUEST_CODE:{
                if (grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults[0]  == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if( cameraAccepted && writeStorageAccepted)
                    {
                        pickFromCamera();
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "please enable the camera", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case  STORAGE_REQUEST_CODE: {
                if (grantResults.length>0)
                {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if( writeStorageAccepted)
                    {
                        pickFromGallery();
                    }
                    else
                    {
                        Toast.makeText(getActivity(), "please enable the storage", Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK)
        {
            if (requestCode == IMAGE_PICK_GALLERY_CODE)
            {
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE)
            {
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(final Uri uri) {
        pd.show();
        String filePAthAndName = storagePath+""+profileOrCoverPhoto+"_"+user.getUid();
        StorageReference storageReference2nd = storageReference.child(filePAthAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final Uri downloadUri = uriTask.getResult();

                if (uriTask.isSuccessful())
                {
                    HashMap<String,Object> results = new HashMap<>();
                    results.put(profileOrCoverPhoto,downloadUri.toString());

                    databaseReference.child(user.getUid()).updateChildren(results).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "image updated...", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Error updating Image....", Toast.LENGTH_SHORT).show();
                        }
                    });

                    if (profileOrCoverPhoto.equals("image"))
                    {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren())
                                {
                                    String  child = ds.getKey();
                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                                for (DataSnapshot ds: snapshot.getChildren())
                                {
                                    String child = ds.getKey();
                                    if (snapshot.child(child).hasChild("Comments"))
                                    {
                                        String child1 = ""+snapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                for (DataSnapshot ds: snapshot.getChildren())
                                                {
                                                    String child = ds.getKey();
                                                    snapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull  DatabaseError error) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull  DatabaseError error) {

                            }
                        });

                    }
                }
                else {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "some error occured", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera()
    {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");

        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void checkUsersStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            uid=user.getUid();
        } else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupInfo).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!TextUtils.isEmpty(s))
                {
                    searchMyPosts(s);
                }
                else
                {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!TextUtils.isEmpty(s))
                {
                    searchMyPosts(s);
                }
                else
                {
                    loadMyPosts();
                }
                return false;
            }
        });

       super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUsersStatus();
        }
        if (id == R.id.action_add_post)
        {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}