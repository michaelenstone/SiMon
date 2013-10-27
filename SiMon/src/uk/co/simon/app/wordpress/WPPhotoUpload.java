package uk.co.simon.app.wordpress;

import net.bican.wordpress.StringHeader;
import net.bican.wordpress.XmlRpcMapped;

public class WPPhotoUpload extends XmlRpcMapped  implements StringHeader {

	public String status;
	public int cloudID;

	public String getStatus() {
		return status;
	}

	public void setProject(String status) {
		this.status = status;
	}

	public long getCloudID() {
		return cloudID;
	}

	public void setCloudID(long cloudID) {
		this.cloudID = (int) cloudID;
	}

	@Override
	public String getStringHeader() {
		final String TAB = ":";
		return "Status" + TAB + "cloudID";
	}

}
