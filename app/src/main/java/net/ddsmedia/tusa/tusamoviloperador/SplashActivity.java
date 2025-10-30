package net.ddsmedia.tusa.tusamoviloperador;

//import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import net.ddsmedia.tusa.tusamoviloperador.Utils.GeoAlarmReceiver;
import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.jsoup.Jsoup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class SplashActivity extends Activity {

    private String mUserInfo;
    private View mControlsView;
    private boolean mVisible;

    private int mStoredUser;
    private String mStoredPass;
    private UserLoginTask mAuthTask;

    SharedPreferences loginData;
    private GeoAlarmReceiver alarm;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
        } else {
            Toast.makeText(SplashActivity.this, R.string.error_internet, Toast.LENGTH_SHORT).show();
        }

        loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
        mStoredUser = loginData.getInt("matricula",0);
        mStoredPass = loginData.getString("password","");

        alarm = new GeoAlarmReceiver();
        showVersion();

        checkPermisos();

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);

        //FirebaseMessaging.getInstance().subscribeToTopic("/topics/todos");
    }

    private void checkPermisos(){
        if (Build.VERSION.SDK_INT >= 33) {
            if(hasPermissionsAPI33(this, PERMISSIONSAPI33)){
                if(mStoredUser == 0){
                    if(alarm != null){
                        alarm.CancelAlarm(getBaseContext());
                    }else{
                        Toast.makeText(getBaseContext(), "Alarm is null", Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //mAuthTask = new UserLoginTask(storedUser, storedPass);
                    //mAuthTask.execute((Void) null);
                    mUserInfo = loginData.getString("info","");
                    Log.i("INFO SHARED",mUserInfo);
                    //checkUpdate();
                    doLogin();
                }
            }else{
                ActivityCompat.requestPermissions(this, PERMISSIONSAPI33, PERMISSION_ALL);
            }
        }else{
            if(hasPermissions(this, PERMISSIONS)){
                if(mStoredUser == 0){
                    if(alarm != null){
                        alarm.CancelAlarm(getBaseContext());
                    }else{
                        Toast.makeText(getBaseContext(), "Alarm is null", Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //mAuthTask = new UserLoginTask(storedUser, storedPass);
                    //mAuthTask.execute((Void) null);
                    mUserInfo = loginData.getString("info","");
                    Log.i("INFO SHARED",mUserInfo);
                    //checkUpdate();
                    doLogin();
                }
            }else{
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }
    }

    private final int PERMISSION_ALL = 1;
    private static String[] PERMISSIONS = {
            //Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.READ_SMS
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_CONTACTS
    };
    private static String[] PERMISSIONSAPI33 = {
            //Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            //Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.READ_SMS
            Manifest.permission.CAMERA,
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.WRITE_CONTACTS
    };
    public boolean hasPermissions(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            //for (String permission : PERMISSIONS) {
            /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }*/
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            //}
        }
        return true;
    }

    public boolean hasPermissionsAPI33(Context context, String[] permissions) {
        if (context != null && permissions != null) {
            //for (String permission : PERMISSIONS) {
            /*if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }*/
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            //}
        }
        return true;
    }

    private void doLogin(){
        mAuthTask = new UserLoginTask(mStoredUser, mStoredPass);
        mAuthTask.execute((Void) null);
    }

    private void goMain(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("user", mUserInfo);
        startActivity(intent);
        finish();
    }

    private void goSalud(int matr){
        Intent intent = new Intent(getBaseContext(), SaludActivity.class);
        intent.putExtra("userId", matr);
        intent.putExtra("user", mUserInfo);
        startActivity(intent);
        finish();
    }

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final int mMatricula;
        private final String mPassword;
        String z = "";
        Boolean isSuccess = false;
        private Usuario mUsuario;

        UserLoginTask(int matr, String password) {
            mMatricula = matr;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String tokenDisp="";
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()){
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String tokenD = task.getResult();
                    //Log.i("tokenD",""+tokenD);

                    SharedPreferences preferences3 = getSharedPreferences("MyToken", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor3 = preferences3.edit();
                    editor3.putString("Token", tokenD);
                    editor3.apply();
                }
            });

            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    /*String query = "SELECT ID_matricula, Nombres, Ap_paterno, Ap_materno, Correo_electronico, " +
                                            "ISNULL((SELECT TOP 1 telefono FROM Telefonos_personal WHERE Id_matricula  = '" + mMatricula + "' AND Adicional = 0),'') AS No_celular, " +
                                            "(SELECT temporal FROM Usuario_tusamovil WHERE fk_matricula  = '" + mMatricula + "') AS temporal, " +
                                            "ISNULL((SELECT TOP 1 CONCAT(tipo_o,fk_orden) FROM orden_status WHERE fk_matricula = '" + mMatricula + "' AND " +
                                                        "(estado < " + Globals.ORDEN_FINALIZADA + ") ),'') AS orden " +
                                        "FROM Personal " +
                                        "WHERE ID_matricula = '" + mMatricula + "' AND ID_empleado = 1 AND " +
                                        "(SELECT password FROM Usuario_tusamovil " +
                                        "WHERE activo = 1 AND fk_matricula = '" + mMatricula + "') = '"+ Globals.cryptPassword(mPassword) +"'";*/
                    String[] param = {String.valueOf(mMatricula), mPassword};
                    String query = Globals.makeQuery(Globals.QUERY_LOGIN, param);
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if(rs.next()){
                        mUsuario = new Usuario(rs);
                        mUserInfo = mUsuario.toJSON().toString();
                        Log.i("USERINFO",mUserInfo);
                        Globals.updInfo(mUsuario,loginData);
                        isSuccess=true;

                        SharedPreferences preferences2 = getSharedPreferences("MyToken", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor2 = preferences2.edit();
                        String newToken = preferences2.getString("SaveDB", "");
                        if(newToken.equals("")){
                            //Log.i("tokenD","54665");

                            tokenDisp = preferences2.getString("Token", "");
                            String query2 = "INSERT INTO log_sesion_app (id_matricula, fecha, token, fecha_cierre, estado) " + "VALUES ("+mMatricula+", GETDATE(), '"+tokenDisp+"', NULL, 1)";
                            PreparedStatement preparedStatement = conn.prepareStatement(query2);
                            preparedStatement.executeUpdate();

                            editor2.putString("SaveDB", "ok");
                            editor2.apply();
                        }

                    }else{
                        Log.i("MSSQLERROR","No hay registro ");
                        isSuccess = false;
                    }
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL");
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;

            if (success) {
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if(mUsuario.getSalud() == 0 && hour > 4){
                    goSalud(mUsuario.getMatricula());
                }else{
                    if(mUsuario.getTemporal() == 0){
                        goMain();
                    }else{
                        Intent intent = new Intent(getBaseContext(), PasswordActivity.class);
                        try {
                            intent.putExtra("user",mUsuario.toJSON().toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        intent.putExtra("init",true);
                        startActivity(intent);
                        finish();
                    }
                }
            } else {
                //se va la señal y cierra sesion sin cambiar Login_app
                /*
                FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/op"+mStoredUser);
                Globals.deleteInfo(loginData);
                if(alarm != null){
                    alarm.CancelAlarm(getBaseContext());
                }else{
                    Toast.makeText(getBaseContext(), "Alarm is null", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(intent);
                finish();*/
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    private void showVersion(){
        String currentVersion = "";

        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo =  pm.getPackageInfo(this.getPackageName(),0);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;
        TextView txtVersion = (TextView) findViewById(R.id.txtVersion);
        txtVersion.setText("v"+currentVersion);
    }

    private void checkUpdate(){
        final String[] latestVersion = {""};
        String currentVersion = "";

        PackageManager pm = this.getPackageManager();
        PackageInfo pInfo = null;

        try {
            pInfo =  pm.getPackageInfo(this.getPackageName(),0);

        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        currentVersion = pInfo.versionName;
        TextView txtVersion = (TextView) findViewById(R.id.txtVersion);
        txtVersion.setText("v"+currentVersion);

        final String finalCurrentVersion = currentVersion;
        Thread downloadThread = new Thread() {
            public void run() {
                Looper.prepare();
                try{
                    // TODO: cambiar url de app en play store
                    latestVersion[0] = Jsoup
                            .connect("http://dds.media/getAppVersion.php?app="+ getPackageName())
                            .timeout(30000)
                            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                            .referrer("http://dds.media").get()
                            .select("h1").first()
                            .ownText();
                    Log.i("VERSIONES", finalCurrentVersion +"::"+ latestVersion[0]);
                    if (!finalCurrentVersion.equalsIgnoreCase(latestVersion[0])){
                        //showUpdateDialog();
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashActivity.this);
                        builder.setTitle("Existe una nueva versión disponible");
                        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                                        ("market://details?id="+getPackageName())));
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doLogin();
                            }
                        });

                        builder.setCancelable(false).show();
                    }else{
                        doLogin();
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.i("EXCEPTION", "VERIFICA TU CONEXION A INTERNET");

                    if(!isFinishing()){
                        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SplashActivity.this);
                        builder.setTitle("No se detectó conexión de Datos o WiFi para conectarse a internet, intentalo más tarde");
                        builder.setPositiveButton("Reintentar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //downloadThread.start();
                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                                //dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("Cerrar App", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        });

                        builder.setCancelable(false).show();
                    }
                }
                Looper.loop();
            }
        };

        downloadThread.start();
    }

    Dialog dialog;
    private void showUpdateDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Existe una nueva versión disponible");
        builder.setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse
                        ("market://details?id="+getPackageName())));
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doLogin();
            }
        });

        builder.setCancelable(false);
        dialog = builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ALL: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("notificaciones","ok");
                } else {
                    Toast.makeText(this,"Debe habilitar los permisos requeridos",Toast.LENGTH_LONG).show();
                }
                checkPermisos();
                return;
            }
        }
    }
}
