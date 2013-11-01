package uk.co.simon.app.filesAndSync;

import uk.co.simon.app.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ProjectLocationAsync extends AsyncTask<Void, Void, Boolean> {

	Context context;
	Activity activity;
	
	public ProjectLocationAsync (Context context, Activity activity) {
		this.context = context;
		this.activity = activity;
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {

		Sync sync = new Sync(context);
		return sync.projectSync();
	}

	@Override
	protected void onPostExecute(final Boolean success) {		    
		if (success) {
			Toast.makeText(context, activity.getString(R.string.msgSyncSuccess), Toast.LENGTH_SHORT).show();
		} else {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String msg = sharedPref.getString("ErrorPref", context.getString(R.string.msgSyncFail) );
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
	}

}
