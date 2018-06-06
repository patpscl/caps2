package com.parkpal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GetNameActivity extends AppCompatActivity {

    private EditText addUserName;
    private Button buttonSetUserName;

    private  DatabaseReference databaseUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_name);

        databaseUser = FirebaseDatabase.getInstance().getReference("users");


        addUserName = findViewById(R.id.field_verification_code);
        buttonSetUserName = findViewById(R.id.button_submit_name);

        buttonSetUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addName();
            }
        });
    }

    private void addName(){
        String tempName = addUserName.getText().toString().trim();
        String s = getIntent().getStringExtra("SESSION_USER_ID");
        if(!TextUtils.isEmpty(tempName)){
            databaseUser.child(s).child("name").setValue(tempName);

            Intent intent = new Intent(GetNameActivity.this,DrawerActivity.class);
            startActivity(intent);
            finish();

        }else{
            Toast.makeText(this,"You should enter a name",Toast.LENGTH_LONG).show();
        }
    }
}
