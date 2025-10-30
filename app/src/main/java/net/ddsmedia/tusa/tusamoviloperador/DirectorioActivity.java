package net.ddsmedia.tusa.tusamoviloperador;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Contacto;
import net.ddsmedia.tusa.tusamoviloperador.model.Orden;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class DirectorioActivity extends Activity implements SearchView.OnQueryTextListener {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private ListView listView;

    //private ArrayList<Orden> arrOrdenes;
    private ArrayList<Contacto> arrContactos;
    //private OrdenesAdapter adapter;
    private ContactosAdapter adapter;

    //private GetOrdenesTask mGetOrdenesTask = null;
    private GetContactosTask mGetContactosTask = null;
    private int mType;
    private View mProgress;
    private TextView mNoOrdenes;

    private int pagina = 0;
    private Boolean flag_loading = false;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directorio);
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

        ActionBar bar = getActionBar();
        bar.setTitle(R.string.activity_directorio);

        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        mProgress = findViewById(R.id.pbContactos);
        mNoOrdenes = (TextView) findViewById(R.id.lblNoComtactos);

        arrContactos = new ArrayList<Contacto>();
        adapter = new ContactosAdapter(this,arrContactos);

        // Ejecutar consulta
        adapter.clear();
        adapter.notifyDataSetChanged();
        mGetContactosTask = new GetContactosTask(mUserInfo.getMatricula());
        mGetContactosTask.execute();
        //

        listView = (ListView) findViewById(R.id.lstContactos);
        listView.setAdapter(adapter);
    }

    public class ContactosAdapter extends ArrayAdapter<Contacto> implements Filterable {

        private class ViewHolder {
            TextView lblMatricula;
            TextView lblEmpleado;
            TextView lblLineaAsignada;
            Button btnWhats;
            Button btnAddContact;
            Button btnCall;
        }

        public ArrayList<Contacto> contactos;
        public ArrayList<Contacto> filteredContactos;
        private ContactoFilter contactoFilter;

        public ContactosAdapter(Context context, ArrayList<Contacto> contactos){
            super(context,R.layout.contacto_item,contactos);
            this.contactos = contactos;
            this.filteredContactos = contactos;

            getFilter();
        }

        @Override
        public int getCount() {
            return filteredContactos.size();
        }

        @Override
        public Contacto getItem(int i) {
            return filteredContactos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Contacto contacto = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.contacto_item, parent, false);

                viewHolder.lblMatricula = (TextView) convertView.findViewById(R.id.lblMatricula);
                viewHolder.lblEmpleado = (TextView) convertView.findViewById(R.id.lblEmpleado);
                viewHolder.lblLineaAsignada = (TextView) convertView.findViewById(R.id.lblLineaAsignada);

                viewHolder.btnWhats = (Button) convertView.findViewById(R.id.btnWhats);
                viewHolder.btnAddContact = (Button) convertView.findViewById(R.id.btnAddContact);
                viewHolder.btnCall = (Button) convertView.findViewById(R.id.btnCall);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.lblMatricula.setText("Matrícula: "+String.valueOf(contacto.getMatricula()));
            viewHolder.lblEmpleado.setText(contacto.getEmpleado());
            viewHolder.lblLineaAsignada.setText(contacto.getLinea_Asignada());

            viewHolder.btnWhats.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                    String msj = "";
                    String numeroTel = contacto.getLinea_Asignada();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    String uri = "whatsapp://send?phone=" + numeroTel + "&text=" + msj;
                    intent.setData(Uri.parse(uri));
                    startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "¡WhatsApp no esta instalado!", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });

            viewHolder.btnAddContact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(
                            ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                            Uri.parse("tel:" + contacto.getLinea_Asignada()));
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, contacto.getEmpleado());
                    intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true);
                    startActivity(intent);
                }
            });

            viewHolder.btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri number = Uri.parse("tel:" + contacto.getLinea_Asignada());
                    Intent dial = new Intent(Intent.ACTION_DIAL, number);
                    startActivity(dial); // Ejecutamos el Intent
                }
            });

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if(contactoFilter == null){
                contactoFilter = new ContactoFilter();
            }
            return contactoFilter;
        }

        private class ContactoFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Contacto> tempList = new ArrayList<Contacto>();
                if(constraint != null && contactos!=null) {
                    for(Contacto contacto : contactos){
                        if(contacto.getEmpleado().toLowerCase().contains(constraint.toString().toLowerCase()) || String.valueOf(contacto.getMatricula()).contains(constraint.toString().toLowerCase())){
                            tempList.add(contacto);
                        }
                    }
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }else {
                    filterResults.count = contactos.size();
                    filterResults.values = contactos;
                }
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                filteredContactos = (ArrayList<Contacto>) results.values;
                notifyDataSetChanged();
            }
        }
    }

    public class GetContactosTask extends AsyncTask<Void, Void, ArrayList<Contacto>> {
        private final int mMatricula;
        String z = "";
        Boolean isSuccess = false;
        ArrayList<Contacto> ordenes = new ArrayList<Contacto>();

        GetContactosTask(int matricula) {
            mMatricula = matricula;
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
        protected ArrayList<Contacto> doInBackground(Void... params) {
            String[] param = {String.valueOf(mMatricula)};
            String query = Globals.makeQuery(Globals.QUERY_DIRECTORIO, param);

            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()){
                        Contacto contactoNueva = new Contacto(rs);
                        ordenes.add(contactoNueva);
                        Log.i("ORDENINFO",contactoNueva.toJSON().toString());
                    }
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" "+query);
                isSuccess=false;
            }
            return ordenes;
        }

        @Override
        protected void onPostExecute(ArrayList<Contacto> contactos) {
            //flag_loading = false;
            super.onPostExecute(contactos);
            Log.i("ORDENES RES",contactos.size()+" contactos encontrados ");
            mGetContactosTask = null;
            mProgress.setVisibility(View.GONE);
            if(contactos.size() > 0){
                adapter.clear();
                adapter.addAll(contactos);
                adapter.notifyDataSetChanged();
            }else{
                Toast.makeText(DirectorioActivity.this,"No hay contactos",Toast.LENGTH_SHORT).show();
                mNoOrdenes.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onCancelled() {
            //flag_loading = false;
            mGetContactosTask = null;
            mProgress.setVisibility(View.GONE);
        }
    }

    private class ExportarContactosTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(DirectorioActivity.this);
            pd.setMessage("Agregando contactos...");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            for (Contacto contacto : arrContactos) {
                ops.clear();

                ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());

                // Nombre del contacto
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contacto.getMatricula()+" "+contacto.getEmpleado())
                        .build());

                // Número de teléfono
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contacto.getLinea_Asignada())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());

                try {
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
            Toast.makeText(DirectorioActivity.this, result ? "Contactos agregados con éxito" : "Error al exportar contactos", Toast.LENGTH_SHORT).show();
        }
    }

    private void exportarContactos() {
        new ExportarContactosTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mnu_directorio, menu);

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
            case R.id.action_export_contacts:
                Log.i("no","okokokko");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSION_REQUEST_CODE);
                    } else {
                        exportarContactos();
                    }
                } else {
                    exportarContactos();
                }
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
