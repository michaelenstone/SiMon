package uk.co.simon.app;

import uk.co.simon.app.filesAndSync.ProjectLocationAsync;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bugsense.trace.BugSenseHandler;

public class ActivityHome extends Activity {

	Context context = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(ActivityHome.this, "6c6b0664");
		setContentView(R.layout.activity_home); 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		context = this;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPref.getBoolean("SyncPref", false)) {
			ProjectLocationAsync mTask = new ProjectLocationAsync(context, this);
			mTask.execute((Void) null);
		}
	}

	public void onClickHome(View view) {
		switch (view.getId()) {
		case R.id.homeButtonProgress:
			//Start Progress Report Activity
			Intent newProgressReport = new Intent(ActivityHome.this, ActivityReport.class);
			newProgressReport.putExtra("reportType", false);
			startActivity(newProgressReport);
			break;
		case R.id.homeButtonSVR:
			//Start Site Visit Report Activity
			Intent newSiteVisitReport = new Intent(ActivityHome.this, ActivityReport.class);
			newSiteVisitReport.putExtra("reportType", true);
			startActivity(newSiteVisitReport);
			break;
		case R.id.homeButtonOpen:
			//Open Existing Reports Activity
			Intent openExistingReport = new Intent(ActivityHome.this, ActivityReports.class);
			startActivity(openExistingReport);
			break;
		case R.id.homeButtonProjects:
			//Open Projects Activity
			Intent openProjects = new Intent(ActivityHome.this, ActivityProjects.class);
			startActivity(openProjects);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.general_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		OptionsMenu options = new OptionsMenu(item,this);
		options.menuSelect();
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
