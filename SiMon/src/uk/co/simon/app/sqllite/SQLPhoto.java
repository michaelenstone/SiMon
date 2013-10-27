package uk.co.simon.app.sqllite;

import android.os.Bundle;


public class SQLPhoto {
	private long id;
	private long reportItemId;
	private String photoPath;
	private long locationId;
	private float azimuth;
	private float pitch;
	private float roll;
	private float GPSX;
	private float GPSY;
	private float GPSZ;
	private long cloudID = 0;

	public long getId() {
		return id;
	}

	public long getReportItemId() {
		return reportItemId;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public long getLocationId() {
		return locationId;
	}

	public float getAzimuth() {
		return azimuth;
	}

	public float getPitch() {
		return pitch;
	}

	public float getGPSX() {
		return GPSX;
	}

	public float getGPSY() {
		return GPSY;
	}

	public float getGPSZ() {
		return GPSZ;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setReportItemId(long reportItemId) {
		this.reportItemId = reportItemId;
	}

	public void setPhoto(String photoPath) {
		this.photoPath = photoPath;
	}

	public void setLocationId(long locationId) {
		this.locationId = locationId;
	}

	public void setAzimuth(float azimuth) {
		this.azimuth = azimuth;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public void setGPSX(float GPSX) {
		this.GPSX = GPSX;
	}

	public void setGPSY(float GPSY) {
		this.GPSY = GPSY;
	}

	public void setGPSZ(float GPSZ) {
		this.GPSZ = GPSZ;
	}

	@Override
	public String toString() {
		return photoPath;
	}
	
	public Bundle toBundle() {
		Bundle bundle = new Bundle();
		bundle.putLong("id", id);
		bundle.putLong("reportItemId", reportItemId);
		bundle.putString("photoPath", photoPath);
		bundle.putLong("locationId", locationId);
		bundle.putFloat("azimuth", azimuth);
		bundle.putFloat("pitch", pitch);
		bundle.putFloat("roll",roll);
		bundle.putFloat("GPSX", GPSX);
		bundle.putFloat("GPSY", GPSY);
		bundle.putFloat("GPSZ", GPSZ);
		
		return bundle;
	}
	
	public void fromBundle(Bundle bundle) {
		this.id = bundle.getLong("id");
		this.reportItemId = bundle.getLong("reportItemId");
		this.photoPath = bundle.getString("photoPath");
		this.locationId = bundle.getLong("locationId");
		this.azimuth = bundle.getFloat("azimuth");
		this.pitch = bundle.getFloat("pitch");
		this.roll = bundle.getFloat("roll");
		this.GPSX = bundle.getFloat("GPSX");
		this.GPSY = bundle.getFloat("GPSY");
		this.GPSZ = bundle.getFloat("GPSZ");
	}

	public float getRoll() {
		return roll;
	}

	public void setRoll(float roll) {
		this.roll = roll;
	}

	
	public long getCloudID() {
		return cloudID;
	}

	
	public void setCloudID(long cloudID) {
		this.cloudID = cloudID;
	}

}
