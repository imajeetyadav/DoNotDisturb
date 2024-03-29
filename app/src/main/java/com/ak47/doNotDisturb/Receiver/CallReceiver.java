package com.ak47.doNotDisturb.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.ak47.doNotDisturb.Database.DatabaseHandler;
import com.ak47.doNotDisturb.Model.Contact;
import com.ak47.doNotDisturb.Service.RingtonePlayingService;

import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class CallReceiver extends BroadcastReceiver {
    private static final String TABLE_CONTACTS_CALL = "contacts";
    String TAG = "Logging - CallReceiver ";

    @Override
    public void onReceive(Context context, Intent intent) {
        String stateStr = Objects.requireNonNull(intent.getExtras()).getString(TelephonyManager.EXTRA_STATE);
        String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
        int state = 0;
        assert stateStr != null;
        if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            state = TelephonyManager.CALL_STATE_IDLE;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            state = TelephonyManager.CALL_STATE_OFFHOOK;
        } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            state = TelephonyManager.CALL_STATE_RINGING;
        }
        onCallStateChanged(context, state, number);
    }

    private void onCallStateChanged(Context context, int state, String number) {
        AudioManager myAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        String mode = PreferenceManager.getDefaultSharedPreferences(context).getString("mode_preference", "Silent");
        SharedPreferences sharedPreferences = context.getSharedPreferences("initial_setup", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (state == TelephonyManager.CALL_STATE_RINGING && checkExistenceInDataBase(number, context)) {

            Log.e(TAG, "Playing");
            if (mode.equals("Do Not Disturb")) {
                try {
                    editor.putBoolean("Ringing_mode", false).apply();
                    Intent startIntent = new Intent(context, RingtonePlayingService.class);
                    Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    startIntent.putExtra("ringtone-uri", ringtoneUri.toString());
                    context.startService(startIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (mode.equals("Silent")) {
                //Silent  Mode to Normal During Calls
                editor.putBoolean("Ringing_mode", false).apply();
                assert myAudioManager != null;
                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }

        } else {
            if (mode.equals("Do Not Disturb")) {
                //Do Not  Disturb Mode

                Intent stopIntent = new Intent(context, RingtonePlayingService.class);
                context.stopService(stopIntent);
                assert myAudioManager != null;
                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                editor.putBoolean("Ringing_mode", true).apply();


            } else if (mode.equals("Silent")) {
                //Silent  Mode

                assert myAudioManager != null;
                myAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                editor.putBoolean("Ringing_mode", true).apply();
            }
        }

    }

    private boolean checkExistenceInDataBase(String number, Context context) {
        //     Log.d(TAG,"check Existence");
        DatabaseHandler db = new DatabaseHandler(context);
        number = number.replaceAll(" ", "");
        List<Contact> contacts = db.getAllContacts(TABLE_CONTACTS_CALL);  // TABLE_CONTACTS_CALL is table name
        for (Contact contactList : contacts) {
            Log.d(TAG, contactList.getPhoneNumber() + " " + number);
            if (contactList.getPhoneNumber().equals(number)) {
                return true;
            }
        }
        return false;
    }
}
