package com.grace.customer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.annotation.Annotation;

import static android.widget.Toast.LENGTH_LONG;




public class MainActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    Button signin;
    EditText email, password;
    Button signup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signin = (Button)findViewById(R.id.signin);
        signup=(Button)findViewById(R.id.signup);

        auth = FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_LONG).show();

                String email1 = email.getText().toString();
                String password1 = password.getText().toString();
                if (email1.isEmpty() || password1.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill in the email and password", Toast.LENGTH_LONG).show();
                }
                else{
                    auth.signInWithEmailAndPassword(email1, password1).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Welocome ", Toast.LENGTH_LONG ).show();
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            }else {
                                Toast.makeText( MainActivity.this, "Incorrect email or password", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Signup.class));

            }
        });
    }
}
