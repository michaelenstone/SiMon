package uk.co.simon.app.sqllite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_CLOUD_ID = "cloud_id";

	public static final String PROJECTS_TABLE_NAME = "projects";
	public static final String COLUMN_PROJECT_NAME = "project_name";
	public static final String COLUMN_PROJECT_NO = "project_no";

	public static final String LOCATIONS_TABLE_NAME = "locations";
	public static final String COLUMN_LOCATION_NAME = "location_name";
	public static final String COLUMN_PROJECT_ID = "project_id";

	public static final String REPORTS_TABLE_NAME = "reports";
	public static final String COLUMN_REPORT_DATE = "date";
	public static final String COLUMN_REPORT_SUPERVISOR = "supervisor";
	public static final String COLUMN_REPORT_REF = "report_ref";
	public static final String COLUMN_REPORT_WEATHER = "weather";
	public static final String COLUMN_REPORT_TEMP = "temp";
	public static final String COLUMN_REPORT_TEMP_TYPE = "temp_type";
	public static final String COLUMN_REPORT_TYPE = "report_type";
	public static final String COLUMN_REPORT_PDF = "report_pdf";

	public static final String REPORT_ITEMS_TABLE_NAME = "report_items";
	public static final String COLUMN_REPORT_ID = "report_id";
	public static final String COLUMN_LOCATION_ID = "location_id";
	public static final String COLUMN_ACTIVITY_OR_ITEM = "activity_or_item";
	public static final String COLUMN_PROGRESS = "progress";
	public static final String COLUMN_ONTIME = "on_time";
	public static final String COLUMN_DESCRIPTION = "description";

	public static final String PHOTOS_TABLE_NAME = "photos";
	public static final String COLUMN_REPORT_ITEM_ID = "report_item_id";
	public static final String COLUMN_FILEPATH = "filepath";
	public static final String COLUMN_AZIMUTH = "azimuth";
	public static final String COLUMN_PITCH= "pitch";
	public static final String COLUMN_ROLL= "roll";
	public static final String COLUMN_GPSX = "gpsx";
	public static final String COLUMN_GPSY = "gpsy";
	public static final String COLUMN_GPSZ = "gpsz";

	private static final String DATABASE_NAME = "sitehelper.db";
	private static final int DATABASE_VERSION = 12;

	private static final String PROJECTS_DATABASE_CREATE = "create table "
			+ PROJECTS_TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PROJECT_NAME  + " text not null, "
			+ COLUMN_PROJECT_NO  + " text not null, "
			+ COLUMN_CLOUD_ID  + " integer not null"
			+ ");";

	private static final String LOCATIONS_DATABASE_CREATE = "create table "
			+ LOCATIONS_TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PROJECT_ID + " integer not null, "
			+ COLUMN_LOCATION_NAME + " text not null, "
			+ COLUMN_CLOUD_ID  + " integer not null"
			+ ");";

	private static final String REPORTS_DATABASE_CREATE = "create table "
			+ REPORTS_TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_PROJECT_ID + " integer not null, "
			+ COLUMN_REPORT_DATE + " text not null, "
			+ COLUMN_REPORT_TYPE + " integer not null, "
			+ COLUMN_REPORT_SUPERVISOR + " text not null, "
			+ COLUMN_REPORT_REF + " text not null, "
			+ COLUMN_REPORT_WEATHER + " text not null, "
			+ COLUMN_REPORT_TEMP + " text not null, "
			+ COLUMN_REPORT_TEMP_TYPE + " text not null, "
			+ COLUMN_REPORT_PDF + " text, "
			+ COLUMN_CLOUD_ID  + " integer not null"
			+ ");";

	private static final String REPORT_ITEMS_DATABASE_CREATE = "create table "
			+ REPORT_ITEMS_TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_REPORT_ID + " integer not null, "
			+ COLUMN_LOCATION_ID + " integer not null, "
			+ COLUMN_ACTIVITY_OR_ITEM + " text not null, "
			+ COLUMN_PROGRESS + " real not null, "
			+ COLUMN_DESCRIPTION + " text not null, "
			+ COLUMN_ONTIME + " text not null, "
			+ COLUMN_CLOUD_ID  + " integer not null"
			+ ");";

	private static final String PHOTOS_DATABASE_CREATE = "create table "
			+ PHOTOS_TABLE_NAME + "(" + COLUMN_ID + " integer primary key autoincrement, "
			+ COLUMN_REPORT_ITEM_ID + " integer not null, "
			+ COLUMN_FILEPATH + " text not null, "
			+ COLUMN_LOCATION_ID + " integer not null, "
			+ COLUMN_AZIMUTH + " real not null, "
			+ COLUMN_PITCH + " real not null, "
			+ COLUMN_ROLL + " real not null, "
			+ COLUMN_GPSX + " real not null, "
			+ COLUMN_GPSY + " real not null, "
			+ COLUMN_GPSZ + " real not null, "
			+ COLUMN_CLOUD_ID  + " integer not null"
			+ ");";

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(PROJECTS_DATABASE_CREATE);
		db.execSQL(LOCATIONS_DATABASE_CREATE);
		db.execSQL(REPORTS_DATABASE_CREATE);
		db.execSQL(REPORT_ITEMS_DATABASE_CREATE);
		db.execSQL(PHOTOS_DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO: Remember to put database upgrade code here!
		Log.w(SQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + PROJECTS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + LOCATIONS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + REPORTS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + REPORT_ITEMS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + PHOTOS_TABLE_NAME);
		onCreate(db);
	}

} 