package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Orden;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class HomeActivity extends Activity {

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
    private Button mBtnPanico;

    private LinearLayout mInfoView;

    private GetOrdenTask mGetOrdenTask;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            mOrdenStr = mUserInfo.getOrden();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mOrdenView = (TextView) findViewById(R.id.txtOrden);
        mProgress = (ProgressBar) findViewById(R.id.pbOrden);
        mInfoView = (LinearLayout) findViewById(R.id.ordeninfoView);
        mVin = (TextView) findViewById(R.id.txtVin);
        mOriDest = (TextView) findViewById(R.id.txtOriDest);
        mDomi = (TextView) findViewById(R.id.txtDomi);
        mIndi = (TextView) findViewById(R.id.txtIndi);
        mBtnSave = (Button) findViewById(R.id.btnSaveP);
        mBtnPanico = (Button) findViewById(R.id.btnPanico);

        mInfoView.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
        if(!mOrdenStr.isEmpty()){
            mOrdenView.setText(mUserInfo.getOrden());
            mProgress.setVisibility(View.VISIBLE);
            mGetOrdenTask = new GetOrdenTask(mOrdenStr);
            mGetOrdenTask.execute((Void) null);
        }else{
            mOrdenView.setText("Sin Asignar");
        }

    }

    private void fillInfo(Orden info){
        mVin.setText(info.getVIN());
        mOriDest.setText(info.getOrigen()+" - "+info.getDestino());
        mDomi.setText(info.getDirOrigen()+" - "+info.getDirDestino());
        mIndi.setText(info.getIndicaciones());

        if(info.getEstado() == Globals.ORDEN_ASIGNADA){
            mBtnSave.setText(getString(R.string.orden_accept));
        }else if(info.getEstado() == Globals.ORDEN_INICIADA){
            mBtnSave.setText(getString(R.string.orden_finish));
        }

        mInfoView.setVisibility(View.VISIBLE);
        mOrden = info;
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
            String query = "SELECT ID_orden, No_chasis, Instrucciones_operador, " +
                    "(SELECT estado FROM orden_status WHERE fk_orden = '" + mOrden + "') AS estado, " +
                    "(SELECT Nombre FROM Directorio WHERE ID_entidad = Origen) AS nomOrigen, " +
                    "(SELECT CONCAT(Direccion,' ',Colonia) FROM Directorio WHERE ID_entidad = Origen) AS dirOrigen, " +
                    "(SELECT CONCAT(Celular,' ',Contacto) FROM Directorio WHERE ID_entidad = Origen) AS contOrigen, " +
                    "(SELECT CONCAT(Poblacion,', ',Estado) FROM Poblaciones WHERE ID_poblacion = (SELECT id_origen FROM Tipo_de_ruta_N WHERE ID_Tipo_ruta = o.Id_tipo_ruta)) AS pobOrigen, " +
                    "(SELECT Nombre FROM Directorio WHERE ID_entidad = Destino) AS nomDestino, " +
                    "(SELECT CONCAT(Direccion,' ',Colonia) FROM Directorio WHERE ID_entidad = Destino) AS dirDestino, " +
                    "(SELECT CONCAT(Celular,' ',Contacto) FROM Directorio WHERE ID_entidad = Destino) AS contDestino, " +
                    "(SELECT CONCAT(Poblacion,', ',Estado) FROM Poblaciones WHERE ID_poblacion = (SELECT id_destino FROM Tipo_de_ruta_N WHERE ID_Tipo_ruta = o.Id_tipo_ruta)) AS pobDestino " +
                    "FROM Orden_traslados AS o " +
                    "WHERE ID_orden = '" + mOrden + "'";
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

            if (success) {
                fillInfo(mOrdenT);
            }
        }

        @Override
        protected void onCancelled() {
            mGetOrdenTask = null;
        }
    }
}
