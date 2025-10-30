package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;

public class NoAsignadasActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String mOrdenStr;
    private ListView listView;

    private ArrayList<Orden> arrOrdenes;
    private OrdenesAdapter adapter;

    private GetOrdenesTask mGetOrdenesTask = null;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mas_ordenes);
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

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle(R.string.activity_mas);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        arrOrdenes = new ArrayList<Orden>();
        //adapter = new OrdenesAdapter(this,arrOrdenes);

        listView = (ListView) findViewById(R.id.lstOrdenes);
        //listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Orden orden = (Orden) parent.getItemAtPosition(position);
                AlertDialog alertDialog = new AlertDialog.Builder(NoAsignadasActivity.this).create();

                String msg = "ORIGEN\n" + orden.getOrigen()+"\n"+orden.getDirOrigen()+"\n"+orden.getContOrigen()+"\n\n";
                msg += "DESTINO\n" + orden.getDestino()+"\n"+orden.getDirDestino()+"\n"+orden.getContDestino();

                if(!orden.getIndicaciones().isEmpty()){
                    msg += "\n\nINDICACIONES\n"+orden.getIndicaciones();
                }

                //alertDialog.setTitle("Info");
                alertDialog.setMessage(msg);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula(), mOrdenStr);
        mGetOrdenesTask.execute((Void) null);
    }

    public static class OrdenesAdapter extends ArrayAdapter<Orden>{

        private static class ViewHolder {
            TextView lblOrden;
            TextView lblVin;
            TextView lblOrigen;
            TextView lblDestino;
        }


        public OrdenesAdapter(Context context, ArrayList<Orden> ordenes){
            super(context,R.layout.orden_item,ordenes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Orden orden = getItem(position);

            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                // If there's no view to re-use, inflate a brand new view for row
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.orden_item, parent, false);

                viewHolder.lblOrden = (TextView) convertView.findViewById(R.id.lblOrdeni);
                viewHolder.lblVin = (TextView) convertView.findViewById(R.id.lblVini);
                viewHolder.lblOrigen = (TextView) convertView.findViewById(R.id.lblOrigeni);
                viewHolder.lblDestino = (TextView) convertView.findViewById(R.id.lblDestinoi);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            /*if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.orden_item, parent, false);
            }

            TextView lblOrden = (TextView) convertView.findViewById(R.id.lblOrdeni);
            TextView lblVin = (TextView) convertView.findViewById(R.id.lblVini);
            TextView lblOrigen = (TextView) convertView.findViewById(R.id.lblOrigeni);
            TextView lblDestino = (TextView) convertView.findViewById(R.id.lblDestinoi);*/

            viewHolder.lblOrden.setText(orden.getID());
            viewHolder.lblVin.setText(orden.getVIN());
            viewHolder.lblOrigen.setText(orden.getOrigen());
            viewHolder.lblDestino.setText(orden.getDestino());

            return convertView;
        }
    }

    public class GetOrdenesTask extends AsyncTask<Void, Void, Boolean> {
        private final int mMatricula;
        private final String mOrden;
        String z = "";
        Boolean isSuccess = false;

        GetOrdenesTask(int matricula, String orden) {
            mMatricula = matricula;
            mOrden = orden;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] param = {mOrden, String.valueOf(mMatricula)};
            String query = Globals.makeQuery(Globals.QUERY_PENDIENTES, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    adapter = new OrdenesAdapter(getApplicationContext(),arrOrdenes);
                    adapter.clear();
                    while (rs.next()){
                        Orden ordenNueva = new Orden(rs);
                        adapter.add(ordenNueva);
                        Log.i("ORDENINFO",ordenNueva.toJSON().toString());
                    }
                    isSuccess=true;

                    if(adapter.getCount() == 0){
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
            mGetOrdenesTask = null;

            if (success) {
                //fillInfo(mOrdenT);
                listView.setAdapter(adapter);
            }
        }

        @Override
        protected void onCancelled() {
            mGetOrdenesTask = null;
        }
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
