package uk.co.simon.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import uk.co.simon.app.filesAndSync.FileManager;
import uk.co.simon.app.sensors.mySensorEventListener;
import uk.co.simon.app.sqllite.DataSourcePhotos;
import uk.co.simon.app.sqllite.DataSourceReportItems;
import uk.co.simon.app.sqllite.SQLPhoto;
import uk.co.simon.app.sqllite.SQLReportItem;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class ActivityTakePhoto extends Activity implements SurfaceHolder.Callback {

	protected static final String TAG = "Activity Take Photo";
	Camera camera;
	SurfaceView surfaceView;
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
	private static SensorManager sensorService;
	private Sensor accelerometer;
	private Sensor magnetometer;
	private Sensor gyro;
	private mySensorEventListener sensorEventListener = new mySensorEventListener();
	private Timer fuseTimer = new Timer();

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		BugSenseHandler.initAndStartSession(ActivityTakePhoto.this, "6c6b0664");
		setContentView(R.layout.activity_take_photo);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		gyro = sensorService.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		if (accelerometer != null  && magnetometer != null && gyro != null) {
			sensorService.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			sensorService.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
			sensorService.registerListener(sensorEventListener, gyro, SensorManager.SENSOR_DELAY_NORMAL);
			mySensorEventListener.calculateFusedOrientationTask fusedOrientation = sensorEventListener.new calculateFusedOrientationTask();
			fuseTimer.scheduleAtFixedRate(fusedOrientation, 1000, sensorEventListener.TIME_CONSTANT);
		} else if (accelerometer != null  && magnetometer != null) {
			sensorService.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			sensorService.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
			mySensorEventListener.calculateFusedOrientationTask fusedOrientation = sensorEventListener.new calculateFusedOrientationTask();
			fuseTimer.scheduleAtFixedRate(fusedOrientation, 1000, sensorEventListener.TIME_CONSTANT);
		} else {
			Toast.makeText(this, "ORIENTATION Sensor not found",
					Toast.LENGTH_SHORT).show();
		}

		getWindow().setFormat(PixelFormat.UNKNOWN);
		surfaceView = (SurfaceView)findViewById(R.id.takePhotoSurfaceView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

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
						p.setFocusMode(Parameters.FOCUS_MODE_AUTO);
						camera.setParameters(p);
						camera.autoFocus(new AutoFocusCallback(){
							public void onAutoFocus(boolean success, Camera camera) {
								camera.takePicture(null, null, mPicture);
							}	
						});
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
	
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(previewing){
			camera.stopPreview();
			previewing = false;
		}

		if (camera != null){
			try {
				camera.setPreviewDisplay(surfaceHolder);
				Camera.Size size = getCameraPreviewSize(camera);
				Camera.Parameters params = setMaxCameraSize(camera);
				params.setPreviewSize(size.width, size.height);
				params.setRotation(90);
				camera.setParameters(params);
				camera.startPreview();
				setCameraDisplayOrientation(this, 0, camera);
				previewing = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Camera.Size getCameraPreviewSize(Camera camera) {

		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> supportedSizes = params.getSupportedPreviewSizes();

		int width = surfaceView.getWidth();
		int height = surfaceView.getHeight();
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) width / height;
        if (supportedSizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        // Try to find an size match aspect ratio and size
        for (Size size : supportedSizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : supportedSizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
	}

	private Camera.Parameters setMaxCameraSize(Camera camera){

		int width = 0;
		int height = 0;

		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> supportedSizes = params.getSupportedPreviewSizes();

		for (Camera.Size size : supportedSizes) {
			if (size.width >= width || size.height >= height ) {
				width=size.width;
				height=size.height;
			}
		}

		params.setPictureSize(width, height);
		return params;
	}
	
	public void surfaceCreated(SurfaceHolder holder) {
		camera = getCameraInstance();
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

	public void surfaceDestroyed(SurfaceHolder holder) {
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
			
			if (accelerometer != null  && magnetometer != null && gyro != null) {
				float[] orientation = sensorEventListener.getFusedOrientation();
				String orientationString = "Azimuth = " + 
						orientation[0] + ", " +
						"Pitch = " + orientation[1] + ", " +
						"Roll = " + orientation[2];
				Toast toast = Toast.makeText(getBaseContext(), "Orientation: " + orientationString, Toast.LENGTH_SHORT);
				toast.show();
				thisPhoto.setAzimuth(orientation[0]);
				thisPhoto.setPitch(orientation[1]);
				thisPhoto.setRoll(orientation[2]);
			} else if (accelerometer != null  && magnetometer != null) {
				float[] orientation = sensorEventListener.getAccMagOrientation();
				String orientationString = "Azimuth = " + 
						orientation[0] + ", " +
						"Pitch = " + orientation[1] + ", " +
						"Roll = " + orientation[2];
				Toast toast = Toast.makeText(getBaseContext(), "Orientation: " + orientationString, Toast.LENGTH_SHORT);
				toast.show();
				thisPhoto.setAzimuth(orientation[0]);
				thisPhoto.setPitch(orientation[1]);
				thisPhoto.setRoll(orientation[2]);
			}
			DataSourcePhotos photosDatasource = new DataSourcePhotos(getBaseContext());
            photosDatasource.open();
            SQLPhoto newPhoto = photosDatasource.createPhoto(thisPhoto);
            thisPhoto.setId(newPhoto.getId());
            photosDatasource.close();
		}
	};

	public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info =  new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0: degrees = 0; break;
		case Surface.ROTATION_90: degrees = 90; break;
		case Surface.ROTATION_180: degrees = 180; break;
		case Surface.ROTATION_270: degrees = 270; break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

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
		sensorService.unregisterListener(sensorEventListener);
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