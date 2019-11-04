package live.qubbe.android.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;

import live.qubbe.android.R;

public class FirebaseMsgService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final String CHANNEL_ID = "qubbe_notifications";
    private final int NOTIFICATION_ID = (int) System.currentTimeMillis();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0)
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        String click_action = Objects.requireNonNull(remoteMessage.getNotification()).getClickAction();
        sendNotification(remoteMessage.getNotification().getBody(), click_action);
    }

    private void sendNotification(String messageBody, String clickAction) {
        Intent intent = new Intent(clickAction);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Bitmap icon2 = BitmapFactory.decodeResource(getResources(),
                R.mipmap.icon);

        createNotificationChannel();
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setLargeIcon(icon2)
                .setSmallIcon(R.mipmap.icon)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            if (notificationManager != null) {
                notificationManager.notify(NOTIFICATION_ID /* ID of notification */, notificationBuilder.build());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Qubbe Notifications";
            String description = "Includes all qubbe app notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            notificationChannel.setDescription(description);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            try {
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}