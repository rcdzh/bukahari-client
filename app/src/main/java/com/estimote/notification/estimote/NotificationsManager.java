package com.estimote.notification.estimote;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.estimote.internal_plugins_api.cloud.proximity.ProximityAttachment;
import com.estimote.notification.MainActivity;
import com.estimote.notification.MyApplication;
import com.estimote.notification.MyRemoteDBHandler;
import com.estimote.proximity_sdk.proximity.ProximityObserver;
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder;
import com.estimote.proximity_sdk.proximity.ProximityZone;
import com.estimote.proximity_sdk.trigger.ProximityTriggerBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class NotificationsManager {

    private static final String TAG = "NotificationsManager";
    private static final boolean DEBUG = true;

    private Context context;
    private NotificationManager notificationManager;
    private Notification helloNotification;
    private Notification goodbyeNotification;
    private int notificationId = 1;
    private Date currTime;
    private String currTimeStr;
    private MyRemoteDBHandler dbHandler;
    private String dbUrl;
    private String userId;

    public NotificationsManager(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.helloNotification = buildNotification("Hello", "You're near your beacon");
        this.goodbyeNotification = buildNotification("Bye bye", "You've left the proximity of your beacon");
    }

    private void setHelloNotification(String title, String text){
        this.helloNotification = buildNotification(title, text);
    }

    private Notification buildNotification(String title, String text) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel contentChannel = new NotificationChannel(
                    "content_channel", "Things near you", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(contentChannel);
        }

        return new NotificationCompat.Builder(context, "content_channel")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
    }

    public void startMonitoring() {
        ProximityObserver proximityObserver =
                new ProximityObserverBuilder(context, ((MyApplication) context).cloudCredentials)
                        .withOnErrorAction(new Function1<Throwable, Unit>() {
                            @Override
                            public Unit invoke(Throwable throwable) {
                                Log.e("app", "proximity observer error: " + throwable);
                                return null;
                            }
                        })
                        .withBalancedPowerMode()
                        .build();

        ProximityZone zone = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("shaula-jojonomic-com-s-not-h32", "example-proximity-zone")
                .inCustomRange(3.0)
                .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        // TODO get this device identifier --> use user ID / email instead
                        // TODO pass currTimeStr and thisDeviceIdentifier to server --> handled by MyRemoteDBHandler

                        currTime = Calendar.getInstance().getTime();
                        dbHandler = new MyRemoteDBHandler(dbUrl, userId, currTime);
                        try {
                            dbHandler.post();
                        } catch (IOException e) {
                            e.printStackTrace();
                            if (DEBUG) Log.e(TAG, "dbHandler.post()", e);
                        }
                        currTimeStr = new SimpleDateFormat("EEEE, d MMM yyyy (HH:mm a)", Locale.getDefault())
                                .format(currTime);
                        notificationManager.notify(notificationId, buildNotification("Blueberry says hi!",
                                String.format("Your latest checkpoint was in %s.", currTimeStr)));
                        // notificationManager.notify(notificationId, helloNotification);
                        return null;
                    }
                })
                .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
                    @Override
                    public Unit invoke(ProximityAttachment attachment) {
                        // notificationManager.notify(notificationId, goodbyeNotification);
                        return null;
                    }
                })
                .create();
        proximityObserver.addProximityZone(zone);
        proximityObserver.start();

        // on Android 8.0 and later, you can use the Proximity Trigger to trigger an intent ... or,
        // more relevant to this example, a notification ... even if the app is killed!
        //
        // read more about it on:
        // https://github.com/estimote/android-proximity-sdk#background-scanning-using-proximity-trigger-android-80

//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            new ProximityTriggerBuilder(context)
//                    .displayNotificationWhenInProximity(helloNotification)
//                    .triggerOnlyOnce()
//                    .withNotificationId(notificationId)
//                    .build()
//                    .start();
//        }
    }

}
