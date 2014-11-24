package uk.co.simon.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.simon.app.adapters.AdapterImages;
import uk.co.simon.app.filesAndSync.FileManager;
import uk.co.simon.app.sqllite.DataSourceLocations;
import uk.co.simon.app.sqllite.DataSourcePhotos;
import uk.co.simon.app.sqllite.DataSourceReportItems;
import uk.co.simon.app.sqllite.SQLLocation;
import uk.co.simon.app.sqllite.SQLPhoto;
import uk.co.simon.app.sqllite.SQLReportItem;
import uk.co.simon.app.ui.customElements.ExpandableHeightGridView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.splunk.mint.Mint;

public class DialogFragmentReportItem extends DialogFragment {

	private static final int REQ_CODE_PICK_IMAGE = 100;
	private static final int REQ_CODE_TAKE_IMAGE = 200;
	private DataSourceReportItems datasource;
	private DataSourceLocations locationsDatasource;
	private DataSourcePhotos photosDatasource;
	Handler handler;
	private SQLReportItem thisReportItem;
	private View view;
	onDialogResultListener mListener;
	private SQLLocation thisLocation = new SQLLocation();
	Context mContext;
	private long projectId;
	private boolean save = false;

	public DialogFragmentReportItem() {
	}

	public interface onDialogResultListener {
        public void onDialogPositiveClick(DialogFragment dialog);
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mContext = getActivity(); 
		Mint.initAndStartSession(mContext, "6c6b0664");
		projectId = getArguments().getLong("projectId");
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		view = inflater.inflate(R.layout.dialog_fragment_report_item, null);

		final AutoCompleteTextView locationText = (AutoCompleteTextView) view.findViewById(R.id.dailyProgressLocationEditText);

		locationsDatasource = new DataSourceLocations(mContext);
		locationsDatasource.open();

		List<SQLLocation> values = new ArrayList<SQLLocation>();
		values.addAll(locationsDatasource.getAllProjectLocations(projectId));

		ArrayAdapter<SQLLocation> adapter = new ArrayAdapter<SQLLocation> (mContext, R.layout.spinner_row, values);
		locationText.setAdapter(adapter);

		datasource = new DataSourceReportItems(mContext);
		datasource.open();

		if (getArguments().getBoolean("reportType")) {
			LinearLayout progress = (LinearLayout) view.findViewById(R.id.dailyProgressProgressLayout);
			progress.setVisibility(View.GONE);
		}

		if (getArguments().getInt("dialogType")==1) {

			thisReportItem = datasource.getReportItem(getArguments().getLong("reportItemId"));
			save = true;

			final EditText activityText = (EditText) view.findViewById(R.id.dailyProgressActivityEditText);
			final EditText progressText = (EditText) view.findViewById(R.id.dailyProgressProgressEditText);
			final Spinner onTimeSpinner = (Spinner) view.findViewById(R.id.dailyProgressOnTimeSpinner);
			final EditText descriptionText = (EditText) view.findViewById(R.id.dailyProgressDescriptionEditText);

			try {
				thisLocation = locationsDatasource.getLocation(thisReportItem.getLocationId());
				locationText.setText(thisLocation.getLocation());
			} catch (Exception e) {

			}
			activityText.setText(thisReportItem.getReportItem());
			progressText.setText(Float.toString(thisReportItem.getProgress()));
			descriptionText.setText(thisReportItem.getDescription());
			onTimeSpinner.setSelection(getIndex(onTimeSpinner, thisReportItem.getOnTIme()));

			locationText.dismissDropDown();

			populatePhotoGrid();

		} else {
			thisReportItem = new SQLReportItem();
			thisReportItem.setReportId(getArguments().getLong("reportId"));
			thisReportItem = datasource.createReportItem(thisReportItem);
		}

		locationsDatasource.close();
		datasource.close();
		thisLocation.setLocationProjectId(projectId);
		
		builder.setPositiveButton(R.string.dailyProgressItemOK, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {	        	 
				locationsDatasource = new DataSourceLocations(mContext);
				locationsDatasource.open();
				datasource = new DataSourceReportItems(mContext);
				datasource.open();
				thisLocation.setLocation(locationText.getText().toString());
				try {
					thisLocation = locationsDatasource.findLocationId(thisLocation);
				} catch (Exception e) {
					thisLocation = locationsDatasource.createLocation(thisLocation);
				}
				locationsDatasource.close();
				datasource.updateReportItem(packageReportItem(thisLocation.getId()));
				datasource.close();
				mListener.onDialogPositiveClick(DialogFragmentReportItem.this);
				save = true;
				dismiss();
			}
		});

		final Button takePhoto = (Button) view.findViewById(R.id.dailyProgressTakePhotoButton);
		final Button attachPhoto = (Button) view.findViewById(R.id.dailyProgressAttachPhotoButton);

