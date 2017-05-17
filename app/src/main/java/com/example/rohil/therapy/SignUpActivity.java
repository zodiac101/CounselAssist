package com.example.rohil.therapy;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends Activity {


    EditText editTextName;
    EditText editTextEmailId;
    EditText editTextPassword;
    EditText editTextTherapistEmailId;

    Button buttonSignUp;

    private FirebaseAuth mAuth;

    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        getActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        editTextEmailId = (EditText)findViewById(R.id.editTextEmailId);
        editTextName = (EditText)findViewById(R.id.editTextName);
        editTextPassword = (EditText)findViewById(R.id.editTextPassword);
        editTextTherapistEmailId = (EditText)findViewById(R.id.editTextTherapistEmailId);

        buttonSignUp = (Button)findViewById(R.id.buttonSignUp);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

    }

    private void attemptSignUp() {

        mAuth.createUserWithEmailAndPassword(editTextEmailId.getText().toString(), editTextPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = null;
                        if (user!=null){
                            uid = user.getUid();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Error.\nTry Again", Toast.LENGTH_SHORT).show();
                        }

                        DatabaseReference myRef = database.getReference(uid).child("info");
                        myRef.child("name").setValue(editTextName.getText().toString());
                        myRef.child("therapistEmailId").setValue(editTextTherapistEmailId.getText().toString());
                        setResult(1);
                        finish();

                        if (!task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Sign Up Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
