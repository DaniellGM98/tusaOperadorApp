package net.ddsmedia.tusa.tusamoviloperador.Utils;

import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_TUSA;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.NOTIFICATION_TYPE_ORDEN;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.NOTIFICATION_TYPE_PASELISTA;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.NOTIFICATION_TYPE_TRANS;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.NOTIFICATION_TYPE_UPDATE;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import net.ddsmedia.tusa.tusamoviloperador.MainActivity;
import net.ddsmedia.tusa.tusamoviloperador.R;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    private Context mCtx;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    //private Sesion mSesion = null;

    //create channel
    /*final String CHANNEL_ID = "Important_mail_channel";
    NotificationManagerCompat mNotificationManagerCompat;*/
    //create channel

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        //CREATE CHANNEL
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Channel name
            CharSequence name = "Important_mail_channel";
            //Channel description
            String description = "This channel will show notification only to important people";
            //The importance level you assign to a channel applies to all notifications that you post to it.
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //Create the NotificationChannel
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            //Set channel description
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        mNotificationManagerCompat = NotificationManagerCompat.from(this);*/
        //CREATE CHANNEL
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        mCtx = getApplicationContext();
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            try {
                JSONObject data = new JSONObject(remoteMessage.getData());
                int tipo = data.getInt("tipo");
                if(tipo == NOTIFICATION_TYPE_ORDEN){
                    String orden = data.getString("orden");
                    int status = data.getInt("status");

                    sendOrdenNotification(orden, status);
                }else if(tipo == NOTIFICATION_TYPE_UPDATE){
                    sendUpdateNotification();
                }else if(tipo == NOTIFICATION_TYPE_TRANS){
                    String orden = data.getString("orden");
                    String monto = data.getString("monto");

                    sendTransferNotification(orden, monto);
                }else if(tipo == NOTIFICATION_TYPE_PASELISTA){
                    int base = data.getInt("base");
                    int numero = data.getInt("numero");

                    sendPaseListaNotification(base, numero);
                }else if(tipo == Globals.NOTIFICATION_TYPE_SESION){
                    SharedPreferences loginData = mCtx.getSharedPreferences("loginData", Context.MODE_PRIVATE);
                    loginData.edit().clear().commit();
                    if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                        ((ActivityManager)mCtx.getSystemService(ACTIVITY_SERVICE))
                                .clearApplicationUserData(); // note: it has a return value!
                    } else {
                        try {
                            String packageName = mCtx.getPackageName();
                            Runtime runtime = Runtime.getRuntime();
                            runtime.exec("pm clear "+packageName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Log.i("SESION_KILL", "BORRADOOOOOOO");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOrdenNotification(String orden, int status) throws JSONException {
        SharedPreferences loginData = mCtx.getSharedPreferences("loginData", Context.MODE_PRIVATE);
        Log.i("PREF_ORDEN","Orden en memoria: "+loginData.getString("orden",""));
        String userStr = loginData.getString("info","");
        SharedPreferences.Editor editor = loginData.edit();

        JSONObject userJson = new JSONObject(userStr);
        Usuario user = new Usuario(userJson);

        String title = "Orden de traslado";
        String msg = "";

        if(status == Globals.ORDEN_ASIGNADA){
            if(user.getOrden().isEmpty()){
                editor.putString("orden", orden);
                user.setOrden(orden);
            }
            msg = "Se te ha asignado la orden "+orden;
        }else if(status == Globals.ORDEN_INICIADA){
            if(user.getOrden().isEmpty()){
                editor.putString("orden", orden);
                user.setOrden(orden);
            }
            msg = "Se reanuda el traslado de la orden "+orden;
        /*}else if(status == Globals.ORDEN_CANCELADA || status == Globals.ORDEN_FALSO || status == Globals.ORDEN_FINALIZADA){
            editor.putString("orden", "");
            user.setOrden("");
            msg = " La orden "+orden+" ha sido cancelada";
        }*/
        }else if(status == Globals.ORDEN_CANCELADA){
            editor.putString("orden", "");
            user.setOrden("");
            msg = " La orden "+orden+" ha sido marcada como no realizada";
        }else if(status == Globals.ORDEN_FALSO){
            editor.putString("orden", "");
            user.setOrden("");
            msg = " La orden "+orden+" ha sido marcada como traslado en falso";
        }else if(status == Globals.ORDEN_FINALIZADA){
            editor.putString("orden", "");
            user.setOrden("");
            msg = " La orden "+orden+" ha sido finalizada";
        }
        editor.apply();
        editor.commit();
        Globals.updInfo(user,loginData);
        Log.i("PREF_ORDEN","Orden en memoria: "+loginData.getString("orden","")+"::"+user.toJSON().toString());

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user", user.toJSON().toString());

        showNotification(title, msg, intent, NOTIFICATION_TYPE_ORDEN);
        //createSimpleNotification(title, msg, intent, NOTIFICATION_TYPE_ORDEN);
    }

    public void sendUpdateNotification(){
        String title = "TUSA Móvil";
        String msg = "Hay una actualización disponible";

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse
                ("market://details?id="+getPackageName()));

        showNotification(title, msg, intent, NOTIFICATION_TYPE_UPDATE);
        //createSimpleNotification(title, msg, intent, NOTIFICATION_TYPE_UPDATE);
    }

    public void sendTransferNotification(String orden, String monto) throws JSONException {
        SharedPreferences loginData = mCtx.getSharedPreferences("loginData", Context.MODE_PRIVATE);
        Log.i("PREF_ORDEN","Orden en memoria: "+loginData.getString("orden",""));
        String userStr = loginData.getString("info","");

        JSONObject userJson = new JSONObject(userStr);
        Usuario user = new Usuario(userJson);

        String title = "Depósito de efectivo";
        String msg = "Depósito por "+monto+". Orden "+orden;
        //Log.i("PREF_ORDEN","Orden en memoria: "+loginData.getString("orden","")+"::"+user.toJSON().toString());

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user", user.toJSON().toString());

        showNotification(title, msg, intent, NOTIFICATION_TYPE_TRANS);
        //createSimpleNotification(title, msg, intent, NOTIFICATION_TYPE_TRANS);
    }

    public void sendPaseListaNotification(int base, int numero) throws JSONException {
        SharedPreferences loginData = mCtx.getSharedPreferences("loginData", Context.MODE_PRIVATE);
        String userStr = loginData.getString("info","");

        JSONObject userJson = new JSONObject(userStr);
        Usuario user = new Usuario(userJson);

        String title = "Pase de Lista";
        String baseStr = "Escobedo";
        if(base == BASE_TUSA) baseStr = "Sahagún";
        String msg = "Número llegada "+numero+" en base "+baseStr;
        //Log.i("PREF_ORDEN","Orden en memoria: "+loginData.getString("orden","")+"::"+user.toJSON().toString());

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("user", user.toJSON().toString());

        showNotification(title, msg, intent, NOTIFICATION_TYPE_PASELISTA);
        //createSimpleNotification(title, msg, intent, NOTIFICATION_TYPE_PASELISTA);
    }

    public static final int ID_SMALL_NOTIFICATION = 235;

    private static final String NOTIFICATION_CHANNEL_ID = "tusa_notification_channel";

    public void showNotification(String title, String message, Intent intent, int type){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(mCtx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.e("NotificationHelper", "Permission POST_NOTIFICATIONS not granted. Cannot show notification.");
                return;
            }
        }

        PendingIntent resultPendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity
                    (mCtx, type, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }else{
            resultPendingIntent = PendingIntent.getActivity
                    (mCtx, type, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        /*NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx, NOTIFICATION_CHANNEL_ID);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.ic_stat_logo2).setTicker(title)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                //.setSmallIcon(R.mipmap.ic_launcher)
                //.setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();*/

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mCtx, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_logo2)
                .setTicker(title)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.ic_launcher))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT); // Establecer prioridad

        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) mCtx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Notificaciones TUSA", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Notificaciones TUSA Movil Operador");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 400, 200, 400});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(type, notification);
    }

    /*private void createSimpleNotification(String title, String text, Intent intent, int notificationId) {

        //removes all previously shown notifications.
        mNotificationManagerCompat.cancelAll();

        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity
                    (getApplicationContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        }
        else
        {
            pendingIntent = PendingIntent.getActivity
                    (getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_logo2)
                .setTicker(title)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(mCtx.getResources(), R.mipmap.ic_launcher))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(text))
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // notificationId is a unique int for each notification that you must define
        mNotificationManagerCompat.notify(notificationId, notification);
    }*/



    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token);
    }

    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
                .build();
        WorkManager.getInstance(this).beginWith(work).enqueue();
        // [END dispatch_job]
    }

    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.

        //Log.i("Refreshed",""+token);
        preferences = getSharedPreferences("MyToken", Context.MODE_PRIVATE);
        editor = preferences.edit();

        editor.putString("Token", token);
        editor.apply();
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_IMMUTABLE);

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentTitle("FCM Message")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    public static class MyWorker extends Worker {

        public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            // TODO(developer): add long running task here.
            return Result.success();
        }
    }
}