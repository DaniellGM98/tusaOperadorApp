package net.ddsmedia.tusa.tusamoviloperador;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EvidenciaActivity extends Activity {

    private String mUserStr;
    private JSONObject mJSONUserInfo;
    private Usuario mUserInfo;
    private String mOrdenStr;

    private ImageView imgEvidencia;
    private Button btnTake;
    private Button btnSave;

    static final Integer TAKE_PHOTO = 1;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evidencia);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Bundle b = getIntent().getExtras();
        mUserStr = b.getString("user");
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

        imgEvidencia = (ImageView) findViewById(R.id.imgFoto1);

        btnTake = (Button) findViewById(R.id.btnTakeE);
        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    takePhoto();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btnSave = (Button) findViewById(R.id.btnSaveE);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(existeFoto)
                    new uploadToServer(EvidenciaActivity.this).execute();
                else
                    Toast.makeText(EvidenciaActivity.this,"Debe tomar la foto antes",Toast.LENGTH_LONG).show();
            }
        });

        try {
            takePhoto();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** TOMAR FOTO **/
    private Uri fileUri;
    Uri selectedImage;
    String picturePath;
    String ba1;
    public static String URL = "http://app.blinkmensajeros.com/api/upload.php";

    private Uri photoURI;
    String mCurrentPhotoPath;
    Uri imagenFinal;
    private void takePhoto() throws IOException{
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                return;
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(EvidenciaActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        createImageFile());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PHOTO);
            }
        }
        /*Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, TAKE_PHOTO);*/
    }

    private File createImageFile() throws IOException {
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
        return image;
    }

    private Boolean existeFoto = false;
    private byte[] ba;
    private void uploadImg(){
        /*Bitmap bm = BitmapFactory.decodeFile(picturePath);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        ba = bao.toByteArray();*/
        //ba1 = Base64.encodeToString(ba,Base64.DEFAULT);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bm = BitmapFactory.decodeFile(imagenFinal.getPath(),options);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 50, bao);
        byte[] ba = bao.toByteArray();
        ba1 = Base64.encodeToString(ba,Base64.DEFAULT);

        existeFoto = true;

        //new uploadToServer(mIdEnvio).execute();
    }

    public void uploadEvidencia() {
        FTPClient ftpClient = null;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(InetAddress.getByName(Globals.FTP_SERVER));

            if (ftpClient.login(Globals.FTP_USER, Globals.FTP_PASS)) {
                ftpClient.enterLocalPassiveMode(); // important!
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                //ftpClient.changeWorkingDirectory("/Soportes/Imagenes/");

                //ftpClient.changeWorkingDirectory("/EvidenciasFotografica/"+mOrdenStr+"/");
                String directoryPath = "/EvidenciasFotografica/" + mOrdenStr;
                if (!ftpClient.changeWorkingDirectory(directoryPath)) {
                    ftpClient.makeDirectory(directoryPath);
                    ftpClient.changeWorkingDirectory(directoryPath);
                }

                //FileInputStream in = new FileInputStream(new File(picturePath));
                //FileInputStream in = new FileInputStream(ba1);
                //InputStream in = new ByteArrayInputStream(ba1);
                FileInputStream in = new FileInputStream(new File(imagenFinal.getPath()));
                //FileInputStream in = new FileInputStream(ba1);
                boolean result = ftpClient.storeFile(mOrdenStr+".jpg", in);
                in.close();
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

        private ProgressDialog pd = new ProgressDialog(EvidenciaActivity.this);
        protected void onPreExecute() {
            super.onPreExecute();
            if(!isFinishing()) {
                pd.setMessage("Espere, subiendo evidencia!");
                pd.show();
            }
        }

        private Context context;
        uploadToServer(Context ctx){
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
            Toast.makeText(EvidenciaActivity.this, "SE GUARDO LA EVIDENCIA CORRECTAMENTE ", Toast.LENGTH_SHORT).show();
            ((EvidenciaActivity) context).finish();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == TAKE_PHOTO && resultCode == RESULT_OK) {
            /*Bundle extras = intent.getExtras();
            Bitmap imageBitmap = (Bitmap) intent.getExtras().get("data");
            imgEvidencia.setImageBitmap(imageBitmap);

            Log.i("FOTOOO",intent.getDataString()+"::::"+getOriginalImagePath());
            picturePath = getOriginalImagePath();

            uploadImg();*/

            /*selectedImage = intent.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();*/

            imagenFinal = Uri.parse(mCurrentPhotoPath);
            File file = new File(imagenFinal.getPath());
            try {
                InputStream ims = new FileInputStream(file);
                imgEvidencia.setImageBitmap(BitmapFactory.decodeStream(ims));
            } catch (FileNotFoundException e) {
                return;
            }

            // ScanFile so it will be appeared on Gallery
            MediaScannerConnection.scanFile(EvidenciaActivity.this,
                    new String[]{imagenFinal.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

            uploadImg();
        }
    }

    public String getOriginalImagePath() {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }
}
