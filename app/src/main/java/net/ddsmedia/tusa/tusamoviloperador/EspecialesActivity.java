package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.db.MyOpenHelper;
import net.ddsmedia.tusa.tusamoviloperador.model.Operacion;
import net.ddsmedia.tusa.tusamoviloperador.model.Orden;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class EspecialesActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;

    private String[] arrOperaciones;

    private GetOperacionesTask mGetOperacionesTask = null;
    private int mType;

    private EditText txtEspecial;

    //private String[] estado_medidor = {"Operacion1", "Operacion2", "Operacion3"};
    ArrayList<String> array_operaciones = new ArrayList<String>();
    private Spinner spinEspecial;
    private ArrayAdapter adapt;
    String operacion;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_especiales);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mType = b.getInt("tipo");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        arrOperaciones = getResources().getStringArray(R.array.orden_status);//

        ActionBar bar = getActionBar();
        bar.setTitle(R.string.activity_especiales);
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mGetOperacionesTask = new GetOperacionesTask(mUserInfo.getMatricula());
        mGetOperacionesTask.execute((Void) null);

        //spinner
        txtEspecial = findViewById(R.id.txtVNI);
        spinEspecial = (Spinner) findViewById(R.id.spEsp);

        /*ArrayAdapter<CharSequence> adapterEsp = ArrayAdapter.createFromResource(this,
                R.array.operaciones, android.R.layout.simple_spinner_item);
        adapterEsp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinEspecial.setAdapter(adapterEsp);*/

        //spinEspecial.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, estado_medidor));

        adapt = new ArrayAdapter(this, R.layout.spinner_layout, array_operaciones);
        spinEspecial.setAdapter(adapt);
        //spinner

        spinEspecial.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                operacion = adapterView.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }

    public class GetOperacionesTask extends AsyncTask<Void, Void, Boolean> {
        private final int mMatricula;
        Boolean isSuccess = false;

        GetOperacionesTask(int matricula) {
            mMatricula = matricula;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String[] param = {String.valueOf(mMatricula)};
            String query = Globals.makeQuery(Globals.QUERY_OPERACIONES, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()){
                        Operacion operacionNueva = new Operacion(rs);
                        //Log.i("ORDENINFO",operacionNueva.toJSON().toString());
                        array_operaciones.add(operacionNueva.getNombre());
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
            mGetOperacionesTask = null;
        }

        @Override
        protected void onCancelled() {
            mGetOperacionesTask = null;
        }
    }

    @Override
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