package net.ddsmedia.tusa.tusamoviloperador;

import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.convertInputStreamToString;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;

import net.ddsmedia.tusa.tusamoviloperador.Utils.GeoAlarmReceiver;
import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.model.Check;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;

public class LoginActivity extends Activity  {
    //public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private UserLoginTask mAuthTask = null;
    private CheckLoginTask mCheckLoginTask = null;
    private Usuario mUsuario;
    private Check mCheck;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private String mUserInfo;
    private GeoAlarmReceiver alarm;

    String email, password;
    View focusView;

    String elToken;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        alarm = new GeoAlarmReceiver();
    }

    /*private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }*/


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        if (mCheckLoginTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } /*else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mMatricula;
        private final String mPassword;
        //String z = "";
        Boolean isSuccess = false;

        UserLoginTask(String email, String password) {
            mMatricula = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            /*String query = "SELECT ID_matricula, Nombres, Ap_paterno, Ap_materno, Correo_electronico, " +
                        "ISNULL((SELECT TOP 1 telefono FROM Telefonos_personal WHERE Id_matricula  = '" + mMatricula + "' AND Adicional = 0),'') AS No_celular, " +
                        "(SELECT temporal FROM Usuario_tusamovil WHERE fk_matricula  = '" + mMatricula + "') AS temporal, " +
                        "ISNULL((SELECT TOP 1 CONCAT(tipo_o,fk_orden) FROM orden_status WHERE fk_matricula = '" + mMatricula + "' AND " +
                                "(estado < " + Globals.ORDEN_FINALIZADA + ") ),'') AS orden " +
                    "FROM Personal " +
                    "WHERE ID_matricula = '" + mMatricula + "' AND ID_empleado = 1 AND " +
                    "(SELECT password FROM Usuario_tusamovil " +
                    "WHERE activo = 1 AND fk_matricula = '" + mMatricula + "') = '"+ Globals.cryptPassword(mPassword) +"'";*/
            String[] param = {mMatricula, mPassword};
            String query = Globals.makeQuery(Globals.QUERY_LOGIN, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if(rs.next()){
                        mUsuario = new Usuario(rs);
                        Log.i("USERINFO",mUsuario.toJSON().toString());
                        mUserInfo = mUsuario.toJSON().toString();
                        isSuccess=true;

                        SharedPreferences preferences = getSharedPreferences("MyToken", Context.MODE_PRIVATE);
                        elToken = preferences.getString("Token", "");

                        //Log.i("sisisis",""+elToken);
                        //Log.i("olaaaaaa2",""+mMatricula);

                        //sendToken(elToken, mMatricula);
                    }else{
                        Log.i("MSSQLERROR","No hay registro \n"+query);
                        isSuccess = false;
                    }
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Login Excepcion MSSQL\n"+ex.toString()+"\n"+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                mCheckLoginTask = new CheckLoginTask(email, password);
                mCheckLoginTask.execute((Void) null);

                /* //sin verificar doble inicio de sesion
                SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
                try {
                    Globals.saveInfo(mUsuario, mPassword, loginData);
                    FirebaseMessaging.getInstance().subscribeToTopic("op"+mUsuario.getMatricula());
                    FirebaseMessaging.getInstance().subscribeToTopic("todos");
                    //https://trasladosuniversales.com.mx/app/sendPushMsgOP2.php?o=2030&t=Test&m=test155

                    if(alarm != null){
                        alarm.SetAlarm(getBaseContext());
                    }else{
                        Toast.makeText(getBaseContext(), "Alarm is null", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                if(mUsuario.getSalud() == 0 && hour > 4){
                    goSalud(mUsuario.getMatricula());
                }else{
                    if(mUsuario.getTemporal() == 0){
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.putExtra("user", mUserInfo);
                        startActivity(intent);
                        finish();
                    }else{
                        Intent intent = new Intent(getBaseContext(), PasswordActivity.class);
                        intent.putExtra("user", mUserInfo);
                        intent.putExtra("init",true);
                        startActivity(intent);
                        finish();
                    }
                } //sin verificar doble inicio de sesion
                */
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    // task check login
    public class CheckLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mMatricula;
        private final String mPassword;
        //String z = "";
        Boolean isSuccess = false;

        CheckLoginTask(String email, String password) {
            mMatricula = email;
            mPassword = password;
        }

        @SuppressLint("WrongThread")
        @Override
        protected Boolean doInBackground(Void... params) {
            String[] param = {mMatricula, mPassword};
            String query = Globals.makeQuery(Globals.QUERY_CHECK_LOGIN, param);
            try {
                Connection conn = DBConnection.CONN();
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    if(rs.next()){
                        mCheck = new Check(rs);
                        //Log.i("CHECKLOGININFO",mCheck.toJSON().toString());
                        Log.i("CHECKLOGININFO",""+mCheck.getLoginapp());
                        isSuccess=true;

                        if(mCheck.getLoginapp()==0){
                            //Log.i("CHECKLOGININFO","ok");

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    ChangeSesion mChangeSesion;
                                    mChangeSesion = new ChangeSesion(Integer.parseInt(mMatricula));
                                    mChangeSesion.execute((Void) null);
                                }
                            });

                            SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
                            try {
                                Globals.saveInfo(mUsuario, mPassword, loginData);
                                FirebaseMessaging.getInstance().subscribeToTopic("op"+mUsuario.getMatricula());
                                FirebaseMessaging.getInstance().subscribeToTopic("todos");
                                //https://trasladosuniversales.com.mx/app/sendPushMsgOP2.php?o=2030&t=Test&m=test155
                                if(alarm != null){
                                    alarm.SetAlarm(getBaseContext());
                                }else{
                                    Toast.makeText(getBaseContext(), "Alarm is null", Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                            if(mUsuario.getSalud() == 0 && hour > 4){
                                goSalud(mUsuario.getMatricula());
                            }else{
                                if(mUsuario.getTemporal() == 0){
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    intent.putExtra("user", mUserInfo);
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Intent intent = new Intent(getBaseContext(), PasswordActivity.class);
                                    intent.putExtra("user", mUserInfo);
                                    intent.putExtra("init",true);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }else{
                            //Log.i("CHECKLOGININFO","aquiiiii: "+mCheck.getLoginapp());
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    final Toast toast = Toast.makeText(getBaseContext(), "Sesión iniciada en otro dispositivo", Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                        }
                    }else{
                        Log.i("MSSQLERROR","No hay registro \n"+query);
                        isSuccess = false;
                    }
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Login Excepcion MSSQL\n"+ex.toString()+"\n"+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCheckLoginTask = null;
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mCheckLoginTask = null;
            showProgress(false);
        }
    }


    private void goSalud(int matr){
        Intent intent = new Intent(getBaseContext(), SaludActivity.class);
        intent.putExtra("userId", matr);
        intent.putExtra("user", mUserInfo);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack (true);
    }

    public class ChangeSesion extends AsyncTask<Void, Void, Boolean> {
        private final int mMatr;
        Boolean isSuccess = false;
        private ProgressDialog pd = new ProgressDialog(LoginActivity.this);

        ChangeSesion(int matr) {
            mMatr = matr;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isFinishing()) {
                pd.setMessage("Inicio de Sesión");
                pd.show();
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String query = "";
            try {
                Connection conn = DBConnection.CONN();
                query = "UPDATE Personal SET Login_app = 1 WHERE ID_matricula = "+ mMatr +";";
                if (conn == null) {
                    Log.i("MSSQLERROR","Error al conectar con SQL server");
                } else {
                    PreparedStatement preparedStatement = conn.prepareStatement(query);
                    preparedStatement.executeUpdate();
                    isSuccess=true;
                }
            } catch (Exception ex) {
                Log.i("MSSQLERROR","Excepcion MSSQL "+ex.getMessage()+" \n:: QUERY :: "+query);
                isSuccess = false;
            }
            return isSuccess;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mCheckLoginTask = null;
            super.onPostExecute(success);
        }

        @Override
        protected void onCancelled() {
            mCheckLoginTask = null;
        }
    }

    private void sendToken(String token, String mMatr){
        String url = "https://trasladosuniversales.com.mx/app/sendPushSesionToken.php?m="+mMatr+"&t="+ token;
        Log.i("urll",""+url);
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            InputStream inputStream = null;
            inputStream = httpResponse.getEntity().getContent();
            String result = convertInputStreamToString(inputStream);

            //Log.i("SEND_LOG_TOKEN",result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences preferences = getSharedPreferences("MyToken", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("SaveDB", "ok");
        editor.apply();
    }
}
