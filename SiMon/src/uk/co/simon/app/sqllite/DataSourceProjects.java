package uk.co.simon.app.sqllite;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class DataSourceProjects {

	private SQLiteDatabase database;
	private SQLiteHelper dbHelper;
	private String[] allColumns = { SQLiteHelper.COLUMN_ID,
    SQLiteHelper.COLUMN_PROJECT_NAME,
    SQLiteHelper.COLUMN_PROJECT_NO,
	SQLiteHelper.COLUMN_CLOUD_ID  };
	private Context context;

	public DataSourceProjects(Context context) {
		this.context = context;
		dbHelper = new SQLiteHelper(context);
	}

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  public SQLProject createProject(SQLProject project) {
	    ContentValues values = new ContentValues();
	    values.put(SQLiteHelper.COLUMN_PROJECT_NAME, project.getProject());
	    values.put(SQLiteHelper.COLUMN_PROJECT_NO, project.getProjectNumber());
	    values.put(SQLiteHelper.COLUMN_CLOUD_ID, project.getCloudID());
	    long insertId = database.insert(SQLiteHelper.PROJECTS_TABLE_NAME, null, values);
	    Cursor cursor = database.query(SQLiteHelper.PROJECTS_TABLE_NAME,
	        allColumns, SQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	    cursor.moveToFirst();
	    SQLProject newProject = cursorToProject(cursor);
	    cursor.close();
	    return newProject;
	  }
	  
	  public SQLProject updateProject(SQLProject project) {
	    ContentValues values = new ContentValues();
	    values.put(SQLiteHelper.COLUMN_PROJECT_NAME, project.getProject());
	    values.put(SQLiteHelper.COLUMN_PROJECT_NO, project.getProjectNumber());
	    values.put(SQLiteHelper.COLUMN_CLOUD_ID, project.getCloudID());
	    database.update(SQLiteHelper.PROJECTS_TABLE_NAME,
	    		values, 
	    		SQLiteHelper.COLUMN_ID + " = " + project.getId(),
	    		null);
	    Cursor cursor = database.query(SQLiteHelper.PROJECTS_TABLE_NAME,
	        allColumns, SQLiteHelper.COLUMN_ID + " = " + project.getId(), null,
	        null, null, null);
	    cursor.moveToFirst();
	    SQLProject updProject = cursorToProject(cursor);
	    cursor.close();
	    return updProject;
	  }

	  public void deleteProject(SQLProject project) {
		database.delete(SQLiteHelper.PROJECTS_TABLE_NAME, 
				SQLiteHelper.COLUMN_ID + " = " + project.getId(), null);
		Toast toast = Toast.makeText(context, "Project deleted with name: " + project.getProject() + " and Project Number: " + project.getProjectNumber(), Toast.LENGTH_SHORT);
		toast.show();
	  }

	  public List<SQLProject> getAllProjects() {
	    List<SQLProject> projects = new ArrayList<SQLProject>();

	    Cursor cursor = database.query(SQLiteHelper.PROJECTS_TABLE_NAME,
	        allColumns, null, null, null, null, null);

	    cursor.moveToFirst();
	    while (!cursor.isAfterLast()) {
	    	SQLProject project = cursorToProject(cursor);
	    	projects.add(project);
	    	cursor.moveToNext();
	    }
	    // Make sure to close the cursor
	    cursor.close();
	    return projects;
	  }
	  
	  public SQLProject getProject(long id) {

		    Cursor cursor = database.query(SQLiteHelper.PROJECTS_TABLE_NAME,
			        allColumns, SQLiteHelper.COLUMN_ID + " = " + id, null, null, null, null);

		    cursor.moveToFirst();
		    SQLProject project = null;
		    while (!cursor.isAfterLast()) {
		    	project = cursorToProject(cursor);
		    	cursor.moveToNext();
		    }
		    cursor.close();
		    return project;
		  }
	  
	  public SQLProject getProjectFromCloudId(long CloudId) {

		    Cursor cursor = database.query(SQLiteHelper.PROJECTS_TABLE_NAME,
			        allColumns, SQLiteHelper.COLUMN_CLOUD_ID + " = " + CloudId, null, null, null, null);
		    
		    cursor.moveToFirst();
		    SQLProject project = null;
		    while (!cursor.isAfterLast()) {
		    	project = cursorToProject(cursor);
		    	cursor.moveToNext();
		    }
		    cursor.close();
		    return project;
		  }
	  
	  public long getProjectId(String projectName, String projectNumber){
		  String where = SQLiteHelper.COLUMN_PROJECT_NAME + " = ? AND " + SQLiteHelper.COLUMN_PROJECT_NO + " = ?";
		  String[] whereArgs = {projectName,projectNumber};
		  Cursor cursor = database.query(SQLiteHelper.PROJECTS_TABLE_NAME, allColumns, where, whereArgs, null, null, null);
		  cursor.moveToFirst();
		  Long projectId = cursor.getLong(0);
		  return projectId;
	  }
	  
	  private SQLProject cursorToProject(Cursor cursor) {
	    SQLProject project = new SQLProject();
	    project.setId(cursor.getLong(0));
	    project.setProject(cursor.getString(1));
	    project.setProjectNumber(cursor.getString(2));
	    project.setCloudID(cursor.getLong(3));
	    return project;
	  }
}
