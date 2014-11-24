package uk.co.simon.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

import com.splunk.mint.Mint;

public class ActivitySettings extends PreferenceActivity {
    
	private static final int REQ_CODE_PICK_IMAGE = 100;
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Mint.initAndStartSession(ActivitySettings.this, "6c6b0664");
        addPreferencesFromResource(R.xml.preferences);
        Preference imagePicker = (Preference) findPreference("imagePicker");
        imagePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {	        	 
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
                return true;
            }
        });
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

		switch(requestCode) { 
		case REQ_CODE_PICK_IMAGE:
			if(resultCode == Activity.RESULT_OK){  
				String [] proj={MediaStore.Images.Media.DATA};
				Uri contentUri = imageReturnedIntent.getData();
				Cursor cursor = getContentResolver().query( contentUri,
						proj, // Which columns to return
						null,       // WHERE clause; which rows to return (all rows)
						null,       // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String imgFilePath = cursor.getString(column_index);

			    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
			    SharedPreferences.Editor editor = preferences.edit();
			    editor.putString("imagePicker", imgFilePath);
			    editor.commit();
				
			}
		}
	}
}
