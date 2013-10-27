package uk.co.simon.app.sqllite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DataSourceLocations {

	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = { SQLiteHelper.COLUMN_ID,
			SQLiteHelper.COLUMN_PROJECT_ID,
			SQLiteHelper.COLUMN_LOCATION_NAME,
			SQLiteHelper.COLUMN_CLOUD_ID };

	public DataSourceLocations(Context context) {
		dbHelper = new SQLiteHelper(context);
	}


	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public SQLLocation createLocation(SQLLocation location) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_LOCATION_NAME, location.getLocation());
		values.put(SQLiteHelper.COLUMN_PROJECT_ID, location.getProjectId());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, location.getCloudID());
		long insertId = database.insert(SQLiteHelper.LOCATIONS_TABLE_NAME, null,
				values);
		Cursor cursor = database.query(SQLiteHelper.LOCATIONS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		SQLLocation newLocation = cursorToLocation(cursor);
		cursor.close();
		return newLocation;
	}

	public SQLLocation updateLocation(SQLLocation location) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_LOCATION_NAME, location.getLocation());
		values.put(SQLiteHelper.COLUMN_PROJECT_ID, location.getProjectId());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, location.getCloudID());
		database.update(SQLiteHelper.LOCATIONS_TABLE_NAME,
				values, 
				SQLiteHelper.COLUMN_ID + " = " + location.getId(),
				null);
		Cursor cursor = database.query(SQLiteHelper.LOCATIONS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + location.getId(), null,
				null, null, null);
		cursor.moveToFirst();
		SQLLocation updLocation = cursorToLocation(cursor);
		cursor.close();
		return updLocation;
	}

	public List<SQLLocation> getAllProjectLocations(Long ProjectId) {
		List<SQLLocation> locations = new ArrayList<SQLLocation>();

		Cursor cursor = database.query(SQLiteHelper.LOCATIONS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_PROJECT_ID + " = " + ProjectId, null,
				null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLLocation location = cursorToLocation(cursor);
			locations.add(location);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return locations;
	}

	public List<String> getAllProjectLocationNames(Long projectId) {
		List<String> locations = new ArrayList<String>();

		Cursor cursor = database.query(SQLiteHelper.LOCATIONS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_PROJECT_ID + " = " + projectId, null,
				null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String location = cursor.getString(2);
			locations.add(location);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return locations;
	}

	public SQLLocation getLocation(Long locationId) {

		Cursor cursor = database.query(SQLiteHelper.LOCATIONS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + locationId, null,
				null, null, null);

		cursor.moveToFirst();
		SQLLocation location = null;
		while (!cursor.isAfterLast()) {
			location = cursorToLocation(cursor);
			cursor.moveToNext();
		}
		cursor.close();
		return location;
	}

	public Object getLocationFromCloudId(long cloudId) {

		Cursor cursor = database.query(SQLiteHelper.LOCATIONS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_CLOUD_ID + " = " + cloudId, null,
				null, null, null);

		cursor.moveToFirst();
		SQLLocation location = null;
		while (!cursor.isAfterLast()) {
			location = cursorToLocation(cursor);
			cursor.moveToNext();
		}
		cursor.close();
		return location;
	}

	public SQLLocation findLocationId(SQLLocation location) {
		String where = SQLiteHelper.COLUMN_PROJECT_ID + " = ? AND " + SQLiteHelper.COLUMN_LOCATION_NAME + " = ?";
		String[] whereArgs = {Long.toString(location.getProjectId()), location.getLocation()};
		Cursor cursor = database.query(SQLiteHelper.LOCATIONS_TABLE_NAME, allColumns, where, whereArgs, null, null, null);
		cursor.moveToFirst();
		SQLLocation newLocation = cursorToLocation(cursor);
		// Make sure to close the cursor
		cursor.close();
		return newLocation;		
	}

	private SQLLocation cursorToLocation(Cursor cursor) {
		SQLLocation location = new SQLLocation();
		location.setId(cursor.getLong(0));
		location.setLocationProjectId(cursor.getLong(1));
		location.setLocation(cursor.getString(2));
		location.setCloudID(cursor.getLong(3));
		return location;
	}





}
