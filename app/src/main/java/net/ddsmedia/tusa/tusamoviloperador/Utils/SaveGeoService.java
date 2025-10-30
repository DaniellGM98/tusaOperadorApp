package net.ddsmedia.tusa.tusamoviloperador.Utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.util.Log;

import net.ddsmedia.tusa.tusamoviloperador.R;
import net.ddsmedia.tusa.tusamoviloperador.model.Orden;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_ATM;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_ATM_LAT;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_ATM_LONG;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_TUSA_LAT;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_TUSA_LONG;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.LOG_CASETA;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.convertInputStreamToString;

public class SaveGeoService extends Service  {

    DBConnection connectionClass;
    private SharedPreferences loginData;
    private SharedPreferences offline;
    int varLL;
    private Orden mOrden;
    private int mMatricula;

    private String paseLista;
    private String hoyStr;

    private double kmphSpeed = 0.0;

    SharedPreferences speed;

    final String CHANNEL_ID = "Important_mail_channel";
    NotificationManagerCompat mNotificationManagerCompat;

    public SaveGeoService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //startForeground(1,new Notification());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        initGeo();

        connectionClass = new DBConnection();

        //CREATE CHANNEL
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        mNotificationManagerCompat = NotificationManagerCompat.from(this);
        //CREATE CHANNEL
    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "net.ddsmedia.tusa.tusamoviloperador";
        String channelName = "TUSA Movil";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.RED);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_logo2)
                .setContentTitle("TUSA está corriendo en background para garantizar el correcto funcionamiento de la aplicación, reportar el status del operador y/o orden activa")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        }else{
            startForeground(2, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        loginData = getApplicationContext().getSharedPreferences("loginData", Context.MODE_PRIVATE);
        mMatricula = loginData.getInt("matricula",0);

        paseLista = loginData.getString("paselista","");
        Calendar c = new GregorianCalendar();
        hoyStr = Integer.toString(c.get(Calendar.YEAR))+Integer.toString(c.get(Calendar.MONTH)+1)+Integer.toString(c.get(Calendar.DATE));
        Log.i("PASELISTA",paseLista+"::"+hoyStr);
        if(mMatricula > 0){
            checkGPS();

            String orden = "";
            int tipoOrden = 0;
            String ordeni = loginData.getString("orden_info","");
            Log.i("INSERT INTO geo_op",ordeni);
            if(!ordeni.equals("")){
                Log.i("INSERT INTO geo_op2",ordeni);
                try {
                    mOrden = new Orden(new JSONObject(ordeni));
                    Log.i("INSERT INTO geo_op3",""+mOrden.getEstado());
                    if(mOrden.getEstado() != Globals.ORDEN_ASIGNADA && mOrden.getEstado() != Globals.ORDEN_RESGUARDO){
                        Log.i("INSERT INTO geo_op4","test");
                        orden = mOrden.getID();
                        tipoOrden = mOrden.getTipo();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendGPSNotification();
            }

            String currentVersion = "";
            PackageManager pm = this.getPackageManager();
            PackageInfo pInfo = null;
            try {
                pInfo =  pm.getPackageInfo(this.getPackageName(),0);
            } catch (PackageManager.NameNotFoundException e1) {
                e1.printStackTrace();
            }
            currentVersion = pInfo.versionName;

            //double speed = geoInfo.getSpeed();
            //double currentSpeed = round(globalSpeed,3,BigDecimal.ROUND_HALF_UP);
            //kmphSpeed = round((currentSpeed*3.6),3, BigDecimal.ROUND_HALF_UP);

            //kmphSpeed = 2.0;
            //if (geoInfo.hasSpeed() && geoInfo.getSpeed() > 0) globalSpeed = geoInfo.getSpeed();

            //Double currentSpeed = round(globalSpeed,12,BigDecimal.ROUND_HALF_UP);
            //kmphSpeed = round((currentSpeed*3.6),8, BigDecimal.ROUND_HALF_UP);
            //kmphSpeed =11.0;
            //kmphSpeed = round((currentSpeed),8, BigDecimal.ROUND_HALF_UP);

            speed = getSharedPreferences("velocidad", Context.MODE_PRIVATE);
            kmphSpeed = speed.getFloat("speed",0);

            String msg = "Geo Service alive => "+geoInfo.getLatitude()+"::"+geoInfo.getLongitude()+"::"+mMatricula+"::"+orden+"::"+currentVersion+"::"+kmphSpeed+"::"+ordeni;
            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            Log.i("GEOAPP",msg);

            Log.i("geo_op","ordeeeen: "+orden);

            SaveGeo saveGeo = new SaveGeo(mMatricula,
                    orden,tipoOrden,
                    String.valueOf(geoInfo.getLatitude()),
                    String.valueOf(geoInfo.getLongitude()),
                    currentVersion);
            saveGeo.execute("");
        }
        return START_STICKY;
    }

    public static double round(Double number, int precision, int roundingMode) {
        if(number == null) return 0.0d;
        BigDecimal bd = new BigDecimal(number);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class SaveGeo extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;
        Boolean existUser = false;

        private final int mOperador;
        private final String mOrdenStr;
        private final Integer mTipo;
        private final String mLat;
        private final String mLong;
        private final String mVer;

        SaveGeo(int operador, String orden, Integer tipo, String latitud, String longitud, String version){
            mOperador = operador;
            mOrdenStr = orden;
            mTipo = tipo;
            mLat = latitud;
            mLong = longitud;
            mVer = version;
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected void onPostExecute(String r) {
            //if(isSuccess==true) {
                Log.i("GEOAPP",z);
            //}
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                offline = getApplicationContext().getSharedPreferences("offline", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = offline.edit();

                Connection con = connectionClass.CONN();

                if (con == null) {
                    varLL++;
                    z = "Error in connection with SQL server";
                    editor.putString("long"+varLL, mLong);
                    editor.putString("lat"+varLL, mLat);
                    editor.apply();
                } else {

                    // verificar que el usuario no esta dado de baja en Personal
                    String queryExist = "SELECT * FROM Personal WHERE ID_matricula = "+mOperador+" AND Fecha_de_baja IS NULL";
                    Log.i("INSERT INTO", queryExist);
                    try {
                        Connection conn = DBConnection.CONN();
                        if (conn == null) {
                            Log.i("INSERT INTO", "Error al conectar con SQL Server");
                        } else {
                            Statement stmt = conn.createStatement();
                            ResultSet rs = stmt.executeQuery(queryExist);
                            if (rs.next()) {
                                String matricula = rs.getString("ID_matricula");
                                String nombres = rs.getString("Nombres");
                                String baja = rs.getString("Fecha_de_baja"); // O el nombre del campo que deseas imprimir
                                Log.i("INSERT INTO", "Matrícula: " + matricula + ", Nombres: " + nombres + ", Baja: " + baja);
                                existUser = true;
                            } else {
                                Log.i("INSERT INTO", "No se encontró el usuario con matrícula "+mOperador);
                                existUser = false;
                            }
                        }
                    } catch (Exception ex) {
                        Log.i("INSERT INTO", "Excepción MSSQL: " + ex.getMessage() + " " + queryExist);
                        existUser = false;
                    }


                    if(existUser){
                        ///////////////////offline
                        String check = offline.getString("long1", "null");
                        if (!check.equals("null")) {
                            for (int x = 1; x <= varLL; x++) {
                                try {
                                    PreparedStatement preparedStatement;
                                    String query;
                                    int tipo = Globals.LOG_NORMAL;
                                    float distancia = 0;
                                    int idCaseta = 0;
                                    if (!mOrdenStr.isEmpty()) {
                                        Location geoOrigen = mOrden.getGeoOrigen();
                                        Location geoDestino = mOrden.getGeoDestino();
                                        if (mOrden.getEstado() == Globals.ORDEN_ASIGNADA) {
                                            if (geoOrigen.getLatitude() != 0 && geoOrigen.getLongitude() != 0) {
                                                distancia = geoInfo.distanceTo(geoOrigen);
                                                if (distancia <= Globals.ORDEN_DISTANCIA) {
                                                    query = "UPDATE orden_status SET estado = '" + Globals.ORDEN_ORIGEN + "', origen = GETDATE() " +
                                                            "WHERE fk_orden = '" + mOrdenStr + "' ";
                                                    preparedStatement = con.prepareStatement(query);
                                                    preparedStatement.executeUpdate();
                                                    tipo = Globals.LOG_AUTO_ORI;
                                                    mOrden.setEstado(Globals.ORDEN_ORIGEN);
                                                    Globals.updInfoOrden(mOrden, loginData);
                                                    Log.i("GEO_AUTO", "Origen Automatico " + mOrden.getOrigenID());
                                                }
                                            }
                                        } else if (mOrden.getEstado() == Globals.ORDEN_INICIADA) {
                                            if (geoDestino.getLatitude() != 0 && geoDestino.getLongitude() != 0) {
                                                distancia = geoInfo.distanceTo(geoDestino);
                                                if (distancia <= Globals.ORDEN_DISTANCIA) {
                                                    query = "UPDATE orden_status SET estado = '" + Globals.ORDEN_EN_SITIO + "', en_sitio = GETDATE() " +
                                                            "WHERE fk_orden = '" + mOrdenStr + "' ";
                                                    preparedStatement = con.prepareStatement(query);
                                                    preparedStatement.executeUpdate();
                                                    tipo = Globals.LOG_AUTO_DEST;
                                                    mOrden.setEstado(Globals.ORDEN_EN_SITIO);
                                                    Globals.updInfoOrden(mOrden, loginData);
                                                    Log.i("GEO_AUTO", "Destino Automatico " + mOrden.getDestinoID());
                                                }
                                            }
                                        } else if (mOrden.getEstado() == Globals.ORDEN_EN_SITIO) {
                                            if (geoDestino.getLatitude() != 0 && geoDestino.getLongitude() != 0) {
                                                distancia = geoInfo.distanceTo(geoDestino);
                                            }
                                        }
                                        int checarCaseta = checkCasetas(mOrdenStr, con);
                                        if (checarCaseta > 0) {
                                            tipo = LOG_CASETA;
                                            idCaseta = checarCaseta;
                                        }
                                    }
                                    query = "INSERT INTO geo_op (fk_ot, fk_op, tipo_ot, latitud, longitud, app_version, tipo, distancia, velocidad, fk_caseta) " +
                                            "VALUES ('" + mOrdenStr + "','" + mOperador + "','" + mTipo + "','" + offline.getString("lat" + x, "null") + "','" + offline.getString("long" + x, "null") + "','" + mVer + "', '" + tipo + "', '" + distancia + "', '" + kmphSpeed + "', '" + idCaseta + "')";
                                    Log.i("geo_op", "test2 " + query);
                                    preparedStatement = con.prepareStatement(query);
                                    preparedStatement.executeUpdate();
                                    z = "Guardado exitoso: " + query;
                                    isSuccess = true;
                                } catch (Exception ex) {
                                    isSuccess = false;
                                    z = "Exceptions: " + ex.getMessage();
                                }
                                //Log.i("send",""+offline.getString("long"+x,"null"));
                                //Log.i("send",""+offline.getString("lat"+x,"null"));
                            }
                            varLL = 0;
                            offline.edit().clear().apply();
                        }
                        ///////////////////offline

                        PreparedStatement preparedStatement;
                        String query;

                        if (!paseLista.equals(hoyStr)) {
                            checkPaseLista(mOperador, mVer);
                        }

                        int tipo = Globals.LOG_NORMAL;
                        float distancia = 0;
                        int idCaseta = 0;
                        //float distOri = geoInfo.distanceTo(geoOrigen);
                        //float distDes = geoInfo.distanceTo(geoDestino);

                        if (!mOrdenStr.isEmpty()) {
                            Location geoOrigen = mOrden.getGeoOrigen();
                            Location geoDestino = mOrden.getGeoDestino();
                            if (mOrden.getEstado() == Globals.ORDEN_ASIGNADA) {
                                if (geoOrigen.getLatitude() != 0 && geoOrigen.getLongitude() != 0) {
                                    distancia = geoInfo.distanceTo(geoOrigen);
                                    if (distancia <= Globals.ORDEN_DISTANCIA) {
                                        query = "UPDATE orden_status SET estado = '" + Globals.ORDEN_ORIGEN + "', origen = GETDATE() " +
                                                "WHERE fk_orden = '" + mOrdenStr + "' ";
                                        preparedStatement = con.prepareStatement(query);
                                        preparedStatement.executeUpdate();

                                        tipo = Globals.LOG_AUTO_ORI;
                                        /*String[] param = {String.valueOf(mOrden.getOrigenID()), String.valueOf(geoInfo.getLatitude()),
                                                String.valueOf(geoInfo.getLongitude()), String.valueOf(mMatricula),"3"};
                                        String queryDestino = Globals.makeQuery(Globals.QUERY_DESTINOS, param);
                                        PreparedStatement preparedStatementD = con.prepareStatement(queryDestino);
                                        preparedStatementD.executeUpdate();*/
                                        mOrden.setEstado(Globals.ORDEN_ORIGEN);
                                        Globals.updInfoOrden(mOrden, loginData);
                                        Log.i("GEO_AUTO", "Origen Automatico " + mOrden.getOrigenID());
                                    }
                                }
                            } else if (mOrden.getEstado() == Globals.ORDEN_INICIADA) {
                                if (geoDestino.getLatitude() != 0 && geoDestino.getLongitude() != 0) {
                                    distancia = geoInfo.distanceTo(geoDestino);
                                    if (distancia <= Globals.ORDEN_DISTANCIA) {
                                        query = "UPDATE orden_status SET estado = '" + Globals.ORDEN_EN_SITIO + "', en_sitio = GETDATE() " +
                                                "WHERE fk_orden = '" + mOrdenStr + "' ";
                                        preparedStatement = con.prepareStatement(query);
                                        preparedStatement.executeUpdate();

                                        tipo = Globals.LOG_AUTO_DEST;
                                        /*String[] param = {String.valueOf(mOrden.getDestinoID()), String.valueOf(geoInfo.getLatitude()),
                                                String.valueOf(geoInfo.getLongitude()), String.valueOf(mMatricula),"4"};
                                        String queryDestino = Globals.makeQuery(Globals.QUERY_DESTINOS, param);
                                        PreparedStatement preparedStatementD = con.prepareStatement(queryDestino);
                                        preparedStatementD.executeUpdate();*/
                                        mOrden.setEstado(Globals.ORDEN_EN_SITIO);
                                        Globals.updInfoOrden(mOrden, loginData);
                                        Log.i("GEO_AUTO", "Destino Automatico " + mOrden.getDestinoID());
                                    }
                                }
                            } else if (mOrden.getEstado() == Globals.ORDEN_EN_SITIO) {
                                if (geoDestino.getLatitude() != 0 && geoDestino.getLongitude() != 0) {
                                    distancia = geoInfo.distanceTo(geoDestino);
                                }
                            }

                            //if(mOrden.getEstado() == Globals.ORDEN_INICIADA){
                            int checarCaseta = checkCasetas(mOrdenStr, con);
                            if (checarCaseta > 0) {
                                tipo = LOG_CASETA;
                                idCaseta = checarCaseta;
                            }
                            //}
                        }

                        query = "INSERT INTO geo_op (fk_ot, fk_op, tipo_ot, latitud, longitud, app_version, tipo, distancia, velocidad, fk_caseta) " +
                                "VALUES ('" + mOrdenStr + "','" + mOperador + "','" + mTipo + "','" + mLat + "','" + mLong + "','" + mVer + "', '" + tipo + "', '" + distancia + "', '" + kmphSpeed + "', '" + idCaseta + "')";
                        Log.i("geo_op", "test " + query);
                        preparedStatement = con.prepareStatement(query);
                        preparedStatement.executeUpdate();
                        z = "Guardado exitoso2: " + query;
                        isSuccess = true;
                    }
                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exceptions: "+ex.getMessage();
            }
            return z;
        }
    }

    private int checkCasetas(String orden, Connection conn){
        String queryCaseta = "SELECT id_caseta, latitud, longitud FROM cruce_iave " +
                "WHERE id_orden = '" + orden + "' AND fecha IS NULL ";
        Log.i("CASETAAAAA","Hola Casetas::"+queryCaseta);
        String query;
        PreparedStatement stmtUpd;
        int caseta = 0;
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(queryCaseta);
            int idCaseta = 0;
            Location geoCaseta = new Location("");
            float distancia = 0;

            while (rs.next()){
                idCaseta = rs.getInt("id_caseta");
                geoCaseta.setLatitude(Double.parseDouble(rs.getString("latitud")));
                geoCaseta.setLongitude(Double.parseDouble(rs.getString("longitud")));
                Log.i("CASETAAAAA",idCaseta+"::"+geoCaseta.getLatitude()+"::"+geoCaseta.getLongitude());

                if(geoCaseta.getLatitude() != 0 && geoCaseta.getLongitude() != 0) {
                    distancia = geoInfo.distanceTo(geoCaseta);
                    if (distancia <= Globals.CASETA_DISTANCIA) {
                        query = "UPDATE cruce_iave SET fecha = GETDATE() " +
                                    "WHERE id_orden = '" + orden + "' AND id_caseta = " + idCaseta;
                        stmtUpd = conn.prepareStatement(query);
                        stmtUpd.executeUpdate();
                        Log.i("GEO_AUTO","Caseta Automatica "+mOrden.getOrigenID());
                        caseta = idCaseta;
                        break;
                    }
                }
            }
            return caseta;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void checkPaseLista(int matr, String ver){
        Location geoBase = new Location("");
        int base = -1;

        // MTY
        geoBase.setLatitude(BASE_ATM_LAT);
        geoBase.setLongitude(BASE_ATM_LONG);
        if (geoInfo.distanceTo(geoBase) <= Globals.ORDEN_DISTANCIA){
            base = BASE_ATM;
        }else{
            geoBase = new Location("");
            // TUSA
            geoBase.setLatitude(BASE_TUSA_LAT);
            geoBase.setLongitude(BASE_TUSA_LONG);

            if (geoInfo.distanceTo(geoBase) <= Globals.ORDEN_DISTANCIA){
                //base = BASE_TUSA;
            }
        }

        if(base > -1){
            String url = "https://trasladosuniversales.com.mx/app/checkPaseLista.php?m="+matr+"&v="+ver+"&b="+base;
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
                InputStream inputStream = null;
                inputStream = httpResponse.getEntity().getContent();
                String result = convertInputStreamToString(inputStream);

                Log.i("PUSH_PASE LISTA",result);

                if(Integer.parseInt(result) > 0){
                    SharedPreferences.Editor editor = loginData.edit();
                    editor.putString("paselista", hoyStr);
                    editor.apply();
                    editor.commit();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** GEOLOCALIZACION **/
    private Location geoInfo;
    private LocationManager locationManager;
    private LocationListener locationListener;
    double globalSpeed = 0;
    @SuppressLint("MissingPermission")
    private void initGeo(){

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //geoInfo = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        geoInfo = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(geoInfo == null){
            //geoInfo = new Location(LocationManager.PASSIVE_PROVIDER);
            geoInfo = new Location(LocationManager.NETWORK_PROVIDER);
            geoInfo.setLatitude(0);
            geoInfo.setLongitude(0);
        }

        locationListener = new LocationListener() {
            private Location mLastLocation;

            public void onLocationChanged(Location pCurrentLocation) {


                geoInfo = pCurrentLocation;
                //stopGeo();
                //Log.i("AQ111111",""+pCurrentLocation.hasSpeed() +" | "+ pCurrentLocation.getSpeed());

                Double speed = 0.0;
                Double currentSpeed = 0.0;
                if (this.mLastLocation != null){
                    /*globalSpeed = Math.sqrt(
                            Math.pow(pCurrentLocation.getLongitude() - mLastLocation.getLongitude(), 2)
                                    + Math.pow(pCurrentLocation.getLatitude() - mLastLocation.getLatitude(), 2)
                    ) / (pCurrentLocation.getTime() - this.mLastLocation.getTime());*/

                    //double elapsedTime = (pCurrentLocation.getTime() - this.mLastLocation.getTime()) / 1000; // Convert milliseconds to seconds
                    //globalSpeed = this.mLastLocation.distanceTo(pCurrentLocation) / elapsedTime;

                    globalSpeed = 1.0;

                    if (pCurrentLocation.hasSpeed() && pCurrentLocation.getSpeed() > 0) globalSpeed = pCurrentLocation.getSpeed();

                    currentSpeed = round(globalSpeed,12,BigDecimal.ROUND_HALF_UP);
                    //kmphSpeed = round((currentSpeed*3.6),8, BigDecimal.ROUND_HALF_UP);
                    //kmphSpeed =22.0;
                    //kmphSpeed = round((currentSpeed),8, BigDecimal.ROUND_HALF_UP);

                    //Log.i("GPD_INFO", globalSpeed+":::"+currentSpeed+":::"+kmphSpeed+":::"+pCurrentLocation.getLatitude()+";"+pCurrentLocation.getLongitude()+";"+pCurrentLocation.getTime()+":::"+mLastLocation.getLatitude()+";"+mLastLocation.getLongitude()+";"+mLastLocation.getTime());
                }
                this.mLastLocation = pCurrentLocation;
                //Log.i("GPS_INFO", String.valueOf(globalSpeed)+":::"+currentSpeed+":::"+kmphSpeed+":::"+pCurrentLocation.getLatitude()+";"+pCurrentLocation.getLongitude()+";"+pCurrentLocation.getTime()+":::"+mLastLocation.getLatitude()+";"+mLastLocation.getLongitude()+";"+mLastLocation.getTime());


            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 30000, 0, locationListener);
    }

    void stopGeo(){
        locationManager.removeUpdates(locationListener);
    }

    private void checkGPS(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            sendSMSMessage();
        }

        initGeo();
    }


    protected void sendSMSMessage() {
        /*String phoneNo = "7711541170";
        String message = mNombreUsuario+" No tiene activado el GPS. [CERRAR_ENVIO]";
        //Log.i("Send SMS",message);

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, message, null, null);
            //Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
        }catch (Exception e) {
            //Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }*/
        Log.i("GEOAPP","== NO GPS ==");
    }

    public static final int ID_SMALL_NOTIFICATION = 235;
    private void sendGPSNotification(){
        if ( !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            /*Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            PendingIntent resultPendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                resultPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), ID_SMALL_NOTIFICATION, intent, PendingIntent.FLAG_IMMUTABLE);
            }
            else
            {
                resultPendingIntent = PendingIntent.getActivity
                        (getApplicationContext(), ID_SMALL_NOTIFICATION, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            //PendingIntent resultPendingIntent =
              //      PendingIntent.getActivity(
                //            getApplicationContext(),
                  //          ID_SMALL_NOTIFICATION,
                    //        intent,
                      //      PendingIntent.FLAG_UPDATE_CURRENT
                    //);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());
            Notification notification;
            notification = mBuilder.setSmallIcon(R.drawable.ic_notification).setTicker("GPS Desactivado").setWhen(0)
                    .setAutoCancel(true)
                    .setContentIntent(resultPendingIntent)
                    .setContentTitle("GPS Desactivado")
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                    .setContentText("Debes activar el GPS ya que tienes traslado activo")
                    .build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(ID_SMALL_NOTIFICATION, notification);*/

            createSimpleNotification("GPS Desactivado", "Debes activar el GPS ya que tienes traslado activo", ID_SMALL_NOTIFICATION);
        }
    }

    private void createSimpleNotification(String title, String text, int notificationId) {

        //removes all previously shown notifications.
        mNotificationManagerCompat.cancelAll();

        //open the url when user taps the notification
        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/"));
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);

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
                .setSmallIcon(R.drawable.ic_notification)
                .setTicker(title)
                .setWhen(0)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher))
                .setContentText(text)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        // notificationId is a unique int for each notification that you must define
        mNotificationManagerCompat.notify(notificationId, notification);
    }

}
