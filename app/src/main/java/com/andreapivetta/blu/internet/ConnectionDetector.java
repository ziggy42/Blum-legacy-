package com.andreapivetta.blu.internet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class ConnectionDetector {
    private ConnectivityManager connectivityManager;

    public ConnectionDetector(Context context) {
        this.connectivityManager =
                ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    public boolean isConnectingToInternet() {
        if (connectivityManager == null)
            return false;

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public boolean isConnectingToWiFi() {
        if (connectivityManager == null)
            return false;

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && connectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI;
    }
}
