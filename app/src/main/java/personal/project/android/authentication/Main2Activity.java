package personal.project.android.authentication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {
    private static final int INT_CONST =1001 ;
    private FirebaseAuth.AuthStateListener authStateListener;
    private Button button,save;
    private FirebaseAuth auth;
    private StorageReference mStorageRef;
    private ImageView imageView;
    private EditText editText;
    private Uri uriImage;
    private FirebaseAuth mAuth;
    private Bitmap bitmap;
    private ProgressDialog progressDialog,getProgressDialog;
    private Uri url;
    private String name;
    private String displayname;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        auth=FirebaseAuth.getInstance();
        button=findViewById(R.id.button2);
        imageView=findViewById(R.id.imageView);
        save=findViewById(R.id.button3);
       // progressBar=findViewById(R.id.progressBar3);
        editText=findViewById(R.id.editText3);
        textView=findViewById(R.id.textView4);

        mAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(Main2Activity.this);
        getProgressDialog=new ProgressDialog(Main2Activity.this);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(Main2Activity.this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},33);
            }
        }
        loadUserInfo();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setTitle("Saving Details...");
                progressDialog.show();
                saveUserInfo();


            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder=new AlertDialog.Builder(Main2Activity.this);
                builder.setTitle("Are you sure you want to logout?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        //finish();
                        if (ContextCompat.checkSelfPermission(Main2Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
                            Intent in = new Intent(Main2Activity.this, MainActivity.class);
                            startActivity(in);
                            finish();
                        }
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       Toast.makeText(Main2Activity.this,"Logout Cancelled",Toast.LENGTH_SHORT).show();
                    }
                });
                final AlertDialog alertDialog=builder.create();
                alertDialog.show();

            }
        });


    }

    @SuppressLint("SetTextI18n")
    private void loadUserInfo() {   //We use Glide to Load the Image
        final FirebaseUser user=mAuth.getCurrentUser();
        if(user!=null) {
            if (user.getPhotoUrl() != null) {
                Glide.with(Main2Activity.this).load(user.getPhotoUrl()).into(imageView);
            }
            if (user.getDisplayName() != null) {
                displayname = user.getDisplayName();
                editText.setText(displayname);
            }
            {
            if(user.isEmailVerified()){
                textView.setText("Email Verified");
            }
            else{
                textView.setText("Email Not Verified(Click to Verify)");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Main2Activity.this,"Verification Email Sent",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        }}
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()==null){
            finish();
            Intent intent=new Intent(Main2Activity.this,MainActivity.class);
            startActivity(intent);

        }
    }

    private void saveUserInfo() {
        name=editText.getText().toString();

        if(name.isEmpty()){
            editText.setError("Name Required");
            editText.requestFocus();
        }

        FirebaseUser user=mAuth.getCurrentUser();   //We get the current user


        if(user!=null && url!=null){
            UserProfileChangeRequest request=new UserProfileChangeRequest.Builder().setDisplayName(name).setPhotoUri(url).build();
            user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){

                        Toast.makeText(Main2Activity.this,"Profile Updated",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else if(user!=null && url==null) {
            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
            user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {

                        Toast.makeText(Main2Activity.this, "Profile Updated", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {  //To get the result of the Image Chosen
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==INT_CONST && resultCode==RESULT_OK && data!=null && data.getData()!=null){ //see if the code matches and result is ok and data is not null

           uriImage= data.getData();         //Get Image Uri
            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),uriImage);
                imageView.setImageBitmap(bitmap);
                //uploadtoStorage();
                 uploadFile(bitmap);
                getProgressDialog.setTitle("Uploading Image");
                getProgressDialog.show();

            } catch (IOException e) {
                e.printStackTrace();
            }



        }

    }

    private void uploadFile(Bitmap bitmap) {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        final StorageReference ImagesRef = storageRef.child("images/"+auth.getCurrentUser().getUid()+".jpg");


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();
        final UploadTask uploadTask = ImagesRef.putBytes(data);



        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.i("Error:",exception.toString());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.i("problem", task.getException().toString());
                        }

                        return ImagesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            getProgressDialog.dismiss();
                            Toast.makeText(Main2Activity.this,"Upload Successfull",Toast.LENGTH_SHORT).show();
                            url=downloadUri;
                          //StorageReference ref = FirebaseStorage.getInstance().getReference().child("users").child(auth.getCurrentUser().getUid());

                            assert downloadUri != null;
                            Log.i("seeThisUri", downloadUri.toString());// This is the one you should store

                            //ref.child("imageURL").setValue(downloadUri.toString());


                        } else {
                            getProgressDialog.dismiss();
                            Log.i("wentWrong","downloadUri failure");
                        }
                    }
                });
            }
        });

    }
    private void imageChooser() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Picture"),INT_CONST);//Create Image Chooser Method
    }


}
