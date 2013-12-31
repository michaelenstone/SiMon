package uk.co.simon.app.adapters;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import uk.co.simon.app.R;
import uk.co.simon.app.sqllite.SQLPhoto;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class AdapterImages extends BaseAdapter {
   
	private static Context mContext;
	private List<SQLPhoto> list = new ArrayList<SQLPhoto>();

    public AdapterImages(Context c, List<SQLPhoto> images) {
        mContext = c;
        list = images;
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return list.get(position).getPhotoPath();
    }

    public SQLPhoto getSQLPhoto(int position) {
        return list.get(position);
    }
    
    public long getItemId(int position) {
        return list.get(position).getId();
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialise some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(150, 150));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Bitmap bm = null;
		try {
			bm = decodePath(list.get(position).getPhotoPath(), 150);
		} catch (FileNotFoundException e) {
			Toast toast = Toast.makeText(convertView.getContext(), mContext.getString(R.string.errFileNotFound), Toast.LENGTH_LONG);
			toast.show();
		}
        imageView.setImageBitmap(bm);
        return imageView;
    }
    
    static Bitmap decodePath(String selectedImage, int size) throws FileNotFoundException {

    	String imgFilePath;
    	// can post image
    	if (selectedImage.contains("content://")) {
    		String [] proj={MediaStore.Images.Media.DATA};
    		Uri contentUri = Uri.parse(selectedImage);
    		Cursor cursor = mContext.getContentResolver().query( contentUri,
    				proj, // Which columns to return
    				null,       // WHERE clause; which rows to return (all rows)
    				null,       // WHERE clause selection arguments (none)
    				null); // Order-by clause (ascending by name)
    		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    		cursor.moveToFirst();

    		imgFilePath = cursor.getString(column_index);
    		cursor.close();
    	} else {
    		imgFilePath = selectedImage;
    	}

    	// Decode image size
    	final BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(imgFilePath, options);

    	// The new size we want to scale to
    	final int REQUIRED_SIZE = size;

    	// Find the correct scale value. 
    	options.inSampleSize = calculateInSampleSize(options, REQUIRED_SIZE, REQUIRED_SIZE);

    	// Decode bitmap with inSampleSize set
    	options.inJustDecodeBounds = false;
    	return BitmapFactory.decodeFile(imgFilePath, options);

    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        final int heightRatio = Math.round((float) height / (float) reqHeight);
        final int widthRatio = Math.round((float) width / (float) reqWidth);

        // Choose the smallest ratio as inSampleSize value, this will guarantee
        // a final image with both dimensions larger than or equal to the
        // requested height and width.
        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
    }

    return inSampleSize;
}
}
