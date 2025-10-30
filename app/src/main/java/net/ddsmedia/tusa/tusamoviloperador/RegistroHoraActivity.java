package net.ddsmedia.tusa.tusamoviloperador;

import static net.ddsmedia.tusa.tusamoviloperador.BitacoraActivity.parseTime;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.db.MyOpenHelper;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class RegistroHoraActivity extends Activity {

    private Button BtnHora1, BtnHora2, btnCloseTicket, btnSaveTicket;

    private Spinner spinnerAreasTickets;
    private ArrayList<String> listaInformacion;

    private JSONObject mJSONUserInfo;
    public String mUserStr;
    public String mPwdStr;
    private Usuario mUserInfo;
    private String mUserInfo2;
    private Boolean mInit;

    int hour1=0, minute1=0, conv1=0, conv2=0, dif1, hour5=0, minute5=0;
    String Total1;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrohora);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinnerAreasTickets = findViewById(R.id.spinnerAreas);
        BtnHora1 = findViewById(R.id.BtnHora1);
        BtnHora2 = findViewById(R.id.BtnHora2);
        btnCloseTicket = findViewById(R.id.btnCloseTicket);
        btnSaveTicket = findViewById(R.id.btnSaveTicket);
        listaInformacion = new ArrayList<String>();
        listaInformacion.add("Fuera de Servicio");
        listaInformacion.add("En Servicio (Sin manejar)");
        listaInformacion.add("En translado (Manejando)");
        listaInformacion.add("Paradas no programadas");

        ArrayAdapter adaptador = new ArrayAdapter(RegistroHoraActivity.this, R.layout.spinner_layout,listaInformacion);
        spinnerAreasTickets.setAdapter(adaptador);

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle("Nueva solicitud");
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mInit = b.getBoolean("init");

        if(b.getInt("hasta") != 0 && b.getString("actividad") != ""){
            //Toast.makeText(RegistroHoraActivity.this, "ok: "+b.getInt("desde"), Toast.LENGTH_SHORT).show();
            String q = parseTime(b.getInt("desde"));
            String w = parseTime(b.getInt("hasta"));
            hour1 = Integer.parseInt(q.substring(0,2));
            minute1 = Integer.parseInt(q.substring(3,5));
            hour5 = Integer.parseInt(w.substring(0,2));
            minute5 = Integer.parseInt(w.substring(3,5));
            Toast.makeText(RegistroHoraActivity.this, "ok: "+q.substring(3,5), Toast.LENGTH_SHORT).show();
        }

        SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
        mPwdStr = loginData.getString("password","");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        spinnerAreasTickets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long id) {
                //Toast.makeText(NewTicketActivity.this, ""+listaInformacion.get(position), Toast.LENGTH_SHORT).show();
                btnCloseTicket.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intentn = new Intent(getApplicationContext(), BitacoraActivity.class);
                        intentn.putExtra("user", mUserStr);
                        intentn.putExtra("tipo", Globals.QUERY_DISPONIBLES);
                        intentn.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intentn);
                        overridePendingTransition(R.anim.open_next, R.anim.close_next);
                    }
                });

                btnSaveTicket.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(conv1 >= conv2){
                            Toast.makeText(RegistroHoraActivity.this, "Verifica tus datos", Toast.LENGTH_SHORT).show();
                        }else{
                            if(hour5==0 && minute5==0){
                                Toast.makeText(RegistroHoraActivity.this, "Verifica tus datos", Toast.LENGTH_SHORT).show();
                            }else{
                                int conv11 = (((hour1 * 60) + minute1) * 60000);
                                int conv12 = (((hour5 * 60) + minute5) * 60000);

                                dif1 = conv12-conv11;
                                String tota1 = parseTime(dif1);
                                Total1 = tota1.substring(0, 5);

                                //Insert actividad
                                Globals.conn = new MyOpenHelper(getApplicationContext());
                                Globals.db = Globals.conn.getReadableDatabase();
                                if (Globals.db != null) {
                                    Globals.db.execSQL("Replace into bitacora values(null, "+conv11+", "+conv12+", '"+listaInformacion.get(position)+"')");
                                }
                                Globals.db.close();

                                Toast.makeText(RegistroHoraActivity.this, ""+listaInformacion.get(position)+" | "+Total1, Toast.LENGTH_SHORT).show();

                                Intent intentAdd = new Intent(getApplicationContext(), BitacoraActivity.class);
                                intentAdd.putExtra("user", mUserStr);
                                intentAdd.putExtra("tipo", Globals.QUERY_DISPONIBLES);
                                //intentAdd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                startActivity(intentAdd);
                                overridePendingTransition(R.anim.open_next, R.anim.close_next);
                            }
                        }
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        BtnHora1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                if(hour1 == 0 && minute1 == 0){
                    hour1 = c.get(Calendar.HOUR_OF_DAY);
                    minute1 = c.get(Calendar.MINUTE);
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(RegistroHoraActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hora, int minuto) {
                        if(hora<12) {
                            if (hora < 10) {
                                if (minuto < 10) {
                                    BtnHora1.setText("0" + hora + ":0" + minuto+" AM");
                                } else {
                                    BtnHora1.setText("0" + hora + ":" + minuto+" AM");
                                }
                            } else {
                                if (minuto < 10) {
                                    BtnHora1.setText("" + hora + ":0" + minuto+" AM");
                                } else {
                                    BtnHora1.setText("" + hora + ":" + minuto+" AM");
                                }
                            }
                        }else{
                            if (hora < 10) {
                                if (minuto < 10) {
                                    BtnHora1.setText("0" + hora + ":0" + minuto+" PM");
                                } else {
                                    BtnHora1.setText("0" + hora + ":" + minuto+" PM");
                                }
                            } else {
                                if (minuto < 10) {
                                    BtnHora1.setText("" + hora + ":0" + minuto+" PM");
                                } else {
                                    BtnHora1.setText("" + hora + ":" + minuto+" PM");
                                }
                            }
                        }
                        conv1 = (((hora * 60) + minuto) * 60000);
                        hour1 = hora;
                        minute1 = minuto;
                    }
                },hour1,minute1,false);
                timePickerDialog.show();
                timePickerDialog.setCancelable(false);
            }
        });

        BtnHora2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                if(hour5 == 0 && minute5 == 0){
                    hour5 = c.get(Calendar.HOUR_OF_DAY);
                    minute5 = c.get(Calendar.MINUTE);
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(RegistroHoraActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hora, int minuto) {
                        if(((hora * 60) + minuto) * 60000 > conv1) {
                            if(hora<12) {
                                if (hora < 10) {
                                    if (minuto < 10) {
                                        BtnHora2.setText("0" + hora + ":0" + minuto+" AM");
                                    } else {
                                        BtnHora2.setText("0" + hora + ":" + minuto+" AM");
                                    }
                                } else {
                                    if (minuto < 10) {
                                        BtnHora2.setText("" + hora + ":0" + minuto+" AM");
                                    } else {
                                        BtnHora2.setText("" + hora + ":" + minuto+" AM");
                                    }
                                }
                            }else{
                                if (hora < 10) {
                                    if (minuto < 10) {
                                        BtnHora2.setText("0" + hora + ":0" + minuto+" PM");
                                    } else {
                                        BtnHora2.setText("0" + hora + ":" + minuto+" PM");
                                    }
                                } else {
                                    if (minuto < 10) {
                                        BtnHora2.setText("" + hora + ":0" + minuto+" PM");
                                    } else {
                                        BtnHora2.setText("" + hora + ":" + minuto+" PM");
                                    }
                                }
                            }
                            conv2 = (((hora * 60) + minuto) * 60000);
                            hour5 = hora;
                            minute5 = minuto;
                        }else{
                            Toast.makeText(RegistroHoraActivity.this, "La hora ingresada es incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    }
                },hour5,minute5,false);
                timePickerDialog.show();
                timePickerDialog.setCancelable(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                this.finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
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
}
