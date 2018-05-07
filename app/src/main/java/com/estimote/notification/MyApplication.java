package com.estimote.notification;

import android.app.Application;

import com.estimote.cloud_plugin.common.EstimoteCloudCredentials;
import com.estimote.internal_plugins_api.cloud.CloudCredentials;
import com.estimote.notification.estimote.NotificationsManager;

//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class MyApplication extends Application {

    public CloudCredentials cloudCredentials = new EstimoteCloudCredentials("shaula-jojonomic-com-s-not-h32", "993017c874356f74766b47cb01e2f83a");
    private NotificationsManager notificationsManager;

    public void enableBeaconNotifications() {
        notificationsManager = new NotificationsManager(this);
        notificationsManager.startMonitoring();
    }

}
