package uk.co.simon.app.sqllite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DataSourcePhotos {

	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = { SQLiteHelper.COLUMN_ID,
			SQLiteHelper.COLUMN_REPORT_ITEM_ID, 
			SQLiteHelper.COLUMN_FILEPATH,
			SQLiteHelper.COLUMN_LOCATION_ID, 
			SQLiteHelper.COLUMN_AZIMUTH, 
			SQLiteHelper.COLUMN_PITCH, 
			SQLiteHelper.COLUMN_ROLL, 
			SQLiteHelper.COLUMN_GPSX, 
			SQLiteHelper.COLUMN_GPSY, 
			SQLiteHelper.COLUMN_GPSZ,
			SQLiteHelper.COLUMN_CLOUD_ID };
	private Context context;

	public DataSourcePhotos(Context context) {
		this.context = context;
		dbHelper = new SQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public SQLPhoto createPhoto(SQLPhoto photo) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_REPORT_ITEM_ID, photo.getReportItemId());
		values.put(SQLiteHelper.COLUMN_FILEPATH, photo.getPhotoPath());
		values.put(SQLiteHelper.COLUMN_LOCATION_ID, photo.getLocationId());
		values.put(SQLiteHelper.COLUMN_AZIMUTH, photo.getAzimuth());
		values.put(SQLiteHelper.COLUMN_PITCH, photo.getPitch());
		values.put(SQLiteHelper.COLUMN_ROLL, photo.getRoll());
		values.put(SQLiteHelper.COLUMN_GPSX, photo.getGPSX());
		values.put(SQLiteHelper.COLUMN_GPSY, photo.getGPSY());
		values.put(SQLiteHelper.COLUMN_GPSZ, photo.getGPSZ());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, photo.getCloudID());
		long insertId = database.insert(SQLiteHelper.PHOTOS_TABLE_NAME, null,
				values);
		Cursor cursor = database.query(SQLiteHelper.PHOTOS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		SQLPhoto newPhoto = cursorToPhoto(cursor);
		cursor.close();
		return newPhoto;
	}

	public SQLPhoto updatePhoto(SQLPhoto photo) {
		ContentValues values = new ContentValues();
		values.put(SQLiteHelper.COLUMN_REPORT_ITEM_ID, photo.getReportItemId());
		values.put(SQLiteHelper.COLUMN_FILEPATH, photo.getPhotoPath());
		values.put(SQLiteHelper.COLUMN_LOCATION_ID, photo.getLocationId());
		values.put(SQLiteHelper.COLUMN_AZIMUTH, photo.getAzimuth());
		values.put(SQLiteHelper.COLUMN_PITCH, photo.getPitch());
		values.put(SQLiteHelper.COLUMN_ROLL, photo.getRoll());
		values.put(SQLiteHelper.COLUMN_GPSX, photo.getGPSX());
		values.put(SQLiteHelper.COLUMN_GPSY, photo.getGPSY());
		values.put(SQLiteHelper.COLUMN_GPSZ, photo.getGPSZ());
		values.put(SQLiteHelper.COLUMN_CLOUD_ID, photo.getCloudID());
		database.update(SQLiteHelper.PHOTOS_TABLE_NAME,
				values, 
				SQLiteHelper.COLUMN_ID + " = " + photo.getId(),
				null);
		Cursor cursor = database.query(SQLiteHelper.PHOTOS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_ID + " = " + photo.getId(), null,
				null, null, null);
		cursor.moveToFirst();
		SQLPhoto updPhoto = cursorToPhoto(cursor);
		cursor.close();
		return updPhoto;
	}

	public void deletePhoto(SQLPhoto photo) {
		File photoFile = new File(photo.getPhotoPath());
		photoFile.delete();
		database.delete(SQLiteHelper.PHOTOS_TABLE_NAME, 
				SQLiteHelper.COLUMN_ID + " = " + photo.getId(), null);
		Toast toast = Toast.makeText(context, "Photo deleted with id: " + photo.getId(), Toast.LENGTH_SHORT);
		toast.show();
	}

	public List<SQLPhoto> getAllPhotos() {
		List<SQLPhoto> photos = new ArrayList<SQLPhoto>();

		Cursor cursor = database.query(SQLiteHelper.PHOTOS_TABLE_NAME,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLPhoto photo = cursorToPhoto(cursor);
			photos.add(photo);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return photos;
	}

	public List<SQLPhoto> getReportItemPhotos(long reportItemId) {
		List<SQLPhoto> photos = new ArrayList<SQLPhoto>();

		Cursor cursor = database.query(SQLiteHelper.PHOTOS_TABLE_NAME,
				allColumns, SQLiteHelper.COLUMN_REPORT_ITEM_ID + " = " + reportItemId,
				null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			SQLPhoto photo = cursorToPhoto(cursor);
			photos.add(photo);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return photos;
	}
	
	public void deleteReportItemPhotos(long id) {
		List<SQLPhoto> list = getReportItemPhotos(id);
		for (SQLPhoto photo : list) {
			File photoFile = new File(photo.getPhotoPath());
			photoFile.delete();
		}
		database.delete(SQLiteHelper.PHOTOS_TABLE_NAME, 
				SQLiteHelper.COLUMN_REPORT_ITEM_ID + " = " + id, null);
	}
	
	private SQLPhoto cursorToPhoto(Cursor cursor) {
		SQLPhoto photo = new SQLPhoto();
		photo.setId(cursor.getLong(0));
		photo.setReportItemId(cursor.getLong(1));
		photo.setPhoto(cursor.getString(2));
		photo.setLocationId(cursor.getLong(3));
		photo.setAzimuth(cursor.getFloat(4));
		photo.setPitch(cursor.getFloat(5));
		photo.setRoll(cursor.getFloat(6));
		photo.setGPSX(cursor.getFloat(7));
		photo.setGPSY(cursor.getFloat(8));
		photo.setGPSZ(cursor.getFloat(9));
		photo.setCloudID(cursor.getLong(10));
		return photo;
	}
	
}
