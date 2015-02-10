package com.newtpond.testnavdrawer.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.newtpond.testnavdrawer.R;

/**
 * A container for all the network accessibility checking and handling
 */
public final class NetworkAvailable {

    /**
     * Check if network available
     */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager manager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    /**
     * Open dialog if network not available
     */
    public static void noNetworkAlert(Context c) {
            AlertDialog.Builder builder = new AlertDialog.Builder(c);
            builder.setMessage(R.string.network_not_available)
                    .setTitle(R.string.network_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();
    }
}
