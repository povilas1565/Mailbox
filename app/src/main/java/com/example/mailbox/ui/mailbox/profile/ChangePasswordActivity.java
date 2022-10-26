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
import com.example.mailbox.model.request.ChangePasswordRequest;
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

public class ChangePasswordActivity extends AppCompatActivity {

    private static final String TAG = "ChangeEmailActivity";
    EditText editTextOldPassword;
    EditText editTextNewPassword;
    EditText editTextRepeatNewPassword;
    Button changePasswordButton;
    ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextRepeatNewPassword = findViewById(R.id.editTextRepeatNewPassword);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // show back button
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.change_password);
        }
    }

    public void changePasswordOnClick(View view) {
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

        String oldPassword = editTextOldPassword.getText().toString();
        String newPassword = editTextNewPassword.getText().toString();


        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest(oldPassword, newPassword);

        Call<UserResponse> call = MailboxRetrofitClient
                .getInstance(token).getApi().changePassword(token, changePasswordRequest);

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
                            getString(R.string.change_password_failure) + " " + getString(R.string.error_code) + response.code(),
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
                Toast.makeText(getApplicationContext(), getText(R.string.change_password_success), Toast.LENGTH_LONG).show();
                enableViews(true);
                finish();
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.change_password_failure, Toast.LENGTH_LONG).show();
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
                if (errorsJson.getString(i).startsWith("oldPassword:")){
                    TextView textViewWrongOldPassword = findViewById(R.id.textViewWrongOldPassword);
                    textViewWrongOldPassword.setVisibility(View.VISIBLE);
                    textViewWrongOldPassword.setText(errorsJson.getString(i).replace("oldPassword: ",""));
                }
                if (errorsJson.getString(i).startsWith("newPassword:")){
                    TextView textViewWrongNewPassword = findViewById(R.id.textViewWrongNewPassword);
                    textViewWrongNewPassword.setVisibility(View.VISIBLE);
                    textViewWrongNewPassword.setText(errorsJson.getString(i).replace("newPassword: ",""));
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

        //old password
        TextView textViewWrongOldPassword = findViewById(R.id.textViewWrongOldPassword);
        if (
                editTextOldPassword.getText().toString().equals("")){
            textViewWrongOldPassword.setVisibility(View.VISIBLE);
            textViewWrongOldPassword.setText(R.string.wrong_password);
            dataCorrect = false;
        } else {
            textViewWrongOldPassword.setVisibility(View.GONE);
        }

        // new password
        TextView textViewWrongNewPassword = findViewById(R.id.textViewWrongNewPassword);
        if (editTextNewPassword.getText().toString().equals("")){
            textViewWrongNewPassword.setVisibility(View.VISIBLE);
            textViewWrongNewPassword.setText(R.string.wrong_password);
            dataCorrect = false;
        } else {
            textViewWrongNewPassword.setVisibility(View.GONE);
        }

        // repeat new password
        TextView textViewWrongRepeatPassword = findViewById(R.id.textViewWrongRepeatPassword);
        if (!editTextRepeatNewPassword.getText().toString().equals(editTextNewPassword.getText().toString())){
            textViewWrongRepeatPassword.setVisibility(View.VISIBLE);
            dataCorrect = false;
        } else {
            textViewWrongRepeatPassword.setVisibility(View.GONE);
        }
        return dataCorrect;
    }

    public void enableViews(Boolean isActive){
        editTextOldPassword.setEnabled(isActive);
        editTextNewPassword.setEnabled(isActive);
        editTextRepeatNewPassword.setEnabled(isActive);
        changePasswordButton.setEnabled(isActive);
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