		takePhoto.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent newTakePhoto = new Intent(mContext, ActivityTakePhoto.class);
				newTakePhoto.putExtra("reportItemId", thisReportItem.getId());
				startActivityForResult(newTakePhoto, REQ_CODE_TAKE_IMAGE);
			}
		});

		attachPhoto.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {	        	 
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
			}
		});

		builder.setView(view);
		builder.setCancelable(false);
		Dialog built = builder.create();
		built.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		built.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		return built;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (onDialogResultListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " onDialogResultListener");
		}
	}

	public void onDismiss(DialogInterface dialog) {
		if (!save) {
			datasource = new DataSourceReportItems(mContext);
			datasource.open();
			datasource.deleteReportItem(thisReportItem);
			datasource.close();
		}
		super.onDismiss(dialog);
	}

	public SQLReportItem packageReportItem(long locationId) {

		final EditText activityText = (EditText) view.findViewById(R.id.dailyProgressActivityEditText);
		final EditText progressText = (EditText) view.findViewById(R.id.dailyProgressProgressEditText);
		final Spinner onTimeSpinner = (Spinner) view.findViewById(R.id.dailyProgressOnTimeSpinner);
		final EditText descriptionText = (EditText) view.findViewById(R.id.dailyProgressDescriptionEditText);

		thisReportItem.setLocationId(locationId);
		thisReportItem.setReportItem(activityText.getText().toString());
		try  {  
			thisReportItem.setProgress(Float.parseFloat(progressText.getText().toString()));
		} catch( Exception e ) {  			  
			thisReportItem.setProgress(0); 
		}
		thisReportItem.setOnTIme(onTimeSpinner.getSelectedItem().toString());
		thisReportItem.setDescription(descriptionText.getText().toString());
		return thisReportItem;
	}

	private int getIndex(Spinner spinner, String myString){

		int index = 0;

		for (int i=0;i<spinner.getCount();i++){
			if (spinner.getItemAtPosition(i).equals(myString)){
				index = i;
			}
		}
		return index;
	}

	public void populatePhotoGrid() {

		final ExpandableHeightGridView gridView = (ExpandableHeightGridView) view.findViewById(R.id.dailyProgressPhotosGrid);

		photosDatasource = new DataSourcePhotos(mContext);
		photosDatasource.open();

		List<SQLPhoto> images = photosDatasource.getReportItemPhotos(thisReportItem.getId());
		photosDatasource.close();
		AdapterImages adapter = new AdapterImages(mContext, images);

		gridView.setAdapter(adapter);
		gridView.setExpanded(true);
		registerForContextMenu(gridView);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				view.showContextMenu();
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v.getId()==R.id.dailyProgressPhotosGrid) {  	
			menu.add(Menu.NONE, v.getId(), 0, getResources().getString(R.string.contextView));   	
			menu.add(Menu.NONE, v.getId(), 0, getResources().getString(R.string.contextDelete));

			menu.getItem(0).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					ExpandableHeightGridView gridView = (ExpandableHeightGridView) view.findViewById(R.id.dailyProgressPhotosGrid);
					AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
					AdapterImages adapter = (AdapterImages) gridView.getAdapter();
					SQLPhoto photo = adapter.getSQLPhoto(info.position);
					File photoFile = new File(photo.getPhotoPath());
					if (photoFile.exists()) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.fromFile(photoFile), "image/*");
						startActivity(intent);
					} else {
						Toast toast = Toast.makeText(view.getContext(), mContext.getString(R.string.errPhotoFileDoesntExist), Toast.LENGTH_SHORT);
						toast.show();
						photosDatasource = new DataSourcePhotos(mContext);
						photosDatasource.open();
						photosDatasource.deletePhoto(photo);
						photosDatasource.close();
					}
					return true;
				}
			});
			menu.getItem(1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					ExpandableHeightGridView gridView = (ExpandableHeightGridView) view.findViewById(R.id.dailyProgressPhotosGrid);
					AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
					AdapterImages adapter = (AdapterImages) gridView.getAdapter();
					SQLPhoto photo = adapter.getSQLPhoto(info.position);
					photosDatasource = new DataSourcePhotos(mContext);
					photosDatasource.open();
					photosDatasource.deletePhoto(photo);
					photosDatasource.close();
					populatePhotoGrid();
					return true;
				}
			});
		}
	}

	@SuppressLint("SimpleDateFormat")
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

		switch(requestCode) { 
		case REQ_CODE_PICK_IMAGE:
			if(resultCode == Activity.RESULT_OK){  
				String [] proj={MediaStore.Images.Media.DATA};
				Uri contentUri = imageReturnedIntent.getData();
				Cursor cursor = getActivity().getContentResolver().query( contentUri,
						proj, // Which columns to return
						null,       // WHERE clause; which rows to return (all rows)
						null,       // WHERE clause selection arguments (none)
						null); // Order-by clause (ascending by name)
				int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();

				String imgFilePath = cursor.getString(column_index);
				File mediaStorageDir = FileManager.getPictureStorageLocation(mContext);
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				File importedImg = new File(imgFilePath);
				File destinationFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
				if (importedImg.exists()) {
                    FileChannel src;
                    FileChannel dst;
                    try {
                    	src = new FileInputStream(importedImg).getChannel();		
						dst = new FileOutputStream(destinationFile).getChannel();
						dst.transferFrom(src, 0, src.size());
						src.close();
						dst.close();
					} catch (FileNotFoundException e) {
						Mint.logEvent(e.toString());
					} catch (IOException e) {
						Mint.logEvent(e.toString());
					}
                }
				
				cursor.close();
				SQLPhoto newPhoto = new SQLPhoto();
				newPhoto.setPhoto(destinationFile.getAbsolutePath());
				newPhoto.setReportItemId(thisReportItem.getId());
				newPhoto.setLocationId(thisReportItem.getLocationId());
				photosDatasource = new DataSourcePhotos(mContext);
				photosDatasource.open();
				photosDatasource.createPhoto(newPhoto);
				photosDatasource.close();
				populatePhotoGrid();
			}
		case REQ_CODE_TAKE_IMAGE:
			if(resultCode == Activity.RESULT_OK){
				populatePhotoGrid();
			}
		}
	}

	public void onResume() {
		super.onResume();
		populatePhotoGrid();
	}
}