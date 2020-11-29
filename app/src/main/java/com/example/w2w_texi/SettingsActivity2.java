package com.example.w2w_texi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity2 extends AppCompatActivity{
    private String GetType;

    private CircleImageView ProfileImageView;
    private EditText NameEditText,PhoneEditText,DriverCarName;
    private ImageView Closebtn,SaveBtn;
    private TextView ProfileChangebtn;

    private DatabaseReference DataBaseRef;
    private FirebaseAuth mAuth;


    private String Checker = "";
    private Uri ImageUri;
    private String MyUri="";
    private StorageTask uploadtask;
    private StorageReference StorageProfilePicsRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        GetType = getIntent().getStringExtra("Type");
        Toast.makeText(this, GetType,Toast.LENGTH_SHORT).show();
        mAuth=FirebaseAuth.getInstance();
        DataBaseRef= FirebaseDatabase.getInstance().getReference().child("Users").child(GetType);



        StorageProfilePicsRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");


        ProfileImageView = findViewById(R.id.profile_image);

        NameEditText = findViewById(R.id.Name);
        PhoneEditText = findViewById(R.id.Phone_Number);
        DriverCarName = findViewById(R.id.Driver_car_name);
        if(GetType.equals("Drivers"))
        {
            DriverCarName.setVisibility(View.VISIBLE);
        }

        Closebtn = findViewById(R.id.close_btn);
        SaveBtn = findViewById(R.id.save_btn);

        ProfileChangebtn = findViewById(R.id.profile);

        Closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(GetType.equals("Drivers"))
                {
                    startActivity(new Intent(SettingsActivity2.this,DriversMapActivity.class));
                }
                else
                {
                    startActivity(new Intent(SettingsActivity2.this,CustomersMapActivity.class));
                }
            }
        });

        SaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Checker.equals("Click")){
                    validateControllers();

                }
                else{
                    validateAndSaveOnlyInformation();

                }

            }
        });
        ProfileChangebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Checker = "Clicked";
                CropImage.activity().setAspectRatio(1,1).start(SettingsActivity2.this);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode==RESULT_CANCELED && data!=null)
        {
            CropImage.ActivityResult Result= CropImage.getActivityResult(data);
            ImageUri=Result.getUri();
            ProfileImageView.setImageURI( ImageUri);

        }
        else
        {
            if(GetType.equals("Drivers"))
            {
                startActivity(new Intent(SettingsActivity2.this,DriversMapActivity.class) );
            }
            else
            {
                startActivity(new Intent(SettingsActivity2.this,CustomersMapActivity.class) );
            }

            Toast.makeText(this,"Error,Try again" ,Toast.LENGTH_SHORT) .show();
        }

    }

    private void validateControllers(){
        if(TextUtils.isEmpty(NameEditText.getText().toString()))
        {
            Toast.makeText(this,"Please provide your name" ,Toast.LENGTH_SHORT) .show();
        }
        else if(TextUtils.isEmpty(PhoneEditText.getText().toString()))
        {
            Toast.makeText(this,"Please provide your Phone Number" ,Toast.LENGTH_SHORT) .show();
        }
        else  if(GetType.equals("Drivers") && TextUtils.isEmpty(DriverCarName.getText().toString()))
        {
            Toast.makeText(this,"Please provide your car name" ,Toast.LENGTH_SHORT) .show();
        }
        else if(Checker.equals("Clicked"))
        {
            UploadProfilePicture();
        }

    }

    private void UploadProfilePicture() {

        final ProgressDialog ProgressDialog = new ProgressDialog(this);
        ProgressDialog.setTitle("Setting Account Information");
        ProgressDialog.setMessage("Please Wait,While we are settings your account information");
        ProgressDialog.show();

        if (ImageUri != null)
        {
            final StorageReference  fileRef=StorageProfilePicsRef.child(mAuth.getCurrentUser().getUid()+".jpg");
            uploadtask = fileRef.putFile(ImageUri);
            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();

                    }
                    return fileRef.getDownloadUrl();


                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful())
                    {
                        Uri downloadUri=task.getResult();
                        MyUri=downloadUri.toString();

                        HashMap<String,Object> Usermap=new HashMap<>();
                        Usermap.put("Uid",mAuth.getCurrentUser().getUid());
                        Usermap.put("Name",NameEditText.getText().toString());
                        Usermap.put("Phone",PhoneEditText.getText().toString());
                        Usermap.put("Image",MyUri);

                        if(GetType.equals("Drivers"))
                        {
                            Usermap.put("Car",DriverCarName.getText().toString());
                        }
                        DataBaseRef.child(mAuth.getCurrentUser().getUid()).updateChildren(Usermap);

                        ProgressDialog.dismiss();

                        if(GetType.equals("Drivers"))
                        {
                            startActivity(new Intent(SettingsActivity2.this,DriversMapActivity.class) );
                        }
                        else {
                            startActivity(new Intent(SettingsActivity2.this,CustomersMapActivity.class) );
                        }

                    }

                }
            });
        }
        else {
            Toast.makeText(this,"Images not selected" ,Toast.LENGTH_SHORT) .show();
        }


    }
    private void validateAndSaveOnlyInformation(){
        if(TextUtils.isEmpty(NameEditText.getText().toString()))
        {
            Toast.makeText(this,"Please provide your name" ,Toast.LENGTH_SHORT) .show();
        }
        else if(TextUtils.isEmpty(PhoneEditText.getText().toString()))
        {
            Toast.makeText(this,"Please provide your Phone Number" ,Toast.LENGTH_SHORT) .show();
        }
        else  if(GetType.equals("Drivers") && TextUtils.isEmpty(DriverCarName.getText().toString()))
        {
            Toast.makeText(this,"Please provide your car name" ,Toast.LENGTH_SHORT) .show();
        }
        else if(Checker.equals("Clicked"))
        {
            UploadProfilePicture();
        }
        else{
            HashMap<String,Object> Usermap=new HashMap<>();
            Usermap.put("Uid",mAuth.getCurrentUser().getUid());
            Usermap.put("Name",NameEditText.getText().toString());
            Usermap.put("Phone",PhoneEditText.getText().toString());

            if(GetType.equals("Drivers"))
            {
                Usermap.put("Car",DriverCarName.getText().toString());
            }
            DataBaseRef.child(mAuth.getCurrentUser().getUid()).updateChildren(Usermap);

            if(GetType.equals("Drivers"))
            {
                startActivity(new Intent(SettingsActivity2.this,DriversMapActivity.class) );
            }
            else {
                startActivity(new Intent(SettingsActivity2.this,CustomersMapActivity.class) );
            }
        }
    }

    private void getUserInformation(){
        DataBaseRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.getChildrenCount() >0){
                    String name = snapshot.child("name").getValue().toString();
                    String phone = snapshot.child("phone").getValue().toString();

                    NameEditText.setText(name);
                   PhoneEditText.setText(phone);


                    if(GetType.equals("Drivers"))
                    {
                        String car = snapshot.child("car").getValue().toString();
                        DriverCarName.setText(car);
                    }

                    if(snapshot.hasChild("image")){
                        String image = snapshot.child("image").getValue().toString();
                        Picasso.get().load(image).into(ProfileImageView);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}