package com.example.rohil.therapy;

import android.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {


    EditText editTextPlace;
    EditText editTextTrigger;
    EditText editTextThoughts;
    EditText editTextReaction;
    Button buttonAdd;

    String place;
    String trigger;
    String thoughts;
    String reaction;
    String dateTime;

    String therapistEmailId;
    String name;
    String uid;

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                    uid = user.getUid();

                    databaseReference.child(user.getUid()).child("info").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            therapistEmailId = dataSnapshot.child("therapistEmailId").getValue().toString();
                            name = dataSnapshot.child("name").getValue().toString();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
            }
        };



        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck!= PackageManager.PERMISSION_GRANTED){

            int i = 0;
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, i);
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck!= PackageManager.PERMISSION_GRANTED){

            int i = 0;
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, i);
        }


        editTextPlace  = (EditText)findViewById(R.id.editTextPlace);
        editTextTrigger = (EditText)findViewById(R.id.editTextTrigger);
        editTextThoughts = (EditText)findViewById(R.id.editTextThoughts);
        editTextReaction = (EditText)findViewById(R.id.editTextReaction);
        buttonAdd = (Button)findViewById(R.id.buttonAdd);

        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference();

        editTextReaction.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i== EditorInfo.IME_ACTION_DONE){
                    buttonAdd.callOnClick();
                    return true;
                }
                return false;
            }
        });


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                place = editTextPlace.getText().toString();
                trigger = editTextTrigger.getText().toString();
                thoughts = editTextThoughts.getText().toString();
                reaction = editTextReaction.getText().toString();

                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM HH:mm");
                dateTime = df.format(c.getTime());

                databaseReference.child(uid).child("report").child(dateTime).child("DateTime").setValue(dateTime);
                databaseReference.child(uid).child("report").child(dateTime).child("Place").setValue(place);
                databaseReference.child(uid).child("report").child(dateTime).child("Trigger").setValue(trigger);
                databaseReference.child(uid).child("report").child(dateTime).child("Thoughts").setValue(thoughts);
                databaseReference.child(uid).child("report").child(dateTime).child("Reaction").setValue(reaction);

                editTextPlace.setText("");
                editTextTrigger.setText("");
                editTextReaction.setText("");
                editTextThoughts.setText("");

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == R.id.item_send){

            Intent intent = new Intent(getApplicationContext(), SendActivity.class);
            intent.putExtra("uid", uid);
            intent.putExtra("therapistEmailId", therapistEmailId);
            intent.putExtra("name", name);
            startActivity(intent);
            return true;
        }

        if (item.getItemId() == R.id.item_SignOut){
            mAuth.signOut();
        }
        return  false;
    }
}
