package com.payment.com;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public final class Functions {
    private static final String CHANNEL_ID = "akashpay_events";
    private Functions() {}

    public static void createNotification(Context context, String title, String messageBody) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager != null) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "AkashPay Events", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("AkashPay connection and payment updates");
            manager.createNotificationChannel(channel);
        }
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.selfpayfev)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        NotificationManagerCompat.from(context).notify((int) (System.currentTimeMillis() & 0x7fffffff), builder.build());
    }
}
