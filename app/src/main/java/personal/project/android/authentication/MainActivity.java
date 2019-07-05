package personal.project.android.authentication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.mtp.MtpStorageInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private EditText mEmail,mPassword;
    private FirebaseAuth mAuth;
    private TextView textView;
    String email,pass;
    private FirebaseAuth.AuthStateListener stateListener;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth=FirebaseAuth.getInstance();

        button=findViewById(R.id.button);
        textView=findViewById(R.id.textView2);
        mEmail=findViewById(R.id.editText);
        mPassword=findViewById(R.id.editText2);
        progressBar=findViewById(R.id.progressBar);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,SignUp.class);
                startActivity(intent);
                finish();
            }
        });

     stateListener= new AuthStateListener() {  //acts according to the change in the authentication states If a user Has logged in previously he will stay logged in
                                               //He won't have to login again and again
         @Override
         public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
             if(firebaseAuth.getCurrentUser()!=null){

                 Intent intent=new Intent(MainActivity.this,Main2Activity.class);
                 startActivity(intent);
                 finish();
             }


         }
     };

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignIn();
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(stateListener);
    }

    private void SignIn() {
        email = mEmail.getText().toString();
        pass = mPassword.getText().toString();
        if(email.isEmpty() ||pass.isEmpty())
        { if (email.isEmpty()) {
            mEmail.setError("Username cannot be left blank");
            mEmail.requestFocus();
        }
         if(pass.isEmpty()){
            mPassword.setError("Password cannot be left blank");
            mPassword.requestFocus();
        }
        }
        else {

            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {  //Inside The task Variable the results are Stored
                    if (!task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Username or Password is Incorrect", Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }
}
