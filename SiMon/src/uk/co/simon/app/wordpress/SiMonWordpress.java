package uk.co.simon.app.wordpress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import net.bican.wordpress.Attachment;
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
	
	private static byte[] getBytesFromFile(File file) {
	    byte[] result = null;
	    InputStream is = null;
	    try {
	      is = new FileInputStream(file);

	      // Get the size of the file
	      long length = file.length();

	      // You cannot create an array using a long type.
	      // It needs to be an int type.
	      // Before converting to an int type, check
	      // to ensure that file is not larger than Integer.MAX_VALUE.
	      if (length > Integer.MAX_VALUE) {
	        // File is too large
	      }

	      // Create the byte array to hold the data
	      byte[] bytes = new byte[(int) length];

	      // Read in the bytes
	      int offset = 0;
	      int numRead = 0;
	      while (offset < bytes.length
	          && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
	        offset += numRead;
	      }

	      // Ensure all the bytes have been read in
	      if (offset < bytes.length) {
	        throw new IOException(
	            "Could not completely read file " + file.getName()); //$NON-NLS-1$
	      }
	      result = bytes;
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    } catch (IOException e) {
	      e.printStackTrace();
	    } finally {
	      if (is != null)
	        try {
	          is.close();
	        } catch (IOException e) {
	          e.printStackTrace();
	        }
	    }
	    return result;
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
	
	public int deleteProject(SQLProject project) throws XmlRpcFault {
		return this.simon.deleteProject(this.username, this.password, 
				project.getCloudID());
	}

	@SuppressWarnings("unchecked")
	public List<WPLocation> getLocations(long projectId, long searchFrom) throws XmlRpcFault {
		XmlRpcArray r = this.simon.getLocations(super.username, super.password, projectId, searchFrom);
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
	
	public WPPhotoUpload uploadPhoto(long cloudID, long reportItemCloudID, long projectCloudID, long locationCloudID) throws XmlRpcFault {
		XmlRpcStruct r = this.simon.uploadPhoto(this.username, this.password, cloudID, reportItemCloudID, projectCloudID, locationCloudID);
		WPPhotoUpload result = new WPPhotoUpload();
	    result.fromXmlRpcStruct(r);
	    return result;
	}
	
	public String uploadPhotoFile(String mimeType, File file,
		      long cloudID) throws XmlRpcFault {
		    Attachment att = new Attachment();
		    att.setType(mimeType);
		    att.setOverwrite(true);
		    att.setName(file.getName());
		    att.setBits(getBytesFromFile(file));
		    XmlRpcStruct d = att.toXmlRpcStruct();
		    return this.simon.uploadPhotoFile(this.username, this.password, d, cloudID);
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
	
	XmlRpcArray getLocations(String username, String password, long projectCloudID, long searchFrom)
			throws XmlRpcFault;
	
	int uploadProject(String username, String password, 
			long cloudID, String projectName, String projectNumber) throws XmlRpcFault;
	
	int deleteProject(String username, String password, 
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
	
	XmlRpcStruct uploadPhoto(String username, String password, long cloudID, long reportItemCloudID, 
			long projectCloudID, long locationCloudID) throws XmlRpcFault;
	
	String uploadPhotoFile(String username, String password,
		      XmlRpcStruct data, long cloudID) throws XmlRpcFault;
	
	XmlRpcStruct getSiMonUserInfo(String username, String password)
		      throws XmlRpcFault;
}
