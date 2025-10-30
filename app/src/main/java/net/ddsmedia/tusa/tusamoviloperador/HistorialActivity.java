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
import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class HistorialActivity extends Activity implements SearchView.OnQueryTextListener {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    //private String mOrdenStr;
    private ListView listView;

    private String[] arrEstados;

    private ArrayList<Orden> arrOrdenes;
    private OrdenesAdapter adapter;

    private GetOrdenesTask mGetOrdenesTask = null;
    private int mType;
    private View mProgress;
    private TextView mNoOrdenes;

    private Spinner mMeses;
    private Spinner mStatus;

    private int pagina = 0;
    private Boolean flag_loading = false;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mType = b.getInt("tipo");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            //mOrdenStr = "";//mUserInfo.getOrden();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        arrEstados = getResources().getStringArray(R.array.orden_status);


        ActionBar bar = getActionBar();
        bar.setTitle(R.string.activity_hist);


        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mMeses = (Spinner) findViewById(R.id.spMes);
        ArrayAdapter<CharSequence> adapterMes = ArrayAdapter.createFromResource(this,
                R.array.hist_meses, android.R.layout.simple_spinner_item);
        adapterMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mMeses.setAdapter(adapterMes);
        mMeses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula(), position, mStatus.getSelectedItemPosition());
                mGetOrdenesTask.execute();
            }

            public void onNothingSelected(AdapterView<?> arg0) {// do nothing
            }
        });

        mStatus = (Spinner) findViewById(R.id.spEdo);
        ArrayAdapter<CharSequence> adapterEdo = ArrayAdapter.createFromResource(this,
                R.array.hist_status, android.R.layout.simple_spinner_item);
        adapterEdo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatus.setAdapter(adapterEdo);
        mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula(), mMeses.getSelectedItemPosition(), position);
                mGetOrdenesTask.execute();
            }

            public void onNothingSelected(AdapterView<?> arg0) {// do nothing
            }
        });

        mProgress = findViewById(R.id.pbOrdenes);
        mNoOrdenes = (TextView) findViewById(R.id.lblNoOrdenes);

        arrOrdenes = new ArrayList<Orden>();
        adapter = new OrdenesAdapter(this,arrOrdenes);

        listView = (ListView) findViewById(R.id.lstOrdenes);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View v,
                                           int position, long arg3) {
                Orden orden = (Orden) parent.getItemAtPosition(position);
                Intent intent = new Intent(HistorialActivity.this, EvidenciaActivity.class);
                intent.putExtra("user", mUserStr);
                intent.putExtra("orden", orden.getID());
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition (R.anim.open_next, R.anim.close_next);
                return true;
            }
        });

        //mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula(),0,0);
        //mGetOrdenesTask.execute();

        /*listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0){
                    if(flag_loading == false && totalItemCount > 49){
                        flag_loading = true;
                        pagina++;
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        mGetOrdenesTask = new GetOrdenesTask(mUserInfo.getMatricula(), mMeses.getSelectedItemPosition(), mStatus.getSelectedItemPosition());
                        mGetOrdenesTask.execute();
                    }
                }
            }
        });*/

    }

    public static class OrdenesAdapter extends ArrayAdapter<Orden> implements Filterable {

        private static class ViewHolder {
            TextView lblOrden;
            TextView lblFecha;
            TextView lblOrigen;
            TextView lblDestino;
            TextView lblStatus;
            TextView lblMonto;
        }

        public ArrayList<Orden> ordenes;
        public ArrayList<Orden> filteredOrdenes;
        private OrdenFilter ordenFilter;

        public OrdenesAdapter(Context context, ArrayList<Orden> ordenes){
            super(context,R.layout.ordenh_item,ordenes);
            this.ordenes = ordenes;
            this.filteredOrdenes = ordenes;

            getFilter();
        }

        @Override
        public int getCount() {
            return filteredOrdenes.size();
        }

        @Override
        public Orden getItem(int i) {
            return filteredOrdenes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Orden orden = getItem(position);

            ViewHolder viewHolder; // view lookup cache stored in tag
            if (convertView == null) {
                // If there's no view to re-use, inflate a brand new view for row
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.ordenh_item, parent, false);

                viewHolder.lblOrden = (TextView) convertView.findViewById(R.id.lblOrdeni);
                viewHolder.lblFecha = (TextView) convertView.findViewById(R.id.lblFechai);
                viewHolder.lblOrigen = (TextView) convertView.findViewById(R.id.lblOrigeni);
                viewHolder.lblDestino = (TextView) convertView.findViewById(R.id.lblDestinoi);
                viewHolder.lblStatus = (TextView) convertView.findViewById(R.id.lblStatus);
                viewHolder.lblMonto = (TextView) convertView.findViewById(R.id.lblMonto);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.lblOrden.setText(orden.getID());
            viewHolder.lblFecha.setText(orden.getIniciada());
            viewHolder.lblOrigen.setText(orden.getOrigen());
            viewHolder.lblDestino.setText(orden.getDestino());
            if(orden.getContDestino().equals("NO")){
                if (orden.getSemanaDePago() != null) {
                    viewHolder.lblStatus.setText("Sin Comprobar - Semana "+orden.getSemanaDePago());
                } else {
                    viewHolder.lblStatus.setText("Sin Comprobar - Sin semana");
                }
            }else if(orden.getPobDestino().equals("NO")){
                if (orden.getSemanaDePago() != null) {
                    viewHolder.lblStatus.setText("Pendiente Pago - Semana "+orden.getSemanaDePago());
                } else {
                    viewHolder.lblStatus.setText("Sin Comprobar - Sin semana");
                }
            }else{
                if (orden.getSemanaDePago() != null) {
                    viewHolder.lblStatus.setText("PAGADA - Semana "+orden.getSemanaDePago());
                } else {
                    viewHolder.lblStatus.setText("Sin Comprobar - Sin semana");
                }
            }

            String monto = orden.getDirDestino();
            if(orden.getContDestino().equals("SI") && (Double.parseDouble(orden.getPobOrigen()) > 1.00 || Double.parseDouble(orden.getPobOrigen()) < -1.00)){
                monto += " (" + orden.getPobOrigen() + ")";
            }
            viewHolder.lblMonto.setText(monto);

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if(ordenFilter == null){
                ordenFilter = new OrdenFilter();
            }
            return ordenFilter;
        }

        private class OrdenFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Orden> tempList = new ArrayList<Orden>();
                if(constraint != null && ordenes!=null) {

                    for(Orden orden : ordenes){
                        if(orden.getID().toLowerCase().contains(constraint.toString().toLowerCase())){
                            tempList.add(orden);
                        }
                    }
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }else {
                    filterResults.count = ordenes.size();
                    filterResults.values = ordenes;
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                filteredOrdenes = (ArrayList<Orden>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    public class GetOrdenesTask extends AsyncTask<Void, Void, ArrayList<Orden>> {
        private final int mMatricula;
        String z = "";
        Boolean isSuccess = false;
        ArrayList<Orden> ordenes = new ArrayList<Orden>();
        String mes;
        String edo;

        GetOrdenesTask(int matricula, int mes, int edo) {
            mMatricula = matricula;
            this.mes = String.valueOf(mes);
            this.edo = String.valueOf(edo);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
            mNoOrdenes.setVisibility(View.GONE);
            adapter.clear();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected ArrayList<Orden> doInBackground(Void... params) {
            int offset = pagina * 50;
            String[] param = {String.valueOf(mMatricula), mes, edo, String.valueOf(offset)};
            String query = Globals.makeQuery(Globals.QUERY_HISTORIAL, param);

            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    //Log.i("QUERY_DEBUG", query);

                    while (rs.next()){
                        //Log.i("RS_LOG","ID_orden: " + rs.getString("ID_orden") + ", " + "Semana_de_pago: " + rs.getString("Semana_de_pago"));
                        Orden ordenNueva = new Orden(rs);
                        //ordenNueva.setEstadoStr(arrEstados[ordenNueva.getEstado()]);
                        ordenes.add(ordenNueva);
                        Log.i("ORDENINFO",ordenNueva.toJSON().toString());
                    }
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess = false;
            }
            return ordenes;
        }

        @Override
        protected void onPostExecute(ArrayList<Orden> ordens) {
            //flag_loading = false;
            super.onPostExecute(ordens);
            Log.i("ORDENES RES",ordens.size()+" ordenes encontradas ");
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);
            if(ordens.size() > 0){
                adapter.clear();
                adapter.addAll(ordens);
                adapter.notifyDataSetChanged();
            }else{
                Toast.makeText(HistorialActivity.this,"No hay ordenes",Toast.LENGTH_SHORT).show();
                mNoOrdenes.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            //flag_loading = false;
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mnu_ordenes, menu);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.orden_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);

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
