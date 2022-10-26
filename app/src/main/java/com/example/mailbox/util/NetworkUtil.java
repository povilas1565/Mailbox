package com.example.mailbox.util;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.mailbox.R;

import org.jetbrains.annotations.NotNull;

public class NetworkUtil {

    public static final String NOTIFICATION_CHANNEL_ID = "10002";

    /**
     * Check if there is an internet connection. If there is not, displays
     * alert dialog.
     * @param context context
     * @return Returns true if there is internet connection. Otherwise false.
     */
    public static boolean isNoInternetConnection(@NotNull Context context, boolean alert){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        boolean connection = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (connection){
            return false;
        } else {
            if (alert)
                infoAlertDialog(context, R.string.no_internet_connection);
            return true;
        }
    }

    /**
     * Builds an info alert dialog
     * @param context context
     * @param message Alert dialog message
     */
    public static void infoAlertDialog(Context context, int message){
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("OK",null)
                .show();
    }

}
