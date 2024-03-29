package com.ak47.doNotDisturb.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ak47.doNotDisturb.Activities.InterstitialAdActivity;
import com.ak47.doNotDisturb.Helper.InterstitialAdManager;
import com.ak47.doNotDisturb.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class AdLoadForegroundService extends Service {
    String TAG = "Logging - LoadAdForegroundService";
    int foregroundServiceID = 250;
    String notificationChannelIdForAdsAndService = "3000";
    private InterstitialAd interstitialAd;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: " + "called");
        Notification foregroundServiceNotification = new NotificationCompat.Builder(getApplicationContext(),
                notificationChannelIdForAdsAndService)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle("Don't Bother")
                .setContentText("Loading online contents...")
                .setGroup("adsServiceGroup")
                .build();
        startForeground(foregroundServiceID, foregroundServiceNotification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: " + "service started");
        loadAd();
        return START_STICKY;
    }

    private void loadAd() {
        interstitialAd = new InterstitialAd(getApplicationContext());
        interstitialAd.setAdUnitId(getApplicationContext().getString(R.string.ad_interstitial_id_1));
        AdRequest adRequest = new AdRequest.Builder().build();

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded: " + "ad loaded");

                InterstitialAdManager interstitialAdManager = InterstitialAdManager.getInstance();
                interstitialAdManager.setInterstitialAd(interstitialAd);

                Intent interstitialAdActivityIntent = new Intent(getApplicationContext(), InterstitialAdActivity.class);
                PendingIntent interstitialAdActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), 500, interstitialAdActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannelIdForAdsAndService)
                        .setSmallIcon(R.drawable.ic_check)
                        .setContentTitle("Show your appreciation!")
                        .setContentText("This is to support the development of the app.")
                        .setOngoing(true)
                        .setGroup("adsServiceGroup")
                        .setContentIntent(interstitialAdActivityPendingIntent)
                        .setAutoCancel(true);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getApplicationContext());
                notificationManagerCompat.notify(450, builder.build());
                stopSelf();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.d(TAG, "onAdFailedToLoad: " + "ad failed to load, code = " + i);
                stopSelf();
            }
        });

        interstitialAd.loadAd(adRequest);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + "called");
    }
}
