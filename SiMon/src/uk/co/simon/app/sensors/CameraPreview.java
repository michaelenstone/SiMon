package uk.co.simon.app.sensors;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;  
	private Camera mCamera;

	@SuppressWarnings("deprecation")
	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public CameraPreview(Context context) {
		super(context);
	}
	public CameraPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(holder);
			Camera.Size size = getCameraPreviewSize(mCamera);
			Camera.Parameters params = setMaxCameraSize(mCamera);
			params.setPreviewSize(size.width, size.height);
			params.setRotation(90);
			mCamera.setParameters(params);
			mCamera.startPreview();
		} catch (IOException e) {
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// empty. Take care of releasing the Camera preview in your activity.
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.

		if (mHolder.getSurface() == null){
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e){
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			Camera.Size size = getCameraPreviewSize(mCamera);
			Camera.Parameters params = setMaxCameraSize(mCamera);
			Camera.CameraInfo info = new Camera.CameraInfo();
			android.hardware.Camera.getCameraInfo(0, info);
			int rotation = this.getDisplay().getRotation();
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
			mCamera.setDisplayOrientation(result);
			params.setRotation(result);
			params.setPreviewSize(size.width, size.height);
			mCamera.setParameters(params);
			mCamera.startPreview();
			mCamera.autoFocus(null);
		} catch (Exception e){

		}
	}

	private Camera.Size getCameraPreviewSize(Camera camera) {

		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> supportedSizes = params.getSupportedPreviewSizes();

		int width = mHolder.getSurfaceFrame().width();
		int height = mHolder.getSurfaceFrame().height();
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

		int maxSize = 0;
		int width = 0;
		int height = 0;

		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();

		for (Camera.Size size : supportedSizes) {
			int MP = size.width * size.height;
			if (MP > maxSize) {
				width=size.width;
				height=size.height;
				maxSize = MP;
			}
		}

		params.setPictureSize(width, height);
		return params;
	}
}
