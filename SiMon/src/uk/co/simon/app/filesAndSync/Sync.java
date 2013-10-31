package uk.co.simon.app.filesAndSync;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

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
			try {
				List<WPProject> WPProjects = wp.getProjects();
				List<SQLProject> projectsNotOnServer = new ArrayList<SQLProject>();
				List<SQLProject> projectsNotOnDevice = new ArrayList<SQLProject>();
				projectsNotOnServer.addAll(projects);
				for (WPProject project : WPProjects) {
					SQLProject sqlProject = datasource.getProjectFromCloudId(project.getCloudID());
					if (sqlProject == null) {
						projectsNotOnDevice.add(sqlProject);
					} else {
						projectsNotOnServer.remove(sqlProject);
					}
				}
				int counter = 0;
				for (SQLProject project : projectsNotOnDevice) {
					if (projects.size()+counter<projectLimit) {
						datasource.createProject(project);
						counter++;
					} else {
						wp.projectLimit(project);
						Toast toast = Toast.makeText(context, context.getString(R.string.errProjectDeviceLimit) 
								+ project.getProject(), Toast.LENGTH_SHORT);
						toast.show();
					}
				}
				for (SQLProject project : projectsNotOnServer) {
					project.setCloudID(wp.uploadProject(project));
					if (project.getCloudID()>-1) {
						datasource.updateProject(project);
					} else {
						Toast toast = Toast.makeText(context, context.getString(R.string.errProjectServerLimit) 
								+ project.getProject(), Toast.LENGTH_SHORT);
						toast.show();
						return true;
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
			List<SQLProject> syncedProjects = datasource.getAllProjects();
			for (SQLProject project : syncedProjects) {
				locationSync(project);
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
			try {
				List<WPLocation> WPLocations = wp.getLocations(project.getCloudID());
				List<SQLLocation> locationsNotOnServer = new ArrayList<SQLLocation>();
				List<SQLLocation> locationsNotOnDevice = new ArrayList<SQLLocation>();
				locationsNotOnServer.addAll(locations);
				for (WPLocation location : WPLocations) {
					if (location.getCloudID()>-1){
						SQLLocation SQLlocation = location.toSQLLocation();
						if (SQLlocation == null) {
							locationsNotOnDevice.add(SQLlocation);
						} else {
							locationsNotOnServer.remove(SQLlocation);
						}
					}
				}
				for (SQLLocation location : locationsNotOnDevice) {
					datasource.createLocation(location);
				}
				for (SQLLocation location : locationsNotOnServer) {
					location.setCloudID(wp.uploadLocation(location, project.getCloudID()));
					datasource.updateLocation(location);
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
				if (report.hasPDF()) {
					uploadPDF(report);
				}
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
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"http://www.simon-app.com/wp-content/plugins/SiMon%20Plugin/Upload.php?photo=set");

				try {
					MultipartEntity entity = new MultipartEntity();

					entity.addPart("report_item_id", new StringBody(String.valueOf(reportItem.getCloudID())));
					entity.addPart("project_id", new StringBody(String.valueOf(projectCloudID)));
					entity.addPart("location_id", new StringBody(String.valueOf(locationCloudID)));
					entity.addPart("user_id", new StringBody(String.valueOf(userID)));


					File file = new File(photo.getPhotoPath());
					entity.addPart("image", new FileBody(file,"image/jpeg"));

					httppost.setEntity(entity);
					HttpResponse response = httpclient.execute(httppost);

					HttpEntity resEntity = response.getEntity();

					BufferedReader reader = new BufferedReader(new InputStreamReader(
							resEntity.getContent(), "UTF-8"));
					String sResponse;
					StringBuilder s = new StringBuilder();

					while ((sResponse = reader.readLine()) != null) {
						s = s.append(sResponse);
					}
					Log.w("test", "Response: " + s);
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			}
			datasource.close();
			return true;
		} else {
			return false;
		}
	}

	public boolean uploadPDF(SQLReport report) {
		if(networkIsAvailable()) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://www.simon-app.com/wp-content/plugins/SiMon%20Plugin/Upload.php?pdf=set");

			try {
				MultipartEntity entity = new MultipartEntity();

				entity.addPart("user_id", new StringBody(String.valueOf(userID)));
				entity.addPart("report_id", new StringBody(String.valueOf(report.getCloudID())));

				File file = new File(report.getPDF());
				entity.addPart("image", new FileBody(file,"application/pdf"));

				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity resEntity = response.getEntity();

				BufferedReader reader = new BufferedReader(new InputStreamReader(
						resEntity.getContent(), "UTF-8"));
				String sResponse;
				StringBuilder s = new StringBuilder();

				while ((sResponse = reader.readLine()) != null) {
					s = s.append(sResponse);
				}
				Log.w("test", "Response: " + s);
			} catch (ClientProtocolException e) {
			} catch (IOException e) {
			}
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