package uk.co.simon.app.filesAndSync;

import uk.co.simon.app.ActivityHome;
import uk.co.simon.app.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ProjectLocationAsync extends AsyncTask<Void, Void, Boolean> {

	Context context;
	Activity activity;
	ProgressDialog progress;
	
	public ProjectLocationAsync (Context context, Activity activity, ProgressDialog progress) {
		this.context = context;
		this.activity = activity;
		this.progress = progress;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

		Sync sync = new Sync(activity);
		return sync.projectSync();
	}

	@Override
	protected void onPostExecute(final Boolean success) {		    
		try {
			progress.dismiss();
		} catch (Exception e) {	
		}
		if (activity.getClass().getSimpleName().contains("ActivityHome")) {
			activity.finish();
			Intent home = new Intent(activity, ActivityHome.class);
			activity.startActivity(home);
		}
		if (success) {
			Toast.makeText(context, activity.getString(R.string.msgSyncSuccess), Toast.LENGTH_SHORT).show();
		} else {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String msg = sharedPref.getString("ErrorPref", context.getString(R.string.msgSyncFail) );
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}

}
