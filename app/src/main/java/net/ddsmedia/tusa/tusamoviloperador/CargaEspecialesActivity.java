package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.squareup.picasso.Picasso;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Especial;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class CargaEspecialesActivity extends Activity {

    private String mUserStr, name, operacion, vin, rutaImagen;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;

    private ImageView imgFoto;
    private Button btnFoto;
    private Uri fotoUri;
    static final int REQUEST_TAKE_PHOTO = 1;

    private ListView listEspeciales;
    private ArrayList<Especial> listaEspeciales;
    private EspecialAdapter adaptador;

    private String[] imagenes = null;
    private int foto=0;
    private Boolean bandera = true;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carga_especiales);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        name = b.getString("name");
        operacion = b.getString("operacion");
        vin = b.getString("vin");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ActionBar bar = getActionBar();
        bar.setTitle(R.string.activity_carga_especiales);
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);

        btnFoto = (Button) findViewById(R.id.btnFoto);
        imgFoto = (ImageView) findViewById(R.id.imgFoto);

        btnFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(foto>=9){
                    Toast.makeText(CargaEspecialesActivity.this, "Se ha alcanzado el máximo de imágenes", Toast.LENGTH_SHORT).show();
                }else{
                    abrirCamara();
                }
            }
        });

        listEspeciales = findViewById(R.id.listviewEspeciales);
        File storageDir = getExternalFilesDir("Especiales");
        File[] files = storageDir.listFiles();
        if(files.length>0){
            consultarListaEspeciales();
            adaptador = new CargaEspecialesActivity.EspecialAdapter(CargaEspecialesActivity.this, listaEspeciales);
            listEspeciales.setAdapter(adaptador);
            listEspeciales.setClickable(true);
            listEspeciales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Especial item = (Especial) adapterView.getItemAtPosition(position);
                    Intent intent = new Intent(CargaEspecialesActivity.this, VerActivity.class);
                    intent.putExtra("user", mUserStr);
                    intent.putExtra("ruta", item.getRuta());
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    overridePendingTransition(R.anim.open_next, R.anim.close_next);
                }
            });
        }
    }

    //Tomar foto
    private void abrirCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imagenArchivo = null;
        try {
            imagenArchivo = crearImagen();
        } catch (IOException ex){
            Log.e("Error",ex.toString());
        }
        if(imagenArchivo != null){
            fotoUri = FileProvider.getUriForFile(CargaEspecialesActivity.this,
                    "net.ddsmedia.tusa.tusamoviloperador.provider",imagenArchivo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,fotoUri);
            startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            Log.i("ABS_PATH","okokokokokkkko ");
        }
    }

    private File crearImagen() throws IOException {

        Log.i("ABS_PATH","Iniciando ");
        String timeStamp = new SimpleDateFormat("yyyy").format(new Date());
        String timeStamp2 = new SimpleDateFormat("MM").format(new Date());
        String timeStamp3 = new SimpleDateFormat("dd").format(new Date());
        String timeStamp4 = new SimpleDateFormat("HH").format(new Date());
        String timeStamp5 = new SimpleDateFormat("mm").format(new Date());
        String timeStamp6 = new SimpleDateFormat("ss").format(new Date());
        String imageFileName = "Operacion_" + name + "_" + timeStamp + "_" + timeStamp2 + "_" + timeStamp3 + "_" + timeStamp4 + "_" + timeStamp5 + "_" + timeStamp6 + "_";
        //Log.i("ABS_PATH",""+imageFileName);

        //File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        File storageDir = getExternalFilesDir("Especiales");

        File image = File.createTempFile(imageFileName,".jpg", storageDir);

        rutaImagen = image.getAbsolutePath();
        Log.i("ABS_PATH",rutaImagen);

        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //Bitmap imgBitmap = BitmapFactory.decodeFile(rutaImagen);
            //Log.i("RutaImagen",""+rutaImagen);
            //imgFoto.setImageBitmap(imgBitmap);
            finish();
            startActivity(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_carga_especiales,menu);
        MenuItem menuItem2 = menu.findItem(R.id.mnu_subir);
        menuItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(foto<9){
                    Toast.makeText(CargaEspecialesActivity.this, "Debe tomar 9 fotografías para sincronizar", Toast.LENGTH_LONG).show();
                }else{
                    new CargaEspecialesActivity.uploadToServer(CargaEspecialesActivity.this).execute();
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                overridePendingTransition(R.anim.open_main, R.anim.close_next);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    //LISTA
    private void consultarListaEspeciales(){

        Especial especial = null;
        listaEspeciales = new ArrayList<Especial>();

        File f = new File("/storage/emulated/0/Android/data/net.ddsmedia.tusa.tusamoviloperador/files/Especiales/");
        File[] files = f.listFiles();

        Log.i("total archivos",""+files.length);
        imagenes = new String[9];
        //for (int i=0; i<files.length; i++) {
        for (int i=(files.length-1); i>-1; i--) {
            if (files[i].length() > 0) {
                File file = files[i];
                if (file.isFile()) {
                    String name = file.getName();
                    String[] namedos = name.split("_");
                    if (namedos[1].equals(operacion)) {
                        if (namedos[2].equals(vin)) {
                            especial = new Especial();
                            foto++;
                            especial.setId(foto);
                            Log.i("OPERACION", "" + namedos[1]);
                            especial.setOperacion(namedos[1]);
                            Log.i("VIN", "" + namedos[2]);
                            especial.setVin(namedos[2]);
                            Log.i("AÑO", "" + namedos[3]);
                            especial.setAño(namedos[3]);
                            Log.i("MES", "" + namedos[4]);
                            especial.setMes(namedos[4]);
                            Log.i("DIA", "" + namedos[5]);
                            especial.setDia(namedos[5]);
                            Log.i("HORA", "" + namedos[6]);
                            especial.setHora(namedos[6]);
                            Log.i("MINUTO", "" + namedos[7]);
                            especial.setMinuto(namedos[7]);
                            Log.i("SEGUNDO", "" + namedos[8]);
                            especial.setSegundo(namedos[8]);
                            Log.i("RUTA", "" + file.getAbsolutePath());
                            especial.setRuta(file.getAbsolutePath());
                            imagenes[foto-1] = file.getAbsolutePath();
                            listaEspeciales.add(especial);
                        }
                    }
                }
            }
        }
        /*if(imagenes!=null) {
            for (int i = 0; i < imagenes.length; i++) {
                Log.i("EVIIIIII", "" + imagenes[i]);
            }
        }*/
    }

    public class EspecialAdapter extends ArrayAdapter<Especial> implements Filterable {

        private class ViewHolder {
            TextView txtOperacion, txtVin, txtFecha;
            ImageView imgMedidor;
        }

        public ArrayList<Especial> evidencia;
        public ArrayList<Especial> filteredMedidores;

        public EspecialAdapter(Context context, ArrayList<Especial> evidencia){
            super(context,R.layout.mylistviewespeciales,evidencia);
            this.evidencia = evidencia;
            this.filteredMedidores = evidencia;

            getFilter();
        }

        @Override
        public int getCount() {
            return filteredMedidores.size();
        }

        @Override
        public Especial getItem(int i) {
            return filteredMedidores.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Especial evidencia = getItem(position);

            ViewHolder viewHolder;
            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.mylistviewespeciales, parent, false);

                viewHolder.txtOperacion = convertView.findViewById(R.id.txtOperacion);
                viewHolder.txtVin = convertView.findViewById(R.id.txtVin);
                viewHolder.txtFecha = convertView.findViewById(R.id.txtFecha);
                viewHolder.imgMedidor = convertView.findViewById(R.id.imgMedidor);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.txtOperacion.setText(evidencia.getId()+". Operación: "+evidencia.getOperacion());
            viewHolder.txtVin.setText("VIN: "+evidencia.getVin());
            viewHolder.txtFecha.setText("Fecha: "+evidencia.getAño()+"/"+evidencia.getMes()+"/"+evidencia.getDia()+"  "+evidencia.getHora()+":"+evidencia.getMinuto()+":"+evidencia.getSegundo());

            Picasso.with(CargaEspecialesActivity.this)
                    .load(new File(evidencia.getRuta()))
                    .resize(150, 150)
                    .into(viewHolder.imgMedidor);

            //Diferentes tipos de carga en imageview
            //Uri myUri = (Uri.parse(evidencia.getRuta()));
            //viewHolder.imgMedidor.setImageURI(myUri);

            //Bitmap bMap = BitmapFactory.decodeFile(evidencia.getRuta());
            //viewHolder.imgMedidor.setImageBitmap(bMap);

            //viewHolder.imgMedidor.setImageResource(R.drawable.vitam);

            return convertView;
        }
    }

    //Carga de imágenes al servidor
    public void uploadEvidencia() {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName(Globals.FTP_SERVER));
            if (ftpClient.login(Globals.FTP_USER, Globals.FTP_PASS)) {
                ftpClient.enterLocalPassiveMode(); // important!
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                if(!ftpClient.changeWorkingDirectory("/OperacionesEspeciales/"+operacion+"/")){
                    ftpClient.makeDirectory("/OperacionesEspeciales/"+operacion+"/");
                }
                if(!ftpClient.changeWorkingDirectory("/OperacionesEspeciales/" + operacion + "/" + vin + "/")) {
                    ftpClient.makeDirectory("/OperacionesEspeciales/" + operacion + "/" + vin + "/");
                }
                ftpClient.changeWorkingDirectory("/OperacionesEspeciales/"+operacion+"/"+vin+"/");
                boolean result = false;
                for (int i = 0; i < 9; i++) {
                    FileInputStream in = new FileInputStream(new File(imagenes[i]));
                    result = ftpClient.storeFile(vin+"_"+(i+1)+ ".jpg", in);
                    in.close();
                }
                if (result)
                    Log.v("upload result", "succeeded");
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            Log.v("count", "error");
            e.printStackTrace();
        }
    }

    public class uploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(CargaEspecialesActivity.this);

        protected void onPreExecute() {
            super.onPreExecute();
            if (!isFinishing()) {
                pd.setMessage("Subiendo evidencia fotográfica\nEste proceso puede tardar varios minutos\nNo cierre la aplicación");
                pd.show();
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
            }
        }

        private Context context;
        uploadToServer(Context ctx) {
            this.context = ctx;
        }

        @Override
        protected String doInBackground(Void... params) {
            uploadEvidencia();
            return "Success";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pd.hide();
            pd.dismiss();
            bandera = false;
            Toast.makeText(CargaEspecialesActivity.this, "Se sincronizó correctamente", Toast.LENGTH_SHORT).show();
            //((CargaEspecialesActivity) context).finish();
        }
    }
}
