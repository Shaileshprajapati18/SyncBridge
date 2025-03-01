package com.example.myapplication.Activites;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.Model.DatabaseHelper;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class profile_settings extends AppCompatActivity {

    TextView Firstname,Lastname,Phonenumber,Email;
    ImageView arrow_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_settings);


        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

        arrow_back=findViewById(R.id.arrow_back);
        Firstname=findViewById(R.id.firstname);
        Lastname=findViewById(R.id.lastname);
        Phonenumber=findViewById(R.id.phonenumber);
        Email=findViewById(R.id.email);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userEmail = null;
                    try {
                        userEmail = userSnapshot.child("email").getValue(String.class);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    if (currentUserEmail != null && currentUserEmail.equals(userEmail)) {
                        userFound = true;

                        String firstname = userSnapshot.child("firstname").getValue(String.class);
                        String lastname = userSnapshot.child("lastname").getValue(String.class);
                        String phoneNumber = userSnapshot.child("phoneNumber").getValue(String.class);
                        String email = userSnapshot.child("email").getValue(String.class);

                        Firstname.setText(firstname);
                        Lastname.setText(lastname);
                        Phonenumber.setText(phoneNumber);
                        Email.setText(email);

                        break;
                    }
                }

                if (!userFound) {
                    Firstname.setText("firstname");
                    Lastname.setText("lastname");
                    Phonenumber.setText("phoneNumber");
                    Email.setText("email");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profile_settings.this, "Data not retrieved", Toast.LENGTH_SHORT).show();
            }
        });

       arrow_back.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();
           }
       });
    }
}