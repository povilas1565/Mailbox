package com.example.mailbox.ui.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mailbox.R;
import com.example.mailbox.api.AuthRetrofitClient;
import com.example.mailbox.model.request.UserRegisterRequest;
import com.example.mailbox.ui.main.MainActivity;
import com.example.mailbox.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    EditText usernameEditText;
    EditText passwordEditText;
    EditText repeatPasswordEditText;
    EditText emailEditText;
    Button registerButton;
    ProgressBar loadingProgressBar;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // show back button, change header
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.register_header);
        }

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        repeatPasswordEditText = findViewById(R.id.editTextRepeatPassword);
        emailEditText = findViewById(R.id.editTextEmail);

        registerButton = findViewById(R.id.createAccountButton);
        loadingProgressBar = findViewById(R.id.loadingRegister);
        context = this;
    }

    public void signupOnClick(View view) {

        if (!isDataCorrect() || NetworkUtil.isNoInternetConnection(getApplicationContext(), true)){
            return;
        }

        //disable views
        enableViews(false);

        //hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String email = emailEditText.getText().toString();

        UserRegisterRequest userRegisterRequest = new UserRegisterRequest(username, password, email);

        //launch a task to get the JWT from the server
        Call<Void> call = AuthRetrofitClient
                .getInstance().getApi().userRegister(userRegisterRequest);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() != 200) {
                    // TODO handle errors

                    if (response.code() == 400){
                        handleBadRequest(response);
                    }

                    Toast.makeText(
                            getApplicationContext(),
                            getString(R.string.register_failed_toast) + " " + getString(R.string.error_code) + response.code(),
                            Toast.LENGTH_LONG).show();
                    enableViews(true);
                    return;
                }
                enableViews(true);

                new AlertDialog.Builder(context)
                        .setTitle(R.string.register_success_title)
                        .setMessage(R.string.register_success_message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .show();

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getApplicationContext(), R.string.register_failed_toast, Toast.LENGTH_LONG).show();
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
                if (errorsJson.getString(i).startsWith("email:")){
                    TextView textViewWrongEmail = findViewById(R.id.textViewWrongEmail);
                    textViewWrongEmail.setVisibility(View.VISIBLE);
                    textViewWrongEmail.setText(errorsJson.getString(i).replace("email: ",""));
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
        if (usernameEditText.getText().toString().equals("")){
            textViewWrongUsername.setVisibility(View.VISIBLE);
            textViewWrongUsername.setText(R.string.wrong_username);
            dataCorrect = false;
        } else {
            textViewWrongUsername.setVisibility(View.GONE);
        }

        // password
        TextView textViewEnterPassword = findViewById(R.id.textViewEnterPassword);
        if (passwordEditText.getText().toString().equals("")){
            textViewEnterPassword.setVisibility(View.VISIBLE);
            dataCorrect = false;
        } else {
            textViewEnterPassword.setVisibility(View.GONE);
        }

        // repeat password
        TextView textViewEnterRepeatPassword = findViewById(R.id.textViewEnterRepeatPassword);
        if (!repeatPasswordEditText.getText().toString().equals(passwordEditText.getText().toString())){
            textViewEnterRepeatPassword.setVisibility(View.VISIBLE);
            dataCorrect = false;
        } else {
            textViewEnterRepeatPassword.setVisibility(View.GONE);
        }

        // email
        TextView textViewWrongEmail = findViewById(R.id.textViewWrongEmail);
        if (emailEditText.getText().toString().equals("")){
            textViewWrongEmail.setVisibility(View.VISIBLE);
            textViewWrongEmail.setText(R.string.wrong_email);
            dataCorrect = false;
        } else {
            textViewWrongEmail.setVisibility(View.GONE);
        }

        return dataCorrect;
    }

    public void enableViews(Boolean isActive){
        usernameEditText.setEnabled(isActive);
        passwordEditText.setEnabled(isActive);
        repeatPasswordEditText.setEnabled(isActive);
        emailEditText.setEnabled(isActive);
        registerButton.setEnabled(isActive);
        if (isActive){
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }else{
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}