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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.util.Date;

public class FotosActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String mOrdenStr;
    private int tipo;

    private ImageView imgEvidencia1;
    private ImageView imgEvidencia2;
    private ImageView imgEvidencia3;
    private ImageView imgEvidencia4;
    private Button btnTake1;
    private Button btnTake2;
    private Button btnTake3;
    private Button btnTake4;
    private Button btnSave;

    static final Integer TAKE_PHOTO_1 = 1;
    static final Integer TAKE_PHOTO_2 = 2;
    static final Integer TAKE_PHOTO_3 = 3;
    static final Integer TAKE_PHOTO_4 = 4;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fotos);
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
        bar.setTitle(R.string.activity_fotos);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeButtonEnabled(true);


        imgEvidencia1 = (ImageView) findViewById(R.id.imgFoto1);
        imgEvidencia2 = (ImageView) findViewById(R.id.imgFoto2);
        imgEvidencia3 = (ImageView) findViewById(R.id.imgFoto3);
        imgEvidencia4 = (ImageView) findViewById(R.id.imgFoto4);

        btnTake1 = (Button) findViewById(R.id.btnTake1);
        btnTake2 = (Button) findViewById(R.id.btnTake2);
        btnTake3 = (Button) findViewById(R.id.btnTake3);
        btnTake4 = (Button) findViewById(R.id.btnTake4);
        btnTake1.setOnClickListener(clicks);
        btnTake2.setOnClickListener(clicks);
        btnTake3.setOnClickListener(clicks);
        btnTake4.setOnClickListener(clicks);
    }

    private View.OnClickListener clicks = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                String numero = view.getTag().toString();//.substring(-1);
                Log.i("FOTOS", "Sacando foto numero "+numero);
                takePhoto(numero);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * TOMAR FOTO
     **/

    private Uri photoURI;
    String mCurrentPhotoPath;

    private void takePhoto(String num) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.i("FOTOOOOO====",ex.getMessage());
                return;
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(FotosActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        createImageFile());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Integer.parseInt(num));
            }
        }
    }

    private File createImageFile() throws IOException {
        Log.i("ABS_PATH","Iniciando ");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        Log.i("ABS_PATH",mCurrentPhotoPath);
        return image;
    }

    public void uploadEvidencia() {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName(Globals.FTP_SERVER));

            if (ftpClient.login(Globals.FTP_USER, Globals.FTP_PASS)) {
                ftpClient.enterLocalPassiveMode(); // important!
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.changeWorkingDirectory("/EvidenciasFotografica/");
                ftpClient.makeDirectory(mOrdenStr);
                ftpClient.changeWorkingDirectory("/EvidenciasFotografica/"+mOrdenStr+"/");

                FileInputStream in;
                boolean result = false;
                for (int i = 0; i < 4; i++) {
                    Bitmap original = BitmapFactory.decodeStream(getContentResolver().openInputStream(imagenes[i]));
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    original.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    File file = new File(Environment.getExternalStorageDirectory() + File.separator + "tusatemp_file.jpg");
                    try {
                        FileOutputStream fo = new FileOutputStream(file);
                        fo.write(out.toByteArray());
                        fo.flush();
                        fo.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    in = new FileInputStream(file);
                    result = ftpClient.storeFile(((i + 1) + tipo)  + ".jpg", in);
                    in.close();
                }
                if (result) {
                    Log.v("upload result", "succeeded");

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

        private ProgressDialog pd = new ProgressDialog(FotosActivity.this);

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
            Toast.makeText(FotosActivity.this, "SE GUARDO LA EVIDENCIA CORRECTAMENTE ", Toast.LENGTH_SHORT).show();
            ((FotosActivity) context).finish();
        }
    }

    private Uri[] imagenes = new Uri[4];
    private int imgNum = -1;
    private int tomadas = 0;

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            imgNum = requestCode - 1;
            Log.i("FOTOS", "Recibiendo foto numero "+requestCode);
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
                tomadas++;
            } catch (FileNotFoundException e) {
                return;
            }

            MediaScannerConnection.scanFile(FotosActivity.this,
                    new String[]{imagenes[imgNum].getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
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
            case R.id.mnu_subir:
                if (tomadas > 3)
                    new FotosActivity.uploadToServer(FotosActivity.this).execute();
                else
                    Toast.makeText(FotosActivity.this, "Debe tomar las 4 fotos antes", Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
    }

}