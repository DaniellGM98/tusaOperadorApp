package net.ddsmedia.tusa.tusamoviloperador;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.legacy.app.ActionBarDrawerToggle;
import androidx.fragment.app.FragmentActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import net.ddsmedia.tusa.tusamoviloperador.Utils.AdapterNavDrawerList;
import net.ddsmedia.tusa.tusamoviloperador.Utils.Globals;
import net.ddsmedia.tusa.tusamoviloperador.Utils.NavDrawerItem;
import net.ddsmedia.tusa.tusamoviloperador.model.Usuario;
import net.ddsmedia.tusa.tusamoviloperador.Utils.DBConnection;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity implements LocationListener {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;

	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private AdapterNavDrawerList adapter;

	private String userData;
	private JSONObject mJSONUserInfo;
	public String mUserStr;
	private Usuario mUserInfo;
	private Bundle bundle;
	private String mNombreUsuario;

	SharedPreferences preferences;
	SharedPreferences.Editor editor;

	@SuppressLint("SourceLockedOrientationActivity")
	@SuppressWarnings("ResourceType")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nav_drawer_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Bundle b = getIntent().getExtras();
		mUserStr = b.getString("user");

		//Velocidad
		if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// Permission is not granted
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
		}else {
			//start the program if permission is granted
			//doStuff();
			LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			if (lm != null) {
				if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					return;
				}
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			}
		}
		//Velocidad

		if(mUserStr == null){
			SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
			mUserStr = loginData.getString("info","");
		}

		bundle = new Bundle();
		bundle.putString("user",mUserStr);

		mTitle = getTitle();
		try {
			mJSONUserInfo = new JSONObject(mUserStr);
			mUserInfo = new Usuario(mJSONUserInfo);
			mTitle = mDrawerTitle = mUserInfo.getNombreLargo();

			Log.i("USER_ID",mUserInfo.getMatricula()+"");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		// navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
		// navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons2);
		navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons3);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		mDrawerLayout.setDrawerShadow(R.drawable.navigation_drawer_shadow, GravityCompat.START);

		navDrawerItems = new ArrayList<NavDrawerItem>();

		// adding nav drawer items to array
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[0], navMenuIcons.getResourceId(0, -1), Globals.MNU_HOME));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[1], navMenuIcons.getResourceId(1, -1), Globals.MNU_HISTORIAL));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[2], navMenuIcons.getResourceId(2, -1), Globals.MNU_DISPONIBLES));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[8], navMenuIcons.getResourceId(1, -1), Globals.MNU_ESPECIALES));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[9], navMenuIcons.getResourceId(10, -1), Globals.MNU_CONVERTIDOR));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[10], navMenuIcons.getResourceId(11, -1), Globals.MNU_DIRECTORIO));
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[12], navMenuIcons.getResourceId(13, -1), Globals.MNU_DOCUMENTOS));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[3], navMenuIcons.getResourceId(3, -1), Globals.MNU_PASSWORD));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[11], navMenuIcons.getResourceId(12, -1), Globals.MNU_SESION));
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[6], navMenuIcons.getResourceId(0, -1), Globals.MNU_TICKETS));
		//navDrawerItems.add(new NavDrawerItem(navMenuTitles[7], navMenuIcons.getResourceId(0, -1), Globals.MNU_BITACORA));
		navDrawerItems.add(new NavDrawerItem(navMenuTitles[4], navMenuIcons.getResourceId(4, -1), Globals.MNU_EXIT));

		// Recycle the typed array
		navMenuIcons.recycle();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new AdapterNavDrawerList(getApplicationContext(), navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		ActionBar bar = getActionBar();
		//bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));
		bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.header)));

		// get screen device width and height
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		// checking internet connection
		if (!Globals.isNetworkAvailable(MainActivity.this)) {
			Toast.makeText(MainActivity.this, getString(R.string.error_internet), Toast.LENGTH_SHORT).show();
		}

		//mma = new AdapterMainMenu(this);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer,
				R.string.app_name, R.string.app_name
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}

		checkGPS();

		FirebaseMessaging.getInstance().getToken()
				.addOnCompleteListener(new OnCompleteListener<String>() {
					@Override
					public void onComplete(@NonNull Task<String> task) {
						if (!task.isSuccessful()) {
							//Log.w(TAG, "Fetching FCM registration token failed", task.getException());
							return;
						}
						// Get new FCM registration token
						String token = task.getResult();
						Log.i("tokenFIRE",""+token);
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("D");
		String formattedDate = df.format(calendar.getTime());
		int a = Integer.parseInt(formattedDate);

		SharedPreferences preferences;
		preferences = getSharedPreferences("EncuestaSalud", Context.MODE_PRIVATE);
		int b = preferences.getInt("diaEncuesta", 0);
		//Log.i("SHARE_DIAENCUESTA",""+a+" : "+b);

		if(a!=b) {
			Intent intentsa = new Intent(getApplicationContext(), SaludActivity.class);
			intentsa.putExtra("user", mUserStr);
			intentsa.putExtra("userId", mUserInfo.getMatricula());
			intentsa.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentsa);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		//dbhelper.deleteAllData();
		//dbhelper.close();
		finish();
		overridePendingTransition(R.anim.open_main, R.anim.close_next);
	}

	@Override
	public void onLocationChanged(@NonNull Location location) {
		if (location != null) {
			float nCurrentSpeed = location.getSpeed() * 3.6f;
			//tv_speed.setText(String.format("%.2f", nCurrentSpeed) + " km/h");
			//Toast.makeText(MainActivity.this, ""+nCurrentSpeed, Toast.LENGTH_SHORT).show();

			//Log.i("OKOKKO",""+Float.parseFloat(String.format("%.2f", nCurrentSpeed)));

			preferences = getSharedPreferences("velocidad", Context.MODE_PRIVATE);
			editor = preferences.edit();
			editor.putFloat("speed", Float.parseFloat(String.format("%.2f", nCurrentSpeed)));
			editor.apply();
		}
	}

	/**
	 * Slide menu item click listener
	 */
	private class SlideMenuClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		//menu.findItem(R.id.ic_menu).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		Integer action = navDrawerItems.get(+position).getMnu_option();
		final CloseSesion[] mCloseSesion = new CloseSesion[1];
		switch (action) {
		case 0:
			//fragment = new ActivityHome();
				fragment = new OrdenFragment();
				fragment.setArguments(bundle);
			break;
		case 1:
			Intent intenth = new Intent(getApplicationContext(), HistorialActivity.class);
			intenth.putExtra("user", mUserStr);
			//intenth.putExtra("init",false);
			intenth.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intenth);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
		case 2:
			Intent intentn = new Intent(getApplicationContext(), MasOrdenesActivity.class);
			intentn.putExtra("user", mUserStr);
			intentn.putExtra("tipo", Globals.QUERY_DISPONIBLES);
			intentn.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentn);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
		case 3:
			Intent intents = new Intent(getApplicationContext(), PasswordActivity.class);
			intents.putExtra("user", mUserStr);
			intents.putExtra("init",false);
			intents.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intents);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
		case 4:
			/*SharedPreferences loginData = getSharedPreferences("loginData", Context.MODE_PRIVATE);
			Globals.deleteInfo(loginData);
			FirebaseMessaging.getInstance().unsubscribeFromTopic("/topics/op"+mUserInfo.getMatricula());
			Intent i= new Intent(MainActivity.this, LoginActivity.class);
			startActivity(i);*/
			//finish();
			mDrawerLayout.closeDrawer(mDrawerList);
			moveTaskToBack (true);
			break;
		case 5:
			Intent intentt = new Intent(getApplicationContext(), TicketsActivity.class);
			intentt.putExtra("user", mUserStr);
			intentt.putExtra("tipo", Globals.QUERY_DISPONIBLES);
			intentt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentt);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;
		case 6:
			Intent intentb = new Intent(getApplicationContext(), BitacoraActivity.class);
			intentb.putExtra("user", mUserStr);
			intentb.putExtra("tipo", Globals.QUERY_DISPONIBLES);
			intentb.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentb);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;

		case 7:
			Intent intente = new Intent(getApplicationContext(), EspecialesActivity.class);
			intente.putExtra("user", mUserStr);
			//intente.putExtra("init",false);
			intente.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intente);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;

		case 8:
			Intent intentC = new Intent(getApplicationContext(), ConvertidorActivity.class);
			intentC.putExtra("user", mUserStr);
			intentC.putExtra("init",false);
			intentC.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentC);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;

		case 9:
			Intent intentD = new Intent(getApplicationContext(), DirectorioActivity.class);
			intentD.putExtra("user", mUserStr);
			intentD.putExtra("init",false);
			intentD.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentD);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;

		case 10:
			SharedPreferences preferences;
			preferences = getSharedPreferences("loginData", Context.MODE_PRIVATE);
			int matri = preferences.getInt("matricula", 0);

			Context context=MainActivity.this;

			new AlertDialog.Builder(context)
					.setTitle("Cerrar sesión")
					.setMessage("¿Deseas cerrar sesión?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mCloseSesion[0] = new CloseSesion(matri);
							mCloseSesion[0].execute((Void) null);

							SharedPreferences.Editor editor = getSharedPreferences("loginData", MODE_PRIVATE).edit();
							editor.clear().apply();
							SharedPreferences.Editor editor2 = getSharedPreferences("EncuestaSalud", MODE_PRIVATE).edit();
							editor2.clear().apply();
						}
					})
					.setNegativeButton(android.R.string.no, null)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();

			//Intent intentS = new Intent(getBaseContext(), LoginActivity.class);
			//startActivity(intentS);
			//finish();

			break;

		/*case 11:
			Intent intentDoc = new Intent(getApplicationContext(), DocumentosActivity.class);
			intentDoc.putExtra("user", mUserStr);
			//intente.putExtra("init",false);
			intentDoc.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intentDoc);
			overridePendingTransition(R.anim.open_next, R.anim.close_next);
			break;*/

		default:
			break;
		}

		if (fragment != null) {
			android.app.FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();

		Log.i("TEST","TEST SESION");
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private void checkGPS(){
		final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

		if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			sendSMSMessage();
		}
	}

	protected void sendSMSMessage() {
		/*String phoneNo = "7711541170";
		String message = mNombreUsuario+" No tiene activado el GPS. [ABRE_APP]";

		try {
			SmsManager smsManager = SmsManager.getDefault();
			smsManager.sendTextMessage(phoneNo, null, message, null, null);
		}catch (Exception e) {
			e.printStackTrace();
		}*/
		Log.i("GEOAPP","== NO GPS ==");
	}

	public class CloseSesion extends AsyncTask<Void, Void, Boolean> {
		private final int mMatr;
		Boolean isSuccess = false;
		private ProgressDialog pd = new ProgressDialog(MainActivity.this);

		CloseSesion(int matr) {
			mMatr = matr;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!isFinishing()) {
				pd.setMessage("Cerrando Sesión");
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
				query = "UPDATE Personal SET Login_app = 0 WHERE ID_matricula = "+ mMatr +";";
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
			Context mCtx = getApplicationContext();

			if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
				((ActivityManager)mCtx.getSystemService(ACTIVITY_SERVICE))
						.clearApplicationUserData(); // note: it has a return value!
			} else {
				try {
					String packageName = mCtx.getPackageName();
					Runtime runtime = Runtime.getRuntime();
					runtime.exec("pm clear "+packageName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return isSuccess;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			//mCloseSesion = null;
			super.onPostExecute(success);
		}

		@Override
		protected void onCancelled() {
			//mCloseSesion = null;
		}
	}

}
