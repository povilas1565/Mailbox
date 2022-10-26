package com.example.mailbox.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.mailbox.R;
import com.example.mailbox.data.UserDatabase;
import com.example.mailbox.ui.auth.LoginActivity;
import com.example.mailbox.ui.auth.RegisterActivity;
import com.example.mailbox.ui.mailbox.MailboxActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UserDatabase db = UserDatabase.getInstance(getApplicationContext());
        if (db.isUserLoggedIn()){
            db.close();
            Intent intent = new Intent(getApplicationContext(), MailboxActivity.class);
            startActivity(intent);
            finish();
        }
        db.close();
    }



    public void loginOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void signupOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
        finish();
    }
}