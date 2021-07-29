package com.example.socalio;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {


    Button mRegisterBtn;
    EditText mEmailEt, mPasswordEt;

    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("create account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mRegisterBtn = findViewById(R.id.button);
        mEmailEt = findViewById(R.id.email);
        mPasswordEt = findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");

        mRegisterBtn.setOnClickListener(view -> {
            String email = mEmailEt.getText().toString().trim();
            String password = mPasswordEt.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                mEmailEt.setError("Invalid");
                mEmailEt.setFocusable(true);

            }
            else if (password.length()<6)
            {
                mPasswordEt.setError("password length must be 6");
                mPasswordEt.setFocusable(true);
            }
            else
            {
                registerUser(email,password);
            }
        });
    }
    private void registerUser(String email, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if(task.isSuccessful())
            {
                progressDialog.dismiss();
                FirebaseUser user = mAuth.getCurrentUser();

                assert user != null;
                String email1 = user.getEmail();
                String uid  = user.getUid();

                HashMap<Object, String> hashMap = new HashMap<>();

                hashMap.put("email", email1);
                hashMap.put("uid",uid);
                hashMap.put("name","");
                hashMap.put("onlineStatus","online");
                hashMap.put("typingTo","noOne");
                hashMap.put("phone","");
                hashMap.put("image","");
                hashMap.put("cover","");

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("Users");
                reference.child(uid).setValue(hashMap);

                Toast.makeText(RegisterActivity.this, "registered... "+user.getEmail(), Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                finish();
            }
            else
            {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "authentication failed", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}