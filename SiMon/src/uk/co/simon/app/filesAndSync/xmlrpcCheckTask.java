package uk.co.simon.app.filesAndSync;

import uk.co.simon.app.R;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class xmlrpcCheckTask extends AsyncTask<Void, Void, Boolean> {

	Context context;
	Activity activity;
	
	public xmlrpcCheckTask (Context context, Activity activity) {
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
			Toast toast = Toast.makeText(context, activity.getString(R.string.msgSyncSuccess), Toast.LENGTH_SHORT);
			toast.show();
		}
	}

}
