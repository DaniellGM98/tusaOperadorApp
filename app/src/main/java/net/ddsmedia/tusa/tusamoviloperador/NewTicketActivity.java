package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

public class NewTicketActivity extends Activity {

    private EditText txtMensaje2Ticket;
    private Button btnCloseTicket, btnSaveTicket;

    private Spinner spinnerAreasTickets;
    private ArrayList<String> listaInformacion;

    private JSONObject mJSONUserInfo;
    public String mUserStr;
    public String mPwdStr;
    private Usuario mUserInfo;
    private String mUserInfo2;
    private Boolean mInit;

    private UserLoginTask mAuthTask = null;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newticket);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        spinnerAreasTickets = findViewById(R.id.spinnerAreas);
        btnCloseTicket = findViewById(R.id.btnCloseTicket);
        btnSaveTicket = findViewById(R.id.btnSaveTicket);
        txtMensaje2Ticket = findViewById(R.id.txtMensaje2Ticket);

        listaInformacion = new ArrayList<String>();
        listaInformacion.add("Tráfico");
        listaInformacion.add("Asignación");
        listaInformacion.add("Comprobación");
        listaInformacion.add("Administración");
        listaInformacion.add("Nóminas");
        listaInformacion.add("Sistemas");

        ArrayAdapter adaptador = new ArrayAdapter(NewTicketActivity.this, R.layout.spinner_layout,listaInformacion);
        spinnerAreasTickets.setAdapter(adaptador);

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle("Nueva solicitud");
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

        spinnerAreasTickets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long id) {
                //Toast.makeText(NewTicketActivity.this, ""+listaInformacion.get(position), Toast.LENGTH_SHORT).show();
                btnCloseTicket.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intentn = new Intent(getApplicationContext(), MainActivity.class);
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
                        Toast.makeText(NewTicketActivity.this, ""+listaInformacion.get(position)+" | "+txtMensaje2Ticket.getText().toString(), Toast.LENGTH_SHORT).show();

                        //mAuthTask = new UserLoginTask("1988", "1.3.4");
                        //mAuthTask.execute((Void) null);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
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

    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String fk_op;
        private final String app_version;
        Boolean isSuccess = false;

        UserLoginTask(String idd, String version){
            fk_op = idd;
            app_version = version;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] param = {fk_op, app_version};
            String query = Globals.makeQuery(Globals.QUERY_TICKETS, param);
            PreparedStatement preparedStatement;
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;
                    Log.i("Success","success");
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL\n"+ex.toString()+"\n"+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.putExtra("user", mUserInfo2);
                startActivity(intent);
                finish();
                Log.i("Excelent","OK");
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }
}
