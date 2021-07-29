package com.example.socalio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccntTv, mRecoverPassTv;
    Button mLoginBtn;

    private FirebaseAuth mAuth;

    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login account");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mEmailEt = findViewById(R.id.email);
        mPasswordEt = findViewById(R.id.password);
        mLoginBtn = findViewById(R.id.button);
        notHaveAccntTv = findViewById(R.id.textView);
        mRecoverPassTv = findViewById(R.id.forget_password);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    mEmailEt.setError("Invalid Email");
                    mEmailEt.setFocusable(true);
                }
                else
                {
                    loginUser(email,passw);
                }
            }
        });

        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPassworDailog();
            }
        });


        pd = new ProgressDialog(this);
    }

    private void showRecoverPassworDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("recover password");
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);
        emailEt.setHint("email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);

        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        builder.setPositiveButton("recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });
        builder.create().show();

    }


    private void beginRecovery(String email) {
        pd.setMessage("SENDING EMAIL.....");
        pd.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "email sent", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }


    private void loginUser(String email, final String passw) {
        pd.setMessage("LOGGIN");
        pd.show();
        mAuth.signInWithEmailAndPassword(email,passw).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    pd.dismiss();
                    FirebaseUser user = mAuth.getCurrentUser();
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish();
                }
                else
                {
                    pd.dismiss();
                    Toast.makeText(LoginActivity.this, "authentication failed", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}