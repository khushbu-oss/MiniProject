package com.example.w2w_texi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DriverLoginActivity extends AppCompatActivity {

    private Button DriverLoginBtn;
    private Button DriverRegisterBtn;
    private TextView DriverStatus;
    private TextView DriverRegisterLink;
    private EditText Email_Driver;
    private EditText Pass_Driver;
    private FirebaseAuth mAuth;
    private ProgressDialog Loadingbar;

    private DatabaseReference DriverDataBaseRef;
    private  String OnlineDriverID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        mAuth=FirebaseAuth.getInstance();


        DriverLoginBtn=(Button) findViewById(R.id.login_btn);
        DriverRegisterBtn=(Button) findViewById(R.id.register_btn);
        DriverStatus=(TextView) findViewById(R.id.Driver_status);
        DriverRegisterLink=(TextView) findViewById(R.id.register_link);
        Email_Driver=(EditText) findViewById(R.id.Email);
        Pass_Driver=(EditText) findViewById(R.id.Password);
        Loadingbar=new ProgressDialog(this);


        DriverRegisterBtn.setVisibility(View.INVISIBLE);
        DriverRegisterBtn.setEnabled(false);


        DriverRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DriverLoginBtn.setVisibility(View.INVISIBLE);
                DriverRegisterLink.setVisibility(View.INVISIBLE);
                DriverStatus.setText("Register Driver");

                DriverRegisterBtn.setVisibility(View.VISIBLE);
                DriverRegisterBtn.setEnabled(true);

            }
        });



        DriverRegisterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String Email=Email_Driver.getText().toString();
                String Pass=Pass_Driver.getText().toString();
                RegisterDriver(Email,Pass);
            }
        });

        DriverLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email=Email_Driver.getText().toString();
                String Pass=Pass_Driver.getText().toString();
                SigInDriver(Email,Pass);


            }
        });


    }
    private void SigInDriver(String email, String pass) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(DriverLoginActivity.this,"Please write Email...",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(DriverLoginActivity.this,"Please write Password...",Toast.LENGTH_SHORT).show();


        }
        else{

            Loadingbar.setTitle("Driver login");
            Loadingbar.setMessage("Please wait, while we are checking your credienties....");
            Loadingbar.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){



                        Intent DriverIntent=new Intent(DriverLoginActivity.this,DriversMapActivity.class);
                        startActivity(DriverIntent);
                        Toast.makeText(DriverLoginActivity.this,"Driver logged in successfull...",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();




                    }
                    else{
                        Toast.makeText(DriverLoginActivity.this,"Login unsuccessfull...,Please try again later",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();
                    }

                }
            });
        }

    }

    private void RegisterDriver(String email, String pass) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(DriverLoginActivity.this,"Please write Email...",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(DriverLoginActivity.this,"Please write Password...",Toast.LENGTH_SHORT).show();


        }
        else{

            Loadingbar.setTitle("Driver registration");
            Loadingbar.setMessage("Please wait, while we are register your data....");
            Loadingbar.show();
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        OnlineDriverID = mAuth.getCurrentUser().getUid();
                        DriverDataBaseRef = FirebaseDatabase.getInstance().getReference().child("User").child("Customer").child(OnlineDriverID);
                        DriverDataBaseRef.setValue(true);

                        Toast.makeText(DriverLoginActivity.this,"Driver register successfull...",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();
                        Intent DriverIntent=new Intent(DriverLoginActivity.this,DriversMapActivity.class);
                        startActivity(DriverIntent);


                    }
                    else{
                        Toast.makeText(DriverLoginActivity.this,"Registration unsuccessfull...,Please try again later",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();
                    }

                }
            });
        }
    }

}