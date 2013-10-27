package uk.co.simon.app.filesAndSync;

import uk.co.simon.app.sqllite.SQLReport;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
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
			//if (sync.projectSync()) {
				return sync.reportSync(report);
			//} else {
			//	return false;
			//}
		} else {
			return false;
		}
	}

	protected void onPostExecute(Boolean result) {
		String message = new String();
		if (result){
			message = "Connection Successful";
		} else {
			message = "Connection Unsuccessful, Try Again";
		}
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.show();
		try {
			uploadProgress.dismiss();
			uploadProgress = null;
		} catch (Exception e) {
		}
	}

}
