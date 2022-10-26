package com.example.mailbox.ui.mailbox.home;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mailbox.R;
import com.example.mailbox.api.MailboxRetrofitClient;
import com.example.mailbox.data.MailboxDatabase;
import com.example.mailbox.data.UserDatabase;
import com.example.mailbox.model.request.AddMailboxRequest;
import com.example.mailbox.util.NetworkUtil;
import com.example.mailbox.util.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMailboxActivity extends AppCompatActivity {

    private static final String TAG = "AddMailboxActivity";
    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextRepeatPassword;
    EditText editTextName;
    Button addMailboxButton;
    ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mailbox);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextMailboxPassword);
        editTextRepeatPassword = findViewById(R.id.editTextRepeatPassword);
        editTextName = findViewById(R.id.editTextMailboxName);
        addMailboxButton = findViewById(R.id.addMailboxButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // show back button
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.add_mailbox_header);
        }
    }

    public void addMailboxOnClick(View view) {
        if (!isDataCorrect() || NetworkUtil.isNoInternetConnection(getApplicationContext(), true)){
            return;
        }

        UserDatabase db = UserDatabase.getInstance(getApplicationContext());
        String token = db.getJwtToken();
        if (token == null){
            Toast.makeText(getApplicationContext(), R.string.no_token, Toast.LENGTH_LONG).show();
            UserUtil.logoutUser(getApplicationContext());
        }
        enableViews(false);

        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String name = editTextName.getText().toString();

        AddMailboxRequest addMailboxRequest = new AddMailboxRequest(username, password, name);

        Call<Void> call = MailboxRetrofitClient
                .getInstance(token).getApi().addMailbox(token,addMailboxRequest);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() != 200) {
                    // TODO handle errors
                    if (response.code() == 400){
                        handleBadRequest(response);
                        return;
                    }

                    if (response.code() == 403){
                        UserUtil.logoutUser(getApplicationContext());
                        return;
                    }

                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.add_mailbox_failed_toast) + " " + getString(R.string.error_code) + response.code(),
                            Toast.LENGTH_LONG).show();
                    enableViews(true);
                    return;
                }

                UserDatabase userDatabase = UserDatabase.getInstance(getApplicationContext());
                // save new token to database
                String token = response.headers().get("Authorization");
                if (token != null){
                    userDatabase.saveJWT(userDatabase.getUsername(),token);
                }
                userDatabase.close();

                enableViews(true);

                UserUtil.downloadUserData(getApplicationContext(), false, null, false);
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.add_mailbox_failed_toast, Toast.LENGTH_LONG).show();
                enableViews(true);
            }
        });
    }

    private void handleBadRequest(Response<Void> response) {
        ResponseBody responseBody = response.errorBody();
        try {
            String responseJson = responseBody.string();

            JSONObject obj = new JSONObject(responseJson);
            JSONArray errorsJson = obj.getJSONArray("errors");
            for (int i = 0; i < errorsJson.length(); i++)
            {
                if (errorsJson.getString(i).startsWith("password:")){
                    TextView textViewWrongMailboxPassword = findViewById(R.id.textViewWrongMailboxPassword);
                    textViewWrongMailboxPassword.setVisibility(View.VISIBLE);
                    textViewWrongMailboxPassword.setText(errorsJson.getString(i).replace("password: ",""));
                }
                if (errorsJson.getString(i).startsWith("username:")){
                    TextView textViewWrongUsername = findViewById(R.id.textViewWrongUsername);
                    textViewWrongUsername.setVisibility(View.VISIBLE);
                    textViewWrongUsername.setText(errorsJson.getString(i).replace("username: ",""));
                }
            }


        } catch (IOException | JSONException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        } finally {
            enableViews(true);
        }
    }

    private boolean isDataCorrect() {
        boolean dataCorrect = true;

        //username
        TextView textViewWrongUsername = findViewById(R.id.textViewWrongUsername);
        if (
                editTextUsername.getText().toString().equals("") ||
                !editTextUsername.getText().toString().startsWith("mailbox")
        ){
            textViewWrongUsername.setVisibility(View.VISIBLE);
            textViewWrongUsername.setText(R.string.wrong_username);
            dataCorrect = false;
        } else {
            textViewWrongUsername.setVisibility(View.GONE);
        }

        // password
        TextView textViewWrongMailboxPassword = findViewById(R.id.textViewWrongMailboxPassword);
        if (editTextPassword.getText().toString().equals("")){
            textViewWrongMailboxPassword.setVisibility(View.VISIBLE);
            textViewWrongMailboxPassword.setText(R.string.wrong_password);
            dataCorrect = false;
        } else {
            textViewWrongMailboxPassword.setVisibility(View.GONE);
        }

        // repeat password
        TextView textViewWrongMailboxRepeatPassword = findViewById(R.id.textViewWrongMailboxRepeatPassword);
        if (!editTextRepeatPassword.getText().toString().equals(editTextPassword.getText().toString())){
            textViewWrongMailboxRepeatPassword.setVisibility(View.VISIBLE);
            dataCorrect = false;
        } else {
            textViewWrongMailboxRepeatPassword.setVisibility(View.GONE);
        }

        // name
        UserDatabase userDatabase = UserDatabase.getInstance(getApplicationContext());
        MailboxDatabase mailboxDatabase = MailboxDatabase.getInstance(getApplicationContext());
        boolean isNameUsed = false;
        List<Long> mailboxIds = userDatabase.getMailboxIds();
        for (Long id : mailboxIds){
            if (mailboxDatabase.getMailboxById(id).getName().equals(editTextName.getText().toString())){
                isNameUsed = true;
            }
        }

        TextView textViewWrongMailboxName = findViewById(R.id.textViewWrongMailboxName);
        if (editTextName.getText().toString().equals("") ||
            isNameUsed){
            textViewWrongMailboxName.setVisibility(View.VISIBLE);
            dataCorrect = false;
        } else {
            textViewWrongMailboxName.setVisibility(View.GONE);
        }

        return dataCorrect;
    }

    public void enableViews(Boolean isActive){
        editTextUsername.setEnabled(isActive);
        editTextPassword.setEnabled(isActive);
        editTextRepeatPassword.setEnabled(isActive);
        editTextName.setEnabled(isActive);
        addMailboxButton.setEnabled(isActive);
        if (isActive){
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }else{
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}