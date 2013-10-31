package uk.co.simon.app.wordpress;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.bican.wordpress.Wordpress;
import redstone.xmlrpc.XmlRpcArray;
import redstone.xmlrpc.XmlRpcFault;
import redstone.xmlrpc.XmlRpcProxy;
import redstone.xmlrpc.XmlRpcStruct;
import uk.co.simon.app.sqllite.SQLLocation;
import uk.co.simon.app.sqllite.SQLProject;
import uk.co.simon.app.sqllite.SQLReport;
import uk.co.simon.app.sqllite.SQLReportItem;

public class SiMonWordpress extends Wordpress {

	private SiMon simon = null;

	public SiMonWordpress(String username, String password, String xmlRpcUrl) throws MalformedURLException {
		super(username, password, xmlRpcUrl);
		this.initMetaWebLog();
	}

	@SuppressWarnings("nls")
	private void initMetaWebLog() throws MalformedURLException {
		final URL url = new URL(super.xmlRpcUrl);
		this.simon = (SiMon) XmlRpcProxy.createProxy(url, "SiMon", new Class[] { SiMon.class }, true);
	}
	
	@SuppressWarnings("unchecked")
	public List<WPProject> getProjects() throws XmlRpcFault {
		XmlRpcArray r = this.simon.getProjects(super.username, super.password);
		return super.fillFromXmlRpcArray(r, WPProject.class);
	}
	
	public int uploadProject(SQLProject project) throws XmlRpcFault {
		return this.simon.uploadProject(this.username, this.password, 
				project.getCloudID(), project.getProject(), project.getProjectNumber());
	}
	
	public int projectLimit(SQLProject project) throws XmlRpcFault {
		return this.simon.projectLimit(this.username, this.password, 
				project.getCloudID());
	}

	@SuppressWarnings("unchecked")
	public List<WPLocation> getLocations(long projectId) throws XmlRpcFault {
		XmlRpcArray r = this.simon.getLocations(super.username, super.password, projectId);
		return super.fillFromXmlRpcArray(r, WPLocation.class);
	}
	
	public int uploadLocation(SQLLocation location, long projectCloudID) throws XmlRpcFault {
		return this.simon.uploadLocation(this.username, this.password, 
				location.getCloudID(), location.getLocation(), projectCloudID);
	}
	
	public int uploadReport(SQLReport report, long projectCloudID) throws XmlRpcFault {
		return this.simon.uploadReport(this.username, this.password, report.getCloudID(), projectCloudID, 
				report.getReportDate(), report.getReportType(), report.getSupervisor(), report.getReportRef(), 
				report.getWeather(), report.getTemp(), report.getTempType());
	}
	
	public int uploadReportItem(SQLReportItem reportItem, long projectCloudID, long reportCloudID, long locationCloudID) throws XmlRpcFault {
		return this.simon.uploadReportItem(this.username, this.password, 
				reportItem.getCloudID(), projectCloudID, reportCloudID, locationCloudID, 
				reportItem.getReportItem(), reportItem.getProgress(), reportItem.getDescription(), 
				reportItem.getOnTIme());
	}
	
	public SiMonUser getSimonUserInfo() throws XmlRpcFault {
	    XmlRpcStruct r = simon.getSiMonUserInfo(this.username, this.password);
	    SiMonUser result = new SiMonUser();
	    result.fromXmlRpcStruct(r);
	    return result;
	  }
	
}

interface SiMon {

	XmlRpcArray getProjects(String username, String password)
			throws XmlRpcFault;
	
	XmlRpcArray getLocations(String username, String password, long projectCloudID)
			throws XmlRpcFault;
	
	int uploadProject(String username, String password, 
			long cloudID, String projectName, String projectNumber) throws XmlRpcFault;
	
	int projectLimit(String username, String password, 
			long cloudID)  throws XmlRpcFault;
	
	int uploadLocation(String username, String password, 
			long cloudID, String location, long projectID) throws XmlRpcFault;

	int uploadReport(String username, String password, 
			long cloudID, long projectID, String reportDate, 
			boolean reportType, String supervisor, String reportRef,
			String weather, String temp, long tempType) throws XmlRpcFault;
	
	int uploadReportItem(String username, String password, 
			long cloudID, long projectID, long reportID, long locationID,
			String activityOrItem, float progress, String description,
			String onTime) throws XmlRpcFault;
	
	XmlRpcStruct getSiMonUserInfo(String username, String password)
		      throws XmlRpcFault;
}
