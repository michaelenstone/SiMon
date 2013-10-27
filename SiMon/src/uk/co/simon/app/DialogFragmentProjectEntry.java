package uk.co.simon.app;

import uk.co.simon.app.sqllite.DataSourceProjects;
import uk.co.simon.app.sqllite.SQLProject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class DialogFragmentProjectEntry extends DialogFragment {

	private DataSourceProjects datasource = null;
	onDialogResultListener mListener;
	private View view;
	SQLProject thisProject = new SQLProject();

	public interface onDialogResultListener {
        public void onProjectEntryPositiveClick(DialogFragment dialog);
	}
    
	public DialogFragmentProjectEntry() {
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
	
    public Dialog onCreateDialog(Bundle savedInstanceState) {

		Context mContext = getActivity();
    	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		LayoutInflater inflater = getActivity().getLayoutInflater();
		view = inflater.inflate(R.layout.dialog_fragment_project_entry, null);
        
		datasource = new DataSourceProjects(mContext);
        datasource.open();
        
    	if (getArguments().getInt("dialogType")==1) {
    		
    		builder.setTitle(R.string.updateProjectEntryDialogTitle);
            
            String oldProjectName = getArguments().getString("projectName");
            String oldProjectNumber = getArguments().getString("projectNumber");
            final Long projectId = datasource.getProjectId(oldProjectName, oldProjectNumber);
            thisProject  = datasource.getProject(projectId);

            EditText projectName = (EditText) view.findViewById(R.id.projectEntryProjectNameEditText);
        	EditText projectNumber = (EditText) view.findViewById(R.id.projectEntryProjectNumberEditText);
            projectName.setText(thisProject.getProject());
            projectNumber.setText(thisProject.getProjectNumber());
            
            builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int which) {
                    //Get New Project Name
                	EditText projectName = (EditText) view.findViewById(R.id.projectEntryProjectNameEditText);
                	thisProject.setProject(projectName.getText().toString());
                    //Get New Project Number
                	EditText projectNumber = (EditText) view.findViewById(R.id.projectEntryProjectNumberEditText);
                	String newProjectNumber = projectNumber.getText().toString(); 
                	if (newProjectNumber == null) {
            			newProjectNumber = " ";
            		}
                	thisProject.setProjectNumber(newProjectNumber);
                	// Save the new project to the database
                	datasource.updateProject(thisProject);
                	datasource.close();
    				mListener.onProjectEntryPositiveClick(DialogFragmentProjectEntry.this);
                    dismiss();
                }
            });
    	} else {
    		builder.setTitle(R.string.newProjectEntryDialogTitle);
    		
    		builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int which) {
                    //Get New Project Name
                	EditText projectName = (EditText) view.findViewById(R.id.projectEntryProjectNameEditText);
                	thisProject.setProject(projectName.getText().toString());
                    //Get New Project Number
                	EditText projectNumber = (EditText) view.findViewById(R.id.projectEntryProjectNumberEditText);
                	String newProjectNumber = projectNumber.getText().toString(); 
                	if (newProjectNumber == null) {
            			newProjectNumber = " ";
            		}
                	thisProject.setProjectNumber(newProjectNumber);
                	datasource.createProject(thisProject);
                	datasource.close();
    				mListener.onProjectEntryPositiveClick(DialogFragmentProjectEntry.this);
                    dismiss();
                }
            });
    	}
    	
    	builder.setView(view);
		builder.setCancelable(false);
		Dialog built = builder.create();
		return built;
    }
    
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
