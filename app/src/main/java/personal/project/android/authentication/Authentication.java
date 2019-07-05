package personal.project.android.authentication;

import android.app.Application;

import com.firebase.client.Firebase;

public class Authentication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Firebase.setAndroidContext(this);

    }
}
