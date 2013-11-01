package uk.co.simon.app.filesAndSync;

import uk.co.simon.app.R;
import uk.co.simon.app.sqllite.DataSourceProjects;
import uk.co.simon.app.sqllite.SQLProject;
import uk.co.simon.app.sqllite.SQLReport;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class UploadReport extends AsyncTask<Void, Void, Boolean> {

	SQLReport report;
	Context context;
	ProgressDialog uploadProgress;
	String URL;
	String errMsg;

	public UploadReport(SQLReport report, Context context, ProgressDialog uploadProgress) {
		this.report = report;
		this.context = context;
		this.uploadProgress = uploadProgress;
	}

	protected Boolean doInBackground(Void... params) {
		Sync sync = new Sync(context);
		if (sync.networkIsAvailable()) {
			if (sync.singleProjectSync(report.getProjectId())) {
				DataSourceProjects datasource = new DataSourceProjects(context);
				datasource.open();
				SQLProject project = datasource.getProject(report.getProjectId());
				datasource.close();
				if (sync.locationSync(project)){
					return sync.reportSync(report);
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	protected void onPostExecute(Boolean result) {		    
		if (result) {
			Toast.makeText(context, context.getString(R.string.msgUploadSuccess), Toast.LENGTH_SHORT).show();
		} else {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			String msg = sharedPref.getString("ErrorPref", context.getString(R.string.msgUploadFail) );
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		}
		try {
			uploadProgress.dismiss();
			uploadProgress = null;
		} catch (Exception e) {
		}
	}

}
