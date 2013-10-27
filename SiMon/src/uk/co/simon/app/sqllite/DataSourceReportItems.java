package uk.co.simon.app.sqllite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DataSourceReportItems {

	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = { SQLiteHelper.COLUMN_ID,
			SQLiteHelper.COLUMN_REPORT_ID,
			SQLiteHelper.COLUMN_LOCATION_ID,
			SQLiteHelper.COLUMN_ACTIVITY_OR_ITEM,
			SQLiteHelper.COLUMN_PROGRESS,
			SQLiteHelper.COLUMN_DESCRIPTION,
			SQLiteHelper.COLUMN_ONTIME,
			SQLiteHelper.COLUMN_CLOUD_ID };
	private Context context;

	public DataSourceReportItems(Context context) {
		this.context = context;
		dbHelper = new SQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public SQLReportItem createReportItem(SQLReportItem reportItem) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_REPORT_ID, reportItem.getReportId());
		values.put(SQLiteHelper.COLUMN_LOCATION_ID, reportItem.getLocationId());
		values.put(SQLiteHelper.COLUMN_ACTIVITY_OR_ITEM, reportItem.getReportItem());
		values.put(SQLiteHelper.COLUMN_PROGRESS, reportItem.getProgress());
		values.put(SQLiteHelper.COLUMN_ONTIME, reportItem.getOnTIme());
		values.put(SQLiteHelper.COLUMN_DESCRIPTION, reportItem.getDescription());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, reportItem.getCloudID());
		long insertId = database.insert(SQLiteHelper.REPORT_ITEMS_TABLE_NAME, null,
				values);
		Cursor cursor = database.query(SQLiteHelper.REPORT_ITEMS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		SQLReportItem newReportItem = cursorToReportItem(cursor);
		cursor.close();
		return newReportItem;
	}

	public SQLReportItem updateReportItem(SQLReportItem packageReportItem) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_REPORT_ID, packageReportItem.getReportId());
		values.put(SQLiteHelper.COLUMN_LOCATION_ID, packageReportItem.getLocationId());
		values.put(SQLiteHelper.COLUMN_ACTIVITY_OR_ITEM, packageReportItem.getReportItem());
		values.put(SQLiteHelper.COLUMN_PROGRESS, packageReportItem.getProgress());
		values.put(SQLiteHelper.COLUMN_ONTIME, packageReportItem.getOnTIme());
		values.put(SQLiteHelper.COLUMN_DESCRIPTION, packageReportItem.getDescription());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, packageReportItem.getCloudID());
		database.update(SQLiteHelper.REPORT_ITEMS_TABLE_NAME,
				values, 
				SQLiteHelper.COLUMN_ID + " = " + packageReportItem.getId(),
				null);
		Cursor cursor = database.query(SQLiteHelper.REPORT_ITEMS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + packageReportItem.getId(), null,
				null, null, null);
		cursor.moveToFirst();
		SQLReportItem updReportItem = cursorToReportItem(cursor);
		cursor.close();
		return updReportItem;

	}

	public void deleteReportItem(SQLReportItem reportItem) {
		database.delete(SQLiteHelper.REPORT_ITEMS_TABLE_NAME, 
				SQLiteHelper.COLUMN_ID + " = " + reportItem.getId(), null);
		Toast toast = Toast.makeText(context, "Report deleted with id: " + reportItem.getId(), Toast.LENGTH_SHORT);
		toast.show();
	}

	public List<SQLReportItem> getAllReportItems() {
		List<SQLReportItem> reportItems = new ArrayList<SQLReportItem>();

		Cursor cursor = database.query(SQLiteHelper.REPORT_ITEMS_TABLE_NAME,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLReportItem reportItem = cursorToReportItem(cursor);
			reportItems.add(reportItem);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return reportItems;
	}

	public List<SQLReportItem> getReportItems(long reportId) {
		List<SQLReportItem> reportItems = new ArrayList<SQLReportItem>();

		Cursor cursor = database.query(SQLiteHelper.REPORT_ITEMS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_REPORT_ID + " = " + reportId, null,
				null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLReportItem reportItem = cursorToReportItem(cursor);
			reportItems.add(reportItem);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return reportItems;
	}	

	private SQLReportItem cursorToReportItem(Cursor cursor) {
		SQLReportItem reportItem = new SQLReportItem();
		reportItem.setId(cursor.getLong(0));
		reportItem.setReportId(cursor.getLong(1));
		reportItem.setLocationId(cursor.getLong(2));
		reportItem.setReportItem(cursor.getString(3));
		reportItem.setProgress(cursor.getFloat(4));
		reportItem.setDescription(cursor.getString(5));
		reportItem.setOnTIme(cursor.getString(6));
		reportItem.setCloudID(cursor.getLong(7));
		return reportItem;
	}

	public SQLReportItem getReportItem(long reportItemId) {

		Cursor cursor = database.query(SQLiteHelper.REPORT_ITEMS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + reportItemId, null, null, null, null);

		cursor.moveToFirst();
		SQLReportItem reportItem = cursorToReportItem(cursor);
		// Make sure to close the cursor
		cursor.close();
		return reportItem;
	}

}
