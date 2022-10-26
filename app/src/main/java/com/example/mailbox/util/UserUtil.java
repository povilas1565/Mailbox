package com.example.mailbox.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.mailbox.R;
import com.example.mailbox.api.MailboxRetrofitClient;
import com.example.mailbox.data.MailboxDatabase;
import com.example.mailbox.data.UserDatabase;
import com.example.mailbox.model.Mailbox;
import com.example.mailbox.model.UserResponse;
import com.example.mailbox.alarm.AlarmReceiver;
import com.example.mailbox.ui.mailbox.MailboxActivity;
import com.example.mailbox.ui.main.MainActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.ALARM_SERVICE;

public class UserUtil {
    private static final String TAG = "UserUtil";

    public static void logoutUser(Context context){
        MailboxDatabase mailboxDatabase = MailboxDatabase.getInstance(context);
        mailboxDatabase.resetDatabase();

        UserDatabase userDatabase = UserDatabase.getInstance(context);
        userDatabase.resetDatabase();

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }

    public static <T extends BaseAdapter> void downloadUserData(Context context, boolean isLoggingIn, @Nullable T adapter, boolean showNotification){
        UserDatabase db = UserDatabase.getInstance(context);
        String token = db.getJwtToken();
        if (token == null)
            return;

        Call<UserResponse> call = MailboxRetrofitClient
                .getInstance(token).getApi().getUserDetails(token);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.code() != 200) {
                    if (response.code() == 403)
                        logoutUser(context);
                    Toast.makeText(context, "Response code: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }

                UserResponse userResponse = response.body();

                // save data to database
                MailboxDatabase mailboxDatabase = MailboxDatabase.getInstance(context);
                List<Long> mailboxIds = new ArrayList<>();
                SharedPreferences sharedPref = context.getSharedPreferences("notification", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                for (Mailbox mailbox: userResponse.getMailboxes() ) {
                    mailboxIds.add(mailbox.getMailboxId());
                    mailboxDatabase.saveMailbox(mailbox);
                    boolean showNotificationSettings = sharedPref.getBoolean("showNotification", true);

                    if (showNotification && showNotificationSettings){
                        boolean newMail = mailbox.isNewMail();

                        boolean isNotificationShowed = sharedPref.getBoolean("isNotificationShowed"+mailbox.getMailboxId(), false);

                        if (newMail && !isNotificationShowed){
                            Log.i(TAG, "Showing notification");
                            createMailboxNotification(context, mailbox);
                            editor.putBoolean("isNotificationShowed"+mailbox.getMailboxId(), true);
                        } else if (!newMail){
                            editor.putBoolean("isNotificationShowed"+mailbox.getMailboxId(), false);
                        }
                        editor.apply();
                    }
                }
                UserDatabase userDatabase = UserDatabase.getInstance(context);

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

                //update ui
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }

                if (isLoggingIn){
                    editor.putBoolean("showNotification", true).apply();
                    Intent intent = new Intent(context, MailboxActivity.class);
                    context.startActivity(intent);
                    ((Activity) context).finish();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.w(TAG, "Failed to download data!");
            }
        });

    }

    public static void createMailboxNotification(Context context, Mailbox mailbox){
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel =
                    new NotificationChannel(NetworkUtil.NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME",
                            NotificationManager.IMPORTANCE_DEFAULT);
            channel.setImportance(NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(false);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200});
            channel.setBypassDnd(false);
            channel.setSound(null, null);
            manager.createNotificationChannel(channel);
        }

        String notificationTitle = "New message from your inbox:: " + mailbox.getName();
        String notificationContent = "You have a new message";

        manager.notify(0, new NotificationCompat.Builder(context,
                NetworkUtil.NOTIFICATION_CHANNEL_ID)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_mail)
                .setContentTitle(notificationTitle)
                .setContentText(notificationContent)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build());
    }

    public static <T extends BaseAdapter> void deleteMailbox(Context context, Long id, T adapter) {

        UserDatabase db = UserDatabase.getInstance(context);
        String token = db.getJwtToken();
        if (token == null)
            return;

        Call<UserResponse> call = MailboxRetrofitClient
                .getInstance(token).getApi().deleteMailbox(token, id);

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.code() != 200) {
                    // TODO handle errors

                    if (response.code() == 403)
                        logoutUser(context);

                    Toast.makeText(context, "Response code: " + response.code(), Toast.LENGTH_LONG).show();

                    return;
                }

                UserResponse userResponse = response.body();

                // save data to database
                MailboxDatabase mailboxDatabase = MailboxDatabase.getInstance(context);
                List<Long> mailboxIds = new ArrayList<>();
                for (Mailbox mailbox: userResponse.getMailboxes() ) {
                    mailboxIds.add(mailbox.getMailboxId());
                    mailboxDatabase.saveMailbox(mailbox);
                }

                // save new token to database
                UserDatabase userDatabase = UserDatabase.getInstance(context);
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

                //update ui
                if (adapter != null){
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(context, "Не удалось войти в приложение!", Toast.LENGTH_LONG).show();

            }
        });
    }

    public static void changeEmail(Context context){

    }
}
