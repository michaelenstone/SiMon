package uk.co.simon.app.filesAndSync;

import java.io.File;
import java.io.IOException;
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
import org.apache.http.util.EntityUtils;

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

import com.bugsense.trace.BugSenseHandler;

public class Sync {

	Context context;
	String mEmail;
	String mPassword;
	int userID;
	SiMonWordpress wp = null;
	SiMonUser user;

	public Sync(Context context) {
		this.context = context;
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		mEmail = sharedPref.getString("EmailPref", null);
		mPassword = sharedPref.getString("PasswordPref", null);
		userID = sharedPref.getInt("UserID", 0);
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
		try {
			wp = new SiMonWordpress(mEmail, mPassword, "http://www.simon-app.com/xmlrpc.php");
		} catch (MalformedURLException e) {
			BugSenseHandler.sendEvent(e.toString());
		}		
	}

	public boolean projectSync() {

		if(networkIsAvailable()) {
			DataSourceProjects datasource = new DataSourceProjects(context);
			datasource.open();
			List<SQLProject> projects = datasource.getAllProjects();
			try {
				List<WPProject> WPProjects = wp.getProjects();
				if (!WPProjects.get(0).Project.contains("Not Found")) {
					List<SQLProject> projectsOnDevice = new ArrayList<SQLProject>();
					projectsOnDevice.addAll(projects);
					for (WPProject project : WPProjects) {
						SQLProject sqlProject = datasource.getProjectFromCloudId(project.getCloudID());
						if (sqlProject == null) {
							datasource.createProject(project.toSQLProject());
						} else if(!sqlProject.equals(project.toSQLProject())) {
							SQLProject updateProject = project.toSQLProject();
							updateProject.setId(sqlProject.getId());
							datasource.updateProject(updateProject);
						}
					}
				} else {
					datasource.deleteAllProjects();
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
				BugSenseHandler.sendEvent(e.toString());
				return false;
			} catch (Exception e) {
				BugSenseHandler.sendEvent(e.toString());
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("ErrorPref", context.getString(R.string.msgSyncFail) );
				editor.commit();
				return false;
			}
			List<SQLProject> syncedProjects = datasource.getAllProjects();
			for (SQLProject project : syncedProjects) {
				locationSync(project);
			}
			datasource.close();
			return true;
		} else {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("ErrorPref", context.getString(R.string.msgSyncFail) );
			editor.commit();
			return false;
		}
	}

	/*	public boolean singleProjectSync(long projectID) {

		if(networkIsAvailable()) {
			DataSourceProjects datasource = new DataSourceProjects(context);
			datasource.open();
			SQLProject project = datasource.getProject(projectID);
			Boolean returnState = true;
			try {
				project.setCloudID(wp.uploadProject(project));
				if (project.getCloudID()>-1) {
					datasource.updateProject(project);
				} else {
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("ErrorPref", context.getString(R.string.errProjectServerLimit) );
					editor.commit();
					returnState = false;
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
			} catch (Exception e) {
				e.printStackTrace();
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("ErrorPref", context.getString(R.string.msgUploadFail) );
				editor.commit();
				return false;
			}
			datasource.close();
			return returnState;
		} else {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("ErrorPref", context.getString(R.string.msgUploadFail) );
			editor.commit();
			return false;
		}
	} */

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
				BugSenseHandler.sendEvent(e.toString());
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
				BugSenseHandler.sendEvent(e.toString());
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
					if (!uploadPhotos(reportItem, project.getCloudID(), location.getCloudID())) return false;
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
					BugSenseHandler.sendEvent(e.toString());
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
				File file = new File(photo.getPhotoPath());

				if(file.exists()){ 
					HttpClient httpclient = new DefaultHttpClient();
					HttpPost httppost = new HttpPost(
							"http://www.simon-app.com/wp-content/plugins/SiMon%20Plugin/Upload.php?photo=set");

					try {
						MultipartEntity entity = new MultipartEntity();

						entity.addPart("report_item_id", new StringBody(String.valueOf(reportItem.getCloudID())));
						entity.addPart("project_id", new StringBody(String.valueOf(projectCloudID)));
						entity.addPart("location_id", new StringBody(String.valueOf(locationCloudID)));
						entity.addPart("user_id", new StringBody(String.valueOf(userID)));
						entity.addPart("cloud_id", new StringBody(String.valueOf(photo.getCloudID())));

						entity.addPart("image", new FileBody(file,"image/jpeg"));

						httppost.setEntity(entity);
						HttpResponse response = httpclient.execute(httppost);

						HttpEntity resEntity = response.getEntity();

						String content = EntityUtils.toString(resEntity);
						
						if (content.contains("Error:")) {
							SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor editor = sharedPref.edit();
							editor.putString("ErrorPref", content );
							editor.commit();
							return false;
						} else {
							photo.setCloudID(Long.parseLong(content));
							datasource.updatePhoto(photo);
						}
					} catch (ClientProtocolException e) {
						BugSenseHandler.sendEvent(e.toString());
					} catch (IOException e) {
						BugSenseHandler.sendEvent(e.toString());
					}
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
				entity.addPart("pdf", new FileBody(file,"application/pdf"));

				httppost.setEntity(entity);
				HttpResponse response = httpclient.execute(httppost);

				HttpEntity resEntity = response.getEntity();

				String content = EntityUtils.toString(resEntity);
				
				if (content.contains("Error:")) {
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("ErrorPref", content );
					editor.commit();
					return false;
				}
				
			} catch (ClientProtocolException e) {
				BugSenseHandler.sendEvent(e.toString());
			} catch (IOException e) {
				BugSenseHandler.sendEvent(e.toString());
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