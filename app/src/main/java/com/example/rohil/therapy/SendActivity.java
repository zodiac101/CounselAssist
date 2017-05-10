package com.example.rohil.therapy;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SendActivity extends Activity {

    FirebaseDatabase database;
    DatabaseReference databaseReference;

    AlertDialog.Builder alertDialog;

    String uid;
    String therapistEmailId;
    String name;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==3){
            alertDialog.show();
            //finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        therapistEmailId = intent.getStringExtra("therapistEmailId");
        name = intent.getStringExtra("name");


        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child(uid).child("report");


        String rootPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath()+"/therapy/";
        File dir = new File(rootPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        final File file = new File(rootPath, "test1.csv");

        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final CSVWriter writer;

        try {
            writer = new CSVWriter(new FileWriter(file), ',');
            writer.writeNext(new String[]{"DateTime", "Place", "Trigger", "Thoughts", "Reaction"});
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String[] stringArray1 = new String[5];
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                        stringArray1[0] = snapshot.child("DateTime").getValue().toString();
                        stringArray1[1] = snapshot.child("Place").getValue().toString();
                        stringArray1[2] = snapshot.child("Trigger").getValue().toString();
                        stringArray1[3] = snapshot.child("Thoughts").getValue().toString();
                        stringArray1[4] = snapshot.child("Reaction").getValue().toString();
                        writer.writeNext(stringArray1);
                    }
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    Uri uri = FileProvider.getUriForFile(SendActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{therapistEmailId});
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_generic_text)+name);
                    intent.setType("text/csv");
                    startActivityForResult(intent, 3);


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }


        alertDialog = new AlertDialog.Builder(SendActivity.this);

        alertDialog.setMessage("Delete Old reports?");

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {

                Toast.makeText(getApplicationContext(), uid, Toast.LENGTH_SHORT).show();
                databaseReference.setValue(null);
                finish();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                dialog.cancel();
                finish();
            }
        });


    }




}

