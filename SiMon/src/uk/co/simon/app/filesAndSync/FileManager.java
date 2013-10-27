package uk.co.simon.app.filesAndSync;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class FileManager {

	private static final String applicationStorage = "/Android/data/uk.co.simon.app";
	private static final String images = "/images";
	private static final String pdfs = "/pdfs";
	
	
	public static File getPictureStorageLocation(Context context) {
		File dir = null;
		try {
			dir = new File(Environment.getExternalStorageDirectory() + applicationStorage + images);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		} catch (Exception e) {
			Toast toast = Toast.makeText(context, "Error Creating Directory: " + e, Toast.LENGTH_LONG);
			toast.show();
		} 
		return dir;
	}
	
	public static File getPDFStorageLocation(Context context) {
		File dir = null;
		try {
			dir = new File(Environment.getExternalStorageDirectory() + applicationStorage + pdfs);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		} catch (Exception e) {
			Toast toast = Toast.makeText(context, "Error Creating Directory: " + e, Toast.LENGTH_LONG);
			toast.show();
		} 
		return dir;
	}
}