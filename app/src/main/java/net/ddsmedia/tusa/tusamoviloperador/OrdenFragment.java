package net.ddsmedia.tusa.tusamoviloperador;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.GastosListAdapter;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.Utils.SaveGeoService;
import net.ddsmedia.tusa.tusamoviloperador.model.Caseta;
import net.ddsmedia.tusa.tusamoviloperador.model.Orden;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import static android.view.View.VISIBLE;
import static androidx.core.content.ContentProviderCompat.requireContext;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_ATM;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_ATM_LAT;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_ATM_LONG;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_TUSA_LAT;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.BASE_TUSA_LONG;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_EFECTIVO;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_GASTO;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_NOTA;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_TRANSFER;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.ORDEN_ORIGEN;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.convertInputStreamToString;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.getBoolean;

import androidx.core.content.ContextCompat;


public class OrdenFragment extends Fragment {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String mOrdenStr;
    private Orden mOrden;

    private TextView mOrdenView;
    private ProgressBar mProgress;
    private TextView mVin;
    private TextView mOriDest;
    private TextView mDomi;
    private TextView mIndi;
    private Button mBtnSave;
    private Button mBtnMas;
    private Button mBtnPanico;
    private Button mLista;
    private Button mBtnSalud;

    private LinearLayout mInfoView;

    private GetOrdenTask mGetOrdenTask;
    private DBConnection connectionClass;
    private UpdateOrden mUpdTask;

    private String paseLista;
    int matri;
    private String hoyStr;
    private SharedPreferences loginData;
    private String currentVersion;
    String destin;

    static final Integer EVIDENCIA_INICIO = 0;
    static final Integer EVIDENCIA_FIN = 8;

