package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EvidenciasActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String mOrdenStr;
    private int tipo;

    private ImageView imgEvidencia1;
    private ImageView imgEvidencia2;
    private ImageView imgEvidencia3;
    private ImageView imgEvidencia4;
    private ImageView imgEvidencia5;
    private ImageView imgEvidencia6;
    private ImageView imgEvidencia7;
    private ImageView imgEvidencia8;
    private Button btnTake1;
    private Button btnTake2;
    private Button btnTake3;
    private Button btnTake4;
    private Button btnTake5;
    private Button btnTake6;
    private Button btnTake7;
    private Button btnTake8;
    private Button btnSave;

    static final Integer TAKE_PHOTO_1 = 1;
    static final Integer TAKE_PHOTO_2 = 2;
    static final Integer TAKE_PHOTO_3 = 3;
    static final Integer TAKE_PHOTO_4 = 4;
    static final Integer TAKE_PHOTO_5 = 5;
    static final Integer TAKE_PHOTO_6 = 6;
    static final Integer TAKE_PHOTO_7 = 7;
    static final Integer TAKE_PHOTO_8 = 8;

    @SuppressLint("SourceLockedOrientationActivity")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evidencias);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
        tipo = b.getInt("tipo");
        try {
            mJSONUserInfo = new JSONObject(mUserStr);
            mUserInfo = new Usuario(mJSONUserInfo);
            mOrdenStr = b.getString("orden");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
        bar.setTitle(R.string.activity_evidencia);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);


        imgEvidencia1 = (ImageView) findViewById(R.id.imgFoto1);
        imgEvidencia2 = (ImageView) findViewById(R.id.imgFoto2);
        imgEvidencia3 = (ImageView) findViewById(R.id.imgFoto3);
        imgEvidencia4 = (ImageView) findViewById(R.id.imgFoto4);
        imgEvidencia5 = (ImageView) findViewById(R.id.imgFoto5);
        imgEvidencia6 = (ImageView) findViewById(R.id.imgFoto6);
        imgEvidencia7 = (ImageView) findViewById(R.id.imgFoto7);
        imgEvidencia8 = (ImageView) findViewById(R.id.imgFoto8);

        btnTake1 = (Button) findViewById(R.id.btnTake1);
        btnTake2 = (Button) findViewById(R.id.btnTake2);
        btnTake3 = (Button) findViewById(R.id.btnTake3);
        btnTake4 = (Button) findViewById(R.id.btnTake4);
        btnTake5 = (Button) findViewById(R.id.btnTake5);
        btnTake6 = (Button) findViewById(R.id.btnTake6);
        btnTake7 = (Button) findViewById(R.id.btnTake7);
        btnTake8 = (Button) findViewById(R.id.btnTake8);
        btnTake1.setOnClickListener(clicks);
        btnTake2.setOnClickListener(clicks);
        btnTake3.setOnClickListener(clicks);
        btnTake4.setOnClickListener(clicks);
        btnTake5.setOnClickListener(clicks);
        btnTake6.setOnClickListener(clicks);
        btnTake7.setOnClickListener(clicks);
        btnTake8.setOnClickListener(clicks);


        /*btnSave = (Button) findViewById(R.id.btnSaveE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (existeFoto)
                    new EvidenciasActivity.uploadToServer(EvidenciasActivity.this).execute();
                else
                    Toast.makeText(EvidenciasActivity.this, "Debe tomar la foto antes", Toast.LENGTH_LONG).show();
            }
        });*/

    }

    private View.OnClickListener clicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                String numero = view.getTag().toString();//.substring(-1);
                Log.i("EVIDENCIA", "Sacando foto numero "+numero);
                takePhoto(numero);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * TOMAR FOTO
     **/
    /*private Uri fileUri;
    Uri selectedImage;
    String picturePath;
    String ba1;
    public static String URL = "http://app.blinkmensajeros.com/api/upload.php";*/

    private Uri photoURI;
    String mCurrentPhotoPath;
    Uri imagenFinal;

    private void takePhoto(String num) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            /*try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                return;
            }*/
            //if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(EvidenciasActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        createImageFile(num));
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Integer.parseInt(num));
            //}
        }
    }

    private File createImageFile(String num) throws IOException {
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + "_";
        String imageFileName = ((Integer.parseInt(num)) + tipo) + "_" + mOrdenStr;
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private Boolean existeFoto = false;
    private byte[] ba;

    private void uploadImg() {
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bm = BitmapFactory.decodeFile(imagenFinal.getPath(),options);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba,Base64.DEFAULT);*/

        existeFoto = true;
    }

    public void uploadEvidencia() {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName(Globals.FTP_SERVER));

            if (ftpClient.login(Globals.FTP_USER, Globals.FTP_PASS)) {
                ftpClient.enterLocalPassiveMode(); // important!
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                String directoryPath = "/EvidenciasFotografica/" + mOrdenStr;
                if (!ftpClient.changeWorkingDirectory(directoryPath)) {
                    ftpClient.makeDirectory(directoryPath);
                    ftpClient.changeWorkingDirectory(directoryPath);
                }

                FileInputStream in;
                boolean result = false;
                for (int i = 0; i < 8; i++) {
                    Bitmap original = BitmapFactory.decodeStream(getContentResolver().openInputStream(imagenes[i]));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    original.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    //Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
                    //File file = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera") + File.separator + "tusatemp_file.jpg");

                    //guardados en carpeta Camera externa
                    //File file = new File(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera") + File.separator + ((i + 1) + tipo) + "_" + mOrdenStr + ".jpg");

                    // Obtener la ruta del directorio de almacenamiento interno
                    File internalStorageDir = getApplicationContext().getFilesDir(); // 'context' es la instancia de tu contexto, como una Activity o Application
                    // Crear la ruta del archivo en el almacenamiento interno
                    File file = new File(internalStorageDir, ((i + 1) + tipo) + "_" + mOrdenStr + ".jpg");

                    try {
                        FileOutputStream fo = new FileOutputStream(file);
                        fo.write(out.toByteArray());
                        fo.flush();
                        fo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    in = new FileInputStream(file);
                    //in = new FileInputStream(new File(imagenes[i].getPath()));
                    result = ftpClient.storeFile(((i + 1) + tipo) + "_" + mOrdenStr + ".jpg", in);
                    in.close();
                }
                //FileInputStream in = new FileInputStream(new File(imagenFinal.getPath()));
                //boolean result = ftpClient.storeFile(mOrdenStr+".jpg", in);
                if (result) {
                    Log.v("upload result", "succeeded");

                    try {
                        Connection conn = DBConnection.CONN();
                        // verificar si se cambia campo en bd
                        String query = "UPDATE orden_status SET fotos = '" + (tipo + 8) + "' " +
                                "WHERE fk_orden = '" + mOrdenStr + "' ";
                        if (conn == null) {
                            Log.i("MSSQLERROR","Error al conectar con SQL server");
                        } else {
                            PreparedStatement preparedStatement = conn.prepareStatement(query);
                            preparedStatement.executeUpdate();
                        }
                    } catch (Exception ex) {
                        Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage());
                    }
                }
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception e) {
            Log.v("count", "error");
            e.printStackTrace();
        }

    }

    public class uploadToServer extends AsyncTask<Void, Void, String> {

        private ProgressDialog pd = new ProgressDialog(EvidenciasActivity.this);

        protected void onPreExecute() {
            super.onPreExecute();
            if (!isFinishing()) {
                pd.setMessage("Subiendo evidencia fotográfica!\nEste proceso puede tardar varios minutos.\nNo cierre la aplicación!");
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

            // Obtener el LayoutInflater
            LayoutInflater inflater = getLayoutInflater();

            // Inflar el layout personalizado
            View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

            // Encontrar los elementos del layout personalizado
            ImageView toastIcon = layout.findViewById(R.id.toast_icon);
            TextView toastText = layout.findViewById(R.id.toast_text);

            // Configurar el mensaje del Toast
            toastText.setText("Se guardó la evidencia correctamente");

            // Opcional: cambiar el icono del Toast en tiempo de ejecución
            toastIcon.setImageResource(R.drawable.logoatm);

            // Crear el Toast
            Toast toast = new Toast(getApplicationContext());
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setView(layout);

            // Mostrar el Toast
            toast.show();
            ((EvidenciasActivity) context).finish();
        }
    }

    private Uri[] imagenes = new Uri[8];
    private int imgNum = -1;
    private int tomadas = 0;

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            imgNum = requestCode - 1;
            Log.i("EVIDENCIA", "Recibiendo foto numero "+requestCode);
            //if(if(requestCode == TAKE_PHOTO_1) {
            //imagenFinal = Uri.parse(mCurrentPhotoPath);
            imagenes[imgNum] = Uri.parse(mCurrentPhotoPath);
            File file = new File(imagenes[imgNum].getPath());
            try {
                InputStream ims = new FileInputStream(file);
                if (requestCode == TAKE_PHOTO_1)
                    imgEvidencia1.setImageBitmap(BitmapFactory.decodeStream(ims));
                if (requestCode == TAKE_PHOTO_2)
                    imgEvidencia2.setImageBitmap(BitmapFactory.decodeStream(ims));
                if (requestCode == TAKE_PHOTO_3)
                    imgEvidencia3.setImageBitmap(BitmapFactory.decodeStream(ims));
                if (requestCode == TAKE_PHOTO_4)
                    imgEvidencia4.setImageBitmap(BitmapFactory.decodeStream(ims));
                if (requestCode == TAKE_PHOTO_5)
                    imgEvidencia5.setImageBitmap(BitmapFactory.decodeStream(ims));
                if (requestCode == TAKE_PHOTO_6)
                    imgEvidencia6.setImageBitmap(BitmapFactory.decodeStream(ims));
                if (requestCode == TAKE_PHOTO_7)
                    imgEvidencia7.setImageBitmap(BitmapFactory.decodeStream(ims));
                if (requestCode == TAKE_PHOTO_8)
                    imgEvidencia8.setImageBitmap(BitmapFactory.decodeStream(ims));
                tomadas++;
            } catch (FileNotFoundException e) {
                return;
            }

            MediaScannerConnection.scanFile(EvidenciasActivity.this,
                    new String[]{imagenes[imgNum].getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

            //uploadImg();
            existeFoto = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_evidencias, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Obtener el LayoutInflater
                LayoutInflater inflater2 = getLayoutInflater();

                // Inflar el layout personalizado
                View layout2 = inflater2.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

                // Encontrar los elementos del layout personalizado
                ImageView toastIcon2 = layout2.findViewById(R.id.toast_icon);
                TextView toastText2 = layout2.findViewById(R.id.toast_text);

                // Configurar el mensaje del Toast
                toastText2.setText("Debe tomar las 8 fotos antes");

                // Opcional: cambiar el icono del Toast en tiempo de ejecución
                toastIcon2.setImageResource(R.drawable.logoatm);

                // Crear el Toast
                Toast toast2 = new Toast(getApplicationContext());
                toast2.setDuration(Toast.LENGTH_SHORT);
                toast2.setView(layout2);

                // Mostrar el Toast
                toast2.show();
                return true;
            case R.id.mnu_subir:
                if (tomadas > 7){
                    new EvidenciasActivity.uploadToServer(EvidenciasActivity.this).execute();
                }else {
                    //Toast.makeText(EvidenciasActivity.this, "Debe tomar las 8 fotos antes", Toast.LENGTH_LONG).show();

                    // Obtener el LayoutInflater
                    LayoutInflater inflater = getLayoutInflater();

                    // Inflar el layout personalizado
                    View layout = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

                    // Encontrar los elementos del layout personalizado
                    ImageView toastIcon = layout.findViewById(R.id.toast_icon);
                    TextView toastText = layout.findViewById(R.id.toast_text);

                    // Configurar el mensaje del Toast
                    toastText.setText("Debe tomar las 8 fotos antes");

                    // Opcional: cambiar el icono del Toast en tiempo de ejecución
                    toastIcon.setImageResource(R.drawable.logoatm);

                    // Crear el Toast
                    Toast toast = new Toast(getApplicationContext());
                    toast.setDuration(Toast.LENGTH_SHORT);
                    toast.setView(layout);

                    // Mostrar el Toast
                    toast.show();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // Obtener el LayoutInflater
        LayoutInflater inflater3 = getLayoutInflater();

        // Inflar el layout personalizado
        View layout3 = inflater3.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_container));

        // Encontrar los elementos del layout personalizado
        ImageView toastIcon3 = layout3.findViewById(R.id.toast_icon);
        TextView toastText3 = layout3.findViewById(R.id.toast_text);

        // Configurar el mensaje del Toast
        toastText3.setText("Debe tomar las 8 fotos antes");

        // Opcional: cambiar el icono del Toast en tiempo de ejecución
        toastIcon3.setImageResource(R.drawable.logoatm);

        // Crear el Toast
        Toast toast3 = new Toast(getApplicationContext());
        toast3.setDuration(Toast.LENGTH_SHORT);
        toast3.setView(layout3);

        // Mostrar el Toast
        toast3.show();

    }

}

