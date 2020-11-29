package com.example.w2w_texi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CustomerLoginActivity2 extends AppCompatActivity {

    private Button CustomerLoginBtn;
    private Button CustomerRegisterBtn;
    private TextView CustomerStatus;
    private TextView CustomerRegisterLink;
    private EditText Email_Customer;
    private EditText Pass_Customer;
    private FirebaseAuth mAuth;
    private ProgressDialog Loadingbar;
    private DatabaseReference CustomerDataBaseRef;
    private  String OnlineCustomerID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login2);
        FirebaseApp.initializeApp(getApplicationContext());

        mAuth=FirebaseAuth.getInstance();


        CustomerLoginBtn=(Button) findViewById(R.id.login_btn);
        CustomerRegisterBtn=(Button) findViewById(R.id.register_btn);
        CustomerStatus=(TextView) findViewById(R.id.Customer_status);
        CustomerRegisterLink=(TextView) findViewById(R.id.register_link);
        Email_Customer=(EditText) findViewById(R.id.Email);
        Pass_Customer=(EditText) findViewById(R.id.Password);
        Loadingbar=new ProgressDialog(this);

        CustomerRegisterBtn.setVisibility(View.INVISIBLE);
        CustomerRegisterBtn.setEnabled(false);


        CustomerRegisterLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomerLoginBtn.setVisibility(View.INVISIBLE);
                CustomerRegisterLink.setVisibility(View.INVISIBLE);
                CustomerStatus.setText("Register Customer");

                CustomerRegisterBtn.setVisibility(View.VISIBLE);
                CustomerRegisterBtn.setEnabled(true);

            }
        });



        CustomerRegisterBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String Email=Email_Customer.getText().toString();
                String Pass=Pass_Customer.getText().toString();
                RegisterCustomer(Email,Pass);
            }
        });

        CustomerLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Email=Email_Customer.getText().toString();
                String Pass=Pass_Customer.getText().toString();
                SigInCustomer(Email,Pass);


            }
        });

    }
    private void SigInCustomer(String email, String pass) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(CustomerLoginActivity2.this,"Please write Email...",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(CustomerLoginActivity2.this,"Please write Password...",Toast.LENGTH_SHORT).show();


        }
        else{

            Loadingbar.setTitle("Customer login");
            Loadingbar.setMessage("Please wait, while we are checking your credienties....");
            Loadingbar.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        Intent CustomerIntent=new Intent(CustomerLoginActivity2.this,CustomersMapActivity.class);
                        startActivity(CustomerIntent);

                        Toast.makeText(CustomerLoginActivity2.this,"Customer logged in successfull...",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();
                    }
                    else{
                        Toast.makeText(CustomerLoginActivity2.this,"Login unsuccessfull...,Please try again later",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();
                    }

                }
            });
        }

    }

    private void RegisterCustomer(String email, String pass) {
        if(TextUtils.isEmpty(email)){
            Toast.makeText(CustomerLoginActivity2.this,"Please write Email...",Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(pass)){
            Toast.makeText(CustomerLoginActivity2.this,"Please write Password...",Toast.LENGTH_SHORT).show();


        }
        else{

            Loadingbar.setTitle("Customer registration");
            Loadingbar.setMessage("Please wait, while we are register your data....");
            Loadingbar.show();
            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        OnlineCustomerID = mAuth.getCurrentUser().getUid();
                        CustomerDataBaseRef = FirebaseDatabase.getInstance().getReference().child("User").child("Customer").child(OnlineCustomerID);

                        CustomerDataBaseRef.setValue(true);

                        Intent DriverIntent = new Intent(CustomerLoginActivity2.this,CustomersMapActivity.class);

                        Toast.makeText(CustomerLoginActivity2.this,"Customer register successfull...",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();


                    }
                    else{
                        Toast.makeText(CustomerLoginActivity2.this,"Registration unsuccessfull...,Please try again later",Toast.LENGTH_SHORT).show();
                        Loadingbar.dismiss();
                    }

                }
            });
        }
    }

}