package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Ticket;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;


public class TicketsActivity extends Activity {

    private String mUserStr;
    private int mMatri;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String mOrdenStr;
    private ListView listView;

    private String[] arrEstados;

    private ArrayList<Ticket> arrTickets;
    private OrdenesAdapter adapter;

    private GetOrdenesTask mGetOrdenesTask = null;
    private int mType;
    private int mTipo;
    private View mProgress;
    private TextView mNoOrdenes;

    private Spinner mMeses;

    private int pagina = 0;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        mType = b.getInt("tipo");
        mTipo = b.getInt("tipo");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            mOrdenStr = mUserInfo.getOrden();
            mMatri = mUserInfo.getMatricula();
            Log.i("789",""+mUserInfo.getMatricula());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        arrEstados = getResources().getStringArray(R.array.orden_status);


        ActionBar bar = getActionBar();
        bar.setTitle(R.string.activity_hist);


        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle("Solicitud de  llamada");
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mMeses = (Spinner) findViewById(R.id.spMes);
        //ArrayAdapter<CharSequence> adapterMes = ArrayAdapter.createFromResource(this,
        //        R.array.areas, android.R.layout.simple_spinner_item);
        //adapterMes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //mMeses.setAdapter(adapterMes);
        mMeses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                mGetOrdenesTask = new GetOrdenesTask(mMatri, mOrdenStr, position, 1);
                mGetOrdenesTask.execute();
            }

            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        /*mStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView,
                                       View selectedItemView, int position, long id) {
                adapter.clear();
                adapter.notifyDataSetChanged();
                mGetOrdenesTask = new GetOrdenesTask(mMatri, mOrdenStr, mMeses.getSelectedItemPosition(), position);
                mGetOrdenesTask.execute();
            }

            public void onNothingSelected(AdapterView<?> arg0) {// do nothing
            }
        });*/

        mProgress = findViewById(R.id.pbOrdenes);
        mNoOrdenes = (TextView) findViewById(R.id.lblNoOrdenes);

        //arrOrdenes = new ArrayList<Orden>();
        arrTickets = new ArrayList<Ticket>();
        adapter = new OrdenesAdapter(this,arrTickets);

        listView = (ListView) findViewById(R.id.lstOrdenes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("POS",""+position);

                //Orden orden = (Orden) parent.getItemAtPosition(position);
                Ticket ticket = (Ticket) parent.getItemAtPosition(position);

                //Toast.makeText(TicketsActivity.this, ""+orden.getID()+" | "+orden.getIndicaciones(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(TicketsActivity.this, ""+ticket.getId_geo()+" | "+ticket.getFecha(), Toast.LENGTH_SHORT).show();

                Intent intentAdd = new Intent(getApplicationContext(), SolicitudActivity.class);
                intentAdd.putExtra("id", String.valueOf(ticket.getId_geo()));
                intentAdd.putExtra("inidicaciones",ticket.getFecha());
                intentAdd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentAdd);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }
        });
    }

    public static class OrdenesAdapter extends ArrayAdapter<Ticket> implements Filterable {

        private static class ViewHolder {
            TextView lblOrden;
            TextView lblVin;
            TextView lblOrigen;
            TextView lblDestino;
        }

        public ArrayList<Ticket> tickets;
        public ArrayList<Ticket> filteredOrdenes;
        private OrdenFilter ordenFilter;

        public OrdenesAdapter(Context context, ArrayList<Ticket> tickets){
            super(context,R.layout.orden_item,tickets);
            this.tickets = tickets;
            this.filteredOrdenes = tickets;

            getFilter();
        }

        @Override
        public int getCount() {
            return filteredOrdenes.size();
        }

        @Override
        public Ticket getItem(int i) {
            return filteredOrdenes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Ticket ticket = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {
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

            viewHolder.lblOrden.setText(String.valueOf(ticket.getId_geo()));
            viewHolder.lblVin.setText(ticket.getFecha());
            viewHolder.lblOrigen.setText(String.valueOf(ticket.getId_geo()));
            viewHolder.lblDestino.setText(ticket.getFecha());

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
                ArrayList<Ticket> tempList = new ArrayList<Ticket>();
                if(constraint != null && tickets!=null) {
                    for(Ticket ticket : tickets){
                        if(ticket.getFecha().toLowerCase().contains(constraint.toString().toLowerCase())){
                            tempList.add(ticket);
                        }
                    }
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }else {
                    filterResults.count = tickets.size();
                    filterResults.values = tickets;
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                filteredOrdenes = (ArrayList<Ticket>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    public class GetOrdenesTask extends AsyncTask<Void, Void, ArrayList<Ticket>> {
        private final int id_geo;
        private final String fecha;
        String z = "";
        Boolean isSuccess = false;
        ArrayList<Ticket> ordenes = new ArrayList<Ticket>();
        String mes;
        String edo;

        GetOrdenesTask(int id_geoo, String fechaa, int mes, int edo) {
            id_geo=id_geoo;
            fecha=fechaa;
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
        protected ArrayList<Ticket> doInBackground(Void... params) {
            int offset = pagina * 50;
            String[] param = {String.valueOf(mMatri), "1.3.4", mes, edo, String.valueOf(offset)};
            String query = Globals.makeQuery(Globals.QUERY_LISTTICKETS, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);
                    //adapter = new TicketsActivity.OrdenesAdapter(getApplicationContext(),arrTickets);
                    //                    adapter.clear();
                    while (rs.next()){
                        //Orden ordenNueva = new Orden(rs);
                        Ticket ticketNueva = new Ticket(rs);
                        //ordenNueva.setEstadoStr(arrEstados[ordenNueva.getEstado()]);
                        ordenes.add(ticketNueva);
                        Log.i("ORDENINFO",ticketNueva.toJSON().toString());
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
        protected void onPostExecute(ArrayList<Ticket> ordens) {
            super.onPostExecute(ordens);
            Log.i("TICKETS RES",ordens.size()+" TICKETS encontrados ");
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);
            if(ordens.size() > 0){
                adapter.clear();
                adapter.addAll(ordens);
                adapter.notifyDataSetChanged();
            }else{
                Toast.makeText(TicketsActivity.this,"No hay solicitudes",Toast.LENGTH_SHORT).show();
                mNoOrdenes.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);
        }
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
                this.finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                return true;
            case R.id.mnu_addTicket:
                Intent intentAdd = new Intent(getApplicationContext(), NewTicketActivity.class);
                intentAdd.putExtra("user", mUserStr);
                intentAdd.putExtra("init",false);
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
}

/*
public class TicketsActivity extends Activity {

    private String mUserStr;
    private int mMatri;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String mOrdenStr;
    private ListView listView;

    //private ArrayList<Orden> arrOrdenes;
    private ArrayList<Ticket> arrTickets;
    private TicketsActivity.OrdenesAdapter adapter;

    private TicketsActivity.GetOrdenesTask mGetOrdenesTask = null;

    private int mTipo;
    private View mProgress;
    private TextView mNoOrdenes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);

        //Datos de operador pasados de anterior activity
        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        Log.i("123",""+mUserStr);
        mTipo = b.getInt("tipo");
        Log.i("456",""+mTipo);
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            mOrdenStr = mUserInfo.getOrden();
            mMatri = mUserInfo.getMatricula();
            Log.i("789",""+mUserInfo.getMatricula());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Boton de back y titulo en actionBar
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle("Solicitud de  llamada");
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        //Si no hay ordenes
        mProgress = findViewById(R.id.pbOrdenes);
        mNoOrdenes = (TextView) findViewById(R.id.lblNoOrdenes);

        //arrOrdenes = new ArrayList<Orden>();
        arrTickets = new ArrayList<Ticket>();

        listView = (ListView) findViewById(R.id.lstOrdenes);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                //Orden orden = (Orden) parent.getItemAtPosition(position);
                Ticket ticket = (Ticket) parent.getItemAtPosition(position);

                //Toast.makeText(TicketsActivity.this, ""+orden.getID()+" | "+orden.getIndicaciones(), Toast.LENGTH_SHORT).show();
                //Toast.makeText(TicketsActivity.this, ""+ticket.getId_geo()+" | "+ticket.getFecha(), Toast.LENGTH_SHORT).show();

                Intent intentAdd = new Intent(getApplicationContext(), SolicitudActivity.class);
                intentAdd.putExtra("id", String.valueOf(ticket.getId_geo()));
                intentAdd.putExtra("inidicaciones",ticket.getFecha());
                intentAdd.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentAdd);
                overridePendingTransition(R.anim.open_next, R.anim.close_next);
            }
        });

        mGetOrdenesTask = new TicketsActivity.GetOrdenesTask(mUserInfo.getMatricula(), mOrdenStr);
        mGetOrdenesTask.execute((Void) null);
    }

    public static class OrdenesAdapter extends ArrayAdapter<Ticket>{

        private static class ViewHolder {
            TextView lblOrden;
            TextView lblVin;
            TextView lblOrigen;
            TextView lblDestino;
        }

        public OrdenesAdapter(Context context, ArrayList<Ticket> tickets){
            super(context,R.layout.orden_item,tickets);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Orden orden = getItem(position);
            Ticket ticket = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {
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

            viewHolder.lblOrden.setText(String.valueOf(ticket.getId_geo()));
            viewHolder.lblVin.setText(ticket.getFecha());
            viewHolder.lblOrigen.setText(String.valueOf(ticket.getId_geo()));
            viewHolder.lblDestino.setText(ticket.getFecha());

            return convertView;
        }
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
                // app icon in action bar clicked; go home
                this.finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                return true;
            case R.id.mnu_addTicket:
                Intent intentAdd = new Intent(getApplicationContext(), NewTicketActivity.class);
                intentAdd.putExtra("user", mUserStr);
                intentAdd.putExtra("init",false);
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
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
        //Intent intentt = new Intent(getApplicationContext(), MainActivity.class);
        //intentt.putExtra("user", mUserStr);
        //intentt.putExtra("tipo", Globals.QUERY_DISPONIBLES);
        //intentt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        //startActivity(intentt);
        //overridePendingTransition(R.anim.open_next, R.anim.close_next);
    }

    public class GetOrdenesTask extends AsyncTask<Void, Void, Boolean> {
        private final int id_geo;
        private final String fecha;
        String z = "";
        Boolean isSuccess = false;

        GetOrdenesTask(int id_geoo, String fechaa) {
            id_geo = id_geoo;
            fecha = fechaa;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
            mNoOrdenes.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String[] param = {String.valueOf(mMatri), "1.3.4"};
            //String[] param = {String.valueOf(mMatri), "1.3.4"};

            //String query = Globals.makeQuery(8, param);
            String query = Globals.makeQuery(Globals.QUERY_LISTTICKETS, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    //adapter = new TicketsActivity.OrdenesAdapter(getApplicationContext(),arrOrdenes);
                    adapter = new TicketsActivity.OrdenesAdapter(getApplicationContext(),arrTickets);
                    adapter.clear();
                    while (rs.next()){
                        //Orden ordenNueva = new Orden(rs);
                        Ticket ticketNueva = new Ticket(rs);
                        //adapter.add(ordenNueva);
                        adapter.add(ticketNueva);
                        //Log.i("ORDENINFO",ordenNueva.toJSON().toString());
                        Log.i("ORDENINFO",ticketNueva.toJSON().toString());
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
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);

            if (success) {
                listView.setAdapter(adapter);
            }

            if(adapter.isEmpty()){
                Toast.makeText(TicketsActivity.this,"No hay solicitudes",Toast.LENGTH_SHORT).show();
                mNoOrdenes.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            mGetOrdenesTask = null;
            mProgress.setVisibility(View.GONE);
        }
    }
}

 */