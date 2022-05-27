package xyz.akopartem.subtracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Checker {
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    static LocalDate compare(LocalDate dateOfSession, LocalDate compared)
    {
        long resultDays = ChronoUnit.DAYS.between(compared, dateOfSession);
        return LocalDate.of(0, 1, 1).plusDays(resultDays - 1);
    }
}
