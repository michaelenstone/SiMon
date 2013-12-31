package uk.co.simon.app.sqllite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.simon.app.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DataSourceReports {

	private DataSourceProjects projectsdatasource;
	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = { SQLiteHelper.COLUMN_ID,
			SQLiteHelper.COLUMN_PROJECT_ID,
			SQLiteHelper.COLUMN_REPORT_DATE,
			SQLiteHelper.COLUMN_REPORT_TYPE,
			SQLiteHelper.COLUMN_REPORT_SUPERVISOR,
			SQLiteHelper.COLUMN_REPORT_REF,
			SQLiteHelper.COLUMN_REPORT_WEATHER,
			SQLiteHelper.COLUMN_REPORT_TEMP,
			SQLiteHelper.COLUMN_REPORT_TEMP_TYPE,
			SQLiteHelper.COLUMN_REPORT_PDF,
			SQLiteHelper.COLUMN_CLOUD_ID };
	private Context context;

	public DataSourceReports(Context context) {
		this.context = context;
		dbHelper = new SQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public SQLReport createReport(SQLReport report) {

		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_PROJECT_ID, report.getProjectId());
		values.put(SQLiteHelper.COLUMN_REPORT_DATE, report.getReportDate());
		values.put(SQLiteHelper.COLUMN_REPORT_TYPE, report.getReportType());
		values.put(SQLiteHelper.COLUMN_REPORT_SUPERVISOR, report.getSupervisor());
		values.put(SQLiteHelper.COLUMN_REPORT_REF, report.getReportRef());
		values.put(SQLiteHelper.COLUMN_REPORT_WEATHER, report.getWeather());
		values.put(SQLiteHelper.COLUMN_REPORT_TEMP, report.getTemp());
		values.put(SQLiteHelper.COLUMN_REPORT_TEMP_TYPE, report.getTempType());
		values.put(SQLiteHelper.COLUMN_REPORT_PDF, report.getPDF());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, report.getCloudID());
		long insertId = database.insert(SQLiteHelper.REPORTS_TABLE_NAME, null,
				values);
		Cursor cursor = database.query(SQLiteHelper.REPORTS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		SQLReport newReport = cursorToReport(cursor);
		cursor.close();
		return newReport;
	}

	public SQLReport updateReport(SQLReport report) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_PROJECT_ID, report.getProjectId());
		values.put(SQLiteHelper.COLUMN_REPORT_DATE, report.getReportDate());
		values.put(SQLiteHelper.COLUMN_REPORT_TYPE, report.getReportType());
		values.put(SQLiteHelper.COLUMN_REPORT_SUPERVISOR, report.getSupervisor());
		values.put(SQLiteHelper.COLUMN_REPORT_REF, report.getReportRef());
		values.put(SQLiteHelper.COLUMN_REPORT_WEATHER, report.getWeather());
		values.put(SQLiteHelper.COLUMN_REPORT_TEMP, report.getTemp());
		values.put(SQLiteHelper.COLUMN_REPORT_TEMP_TYPE, report.getTempType());
		values.put(SQLiteHelper.COLUMN_REPORT_PDF, report.getPDF());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, report.getCloudID());
		database.update(SQLiteHelper.REPORTS_TABLE_NAME,
				values, 
				SQLiteHelper.COLUMN_ID + " = " + report.getId(),
				null);
		Cursor cursor = database.query(SQLiteHelper.REPORTS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + report.getId(), null,
				null, null, null);
		cursor.moveToFirst();
		SQLReport updReport = cursorToReport(cursor);
		cursor.close();
		return updReport;
	}

	public void deleteReport(SQLReport report) {
		DataSourceReportItems datasourceReportItems = new DataSourceReportItems(context);
		datasourceReportItems.open();
		datasourceReportItems.deleteReportItems(report);
		datasourceReportItems.close();
		database.delete(SQLiteHelper.REPORTS_TABLE_NAME, 
				SQLiteHelper.COLUMN_ID + " = " + report.getId(), null);
		Toast toast = Toast.makeText(context, context.getString(R.string.msgReportDeleted) + report.getId(), Toast.LENGTH_SHORT);
		toast.show();
	}

	public List<SQLReport> getAllReports() {
		List<SQLReport> reports = new ArrayList<SQLReport>();

		Cursor cursor = database.query(SQLiteHelper.REPORTS_TABLE_NAME,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLReport report = cursorToReport(cursor);
			reports.add(report);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return reports;
	}

	public List<SQLReport> getAllProjectReports(long projectId) {
		List<SQLReport> reports = new ArrayList<SQLReport>();

		Cursor cursor = database.query(SQLiteHelper.REPORTS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_PROJECT_ID + " = " + projectId, null,
				null, null, null);
		
		if (!(cursor.moveToFirst()) || cursor.getCount() ==0) {
			cursor.close();
			return null;
		} else {
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				SQLReport report = cursorToReport(cursor);
				reports.add(report);
				cursor.moveToNext();
			}
			// Make sure to close the cursor
			cursor.close();
			return reports;
		}
	}

	public List<Map<String, String>> mapAllReports() {
		List<Map<String, String>> projects = new ArrayList<Map<String, String>>();
		projectsdatasource.open();

		Cursor cursor = database.query(SQLiteHelper.REPORTS_TABLE_NAME,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLProject project = projectsdatasource.getProject(cursor.getLong(1));
			String projectName = project.getProject();
			Map<String, String> report = new HashMap<String, String>(2);
			report.put("projectName", projectName);
			report.put("reportRef", cursor.getString(5));
			projects.add(report);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return projects;
	}

	public SQLReport getReport(long reportId) {
		Cursor cursor = database.query(SQLiteHelper.REPORTS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + reportId, null,
				null, null, null);
		cursor.moveToFirst();
		SQLReport Report = cursorToReport(cursor);
		cursor.close();
		return Report;
	}

	private SQLReport cursorToReport(Cursor cursor) {

		SQLReport report = new SQLReport();
		report.setId(cursor.getLong(0));
		report.setProjectId(cursor.getLong(1));
		report.setReportDate(cursor.getString(2));
		if (cursor.getLong(3)>0) {
			report.setReportType(true);
		} else {
			report.setReportType(false);
		}
		report.setSupervisor(cursor.getString(4));
		report.setReportRef(cursor.getString(5));
		report.setWeather(cursor.getString(6));
		report.setTemp(cursor.getString(7));
		report.setTempType(cursor.getLong(8));
		report.setPDF(cursor.getString(9));
		report.setCloudID(cursor.getLong(10));
		return report;
	}

}
