package com.example.mailbox.ui.mailbox.profile;

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
import com.example.mailbox.model.request.ChangeEmailRequest;
import com.example.mailbox.model.Mailbox;
import com.example.mailbox.model.UserResponse;
import com.example.mailbox.util.NetworkUtil;
import com.example.mailbox.util.UserUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangeEmailActivity extends AppCompatActivity {

    private static final String TAG = "ChangeEmailActivity";
    EditText editTextOldEmail;
    EditText editTextNewEmail;
    EditText editTextRepeatNewEmail;
    Button changeEmailButton;
    ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        editTextOldEmail = findViewById(R.id.editTextOldEmail);
        editTextNewEmail = findViewById(R.id.editTextNewEmail);
        editTextRepeatNewEmail = findViewById(R.id.editTextRepeatNewEmail);
        changeEmailButton = findViewById(R.id.changeEmailButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // show back button
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.change_email);
        }
    }

    public void changeEmailOnClick(View view) {
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

        String oldEmail = editTextOldEmail.getText().toString();
        String newEmail = editTextNewEmail.getText().toString();
        String repeatNewEmail = editTextRepeatNewEmail.getText().toString();

        ChangeEmailRequest changeEmailRequest = new ChangeEmailRequest(oldEmail, newEmail);

        Call<UserResponse> call = MailboxRetrofitClient
                .getInstance(token).getApi().changeEmail(token, changeEmailRequest);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.code() != 200) {
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
                            getString(R.string.change_email_failure) + " " + getString(R.string.error_code) + response.code(),
                            Toast.LENGTH_LONG).show();
                    enableViews(true);
                    return;
                }

                UserResponse userResponse = response.body();

                // save data to database
                MailboxDatabase mailboxDatabase = MailboxDatabase.getInstance(getApplicationContext());
                List<Long> mailboxIds = new ArrayList<>();

                for (Mailbox mailbox: userResponse.getMailboxes() ) {
                    mailboxIds.add(mailbox.getMailboxId());
                    mailboxDatabase.saveMailbox(mailbox);
                }
                UserDatabase userDatabase = UserDatabase.getInstance(getApplicationContext());

                // save new token to database
                String token = response.headers().get("Authorization");
                if (token != null){
                    userDatabase.saveJWT(userResponse.getUsername(),token);
                }

                userDatabase.saveUser(
                        userResponse.getUsername(),
                        userResponse.getEmail(),
                        mailboxIds
                );
                userDatabase.close();
                mailboxDatabase.close();

                Toast.makeText(getApplicationContext(), getText(R.string.change_email_success), Toast.LENGTH_LONG).show();

                enableViews(true);
                finish();
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.change_email_failure, Toast.LENGTH_LONG).show();
                enableViews(true);
            }
        });
    }

    private void handleBadRequest(Response<UserResponse> response) {
        ResponseBody responseBody = response.errorBody();
        try {
            String responseJson = responseBody.string();

            JSONObject obj = new JSONObject(responseJson);
            JSONArray errorsJson = obj.getJSONArray("errors");
            for (int i = 0; i < errorsJson.length(); i++)
            {
                if (errorsJson.getString(i).startsWith("oldEmail:")){
                    TextView textViewWrongOldEmail = findViewById(R.id.textViewWrongOldEmail);
                    textViewWrongOldEmail.setVisibility(View.VISIBLE);
                    textViewWrongOldEmail.setText(errorsJson.getString(i).replace("oldEmail: ",""));
                }
                if (errorsJson.getString(i).startsWith("newEmail:")){
                    TextView textViewWrongNewEmail = findViewById(R.id.textViewWrongNewEmail);
                    textViewWrongNewEmail.setVisibility(View.VISIBLE);
                    textViewWrongNewEmail.setText(errorsJson.getString(i).replace("newEmail: ",""));
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

        //old email
        TextView textViewWrongOldEmail = findViewById(R.id.textViewWrongOldEmail);
        if (
                editTextOldEmail.getText().toString().equals("")){
            textViewWrongOldEmail.setVisibility(View.VISIBLE);
            textViewWrongOldEmail.setText(R.string.wrong_email);
            dataCorrect = false;
        } else {
            textViewWrongOldEmail.setVisibility(View.GONE);
        }

        // new email
        TextView textViewWrongNewEmail = findViewById(R.id.textViewWrongNewEmail);
        if (editTextNewEmail.getText().toString().equals("")){
            textViewWrongNewEmail.setVisibility(View.VISIBLE);
            textViewWrongNewEmail.setText(R.string.wrong_email);
            dataCorrect = false;
        } else {
            textViewWrongNewEmail.setVisibility(View.GONE);
        }

        // repeat new email
        TextView textViewWrongRepeatEmail = findViewById(R.id.textViewWrongRepeatEmail);
        if (!editTextRepeatNewEmail.getText().toString().equals(editTextNewEmail.getText().toString())){
            textViewWrongRepeatEmail.setVisibility(View.VISIBLE);
            dataCorrect = false;
        } else {
            textViewWrongRepeatEmail.setVisibility(View.GONE);
        }
        return dataCorrect;
    }

    public void enableViews(Boolean isActive){
        editTextOldEmail.setEnabled(isActive);
        editTextNewEmail.setEnabled(isActive);
        editTextRepeatNewEmail.setEnabled(isActive);
        changeEmailButton.setEnabled(isActive);
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