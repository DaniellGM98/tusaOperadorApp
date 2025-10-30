package net.ddsmedia.tusa.tusamoviloperador;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.db.MyOpenHelper;
import net.ddsmedia.tusa.tusamoviloperador.model.Actividad;
import net.ddsmedia.tusa.tusamoviloperador.model.Orden;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BitacoraActivity extends Activity {

    private EditText EdtOperador, EdtLicencia, EdtNumOT, EdtOrigen, EdtDestino, EdtMarca, EdtModelo, EdtPlacas;
    private Button BtnSaveVigencia, BtnSaveFecha, BtnSaveBitacora;

    private JSONObject mJSONUserInfo;
    public String mUserStr;
    public String mPwdStr;
    private Usuario mUserInfo;
    private Boolean mInit;

    private ListView listview;
    private ScrollView scrollView;
    private ArrayList<Actividad> listaActividad;
    //private ArrayList<String> actividades;
    private BitacoraAdapter adapter;

    String vigencia="", fecha="";

    private static final String FORMAT = "%02d:%02d:%02d";

    @SuppressLint({"ClickableViewAccessibility", "SourceLockedOrientationActivity"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitacora);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        EdtOperador = findViewById(R.id.EdtOperador);
        EdtLicencia = findViewById(R.id.EdtLicencia);

        BtnSaveVigencia = findViewById(R.id.BtnSaveVigencia);
        BtnSaveFecha = findViewById(R.id.BtnSaveFecha);

        listview = findViewById(R.id.LstHoras);
        scrollView = findViewById(R.id.Scview);

        EdtNumOT = findViewById(R.id.EdtNumOT);
        EdtOrigen = findViewById(R.id.EdtOrigen);
        EdtDestino = findViewById(R.id.EdtDestino);
        EdtMarca = findViewById(R.id.EdtMarca);
        EdtModelo = findViewById(R.id.EdtModelo);
        EdtPlacas = findViewById(R.id.EdtPlacas);
        BtnSaveBitacora = findViewById(R.id.BtnSaveBitacora);

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle("Bitacora");
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mInit = b.getBoolean("init");
        SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
        mPwdStr = loginData.getString("password","");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

/*
        actividades = new ArrayList<String>();
        actividades.add("Fuera de servicio: 01:50 a 06:50");
        actividades.add("En servicio: 6:50 a 13:00");
        actividades.add("En translado: 13:00 a 21:00");
        actividades.add("Paradas no programadas: 21:00 a 24:00");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, actividades);
 */

        
        consultarListaActividades();

        adapter = new BitacoraAdapter(this,listaActividad);
        listview.setAdapter(adapter);

        scrollView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                findViewById(R.id.Scview).getParent()
                        .requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        listview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });




        //listview.setClickable(true);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Actividad item = (Actividad) adapterView.getItemAtPosition(position);
                //Toast.makeText(BitacoraActivity.this, ""+parseTime(item.getHasta()), Toast.LENGTH_SHORT).show();
                Intent intentAdd = new Intent(getApplicationContext(), RegistroHoraActivity.class);
                intentAdd.putExtra("user", mUserStr);
                intentAdd.putExtra("init",false);
                intentAdd.putExtra("desde", item.getDesde());
                intentAdd.putExtra("hasta", item.getHasta());
                intentAdd.putExtra("actividad", item.getActividad());
                intentAdd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentAdd);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }
        });


        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long arg3) {
                Actividad item = (Actividad) adapterView.getItemAtPosition(position);
                //Toast.makeText(BitacoraActivity.this, ""+parseTime(item.getDesde()), Toast.LENGTH_SHORT).show();

                Globals.conn = new MyOpenHelper(BitacoraActivity.this);
                Globals.db = Globals.conn.getWritableDatabase();
                Globals.db.execSQL("Delete from bitacora WHERE desde="+item.getDesde()+"");
                Globals.db.close();

                Intent intentAdd = new Intent(getApplicationContext(), BitacoraActivity.class);
                intentAdd.putExtra("user", mUserStr);
                intentAdd.putExtra("init",false);
                startActivity(intentAdd);
                return true;
            }
        });



        BtnSaveVigencia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                if(vigencia!=""){
                    year = parseInt(vigencia.substring(6,10));
                    month = (parseInt(vigencia.substring(3,5))-1);
                    day = parseInt(vigencia.substring(0,2));
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(BitacoraActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                if(monthOfYear<9){
                                    if(dayOfMonth<10){
                                        vigencia="0"+dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveVigencia.setText("0"+dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year);
                                    }else{
                                        vigencia = dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveVigencia.setText(dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year);
                                    }
                                }else{
                                    if(dayOfMonth<10){
                                        vigencia = "0"+dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveVigencia.setText("0"+dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    }else {
                                        vigencia = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveVigencia.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    }
                                }
                            }
                        }, year, month, day);
                datePickerDialog.show();
                datePickerDialog.setCancelable(false);
            }
        });

        BtnSaveFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                if(fecha!=""){
                    year = parseInt(fecha.substring(6,10));
                    month = (parseInt(fecha.substring(3,5))-1);
                    day = parseInt(fecha.substring(0,2));
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(BitacoraActivity.this, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                if(monthOfYear<9){
                                    if(dayOfMonth<10){
                                        fecha="0"+dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveFecha.setText("0"+dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year);
                                    }else{
                                        fecha=dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveFecha.setText(dayOfMonth + "/0" + (monthOfYear + 1) + "/" + year);
                                    }
                                }else{
                                    if(dayOfMonth<10){
                                        fecha="0"+dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveFecha.setText("0"+dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    }else {
                                        fecha=dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                                        BtnSaveFecha.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    }
                                }
                            }
                        }, year, month, day);
                datePickerDialog.show();
                datePickerDialog.setCancelable(false);
            }
        });

        BtnSaveBitacora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("Pruebass",""+EdtOperador.getText().toString()+" | "+EdtLicencia.getText().toString()+" | "+BtnSaveVigencia.getText().toString()+" | "+BtnSaveFecha.getText().toString()+" | "+EdtNumOT.getText().toString()+" | "+EdtOrigen.getText().toString()+" | "+EdtDestino.getText().toString()+" | "+EdtMarca.getText().toString()+" | "+EdtModelo.getText().toString()+" | "+EdtPlacas.getText().toString());
            }
        });


        //actividades = new ArrayList<Actividad>();
        //listview = (ListView) findViewById(R.id.lstOrdenes);
        /*listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {



            }
        });
         */
    }

    @SuppressLint("DefaultLocale")
    public static String parseTime(long milliseconds) {
        return String.format(FORMAT,
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(milliseconds)),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_tickets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //this.finish();
                //overridePendingTransition(R.anim.open_main, R.anim.close_next);
                //return true;
                Intent intentAddd = new Intent(getApplicationContext(), MainActivity.class);
                intentAddd.putExtra("user", mUserStr);
                intentAddd.putExtra("init",false);
                intentAddd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentAddd);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                return true;
            case R.id.mnu_addTicket:
                Intent intentAdd = new Intent(getApplicationContext(), RegistroHoraActivity.class);
                intentAdd.putExtra("user", mUserStr);
                intentAdd.putExtra("init",false);
                intentAdd.putExtra("desde", 0);
                intentAdd.putExtra("hasta", 0);
                intentAdd.putExtra("actividad", "");
                intentAdd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentAdd);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    private void consultarListaActividades(){
        Globals.conn = new MyOpenHelper(getApplicationContext());
        Globals.db = Globals.conn.getReadableDatabase();
        Actividad actividad = null;
        listaActividad = new ArrayList<Actividad>();
        Cursor cursor =null;
        cursor = Globals.db.rawQuery("SELECT * from bitacora ORDER BY desde ASC", null);

        while(cursor.moveToNext()){
            Log.i("BASEEE",""+cursor.getInt(1));
            Log.i("BASEEE2",""+cursor.getInt(2));

            actividad = new Actividad();

            actividad.setDesde(cursor.getInt(1));
            actividad.setHasta(cursor.getInt(2));
            actividad.setActividad(cursor.getString(3));
            listaActividad.add(actividad);
        }
    }

    public static class BitacoraAdapter extends ArrayAdapter<Actividad>{

        private static class ViewHolder {
            TextView lblActividad, lblDesde, lblHasta;
        }

        public BitacoraAdapter(Context context, ArrayList<Actividad> actividadess){
            super(context,R.layout.actividad_item,actividadess);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Actividad actividad = getItem(position);

            BitacoraActivity.BitacoraAdapter.ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new BitacoraActivity.BitacoraAdapter.ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.actividad_item, parent, false);

                viewHolder.lblActividad = (TextView) convertView.findViewById(R.id.lblActividad);
                viewHolder.lblDesde = (TextView) convertView.findViewById(R.id.lblDesde);
                viewHolder.lblHasta = (TextView) convertView.findViewById(R.id.lblHasta);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (BitacoraActivity.BitacoraAdapter.ViewHolder) convertView.getTag();
            }

            String a = parseTime(actividad.getDesde());
            String b = parseTime(actividad.getHasta());
            Log.i("BASEEEa",""+actividad.getDesde());
            Log.i("BASEEE2a",""+actividad.getHasta());
            viewHolder.lblActividad.setText(actividad.getActividad());
            viewHolder.lblDesde.setText("Desde: "+a.substring(0, 5));
            viewHolder.lblHasta.setText("Hasta: "+b.substring(0, 5));

            return convertView;
        }
    }
}
