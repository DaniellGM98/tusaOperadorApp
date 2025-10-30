package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.Spinner;
import android.widget.TextView;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;
import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Capacidad;
import net.ddsmedia.tusa.tusamoviloperador.model.Diametro;
import net.ddsmedia.tusa.tusamoviloperador.model.Forma;
import net.ddsmedia.tusa.tusamoviloperador.model.Largo;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ConvertidorActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;

    private GetFormaTanque mGetFormaTanque = null;
    private GetCapacidadTanque mGetCapacidadTanque = null;
    private GetDiametroTanque mGetDiametroTanque = null;
    private GetLargoTanque mGetLargoTanque = null;
    private GetLitrosTanque mGetLitrosTanque = null;

    ArrayList<String> array_forma = new ArrayList<String>();
    ArrayList<String> array_capacidad = new ArrayList<String>();
    ArrayList<String> array_diametro = new ArrayList<String>();
    ArrayList<String> array_largo = new ArrayList<String>();
    private String[] estado_medidor = {"1", "1.5", "2", "2.5", "3", "3.5", "4", "4.5", "5", "5.5", "6", "6.5", "7", "7.5", "8", "8.5", "9", "9.5", "10", "10.5", "11", "11.5", "12", "12.5", "13", "13.5", "14", "14.5", "15", "15.5", "16", "16.5", "17", "17.5", "18", "18.5", "19", "19.5", "20", "20.5", "21", "21.5", "22", "22.5", "23", "23.5", "24", "24.5", "25", "25.5", "26", "26.5", "27", "27.5", "28", "28.5", "29", "29.5", "30", "30.5", "31", "31.5", "32", "32.5", "33"};

    private Spinner spinForma, spinCapacidad, spinDiametro, spinLargo, spinCentimetros;
    private Button btCalcular;
    private TextView txtLitros;
    private ArrayAdapter adapt1, adapt2, adapt3, adapt4, adapt5;
    String forma, capacidad, diametro, largo, centimetros;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convertidor);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ActionBar bar = getActionBar();
        bar.setTitle(R.string.activity_convertidor);
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mGetFormaTanque = new GetFormaTanque(mUserInfo.getMatricula());
        mGetFormaTanque.execute((Void) null);

        //mGetCapacidadTanque = new GetCapacidadTanque(mUserInfo.getMatricula());
        //mGetCapacidadTanque.execute((Void) null);

        //mGetDiametroTanque = new GetDiametroTanque(mUserInfo.getMatricula());
        //mGetDiametroTanque.execute((Void) null);

        //mGetLargoTanque = new GetLargoTanque(mUserInfo.getMatricula());
        //mGetLargoTanque.execute((Void) null);

        spinForma = (Spinner) findViewById(R.id.spForm);
        spinCapacidad = (Spinner) findViewById(R.id.spCap);
        spinDiametro = (Spinner) findViewById(R.id.spDia);
        spinLargo = (Spinner) findViewById(R.id.spLar);
        spinCentimetros = (Spinner) findViewById(R.id.spCm);
        btCalcular = (Button) findViewById(R.id.btCalcular);
        txtLitros = (TextView)findViewById(R.id.txtLitros);

        btCalcular.setEnabled(false);

        adapt1 = new ArrayAdapter(this, R.layout.spinner_layout, array_forma);
        spinForma.setAdapter(adapt1);

        spinForma.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                forma = adapterView.getSelectedItem().toString();
                btCalcular.setEnabled(false);
                mGetCapacidadTanque = new GetCapacidadTanque(forma);
                mGetCapacidadTanque.execute((Void) null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        adapt2 = new ArrayAdapter(this, R.layout.spinner_layout, array_capacidad);
        spinCapacidad.setAdapter(adapt2);

        spinCapacidad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                capacidad = adapterView.getSelectedItem().toString();
                btCalcular.setEnabled(false);
                mGetDiametroTanque = new GetDiametroTanque(forma, capacidad);
                mGetDiametroTanque.execute((Void) null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        adapt3 = new ArrayAdapter(this, R.layout.spinner_layout, array_diametro);
        spinDiametro.setAdapter(adapt3);

        spinDiametro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                diametro = adapterView.getSelectedItem().toString();
                btCalcular.setEnabled(false);
                mGetLargoTanque = new GetLargoTanque(forma, capacidad, diametro);
                mGetLargoTanque.execute((Void) null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        adapt4 = new ArrayAdapter(this, R.layout.spinner_layout, array_largo);
        spinLargo.setAdapter(adapt4);

        spinLargo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                largo = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        adapt5 = new ArrayAdapter(this, R.layout.spinner_layout, estado_medidor);
        spinCentimetros.setAdapter(adapt5);

        spinCentimetros.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                centimetros = adapterView.getSelectedItem().toString();
                centimetros = "cm"+centimetros.replace('.', '_');
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        btCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log.i("LITROSINFO",""+"SELECT "+centimetros+" FROM Volumetria_tanques WHERE Forma_tanque = '"+forma+"' and Capacidad_gal = "+capacidad+" and Diametro_in = "+diametro+" and Largo_in = "+largo);
                consultarLitros("SELECT "+centimetros+" FROM Volumetria_tanques WHERE Forma_tanque = '"+forma+"' and Capacidad_gal = "+capacidad+" and Diametro_in = "+diametro+" and Largo_in = "+largo);
            }
        });

    }

    private void consultarLitros(String sql){
        txtLitros.setText("");
        mGetLitrosTanque = new GetLitrosTanque(sql);
        mGetLitrosTanque.execute((Void) null);
    }

    public class GetFormaTanque extends AsyncTask<Void, Void, Boolean> {
        private final int mMatricula;
        Boolean isSuccess = false;

        GetFormaTanque(int matricula) {
            mMatricula = matricula;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String[] param = {String.valueOf(mMatricula)};
            String query = Globals.makeQuery(Globals.QUERY_FORMA_TANQUE, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()){
                        Forma formaNueva = new Forma(rs);
                        //Log.i("ORDENINFO",formaNueva.toJSON().toString());
                        array_forma.add(formaNueva.getForma_tanque());
                    }
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetFormaTanque = null;
        }

        @Override
        protected void onCancelled() {
            mGetFormaTanque = null;
        }
    }

    public class GetCapacidadTanque extends AsyncTask<Void, Void, Boolean> {
        private final String mForma;
        Boolean isSuccess = false;

        GetCapacidadTanque(String forma) {
            mForma = forma;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String[] param = {String.valueOf(mForma)};
            String query = Globals.makeQuery(Globals.QUERY_CAPACIDAD_TANQUE, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    array_capacidad = new ArrayList<String>();
                    while (rs.next()){
                        Capacidad capacidadNueva = new Capacidad(rs);
                        //Log.i("CAPACIDADINFO",capacidadNueva.toJSON().toString());
                        array_capacidad.add(capacidadNueva.getCapacidad_gal());
                    }
                    adapt2 = new ArrayAdapter(getBaseContext(), R.layout.spinner_layout, array_capacidad);
                    spinCapacidad.setAdapter(adapt2);
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetCapacidadTanque = null;
        }

        @Override
        protected void onCancelled() {
            mGetCapacidadTanque = null;
        }
    }

    public class GetDiametroTanque extends AsyncTask<Void, Void, Boolean> {
        private final String mForma, mCapacidad;
        Boolean isSuccess = false;

        GetDiametroTanque(String matricula, String capacidad) {
            mForma = matricula;
            mCapacidad = capacidad;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String[] param = {String.valueOf(mForma), String.valueOf(mCapacidad)};
            String query = Globals.makeQuery(Globals.QUERY_DIAMETRO_TANQUE, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    array_diametro = new ArrayList<String>();
                    while (rs.next()){
                        Diametro diametroNueva = new Diametro(rs);
                        //Log.i("CAPACIDADINFO",diametroNueva.toJSON().toString());
                        array_diametro.add(diametroNueva.getDiametro_in());
                    }
                    adapt3 = new ArrayAdapter(getBaseContext(), R.layout.spinner_layout, array_diametro);
                    spinDiametro.setAdapter(adapt3);
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetDiametroTanque = null;
        }

        @Override
        protected void onCancelled() {
            mGetDiametroTanque = null;
        }
    }

    public class GetLargoTanque extends AsyncTask<Void, Void, Boolean> {
        private final String mForma, mCapacidad, mDiametro;
        Boolean isSuccess = false;

        GetLargoTanque(String forma, String capacidad, String diametro) {
            mForma = forma;
            mCapacidad = capacidad;
            mDiametro = diametro;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String[] param = {String.valueOf(mForma), String.valueOf(mCapacidad), String.valueOf(mDiametro)};
            String query = Globals.makeQuery(Globals.QUERY_LARGO_TANQUE, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    array_largo = new ArrayList<String>();
                    while (rs.next()){
                        Largo largoNueva = new Largo(rs);
                        //Log.i("CAPACIDADINFO",largoNueva.toJSON().toString());
                        array_largo.add(largoNueva.getLargo_in());
                    }
                    adapt4 = new ArrayAdapter(getBaseContext(), R.layout.spinner_layout, array_largo);
                    spinLargo.setAdapter(adapt4);
                    btCalcular.setEnabled(true);
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetLargoTanque = null;
        }

        @Override
        protected void onCancelled() {
            mGetLargoTanque = null;
        }
    }

    public class GetLitrosTanque extends AsyncTask<Void, Void, Boolean> {
        private final String sql;
        Boolean isSuccess = false;

        GetLitrosTanque(String consultasql) {
            sql = consultasql;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    StringBuilder resultText = new StringBuilder();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    while (rs.next()){
                        Log.i("LITROSINFO",""+rs.getString(1));
                        boolean bool = rs.wasNull();
                        if(bool) {
                            txtLitros.setText("No hay medida");
                        }else{
                            resultText.append(rs.getString(1)).append(" Litros");
                            //Log.i("LITROSINFO",""+resultText.toString());
                            txtLitros.setText(resultText.toString());
                        }
                    }
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+sql);
                isSuccess = false;
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mGetLitrosTanque = null;
        }

        @Override
        protected void onCancelled() {
            mGetLitrosTanque = null;
        }
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_especiales,menu);
        MenuItem menuItem2 = menu.findItem(R.id.mnu_orden_foto);
        menuItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(!operacion.equals("")) {
                    if ((txtEspecial.getText().toString()).replace(" ", "").equals("")) {
                        Toast.makeText(EspecialesActivity.this, "Número de VIN incorrecto", Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(EspecialesActivity.this, CargaEspecialesActivity.class);
                        intent.putExtra("user", mUserStr);
                        intent.putExtra("name", operacion + "_" + (txtEspecial.getText().toString()).replace(" ", ""));
                        intent.putExtra("operacion", operacion);
                        intent.putExtra("vin", (txtEspecial.getText().toString()).replace(" ", ""));
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        overridePendingTransition(R.anim.open_next, R.anim.close_next);
                    }
                }else{
                    Toast.makeText(EspecialesActivity.this, "Debe seleccionar una operación", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        return true;
    }*/

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