    private Boolean showMenuFotos = false;
    private Boolean showMenuPuntos = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_home, container, false);
        initGeo();

        Bundle b = getArguments();
        mUserStr = b.getString("user");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            mOrdenStr = mUserInfo.getOrden();
            matri = mUserInfo.getMatricula();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setHasOptionsMenu(true);

        currentVersion = "";
        PackageManager pm = getActivity().getPackageManager();
        PackageInfo pInfo = null;
        try {
            pInfo =  pm.getPackageInfo(getActivity().getPackageName(),0);
            currentVersion = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }

        checkChangePermisions(matri);


        mOrdenView = (TextView) v.findViewById(R.id.txtOrden);
        mProgress = (ProgressBar) v.findViewById(R.id.pbOrden);
        mInfoView = (LinearLayout) v.findViewById(R.id.ordeninfoView);
        mVin = (TextView) v.findViewById(R.id.txtVin);
        mOriDest = (TextView) v.findViewById(R.id.txtOriDest);
        mDomi = (TextView) v.findViewById(R.id.txtDomi);
        mIndi = (TextView) v.findViewById(R.id.txtIndi);
        mBtnSave = (Button) v.findViewById(R.id.btnSaveO);
        mBtnMas = (Button) v.findViewById(R.id.btnMasO);
        mBtnPanico = (Button) v.findViewById(R.id.btnPanico);

        mInfoView.setVisibility(View.GONE);
        mInfoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInfo();
            }
        });
        mProgress.setVisibility(View.GONE);

        //checkOrden("onCreate");

        mBtnPanico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPanico();
            }
        });

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOrden.getEstado() == Globals.ORDEN_ASIGNADA){
                    mUpdTask = new UpdateOrden(Globals.ORDEN_ORIGEN, mOrdenStr);
                    mUpdTask.execute((Void) null);
                    preguntaFotos(EVIDENCIA_INICIO);
                }else if(mOrden.getEstado() == Globals.ORDEN_ORIGEN){
                    //if(mOrden.getFotos() == 0){
                        //showEvidencia(EVIDENCIA_INICIO);
                    //}else{
                        mUpdTask = new UpdateOrden(Globals.ORDEN_INICIADA, mOrdenStr);
                        mUpdTask.execute((Void) null);
                    //}
                }else if(mOrden.getEstado() == Globals.ORDEN_INICIADA){
                    sitioSino();
                }else if(mOrden.getEstado() == Globals.ORDEN_EN_SITIO){
                    //if(mOrden.getFotos() == 8){
                        //showEvidencia(EVIDENCIA_FIN);
                        //Log.i("","");
                    //}else{
                        finalizarSino();
                    //}
                }else if(mOrden.getEstado() == Globals.ORDEN_RESGUARDO){
                    mUpdTask = new UpdateOrden(Globals.ORDEN_INICIADA, mOrdenStr);
                    mUpdTask.execute((Void) null);
                }else if(mOrden.getEstado() == Globals.ORDEN_FALLA){
                    return;
                }
            }
        });

        mBtnMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MasOrdenesActivity.class);
                intent.putExtra("user", mUserStr);
                intent.putExtra("tipo", Globals.QUERY_PENDIENTES);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                getActivity().overridePendingTransition (R.anim.open_next, R.anim.close_next);
            }
        });

        loginData = getActivity().getSharedPreferences("loginData", Context.MODE_PRIVATE);
        paseLista = loginData.getString("paselista","");
        Calendar c = new GregorianCalendar();
        hoyStr = Integer.toString(c.get(Calendar.YEAR))+Integer.toString(c.get(Calendar.MONTH)+1)+Integer.toString(c.get(Calendar.DATE));
        Log.i("PASELISTA",paseLista+"::"+hoyStr);

        mLista = v.findViewById(R.id.btnLista);
        if(!paseLista.equals(hoyStr)){
            /*String currentVersion = "";
            PackageManager pm = getActivity().getPackageManager();
            PackageInfo pInfo = null;
            try {
                pInfo =  pm.getPackageInfo(getActivity().getPackageName(),0);

            } catch (PackageManager.NameNotFoundException e1) {
                e1.printStackTrace();
            }
            currentVersion = pInfo.versionName;*/

            //checkPaseLista(mUserInfo.getMatricula(), currentVersion);
            PaseLista pasaLista = new PaseLista(mUserInfo.getMatricula(), currentVersion);
            pasaLista.execute("");
        }/*else{
            mLista.setVisibility(View.GONE);
        }*/


        mLista.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLista.setEnabled(false);
                PaseListaSahagun pasaListaSa = new PaseListaSahagun(mUserInfo.getMatricula(), currentVersion);
                pasaListaSa.execute("");
            }
        });

        mBtnSalud = v.findViewById(R.id.btnSalud);
        /*mBtnSalud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SaludActivity.class);
                intent.putExtra("userId", mUserInfo.getMatricula());
                intent.putExtra("user", mUserStr);
                intent.putExtra("tipo", Globals.QUERY_PENDIENTES);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                getActivity().overridePendingTransition (R.anim.open_next, R.anim.close_next);
            }
        });*/

        if(savedInstanceState != null){
            Log.i("SAVED_INSTANCE", "Fecha guardada: "+savedInstanceState.getString(OPEN_DATE));
        }

        return v;
    }

    private void  checkChangePermisions(int matricula){
        // Camara
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            new Thread(() -> {
                try {
                    String mensaje = "Falta activar Permitir en Permiso de Camara";
                    // Construir el JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("m", matricula);
                    jsonObject.put("ms", mensaje);

                    // Crear la conexión
                    URL url = new URL("https://trasladosuniversales.com.mx/app/permisos/logPermisos.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // Enviar el JSON
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // Leer la respuesta
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Log para verificar
                    Log.d("Permisos", "Respuesta: " + result.toString());

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            Log.d("Permiso", "Permiso de Camara SÍ concedido en "+matricula);
        }

        // Ubicacion
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new Thread(() -> {
                try {
                    String mensaje = "Falta activar Permitir todo el tiempo en Permiso de Ubicacion";
                    // Construir el JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("m", matricula);
                    jsonObject.put("ms", mensaje);

                    // Crear la conexión
                    URL url = new URL("https://trasladosuniversales.com.mx/app/permisos/logPermisos.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // Enviar el JSON
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // Leer la respuesta
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Log para verificar
                    Log.d("Permisos", "Respuesta: " + result.toString());

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            Log.d("Permiso", "Permiso de Ubicación SÍ concedido en "+matricula);
        }

        // Contactos
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Permiso", "Permiso de Contactos SÍ concedido en "+matricula);
        } else {
            new Thread(() -> {
                try {
                    String mensaje = "Falta activar Permitir en Permiso de Contactos";
                    // Construir el JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("m", matricula);
                    jsonObject.put("ms", mensaje);

                    // Crear la conexión
                    URL url = new URL("https://trasladosuniversales.com.mx/app/permisos/logPermisos.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // Enviar el JSON
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // Leer la respuesta
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Log para verificar
                    Log.d("Permisos", "Respuesta: " + result.toString());

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // si es version superior a android 12
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                new Thread(() -> {
                    try {
                        String mensaje = "Falta activar Mostrar Notificaciones en Permiso de Notificaciones";
                        // Construir el JSON
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("m", matricula);
                        jsonObject.put("ms", mensaje);

                        // Crear la conexión
                        URL url = new URL("https://trasladosuniversales.com.mx/app/permisos/logPermisos.php");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.setDoOutput(true);

                        // Enviar el JSON
                        OutputStream os = conn.getOutputStream();
                        os.write(jsonObject.toString().getBytes("UTF-8"));
                        os.flush();
                        os.close();

                        // Leer la respuesta
                        InputStream is = conn.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        reader.close();

                        // Log para verificar
                        Log.d("Permisos", "Respuesta: " + result.toString());

                        conn.disconnect();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                Log.d("Permiso", "Permiso de Notificaciones SÍ concedido en " + matricula);
            }
        }

        // Musica y audios
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Permiso", "Permiso de Musica y audios SÍ concedido en "+matricula);
        } else {
            new Thread(() -> {
                try {
                    String mensaje = "Falta activar Permitir en Permiso de Musica y audios";
                    // Construir el JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("m", matricula);
                    jsonObject.put("ms", mensaje);

                    // Crear la conexión
                    URL url = new URL("https://trasladosuniversales.com.mx/app/permisos/logPermisos.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // Enviar el JSON
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // Leer la respuesta
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Log para verificar
                    Log.d("Permisos", "Respuesta: " + result.toString());

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // Fotos y videos
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("Permiso", "Permiso de Fotos y videos SÍ concedido en "+matricula);
        } else {
            new Thread(() -> {
                try {
                    String mensaje = "Falta activar Permitir todo siempre en Permiso de Fotos y videos";
                    // Construir el JSON
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("m", matricula);
                    jsonObject.put("ms", mensaje);

                    // Crear la conexión
                    URL url = new URL("https://trasladosuniversales.com.mx/app/permisos/logPermisos.php");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setDoOutput(true);

                    // Enviar el JSON
                    OutputStream os = conn.getOutputStream();
                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    // Leer la respuesta
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Log para verificar
                    Log.d("Permisos", "Respuesta: " + result.toString());

                    conn.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
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

    public class PaseLista extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        private final int mOperador;
        private final String mVer;

        PaseLista(int operador, String version){
            mOperador = operador;
            mVer = version;
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected void onPostExecute(String r) {
            if(isSuccess==true) {
                Log.i("GEOAPP",z);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {
                    isSuccess = true;

                    checkPaseLista(mOperador, mVer);
                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exceptions";
            }
            return z;
        }
    }

    public class PaseListaSahagun extends AsyncTask<String, String, String> {
        String z = "";

        private final int mOperador;
        private final String mVer;

        PaseListaSahagun(int operador, String version){
            mOperador = operador;
            mVer = version;
        }

        @Override
        protected void onPreExecute() { mProgress.setVisibility(VISIBLE); }

        @Override
        protected void onPostExecute(String r) {
            Log.i("==PASEDELISTA==", r);
            mProgress.setVisibility(View.GONE);
            if(!getActivity().isFinishing()) {
                if(r != "Exceptions" && r != "checando"){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(r)
                            .setTitle("Pase de Lista");
                    builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
                mLista.setEnabled(true);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String query = "";
            try {
                Connection conn = connectionClass.CONN();
                if (conn == null) {
                    z = "Exceptions conn";
                } else {
                    query = "SELECT * FROM Lista_asistencia WHERE Dia = DAY(GETDATE()) AND Mes = MONTH(GETDATE()) AND Anio = YEAR(GETDATE()) " +
                            "AND ID_matricula = "+mOperador+";";
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if(rs.next()){
                        String base = "Escobedo";
                        if(rs.getInt("ID_empresa") == 1)
                            base = "Sahagún";
                        String hora = rs.getString("Hora");
                        z = "Ya pasaste lista en Base "+base+" a las "+hora+" con número de lista: "+rs.getInt("No_lista");
                    }else{
                        Log.i("LISTASAHAGUN","No hay registro. Pasando lista");
                        String url = "https://trasladosuniversales.com.mx/app/checkPaseLista.php?m="+mOperador+"&v="+mVer+"&b=1";
                        try {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
                            InputStream inputStream = null;
                            inputStream = httpResponse.getEntity().getContent();
                            String result = convertInputStreamToString(inputStream);

                            Log.i("RESULTPASELISTA",result);

                            if(Integer.parseInt(result) > 0){
                                z = "Asistencia correcta en Base Sahagún con número de lista: "+result;

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
            } catch (Exception ex) {
                z = "Exceptions: "+query;
            }
            return z;
        }
    }

    private void sendPushCliente(String orden){
        String url = "https://trasladosuniversales.com.mx/app/sendPushCli.php?o="+orden;
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            InputStream inputStream = null;
            inputStream = httpResponse.getEntity().getContent();
            String result = convertInputStreamToString(inputStream);

            Log.i("PUSH_CLIENTE",result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sitioSino(){
        /*SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        java.util.Date ini = null;
        try {
            ini = formatter.parse(mOrden.getIniciada());
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("DAT_ERROR",e.getMessage());
        }
        java.util.Date now = new java.util.Date();
        long dif = now.getTime() - ini.getTime();
        long min = dif / (1000 * 60);
        Log.i("MIN_INICIADA",min+" minutos ::"+ini+"::"+now+"::"+dif);
        if(min > 30){*/
        if(checkSitio()){
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle("Orden de Traslado En Sitio");
            alertDialog.setMessage("¿Esta seguro de marcar la orden "+mOrden.getID()+" como EN SITIO?");

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, En Sitio",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doSitioSi();
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }else{
            Toast.makeText(getActivity(),"No puede marcar la orden en sitio antes de 30 minutos",Toast.LENGTH_SHORT).show();
        }
    }

    private Boolean checkSitio(){
        Boolean res = false;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        java.util.Date ini = null;
        try {
            ini = formatter.parse(mOrden.getIniciada());
        } catch (ParseException e) {
            e.printStackTrace();
            Log.i("DAT_ERROR",e.getMessage());
        }
        java.util.Date now = new java.util.Date();
        long dif = now.getTime() - ini.getTime();
        long min = dif / (1000 * 60);
        Log.i("MIN_INICIADA",min+" minutos ::"+ini+"::"+now+"::"+dif);
        if(min > 30)
            return true;
        return res;
    }

    private void doSitioSi(){
        Intent myIntent = new Intent(getActivity(), SaveGeoService.class);
        getActivity().startService(myIntent);
        mUpdTask = new UpdateOrden(Globals.ORDEN_EN_SITIO, mOrdenStr);
        mUpdTask.execute((Void) null);

        SavePanico savePanicoTask = new SavePanico(mUserInfo.getMatricula(),
                mUserInfo.getOrden(),Globals.ORDEN_PANICO,
                String.valueOf(geoInfo.getLatitude()),
                String.valueOf(geoInfo.getLongitude()), currentVersion, Globals.LOG_AUTO_DEST);
        savePanicoTask.execute("");

        loginData = getActivity().getSharedPreferences("loginData", Context.MODE_PRIVATE);
        String fotosOrden = loginData.getString("fotosOrden","");
        if(fotosOrden.equals("1")){
            showFotos(EVIDENCIA_FIN);
        }else{
            preguntaFotos(EVIDENCIA_FIN);
        }
    }

    private void finalizarSino(){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Finalizar Orden de Traslado");
        alertDialog.setMessage("¿Esta seguro de finalizar la orden "+mOrden.getID()+"?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Finalizar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(getActivity(), SaveGeoService.class);
                        getActivity().startService(myIntent);
                        mUpdTask = new UpdateOrden(Globals.ORDEN_FINALIZADA, mOrdenStr);
                        mUpdTask.execute((Void) null);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void evidenciaSino(final String orden){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Finalizar Orden de Traslado");
        alertDialog.setMessage("¿Agregar evidencia para la orden "+orden+" ahora?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Tomar foto",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intente = new Intent(getActivity(), EvidenciaActivity.class);
                        intente.putExtra("user", mUserStr);
                        intente.putExtra("orden", orden);
                        intente.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        getActivity().startActivity(intente);
                        getActivity().overridePendingTransition (R.anim.open_next, R.anim.close_next);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void checkOrden(String origen){

        // cambiar sesion en bd
        ChangeSesion mChangeSesion;
        mChangeSesion = new ChangeSesion(matri);
        mChangeSesion.execute((Void) null);

        Log.i("CHECK_ORDEN", "Checando orden... => "+origen+mOrdenStr);
        if(!mOrdenStr.isEmpty()){
            mOrdenView.setText(mUserInfo.getOrden());
            mProgress.setVisibility(VISIBLE);
            mGetOrdenTask = new GetOrdenTask(mOrdenStr);
            mGetOrdenTask.execute((Void) null);
            checkGPS();
        }else{
            //mOrdenView.setText("Sin Asignar");
            clearInfo();
        }
    }

    private void clearInfo(){
        mVin.setText("");
        mOriDest.setText("");
        mDomi.setText("");
        mIndi.setText("");

        mOrdenView.setText(R.string.sin_orden);
        mBtnSave.setText("");

        mInfoView.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);

        mOrdenStr = "";
        mUserInfo.setTipoOrden(0);
        mUserInfo.setOrden("");
        try {
            Globals.updInfo(mUserInfo,loginData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i("ON_RESUMEEEEE","Orden onResume: "+mOrdenStr+"::"+mUserInfo.getSalud());
        Bundle b = getActivity().getIntent().getExtras();
        String fecha = b.getString(OPEN_DATE);
        if(fecha != null){
            Log.i("SAVED_EXTRA", "Fecha guardada en intent: "+fecha);
            doSplash();
        }else{
            checkOrden("onResume");
            initGeo();
        }
    }

    static final String OPEN_DATE = "openDate";
    @Override
    public void onSaveInstanceState(Bundle outState) {
        DateFormat df = new SimpleDateFormat("yyyyMMdd");
        String date = df.format(Calendar.getInstance().getTime());
        outState.putString(OPEN_DATE, date);
        getActivity().getIntent().putExtra(OPEN_DATE, date);
        super.onSaveInstanceState(outState);
        Log.i("SAVE_INSTANCE", "Guardadando fecha: "+date);
    }

    public void doSplash(){
        Intent i= new Intent(getActivity(), SplashActivity.class);
        startActivity(i);
        getActivity().finish();
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i("ON_PAUSEEE","Orden onPause: "+mOrdenStr);
        stopGeo();
    }

    private void fillInfo(Orden info){
        mVin.setText(info.getVIN());
        mOriDest.setText(info.getOrigen()+" - "+info.getDestino());
        mDomi.setText(info.getPobOrigen()+" - "+info.getPobDestino());
        mIndi.setText(info.getIndicaciones());
        showMenuFotos = false;
        showMenuPuntos = false;

        if(info.getEstado() == Globals.ORDEN_ASIGNADA){
            mBtnSave.setText(getString(R.string.orden_origen));
        }else if(info.getEstado() == Globals.ORDEN_ORIGEN){
            mBtnSave.setText(getString(R.string.orden_init));
            if(info.getFotos() == 1)
                mBtnSave.setText(getString(R.string.orden_init)+" (Evidencia)");
            showMenuFotos = true;
            showMenuPuntos = true; // cambio para mostrar menu En resguardo
        }else if(info.getEstado() == Globals.ORDEN_INICIADA){
            mBtnSave.setText(getString(R.string.orden_sitio));
            showMenuPuntos = true;
            showMenuFotos = true;
        }else if(info.getEstado() == Globals.ORDEN_EN_SITIO){
            mBtnSave.setText(getString(R.string.orden_finish));
            if(info.getFotos() == 1)
                mBtnSave.setText(getString(R.string.orden_finish)+" (Evidencia)");
            showMenuFotos = true;
        }else if(info.getEstado() == Globals.ORDEN_FALLA){
            mBtnSave.setText(getString(R.string.orden_paused));
        }else if(info.getEstado() == Globals.ORDEN_RESGUARDO){
            mBtnSave.setText(getString(R.string.orden_restart));
        }

        mBtnMas.setVisibility(View.GONE);
        if(info.getExtra() > 0){
            mBtnMas.setVisibility(VISIBLE);
            mBtnMas.setText(info.getExtra() + " ordenes más");
        }

        if(info.getCelAlerta().length() != 10)
            mBtnPanico.setVisibility(View.GONE);

        mInfoView.setVisibility(VISIBLE);
        mProgress.setVisibility(View.GONE);
        mOrden = info;

        if(mOrden.getEstado() == Globals.ORDEN_ASIGNADA)
            origenAutomatico();

        mnuStop.setVisible(true);
        //mnuPause.setVisible(false);
        if(mOrden.getEstado() == Globals.ORDEN_INICIADA){
            sitioAutomatico();
            //mnuStop.setVisible(true);
            //mnuPause.setVisible(true);
        }

        mLista.setVisibility(View.GONE);
        getActivity().invalidateOptionsMenu();
    }

    private void showInfo(){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        //alertDialog.setTitle("Info");
        alertDialog.setMessage("ORIGEN\n" +
                        mOrden.getOrigen()+"\n"+mOrden.getDirOrigen()+"\n"+mOrden.getContOrigen()+"\n\n" +
                        "DESTINO\n" +
                        mOrden.getDestino()+"\n"+mOrden.getDirDestino()+"\n"+mOrden.getContDestino());
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public class UpdateOrden extends AsyncTask<Void, Void, Boolean> {
        private final int mEstado;
        private final String mOrdenID;
        String z = "";
        Boolean isSuccess = false;

        UpdateOrden(int estado, String orden) {
            mEstado = estado;
            mOrdenID = orden;
        }

        @Override
        protected void onPreExecute() { mProgress.setVisibility(VISIBLE); }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Connection conn = DBConnection.CONN();
                String field = "iniciada";
                if(mEstado == Globals.ORDEN_ORIGEN) field = "origen";
                if(mEstado == Globals.ORDEN_EN_SITIO) field = "en_sitio";
                if(mEstado == Globals.ORDEN_FINALIZADA) field = "finalizada";
                if(mEstado == Globals.ORDEN_FALLA) field = "falla";
                if(mEstado == Globals.ORDEN_RESGUARDO) field = "pausa";
                String query = "UPDATE orden_status SET estado = '" + mEstado + "', " + field + " = GETDATE() " +
                        "WHERE fk_orden = '" + mOrdenID + "' ";
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;

                    if(mEstado != Globals.ORDEN_RESGUARDO) sendPushCliente(mOrdenID);

                    if(mEstado == Globals.ORDEN_INICIADA) {
                        //sendSMS("7713593993", "La unidad con numero de vin " + mOrden.getVIN() + " ha iniciado su recorrido de " + mOrden.getOrigen() + " a " + mOrden.getDestino());
                        //sendSMS("7712026000", "La unidad con numero de vin " + mOrden.getVIN() + " ha iniciado su recorrido de " + mOrden.getOrigen() + " a " + mOrden.getDestino());
                    }else if(mEstado == Globals.ORDEN_FINALIZADA){
                        //sendPushCliente(mOrdenID);
                        String queryFecha = "UPDATE Orden_traslados SET Fecha_entrega = GETDATE() " +
                                "WHERE ID_orden = '" + mOrdenID + "' ";
                        PreparedStatement preparedStatementF = conn.prepareStatement(queryFecha);
                        preparedStatementF.executeUpdate();
                        //sendSMS("5554", "La unidad con numero de vin "+mOrden.getVIN()+" ha finalizado su recorrido de "+mOrden.getOrigen()+" a "+mOrden.getDestino());
                    }//else
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage());
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpdTask = null;
            mProgress.setVisibility(View.GONE);
            if(!getActivity().isFinishing()){
                if (success) {
                    Toast.makeText(getActivity(),"Orden Actualizada",Toast.LENGTH_SHORT).show();
                    //SharedPreferences prefs = getActivity().getSharedPreferences("loginData", Context.MODE_PRIVATE);
                    if(mEstado == Globals.ORDEN_FINALIZADA){
                        evidenciaSino(mOrdenStr);

                        mOrdenStr = "";
                        mUserInfo.setOrden("");
                        try {
                            Globals.updInfo(mUserInfo,getActivity().getSharedPreferences("loginData", Context.MODE_PRIVATE));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        clearInfo();
                    }else{
                        checkOrden("UPD "+mEstado);
                        mOrden.setEstado(mEstado);
                        try {
                            Globals.updInfoOrden(mOrden,getActivity().getSharedPreferences("loginData", Context.MODE_PRIVATE));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(mEstado == Globals.ORDEN_INICIADA || mEstado == Globals.ORDEN_ORIGEN){
                            Intent myIntent = new Intent(getActivity(), SaveGeoService.class);
                            getActivity().startService(myIntent);
                        }
                    }
                } else {
                    Toast.makeText(getActivity(),"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mUpdTask = null;
        }
    }

    public class GetOrdenTask extends AsyncTask<Void, Void, Boolean> {
        private final String mOrden;
        String z = "";
        Boolean isSuccess = false;
        private Orden mOrdenT;

        GetOrdenTask(String orden) {
            mOrden = orden;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] param = {mOrden, String.valueOf(mUserInfo.getMatricula())};
            String query = Globals.makeQuery(Globals.QUERY_ORDEN, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if(rs.next()){
                        mOrdenT = new Orden(rs);
                        Log.i("ORDENINFO",mOrdenT.toJSON().toString());
                        destin = mOrdenT.getGeoDestinoStr();

                        Globals.updInfoOrden(mOrdenT,getActivity().getSharedPreferences("loginData", Context.MODE_PRIVATE));
                        isSuccess=true;
                    }else{
                        Log.i("MSSQLERROR","No hay registro ");
                        isSuccess = false;
                    }
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetOrdenTask = null;

            mLista.setVisibility(VISIBLE);
            if (success) {
                fillInfo(mOrdenT);
            }
        }

        @Override
        protected void onCancelled() {
            mGetOrdenTask = null;
        }
    }

    private void sendPanico(){
        SavePanico savePanicoTask = new SavePanico(mUserInfo.getMatricula(),
                mUserInfo.getOrden(),Globals.ORDEN_PANICO,
                String.valueOf(geoInfo.getLatitude()),
                String.valueOf(geoInfo.getLongitude()), currentVersion, Globals.LOG_PANICO);
        savePanicoTask.execute("");

        if(mOrden.getCelAlerta().length() == 10){
            String numero = mOrden.getCelAlerta();
            String message = "Boton de panico por "+mUserInfo.getNombreLargo();
            if(!mOrdenStr.isEmpty()) {
                message += " "+mOrdenStr;
            }
            //if(geoInfo.getLatitude() > 0 && geoInfo.getLongitude() > 0){
            message += " http://maps.google.com/maps?q=loc:"+geoInfo.getLatitude()+","+geoInfo.getLongitude();
            //}
            Log.i("PANICOOOOO",message);

            /*try {
                SmsManager smsManager = SmsManager.getDefault();
                //smsManager.sendTextMessage(Globals.SMS_PANICO, null, message, null, null);
                smsManager.sendTextMessage(numero, null, message, null, null);
            }catch (Exception e) {
                e.printStackTrace();
            }*/
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:"+numero));
            intent.putExtra("sms_body", message);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private void sendSMS(String numero, String mensaje){
        /*try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(numero, null, mensaje, null, null);
        }catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public class SavePanico extends AsyncTask<String, String, String> {
        String z = "";
        Boolean isSuccess = false;

        private final int mOperador;
        private final String mOrden;
        private final Integer mTipo;
        private final String mLat;
        private final String mLong;
        private final String mVersion;
        private final Integer mTipoLog;

        SavePanico(int operador, String orden, Integer tipo, String latitud, String longitud, String version, Integer tipoLog){
            mOperador = operador;
            mOrden = orden;
            mTipo = tipo;
            mLat = latitud;
            mLong = longitud;
            mVersion = version;
            mTipoLog = tipoLog;
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected void onPostExecute(String r) {
            if(isSuccess==true) {
                Log.i("GEOAPP",z);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Connection con = connectionClass.CONN();
                if (con == null) {
                    z = "Error in connection with SQL server";
                } else {
                    String query = "INSERT INTO geo_op (fk_ot, fk_op, tipo_ot, latitud, longitud, tipo, version) " +
                            "VALUES ('" + mOrden + "','" + mOperador + "','" + mTipo + "','" + mLat + "','" + mLong + "', '"+mTipoLog+"', '" +mVersion+ "')";
                    PreparedStatement preparedStatement = con.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    z = "Guardado exitoso";
                    isSuccess = true;
                }
            } catch (Exception ex) {
                isSuccess = false;
                z = "Exceptions";
            }
            return z;
        }
    }

    private Location geoInfo;
    private LocationManager locationManager;
    private LocationListener locationListener;
    @SuppressLint("MissingPermission")
    private void initGeo(){

        //Toast.makeText(getActivity(), "ok1", Toast.LENGTH_SHORT).show();

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        geoInfo = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //Log.i("AQ333333","pruebassss"+geoInfo);
        //Log.i("AQ444444","pruebassss "+geoInfo.getSpeed());

        if(geoInfo == null){
            geoInfo = new Location(LocationManager.NETWORK_PROVIDER);
            geoInfo.setLatitude(0);
            geoInfo.setLongitude(0);
        }

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                //Log.i("AQ333333",""+location.hasSpeed() +" | "+ location.getSpeed());
                //Toast.makeText(getActivity(), "okokok", Toast.LENGTH_SHORT).show();
                geoInfo = location;
                //sitioAutomatico();
                //stopGeo();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 0, locationListener);
    }

    private void checkGPS(){
        if ( !mOrdenStr.isEmpty() && !locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showGPSMessage();
        }
    }

    private void showGPSMessage(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("GPS Desactivado")
                .setMessage("Debes activar tu GPS ya que tienes traslado activo")
                .setCancelable(false)
                .setPositiveButton("Ir a Configuración", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
                //.setNegativeButton("No", new DialogInterface.OnClickListener() {
                //    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                //        dialog.cancel();
                //    }
                //});
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void origenAutomatico(){
        Location geoOrigen = mOrden.getGeoOrigen();
        if(geoOrigen.getLatitude() != 0 && geoOrigen.getLongitude() != 0){
            if(geoInfo.distanceTo(geoOrigen) <= Globals.ORDEN_DISTANCIA){
                //doSitioSi();
                preguntaFotos(EVIDENCIA_INICIO);
                mUpdTask = new UpdateOrden(Globals.ORDEN_ORIGEN, mOrdenStr);
                mUpdTask.execute((Void) null);
                Log.i("GEO_AUTO","Origen Automatico "+mOrden.getOrigenID());

                SavePanico savePanicoTask = new SavePanico(mUserInfo.getMatricula(),
                        mUserInfo.getOrden(),Globals.ORDEN_PANICO,
                        String.valueOf(geoInfo.getLatitude()),
                        String.valueOf(geoInfo.getLongitude()), currentVersion, Globals.LOG_AUTO_ORI);
                savePanicoTask.execute("");
            }
        }
    }

    private void sitioAutomatico(){
        Location geoDestino = mOrden.getGeoDestino();
        if(geoDestino.getLatitude() != 0 && geoDestino.getLongitude() != 0){
            if(geoInfo.distanceTo(geoDestino) <= Globals.ORDEN_DISTANCIA){
                doSitioSi();
                Log.i("GEO_AUTO","Destino Automatico "+mOrden.getDestinoID());
            }
        }
    }

    void stopGeo(){
        locationManager.removeUpdates(locationListener);
    }

    private MenuItem mnuStop;
    private MenuItem mnuPause;
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.mnu_orden, menu);
        MenuItem mnuRoute = menu.findItem(R.id.mnu_orden_ruta);
        MenuItem mnuGastos = menu.findItem(R.id.mnu_orden_gastos);
        //MenuItem mnuFotos = menu.findItem(R.id.mnu_orden_foto);
        mnuStop = menu.findItem(R.id.mnu_orden_stop);
        mnuPause = menu.findItem(R.id.mnu_orden_pause);

        mnuStop.setVisible(showMenuPuntos);
        mnuPause.setVisible(showMenuPuntos);
        if(mOrdenStr.isEmpty()){
            mnuRoute.setVisible(false);
            mnuGastos.setVisible(false);
            //mnuFotos.setVisible(false);
        }else{
            /*if(mOrden.getEstado() == Globals.ORDEN_INICIADA) {
                mnuStop.setVisible(true);
                mnuPause.setVisible(true);
            }*/
        }
        //mnuFotos.setVisible(showMenuFotos);
    }

    private GetCasetasTask mGetCasetasTask;
    private GetGastosTask mGetGastosTask;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_orden_ruta:
                mProgress.setVisibility(VISIBLE);
                mGetCasetasTask = new GetCasetasTask(mOrdenStr);
                mGetCasetasTask.execute((Void) null);
                return true;

            case R.id.mnu_orden_gastos:
                mProgress.setVisibility(VISIBLE);
                mGetGastosTask = new GetGastosTask(mOrdenStr);
                mGetGastosTask.execute((Void) null);
                return true;

            case R.id.mnu_orden_stop:
                stopSiNo();
                return true;

            case R.id.mnu_orden_pause:
                pauseSiNo();
                return true;
/*
            case R.id.mnu_orden_foto:
                int tipo = mOrden.getEstado() == ORDEN_ORIGEN ? EVIDENCIA_INICIO : EVIDENCIA_FIN;
                showFotos(tipo);
                return true;
*/
/*
            case R.id.mnu_orden_foto2:
                Intent intent = new Intent(getActivity(), EvidenciasActivity.class);
                intent.putExtra("user", mUserStr);
                intent.putExtra("orden", mOrden.getID());
                intent.putExtra("tipo", 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
*/
            default:
                break;
        }

        return false;
    }

    private ArrayList<Caseta> casetasList;
    public class GetCasetasTask extends AsyncTask<Void, Void, String[]> {
        private final String mOrden;

        GetCasetasTask(String orden) {
            mOrden = orden;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            //String query = "Select referencia FROM Detalle_gastos WHERE id_orden = '" + mOrden + "' AND (referencia LIKE '%IAVE%' or id_gasto = 5)";
            String query = "SELECT Detalle_gastos.Referencia AS caseta, \n" +
                    "CONCAT(casetas_Plantillas.latitud,',',casetas_Plantillas.longitud) AS geo, \n" +
                    //"casetas_Plantillas.latitud, casetas_Plantillas.longitud, \n" +
                    "CONCAT(Directorio.latitud,',',Directorio.longitud) AS origen, \n" +
                    "CONCAT(Directorio_1.latitud,',',Directorio_1.longitud) AS destino\n" +
                    "FROM Orden_traslados INNER JOIN Directorio INNER JOIN PCasetasporruta INNER JOIN casetas_Plantillas \n" +
                    "    ON PCasetasporruta.Id_Caseta = casetas_Plantillas.ID_Caseta \n" +
                    "    INNER JOIN Tipo_de_ruta_N ON PCasetasporruta.Id_Ruta = Tipo_de_ruta_N.Id_Ruta \n" +
                    "    INNER JOIN Directorio AS Directorio_1 ON Tipo_de_ruta_N.PoblacionDestino = Directorio_1.ID_entidad \n" +
                    "    ON Directorio.ID_entidad = Tipo_de_ruta_N.PoblacionOrigen \n" +
                    "    ON Orden_traslados.Id_tipo_ruta = Tipo_de_ruta_N.id_Tipo_ruta\n " +
                    "    AND Orden_traslados.Id_tipo_ruta = PCasetasporruta.id_Tipo_ruta\n " +
                    "    INNER JOIN Detalle_gastos ON Orden_traslados.ID_orden = Detalle_gastos.ID_orden \n" +
                    "WHERE (PCasetasporruta.consecutivo IS NOT NULL)  \n" +
                    //"AND orden_traslados.ID_orden = '" + mOrden + "' AND Orden_traslados.Id_tipo_ruta = PCasetasporruta.Id_tipo_ruta\n" +
                    "AND orden_traslados.ID_orden = '" + mOrden + "' \n" +
                    "AND (casetas_Plantillas.Nombre = Detalle_gastos.Referencia OR casetas_Plantillas.Nombre +  ' (IAVE)' = Detalle_gastos.Referencia ) \n" +
                    "ORDER BY PCasetasporruta.consecutivo ";
            Log.i("QUERY_CASETAS",query);
            //String[] param = {mOrden, String.valueOf(mUserInfo.getMatricula())};
            //String query = Globals.makeQuery(Globals.QUERY_ORDEN, param);
            String[] res = {};
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    List<String> casetas = new ArrayList<String>();
                    casetasList = new ArrayList<Caseta>();
                    int cont = 0;  float dista = 1000000000;
                    Location current = new Location("");
                    while (rs.next()){
                        casetas.add(rs.getString("caseta"));

                        casetasList.add(new Caseta(rs.getString("caseta"), rs.getString("geo")));

                        /*current.setLatitude(Double.parseDouble(rs.getString("latitud")));
                        current.setLongitude(Double.parseDouble(rs.getString("longitud")));
                        Log.i("CASETA_DIST",rs.getString("caseta")+":::"+geoInfo.distanceTo(current));
                        if(geoInfo.distanceTo(current) < dista){
                            dista = geoInfo.distanceTo(current);
                            cont = 0;
                            waypoints = "";
                        }
                        if(cont < 9){
                            waypoints += rs.getString("latitud")+","+rs.getString("longitud")+"|";
                            cont++;
                        }
                        //waypoints += rs.getString("geo")+"|";
                        //origenR = rs.getString("origen");*/
                        destinoR = rs.getString("destino");
                    }

                    if(casetas.size() == 0){
                        Log.i("MSSQLERROR","No hay registro ");
                        //Log.i("aque", "|"+destin+"|");

                        String[] partes = destin.split(",");

                        if(partes[0].contains(".") && partes[1].contains(".")){
                          if(!partes[0].substring(partes[0].length() - 1).equals("-")){
                              if(!destin.equals("0,0")){
                                casetas.add("\nNo existen casetas para esta orden.\n\nSelecciona para mostrar Destino\n");
                                casetasList.add(new Caseta("Mostrar Destino", destin));
                              }
                          }
                        }
                    }
                    res = casetas.toArray(res);
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
            }
            return res;
        }

        @Override
        protected void onPostExecute(final String[] casetas) {
            mGetCasetasTask = null;
            mProgress.setVisibility(View.GONE);
            showRuta(casetas);
        }

        @Override
        protected void onCancelled() {
            mGetCasetasTask = null;
        }
    }

    //private String origenR;
    private String destinoR;
    private String waypoints = "";

    private void showRuta(String[] casetas){
        if(!getActivity().isFinishing()){
            if(casetas.length == 0){
                Log.i("MSSQLERROR","No hay datosss ");
                //Toast.makeText(getActivity(),"No se encontraron datos. Vuelve a intentar más tarde",Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(),"No se encontraron datos. Verificar datos",Toast.LENGTH_SHORT).show();
            }else{
                AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
                b.setTitle("Casetas en la ruta");
                b.setItems(casetas, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showRutaMaps(which);
                    }

                });

                /*b.setPositiveButton("Ver Ruta en Maps", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin="+origenR+"&destination=18.518496,73.879259&waypoints=18.520561,73.872435|18.519254,73.876614|18.52152,73.877327|18.52019,73.879935&travelmode=driving");
                        String url = "https://www.google.com/maps/dir/?api=1";
                        url += "&origin="+geoInfo.getLatitude()+","+geoInfo.getLongitude()+"&destination="+destinoR;
                        url += "&waypoints="+waypoints.substring(0,waypoints.length()-1);
                        url += "&travelmode=driving";
                        Log.i("MAPAAAAARUTAAA", url);
                        Uri gmmIntentUri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        intent.setPackage("com.google.android.apps.maps");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException ex) {
                            try {
                                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                startActivity(unrestrictedIntent);
                            } catch (ActivityNotFoundException innerEx) {
                                Toast.makeText(getActivity(), "Debes tener Google Maps instalado", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });*/

                b.show();
            }
        }
    }

    private void showRutaMaps(int inicio){
        waypoints = "";
        int fin = inicio + 9;
        if(casetasList.size() < fin) fin = casetasList.size();
        for (int i = inicio; i<fin; i++){
            Caseta current = casetasList.get(i);
            waypoints += current.getGeo()+"|";
        }
        //Uri gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin="+origenR+"&destination=18.518496,73.879259&waypoints=18.520561,73.872435|18.519254,73.876614|18.52152,73.877327|18.52019,73.879935&travelmode=driving");
        String url = "https://www.google.com/maps/dir/?api=1";
        url += "&origin="+geoInfo.getLatitude()+","+geoInfo.getLongitude()+"&destination="+destinoR;
        url += "&waypoints="+waypoints.substring(0,waypoints.length()-1);
        url += "&travelmode=driving";
        Log.i("MAPAAAAARUTAAA", url);
        Uri gmmIntentUri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        intent.setPackage("com.google.android.apps.maps");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                Toast.makeText(getActivity(), "Debes tener Google Maps instalado", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetGastosTask extends AsyncTask<Void, Void, ArrayList<HashMap<String,String>>> {
        private final String mOrden;

        GetGastosTask(String orden) {
            mOrden = orden;
        }

        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(Void... params) {
            String query = "SELECT ID_grupo, FORMAT(Con_nota, 'C', 'en-US') AS nota, FORMAT(Importe_autransf, 'C', 'en-US') AS transferencia, " +
                                    "FORMAT((Efectivo - Importe_autransf), 'C', 'en-US') AS efectivo, " +
                                    "(SELECT Grupo FROM Gastos g WHERE g.ID_grupo = t.ID_grupo) AS gasto, " +
                                    "(SELECT ID_kmtabul FROM Orden_traslados o WHERE o.ID_orden = t.ID_orden) AS kilometros, " +
                                    "(SELECT FORMAT(Sueldo, 'C', 'en-US') FROM Orden_traslados o WHERE o.ID_orden = t.ID_orden) AS sueldo " +
                                "FROM Talon_gastos t WHERE ID_orden = '" + mOrden + "' ORDER BY ID_grupo";
            //String[] param = {mOrden, String.valueOf(mUserInfo.getMatricula())};
            //String query = Globals.makeQuery(Globals.QUERY_ORDEN, param);
            ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
            HashMap<String,String> temp = new HashMap<String, String>();
            temp.put(COLUMN_GASTO, "GASTO");
            temp.put(COLUMN_NOTA, "NOTA");
            temp.put(COLUMN_TRANSFER, "TRANSF");
            temp.put(COLUMN_EFECTIVO, "EFECT");
            list.add(temp);

            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    //Log.i("SQLGastos",query);
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    Double nota = 0.00;
                    Double trans = 0.00;
                    Double efec = 0.00;
                    while (rs.next()){
                        if(list.size() == 1){
                            temp = new HashMap<String, String>();
                            temp.put(COLUMN_GASTO, rs.getString("kilometros"));
                            temp.put(COLUMN_NOTA, rs.getString("sueldo"));
                            temp.put(COLUMN_TRANSFER, "");
                            temp.put(COLUMN_EFECTIVO, "");
                            list.add(temp);
                        }

                        temp = new HashMap<String, String>();
                        temp.put(COLUMN_GASTO, rs.getString("gasto"));
                        temp.put(COLUMN_NOTA, rs.getString("nota").replace("$",""));
                        temp.put(COLUMN_TRANSFER, rs.getString("transferencia").replace("$",""));
                        temp.put(COLUMN_EFECTIVO, rs.getString("efectivo").replace("$",""));
                        list.add(temp);

                        nota += Double.parseDouble(rs.getString("nota").replace("$","").replace(",",""));
                        trans += Double.parseDouble(rs.getString("transferencia").replace("$","").replace(",",""));
                        efec += Double.parseDouble(rs.getString("efectivo").replace("$","").replace(",",""));
                    }

                    DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
                    temp = new HashMap<String, String>();
                    temp.put(COLUMN_GASTO, "TOTAL");
                    temp.put(COLUMN_NOTA, df2.format(nota));
                    temp.put(COLUMN_TRANSFER, df2.format(trans));
                    temp.put(COLUMN_EFECTIVO, df2.format(efec));
                    list.add(temp);

                    /*if(casetas.size() == 0){
                        Log.i("MSSQLERROR","No hay registro ");
                        casetas.add("No existen casetas para esta orden");
                    }*/
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
            }
            return list;
        }

        @Override
        protected void onPostExecute(final ArrayList<HashMap<String,String>> gastos) {
            mGetGastosTask = null;
            mProgress.setVisibility(View.GONE);
            showGastos(gastos);
        }

        @Override
        protected void onCancelled() {
            mGetGastosTask = null;
        }
    }

    public class ChangeSesion extends AsyncTask<Void, Void, Boolean> {
        private final int mMatr;
        Boolean isSuccess = false;

        ChangeSesion(int matr) {
            mMatr = matr;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String query = "";
            try {
                Connection conn = DBConnection.CONN();
                query = "UPDATE Personal SET Login_app = 1 WHERE ID_matricula = "+ mMatr +";";
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" \n:: QUERY :: "+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
        }

        @Override
        protected void onCancelled() {
        }
    }

    private void showGastos(ArrayList<HashMap<String,String>> gastos){
        if(gastos.size() == 0){
            Log.i("MSSQLERROR","No hay datosss ");
            Toast.makeText(getActivity(),"No se encontraron datos. Vuelve a intentar más tarde",Toast.LENGTH_SHORT).show();
        }else{
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
            b.setTitle("Control de Gastos");

            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.fragment_gastos, null);
            b.setView(dialogView);

            HashMap<String, String> map=gastos.get(1);

            final TextView txtKm = (TextView) dialogView.findViewById(R.id.txtKm);
            txtKm.setText(map.get(COLUMN_GASTO));

            TextView txtSueldo = (TextView) dialogView.findViewById(R.id.txtSueldo);
            txtSueldo.setText(map.get(COLUMN_NOTA));

            gastos.remove(1);

            ListView lstGastos = (ListView) dialogView.findViewById(R.id.lstGastos);

            GastosListAdapter adapter = new GastosListAdapter(getActivity(), gastos);
            lstGastos.setAdapter(adapter);

            b.setNegativeButton("Aceptar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            final AlertDialog dialog = b.create();
            dialog.show();
        }
    }

    private void stopSiNo(){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Interrumpir orden");
        alertDialog.setMessage("¿Esta seguro de interrumpir la orden "+mOrden.getID()+" por falla o desperfecto?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Interrumpir",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(getActivity(), SaveGeoService.class);
                        getActivity().startService(myIntent);
                        mUpdTask = new UpdateOrden(Globals.ORDEN_FALLA, mOrdenStr);
                        mUpdTask.execute((Void) null);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void pauseSiNo(){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Orden en Resguardo");
        alertDialog.setMessage("¿Esta seguro de pausar la orden "+mOrden.getID()+" por resguardo?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Pausar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myIntent = new Intent(getActivity(), SaveGeoService.class);
                        getActivity().startService(myIntent);
                        mUpdTask = new UpdateOrden(Globals.ORDEN_RESGUARDO, mOrdenStr);
                        mUpdTask.execute((Void) null);
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void showEvidencia(int tipo){
        Intent intent = new Intent(getActivity(), EvidenciasActivity.class);
        intent.putExtra("user", mUserStr);
        intent.putExtra("orden", mOrden.getID());
        intent.putExtra("tipo", tipo);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        //overridePendingTransition (R.anim.open_next, R.anim.close_next);
    }

    private void preguntaFotos(int tipo){
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle("Evidencia Fotográfica");
        alertDialog.setMessage("¿Agregar fotos de tablero, urea, combustible y refrigerante?");

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Connection conn = DBConnection.CONN();
                            // verificar si se cambia campo en bd
                            String query = "UPDATE orden_status SET fotos = '" + (tipo + 8) + "' " +
                                    "WHERE fk_orden = '" + mOrdenStr + "' ";
                            if (conn == null) {
                                Log.i("MSSQLERROR","Error al conectar con SQL server");
                            } else {
                                PreparedStatement preparedStatement = conn.prepareStatement(query);
                                preparedStatement.executeUpdate();
                            }
                        } catch (Exception ex) {
                            Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage());
                        }

                        dialog.dismiss();

                        checkOrden("onResume");
                        initGeo();
                    }
                });

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Si, Tomar fotos",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showFotos(tipo);
                    }
                });
        alertDialog.show();
    }

    private void showFotos(int tipo){

        SharedPreferences.Editor editor = loginData.edit();
        if(tipo==0){
            editor.putString("fotosOrden", "1");
        }else {
            editor.putString("fotosOrden", "0");
        }
        editor.apply();

        //Intent intent = new Intent(getActivity(), FotosActivity.class);
        Intent intent = new Intent(getActivity(), EvidenciasActivity.class);
        intent.putExtra("user", mUserStr);
        intent.putExtra("orden", mOrden.getID());
        intent.putExtra("tipo", tipo);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        //overridePendingTransition (R.anim.open_next, R.anim.close_next);
    }

}
