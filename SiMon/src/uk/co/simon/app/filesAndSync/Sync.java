package uk.co.simon.app.filesAndSync;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.util.List;

import redstone.xmlrpc.XmlRpcFault;
import uk.co.simon.app.ActivityLogin;
import uk.co.simon.app.R;
import uk.co.simon.app.sqllite.DataSourceLocations;
import uk.co.simon.app.sqllite.DataSourcePhotos;
import uk.co.simon.app.sqllite.DataSourceProjects;
import uk.co.simon.app.sqllite.DataSourceReportItems;
import uk.co.simon.app.sqllite.DataSourceReports;
import uk.co.simon.app.sqllite.SQLLocation;
import uk.co.simon.app.sqllite.SQLPhoto;
import uk.co.simon.app.sqllite.SQLProject;
import uk.co.simon.app.sqllite.SQLReport;
import uk.co.simon.app.sqllite.SQLReportItem;
import uk.co.simon.app.wordpress.SiMonUser;
import uk.co.simon.app.wordpress.SiMonWordpress;
import uk.co.simon.app.wordpress.WPLocation;
import uk.co.simon.app.wordpress.WPPhotoUpload;
import uk.co.simon.app.wordpress.WPProject;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Sync {

	Context context;
	String mEmail;
	String mPassword;
	int userID;
	int projectLimit;
	SiMonWordpress wp = null;
	SiMonUser user;

	public Sync(Context context) {
		this.context = context;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		mEmail = sharedPref.getString("EmailPref", null);
		mPassword = sharedPref.getString("PasswordPref", null);
		userID = sharedPref.getInt("UserID", 0);
		projectLimit = sharedPref.getInt("ProjectLimit", 5);
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		try {
			wp = new SiMonWordpress(mEmail, mPassword, "http://www.simon-app.com/xmlrpc.php");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}		
	}

	public boolean projectSync() {

		if(networkIsAvailable()) {
			DataSourceProjects datasource = new DataSourceProjects(context);
			datasource.open();
			List<SQLProject> projects = datasource.getAllProjects();
			if (!projects.isEmpty()) {
				for (SQLProject project : projects) {
					try {
						project.setCloudID(wp.uploadProject(project));
						datasource.updateProject(project);
						locationSync(project);
					} catch (XmlRpcFault e) {
						if (e.getMessage().contains("invalid") && e.getMessage().contains("password")) {
							SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString("PasswordPref", null);
							editor.commit();
							Intent login = new Intent(context, ActivityLogin.class);
							context.startActivity(login);
							return false;
						} else if (e.getMessage().contains("Project Limit Reached")) {
							Toast toast = Toast.makeText(context, context.getString(R.string.errProjectServerLimit), Toast.LENGTH_SHORT);
							toast.show();
							return false;
						}
						e.printStackTrace();
					}
				}
			}
			try {
				List<WPProject> WPProjects = wp.getProjects();
				int counter = 0;
				for (WPProject project : WPProjects) {
					if (project.getCloudID()>-1) {
						SQLProject SQLproject = project.toSQLProject();
						if (datasource.getProjectFromCloudId(SQLproject.getCloudID()) == null) {	
							if (projects.size()+counter<projectLimit) {
								datasource.createProject(SQLproject);
								locationSync(SQLproject);
								counter++;
							} else {
								wp.deleteProject(SQLproject);
								Toast toast = Toast.makeText(context, context.getString(R.string.errProjectDeviceLimit) 
										+ SQLproject.getProject(), Toast.LENGTH_SHORT);
								toast.show();
							}
						}
					}
				}
			} catch (XmlRpcFault e) {
				e.printStackTrace();
			}

			datasource.close();
			return true;
		} else {
			return false;
		}
	}

	public boolean locationSync(SQLProject project) {

		if(networkIsAvailable()) {
			DataSourceLocations datasource = new DataSourceLocations(context);
			datasource.open();
			List<SQLLocation> locations = datasource.getAllProjectLocations(project.getId());
			long highestCloudID = 0;
			if (!locations.isEmpty()) {
				for (SQLLocation location : locations) {
					try {
						location.setCloudID(wp.uploadLocation(location, project.getCloudID()));
						datasource.updateLocation(location);
					} catch (XmlRpcFault e) {
						if (e.getMessage().contains("invalid") && e.getMessage().contains("password")) {
							SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString("PasswordPref", null);
							editor.commit();
							Intent login = new Intent(context, ActivityLogin.class);
							context.startActivity(login);
							return false;
						}
						e.printStackTrace();
					}
					if (location.getCloudID() > highestCloudID ) highestCloudID = location.getCloudID();
				}
			}
			try {
				List<WPLocation> WPLocations = wp.getLocations(project.getCloudID(), highestCloudID);
				for (WPLocation location : WPLocations) {
					if (location.getCloudID()>-1){
						SQLLocation SQLlocation = location.toSQLLocation();
						if (datasource.getLocationFromCloudId(SQLlocation.getCloudID()) == null) {
							datasource.createLocation(SQLlocation);
						}
					}
				}
			} catch (XmlRpcFault e) {
				e.printStackTrace();
			}

			datasource.close();
			return true;
		} else {
			return false;
		}
	}

	public boolean reportSync(SQLReport report) {
		if(networkIsAvailable()) {
			DataSourceProjects datasource = new DataSourceProjects(context);
			datasource.open();
			SQLProject project = datasource.getProject(report.getProjectId());
			datasource.close();
			try {
				report.setCloudID(wp.uploadReport(report, project.getCloudID()));
				DataSourceReports datasourceReports = new DataSourceReports(context);
				datasourceReports.open();
				datasourceReports.updateReport(report);
				datasourceReports.close();
			} catch (XmlRpcFault e) {
				if (e.getMessage().contains("invalid") && e.getMessage().contains("password")) {
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("PasswordPref", null);
					editor.commit();
					Intent login = new Intent(context, ActivityLogin.class);
					context.startActivity(login);
					return false;
				}
				e.printStackTrace();
			}
			return reportItemsSync(report);
		} else {
			return false;
		}
	}

	public boolean reportItemsSync(SQLReport report) {
		if(networkIsAvailable()) {
			DataSourceProjects datasource = new DataSourceProjects(context);
			datasource.open();
			SQLProject project = datasource.getProject(report.getProjectId());
			datasource.close();
			DataSourceReportItems datasourceReportItems = new DataSourceReportItems(context);
			datasourceReportItems.open();
			List<SQLReportItem> reportItems = datasourceReportItems.getReportItems(report.getId());
			for (SQLReportItem reportItem : reportItems) {
				try {
					DataSourceLocations datasourceLocations = new DataSourceLocations(context);
					datasourceLocations.open();
					SQLLocation location = datasourceLocations.getLocation(reportItem.getLocationId());
					datasourceLocations.close();
					reportItem.setCloudID(wp.uploadReportItem(reportItem, project.getCloudID(), report.getCloudID(), location.getCloudID()));
					datasourceReportItems.updateReportItem(reportItem);
					uploadPhotos(reportItem, project.getCloudID(), location.getCloudID());
				} catch (XmlRpcFault e) {
					if (e.getMessage().contains("invalid") && e.getMessage().contains("password")) {
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString("PasswordPref", null);
						editor.commit();
						Intent login = new Intent(context, ActivityLogin.class);
						context.startActivity(login);
						return false;
					}
					e.printStackTrace();
				}
			}
			datasourceReportItems.close();
			return true;

		} else {
			return false;
		}
	}
	
	public boolean uploadPhotos(SQLReportItem reportItem, long projectCloudID, long locationCloudID) {
		if(networkIsAvailable()) {
			DataSourcePhotos datasource = new DataSourcePhotos(context);
			datasource.open();
			List<SQLPhoto> photos = datasource.getReportItemPhotos(reportItem.getId());
			for (SQLPhoto photo : photos) {
				try {
					WPPhotoUpload photoUpload = wp.uploadPhoto(photo.getCloudID(), reportItem.getCloudID(), projectCloudID, locationCloudID);
					photo.setCloudID(photoUpload.getCloudID());
					if (!photoUpload.getStatus().contains("Exists")){
						File photoFile = new File(photo.getPhotoPath());
						String response = wp.uploadPhotoFile(URLConnection.guessContentTypeFromName(photoFile.getName()), photoFile, photo.getCloudID());
						if (response.contains("Error")) {
							Log.w("Photo Upload Error", response);
						}
					}
				} catch (XmlRpcFault e) {
					if (e.getMessage().contains("invalid") && e.getMessage().contains("password")) {
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString("PasswordPref", null);
						editor.commit();
						Intent login = new Intent(context, ActivityLogin.class);
						context.startActivity(login);
						return false;
					}
					e.printStackTrace();
				}
			}
			datasource.close();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean networkIsAvailable() {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
}