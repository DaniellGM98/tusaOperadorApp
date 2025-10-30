package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.convertInputStreamToString;

public class SaludActivity extends Activity {

    ListView listSintomas;
    ArrayAdapter<String> adapter;
    Button btnSend;
    int mMatr;
    String mUserInfo;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salud);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle(R.string.activity_salud);
        //bar.setDisplayHomeAsUpEnabled(true);
        //bar.setHomeButtonEnabled(true);

        Bundle b = getIntent().getExtras();
        mMatr = b.getInt("userId");
        mUserInfo = b.getString("user");

        listSintomas = findViewById(R.id.lstSintomas);
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice,
                getResources().getStringArray(R.array.salud_sintomas));
        listSintomas.setAdapter(adapter);

        btnSend = findViewById(R.id.btnSendSalud);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEncuesta();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_salud, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mnuHelp:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SaludActivity.this);
        builder.setMessage("Para el llenado del cuestionario, seleccione el tipo de malestar que presenta y posteriormente presiona el botón de \"Enviar Encuesta\".\n\nEn caso de no presentar nungún síntoma, solo presiona el botón \"Enviar Encuesta\".")
                .setTitle("Encuesta de Salud")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendEncuesta(){
        int cuantos = 0;
        String correo = "";
        String bdQuery = "";

        for (int i=0; i<listSintomas.getCount(); i++){
            if(listSintomas.isItemChecked(i)){
                cuantos++;
                correo += listSintomas.getItemAtPosition(i) + ";";
                bdQuery += "1, ";
            }else{
                bdQuery += "0, ";
            }
        }

        preferences = getSharedPreferences("EncuestaSalud", Context.MODE_PRIVATE);
        editor = preferences.edit();

        if(cuantos == 0){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final String finalBdQuery = bdQuery.substring(0,bdQuery.length()-2);
            final int finalCuantos = cuantos;
            final String finalCorreo = correo;
            builder.setMessage("¿Desea enviar la encuesta SIN síntomas?")
                    .setTitle("Encuesta de Salud")
                    .setPositiveButton("Si, Enviar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mSaveSalud = new SaveSalud(mMatr, finalBdQuery, finalCuantos, finalCorreo);
                            mSaveSalud.execute((Void) null);

                            //Shared preferences
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat df = new SimpleDateFormat("D");
                            String formattedDate = df.format(calendar.getTime());
                            int a = Integer.parseInt(formattedDate);
                            editor.putInt("diaEncuesta", a);
                            editor.apply();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false);
            builder.create();
            builder.show();
        }else{
            //Shared preferences
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("D");
            String formattedDate = df.format(calendar.getTime());
            int a = Integer.parseInt(formattedDate);
            editor.putInt("diaEncuesta", a);
            editor.apply();

            bdQuery = bdQuery.substring(0,bdQuery.length()-2);
            correo = correo.substring(0,correo.length()-1);
            mSaveSalud = new SaveSalud(mMatr, bdQuery, cuantos, correo);
            mSaveSalud.execute((Void) null);
        }
    }

    private void sendMail(String sintomas){
        String url = "https://trasladosuniversales.com.mx/app/sendMailSalud.php?t=0&m="+mMatr+"&s="+ URLEncoder.encode(sintomas);
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            InputStream inputStream = null;
            inputStream = httpResponse.getEntity().getContent();
            String result = convertInputStreamToString(inputStream);

            Log.i("MAIL_SALUD",result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SaveSalud mSaveSalud;
    public class SaveSalud extends AsyncTask<Void, Void, Boolean> {
        private final int mMatr;
        private final String mValues;
        private final int mCuantos;
        private final String mCorreo;
        Boolean isSuccess = false;
        private ProgressDialog pd = new ProgressDialog(SaludActivity.this);

        SaveSalud(int matr, String values, int cuantos, String correo) {
            mMatr = matr;
            mValues = values;
            mCuantos = cuantos;
            mCorreo = correo;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isFinishing()) {
                pd.setMessage("Enviando encuesta");
                pd.show();
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String query = "";
            try {
                Connection conn = DBConnection.CONN();
                query = "INSERT INTO Estado_de_Salud (ID_matricula, Fecha, fiebre, Tos, Moco, Con_nas, Estornudos, Dolor_Garganta, " +
                                    "Malestar_Garganta, Dif_respirar, Flema, Vomito, Diarrea, Cansancio_Deb, QuebraHueso, Dolor_Cabeza) " +
                            "VALUES ("+mMatr+", GETDATE(), "+mValues+") ";
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;
                    if(mCuantos > 1) sendMail(mCorreo);
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" \n:: QUERY :: "+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSaveSalud = null;
            super.onPostExecute(success);
            pd.hide();
            pd.dismiss();
            if(!isFinishing()){
                if (success) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SaludActivity.this);
                    builder.setMessage("Se enviaron los datos correctamente")
                            .setTitle("Encuesta de Salud")
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    goMain();
                                }
                            })
                            .setCancelable(false);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    // Toast.makeText(SaludActivity.this,"Se guardaron los datos correctamente",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SaludActivity.this,"Ocurrio algo extraño",Toast.LENGTH_SHORT).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mSaveSalud = null;
        }
    }

    private void goMain(){
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("user", mUserInfo);
        startActivity(intent);
        finish();
    }
}