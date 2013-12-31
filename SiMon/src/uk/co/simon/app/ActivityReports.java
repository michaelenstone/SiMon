package uk.co.simon.app;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.simon.app.adapters.AdapterReports;
import uk.co.simon.app.filesAndSync.PDFCreator;
import uk.co.simon.app.filesAndSync.UploadReport;
import uk.co.simon.app.sqllite.DataSourceReports;
import uk.co.simon.app.sqllite.SQLReport;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class ActivityReports extends FragmentActivity {

	SQLReport thisReport = new SQLReport();
	ProgressDialog uploadProgress = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BugSenseHandler.initAndStartSession(ActivityReports.this, "6c6b0664");
		setContentView(R.layout.activity_reports);
		setTitle(R.string.title_activity_reports);

		ListView ReportsList = (ListView) PopulateList();
		registerForContextMenu(ReportsList);

		ReportsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openContextMenu(view);
			}
		});
	}

	public void onClickReports(View view) {
		switch (view.getId()) {
		case R.id.newReportButton:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
			alertDialogBuilder.setTitle(this.getString(R.string.newReportDialogTitle));

			// set dialog message
			alertDialogBuilder
			.setMessage(this.getString(R.string.newReportDialogText))
			.setCancelable(true)
			.setPositiveButton(this.getString(R.string.title_activity_progress_report),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					Intent newProgressReport = new Intent(getBaseContext(), ActivityReport.class);
					newProgressReport.putExtra("reportType", false);
					startActivity(newProgressReport);
					finish();
				}
			})
			.setNegativeButton(this.getString(R.string.title_activity_site_visit_report),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					Intent newProgressReport = new Intent(getBaseContext(), ActivityReport.class);
					newProgressReport.putExtra("reportType", true);
					startActivity(newProgressReport);
					finish();
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId()==R.id.reportsList) {
			ListView ReportsList = (ListView)findViewById(R.id.reportsList);
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
			AdapterReports adapter = (AdapterReports) ReportsList.getAdapter();
			SQLReport reportAdapter = adapter.getItem(info.position);
			DataSourceReports datasource = new DataSourceReports(this);
			datasource.open();
			SQLReport report = datasource.getReport(reportAdapter.getId());
			datasource.close();
			menu.setHeaderTitle(report.getReportRef() + " - " + report.getReportDate());
			List<String> menuItems = new ArrayList<String>();
			if (report.hasPDF()) {
				menuItems.add(getResources().getString(R.string.contextEdit));
				menuItems.add(getResources().getString(R.string.contextDelete));
				menuItems.add(getResources().getString(R.string.menuChangeReportType));
				menuItems.add(getResources().getString(R.string.contextUpload));
				menuItems.add(getResources().getString(R.string.contextUpdatePDF));
				menuItems.add(getResources().getString(R.string.contextOpenPDF));
			} else {
				menuItems.add(getResources().getString(R.string.contextEdit));
				menuItems.add(getResources().getString(R.string.contextDelete));
				menuItems.add(getResources().getString(R.string.menuChangeReportType));
				menuItems.add(getResources().getString(R.string.contextUpload));
				menuItems.add(getResources().getString(R.string.contextCreatePDF)); 
			}
			for (int i = 0; i<menuItems.size(); i++) {
				menu.add(Menu.NONE, i, i, menuItems.get(i));
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		ListView ReportsList = (ListView)findViewById(R.id.reportsList);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		AdapterReports adapter = (AdapterReports) ReportsList.getAdapter();
		thisReport = adapter.getItem(info.position);
		DataSourceReports datasource = new DataSourceReports(this);
		switch (item.getItemId()) {
		case 0:
			Intent newProgressReport = new Intent(this, ActivityReport.class);
			newProgressReport.putExtra("reportId", thisReport.getId());
			newProgressReport.putExtra("reportType", thisReport.getReportType());
			startActivity(newProgressReport);
			break;
		case 1:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle(this.getString(R.string.deleteReportDialogTitle))
			.setMessage(this.getString(R.string.deleteReportDialogText))
			.setCancelable(true)
			.setPositiveButton(this.getString(R.string.contextDelete),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					DataSourceReports datasource = new DataSourceReports(getBaseContext());
					datasource.open();
					datasource.deleteReport(thisReport);
					datasource.close();
					PopulateList();
					dialog.dismiss();
				}
			})
			.setNegativeButton(this.getString(R.string.alert_dialog_cancel),new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.dismiss();
				}
			});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
			break;
		case 2:
			SQLReport tempReport = new SQLReport();
			tempReport.setId(thisReport.getId());
			tempReport.setPDF(thisReport.getPDF());
			tempReport.setProjectId(thisReport.getProjectId());
			tempReport.setReportDate(thisReport.getReportDate());
			tempReport.setSupervisor(thisReport.getSupervisor());
			tempReport.setTemp(thisReport.getTemp());
			tempReport.setTempType(thisReport.getTempType());
			tempReport.setWeather(thisReport.getWeather());
			String reportRef = new String();
			if (thisReport.getReportType()){
				reportRef=thisReport.getReportRef().replaceFirst(this.getString(R.string.dailyProgressSVRRef), this.getString(R.string.dailyProgressPRRef));
			} else {
				reportRef=thisReport.getReportRef().replaceFirst(this.getString(R.string.dailyProgressPRRef), this.getString(R.string.dailyProgressSVRRef));
			}
			tempReport.setReportRef(reportRef);
			tempReport.setReportType(!thisReport.getReportType());
			datasource.open();
			datasource.updateReport(tempReport);
			datasource.close();
			PopulateList();
			break;
		case 3:
			uploadProgress = new ProgressDialog(this);
			uploadProgress.setTitle(this.getString(R.string.uploadReportDialogTitle));
			uploadProgress.setMessage(this.getString(R.string.uploadReportDialogText));
			uploadProgress.setCancelable(false);
			uploadProgress.show();
			UploadReport reportUpload = new UploadReport(thisReport,this,uploadProgress);
			reportUpload.execute();
			break;
		case 4:
			if (thisReport.hasPDF()) {    			
				File oldPDF = new File(thisReport.getPDF());
				if (!oldPDF.delete()){
					Toast toast = Toast.makeText(this, this.getString(R.string.errPDFDelete), Toast.LENGTH_SHORT);
					toast.show();
					thisReport.setPDF(null);
				} else {
					ProgressDialog progress = new ProgressDialog(this);
					progress.setTitle(this.getString(R.string.createPDFDialogTitle));
					progress.setMessage(this.getString(R.string.createPDFDialogText));
					progress.setCancelable(false);
					progress.show();
					PDFCreator createPDF = new PDFCreator(thisReport, this, progress);
					createPDF.execute();  
				}
			} else {
				ProgressDialog progress = new ProgressDialog(this);
				progress.setTitle(this.getString(R.string.createPDFDialogTitle));
				progress.setMessage(this.getString(R.string.createPDFDialogText));
				progress.show();
				PDFCreator createPDF = new PDFCreator(thisReport, this, progress);
				createPDF.execute();
			}
			break;
		case 5:
			File reportPDF = new File(thisReport.getPDF());
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(reportPDF), "application/pdf");
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
			break;
		}
		return true;
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
		PopulateList();
	}

	@Override
	protected void onPause() {
		if (uploadProgress != null) {
			uploadProgress.dismiss();
		}
		super.onPause();
	}

	private ListView PopulateList() {

		List<SQLReport> values = new ArrayList<SQLReport>();
		DataSourceReports datasource = new DataSourceReports(this);

		datasource.open();        
		values = datasource.getAllReports();
		datasource.close();
		ListView ReportsList = (ListView)findViewById(R.id.reportsList);

		AdapterReports adapter = new AdapterReports(this, values);
		ReportsList.setAdapter(adapter);

		return ReportsList;
	}
}
