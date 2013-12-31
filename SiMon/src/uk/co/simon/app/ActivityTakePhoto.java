package uk.co.simon.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import uk.co.simon.app.filesAndSync.FileManager;
import uk.co.simon.app.sensors.CameraPreview;
import uk.co.simon.app.sqllite.DataSourcePhotos;
import uk.co.simon.app.sqllite.DataSourceReportItems;
import uk.co.simon.app.sqllite.SQLPhoto;
import uk.co.simon.app.sqllite.SQLReportItem;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class ActivityTakePhoto extends Activity {

	Camera camera;
	private CameraPreview preview;
	//SurfaceView surfaceView;
	SurfaceHolder surfaceHolder;
	boolean previewing = false;
	LayoutInflater controlInflater = null;   
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private DataSourceReportItems datasource;
	private SQLReportItem thisReportItem;
	private SQLPhoto thisPhoto = new SQLPhoto();
	private LinearLayout resultButtons;
	private Button captureButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BugSenseHandler.initAndStartSession(ActivityTakePhoto.this, "6c6b0664");
		setContentView(R.layout.activity_take_photo);

		camera = getCameraInstance();
		preview = new CameraPreview(this, camera);
		FrameLayout previewFrame = (FrameLayout) findViewById(R.id.takePhotoFrameLayout);
		previewFrame.addView(preview);

		resultButtons = (LinearLayout) findViewById(R.id.photoResultLayout);
		resultButtons.setVisibility(View.GONE);

		Bundle extras = getIntent().getExtras();

		datasource = new DataSourceReportItems(this);
		datasource.open();
		thisReportItem = datasource.getReportItem(extras.getLong("reportItemId"));
		datasource.close();

		// Add a listener to the Capture button
		captureButton = (Button) findViewById(R.id.takePhotoButton);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						// get an image from the camera
						Parameters p = camera.getParameters();
						p.setFlashMode(Parameters.FLASH_MODE_AUTO);
						SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
						if (sharedPref.getBoolean("autoFocusPref", false)) {
							List<String> focusModes = p.getSupportedFocusModes();
							if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
							    p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
							} else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
								p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
							}
							camera.setParameters(p);
							camera.autoFocus( new AutoFocusCallback(){
								public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {
									camera.takePicture(null, null, mPicture);
							    }});
						} else {
							camera.setParameters(p);
							camera.takePicture(null, null, mPicture);
						}
					}
				}
				);

		Button discardButton = (Button) findViewById(R.id.discardPhotoButton);
		discardButton.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {	    			
						File oldPhoto = new File(thisPhoto.getPhotoPath());
						oldPhoto.delete();
						DataSourcePhotos photosDatasource = new DataSourcePhotos(getBaseContext());
						photosDatasource.open();
						photosDatasource.deletePhoto(thisPhoto);
						photosDatasource.close();
						resultButtons = (LinearLayout) findViewById(R.id.photoResultLayout);
						resultButtons.setVisibility(View.GONE);
						captureButton.setVisibility(View.VISIBLE);
						camera.startPreview();
					}
				}
				);

		Button keepButton = (Button) findViewById(R.id.keepPhotoButton);
		keepButton.setOnClickListener(
				new View.OnClickListener() {
					public void onClick(View v) {
						resultButtons = (LinearLayout) findViewById(R.id.photoResultLayout);
						resultButtons.setVisibility(View.GONE);
						captureButton.setVisibility(View.VISIBLE);
						camera.startPreview();
					}
				}
				);
	}

	public Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e){
			Toast toast = Toast.makeText(getBaseContext(), "Camera Unavailable", Toast.LENGTH_SHORT);
			toast.show();
		}
		return c; // returns null if camera is unavailable
	}

	private PictureCallback mPicture = new PictureCallback() {

		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null){
				Toast toast = Toast.makeText(getBaseContext(), "Error creating media file, check storage permissions", Toast.LENGTH_SHORT);
				toast.show();
				return;
			}

			resultButtons = (LinearLayout) findViewById(R.id.photoResultLayout);
			resultButtons.setVisibility(View.VISIBLE);
			captureButton.setVisibility(View.GONE);

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Toast toast = Toast.makeText(getBaseContext(), "File not found: " + e.getMessage(), Toast.LENGTH_LONG);
				toast.show();
				return;
			} catch (IOException e) {
				Toast toast = Toast.makeText(getBaseContext(), "Error accessing file: " + e.getMessage(), Toast.LENGTH_LONG);
				toast.show();
				return;
			}           

			thisPhoto.setPhoto(pictureFile.getAbsolutePath());
			thisPhoto.setReportItemId(thisReportItem.getId());
			thisPhoto.setLocationId(thisReportItem.getLocationId());

			DataSourcePhotos photosDatasource = new DataSourcePhotos(getBaseContext());
			photosDatasource.open();
			SQLPhoto newPhoto = photosDatasource.createPhoto(thisPhoto);
			thisPhoto.setId(newPhoto.getId());
			photosDatasource.close();
		}
	};

	@SuppressLint("SimpleDateFormat")
	private File getOutputMediaFile(int type){

		File mediaStorageDir = FileManager.getPictureStorageLocation(getBaseContext());

		if (mediaStorageDir!=null) {
			// Create a media file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			File mediaFile;
			if (type == MEDIA_TYPE_IMAGE){
				mediaFile = new File(mediaStorageDir.getPath() + File.separator +
						"IMG_"+ timeStamp + ".jpg");
			} else if(type == MEDIA_TYPE_VIDEO) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator +
						"VID_"+ timeStamp + ".mp4");
			} else {
				return null;
			}
			return mediaFile;
		} else {
			return null;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();      // if you are using MediaRecorder, release it first
		releaseCamera();              // release the camera immediately on pause event
		if (getParent()==null) {
			setResult(Activity.RESULT_OK);
		} else {
			getParent().setResult(Activity.RESULT_OK);
		}
	}

	private void releaseCamera(){
		if (camera != null){
			camera.release();        // release the camera for other applications
			camera = null;
		}
	}
}