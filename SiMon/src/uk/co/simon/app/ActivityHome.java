package uk.co.simon.app;

import java.util.List;

import uk.co.simon.app.filesAndSync.ProjectLocationAsync;
import uk.co.simon.app.sqllite.DataSourceProjects;
import uk.co.simon.app.sqllite.SQLProject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.splunk.mint.Mint;

public class ActivityHome extends Activity {

	Context context = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mint.initAndStartSession(ActivityHome.this, "6c6b0664");
		setContentView(R.layout.activity_home); 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		context = this;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		DataSourceProjects datasource = new DataSourceProjects(this);
		datasource.open();
		List<SQLProject> projects = datasource.getAllProjects();
		datasource.close();
		String Message = getResources().getString(R.string.homeText);
		if (projects.size() < 1) {
			Button button1 = (Button) findViewById(R.id.homeButtonReports);
			button1.setEnabled(false);
			Button button2 = (Button) findViewById(R.id.homeButtonSettings);
			button2.setEnabled(false);
			Button button3 = (Button) findViewById(R.id.homeButtonSync);
			button3.setEnabled(false);
			Message = getResources().getString(R.string.firstHomeText);
		} else {
			if (sharedPref.getBoolean("SyncPref", false)) {
				ProgressDialog syncProgress = new ProgressDialog(this);
				syncProgress.setTitle(context.getString(R.string.menuSync));
				syncProgress.setMessage(context.getString(R.string.messageSync));
				syncProgress.setCancelable(false);
				syncProgress.show();
				ProjectLocationAsync mTask = new ProjectLocationAsync(context, this, syncProgress);
				mTask.execute((Void) null);
			}
		}
		TextView welcomeMessage = (TextView) findViewById(R.id.homeTextView);
		welcomeMessage.setText(Message);
	}

	public void onClickHome(View view) {
		switch (view.getId()) {
		case R.id.homeButtonReports:
			//Start Progress Report Activity
			Intent openExistingReport = new Intent(ActivityHome.this, ActivityReports.class);
			startActivity(openExistingReport);
			break;
		case R.id.homeButtonSettings:
			//Start Site Visit Report Activity
			Intent openSettings = new Intent(ActivityHome.this, ActivitySettings.class);
			startActivity(openSettings);
			break;
		case R.id.homeButtonSync:
			//Open Existing Reports Activity
			ProgressDialog syncProgress = new ProgressDialog(this);
			syncProgress.setTitle(context.getString(R.string.menuSync));
			syncProgress.setMessage(context.getString(R.string.messageSync));
			syncProgress.setCancelable(false);
			syncProgress.show();
			ProjectLocationAsync mTask = new ProjectLocationAsync(context, this, syncProgress);
			mTask.execute((Void) null);
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
