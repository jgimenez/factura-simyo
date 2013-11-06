package es.gimix.simyo;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockActivity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// set version information
		TextView appString = (TextView) findViewById(R.id.version_string);
		appString.setText(getCurrentVersionName());
	}
	
	private String getCurrentVersionName() {
		PackageInfo pkgInfo;
		try {
			pkgInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			throw new RuntimeException(e); // current application does not exist ¡!
		}
		return pkgInfo.versionName;
	}

	public void doContact(View view) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "jordi@mobilejazz.cat" });
		i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + getCurrentVersionName());
		i.putExtra(android.content.Intent.EXTRA_TEXT,
				"\n\n¡Gracias por contactar!");
		try {
			startActivity(i);
		} catch (Exception e) {/* no mail app */
		}
	}
	
	public void doWebsite(View view) {
		Uri uri = Uri.parse( "http://www.mobilejazz.cat/" );
		startActivity( new Intent( Intent.ACTION_VIEW, uri ) );
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.about_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		case R.id.menu_web:
			doWebsite(null);
			return true;
		case R.id.menu_email:
			doContact(null);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
