package uk.co.simon.app;

import java.util.ArrayList;
import java.util.List;

import com.bugsense.trace.BugSenseHandler;

import uk.co.simon.app.adapters.AdapterProjects;
import uk.co.simon.app.sqllite.DataSourceProjects;
import uk.co.simon.app.sqllite.DataSourceReports;
import uk.co.simon.app.sqllite.SQLProject;
import uk.co.simon.app.sqllite.SQLReport;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityProjects extends FragmentActivity implements uk.co.simon.app.DialogFragmentProjectEntry.onDialogResultListener {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		BugSenseHandler.initAndStartSession(ActivityProjects.this, "6c6b0664");
        setContentView(R.layout.activity_projects);
        setTitle(R.string.title_activity_projects);
        
        ListView ProjectsList = (ListView) PopulateList();
        registerForContextMenu(ProjectsList);
        
        ProjectsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        	  public void onItemClick(AdapterView<?> parent, View view,
        	    int position, long id) {
        		view.showContextMenu();
        	  }
        	});
    }
    
    public void onClickProjects(View view) {
    	switch (view.getId()) {
        case R.id.newProjectButton:
		        //Prepare information for passing to dialog
		    	Bundle args = new Bundle();
		    	args.putInt("dialogType", 0);
		    	FragmentManager fm = getSupportFragmentManager();
		        DialogFragmentProjectEntry dialogFragmentProjectEntry = new DialogFragmentProjectEntry();
		        dialogFragmentProjectEntry.setArguments(args);
		        dialogFragmentProjectEntry.show(fm, "dialog_fragment_project_entry");
	        	break;
        }
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    	ContextMenuInfo menuInfo) {
    	if (v.getId()==R.id.projectsList) {
    		ListView ProjectsList = (ListView)findViewById(R.id.projectsList);
    		AdapterView.AdapterContextMenuInfo info = 
    				(AdapterView.AdapterContextMenuInfo)menuInfo;
    		AdapterProjects adapter = (AdapterProjects) ProjectsList.getAdapter();
    		SQLProject project = adapter.getItem(info.position);
    		menu.setHeaderTitle(project.getProject() + " - " + project.getProjectNumber());
    		String[] menuItems = new String[] 
    				{getResources().getString(R.string.contextEdit), 
    				getResources().getString(R.string.contextDelete)};
    		for (int i = 0; i<menuItems.length; i++) {
    			menu.add(Menu.NONE, i, i, menuItems[i]);
    		}
    	}
    }

 	@Override
    public boolean onContextItemSelected(MenuItem item) {
    	ListView ProjectsList = (ListView)findViewById(R.id.projectsList);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		AdapterProjects adapter = (AdapterProjects) ProjectsList.getAdapter();
		SQLProject project = adapter.getItem(info.position);
    	switch (item.getItemId()) {
    	case 0:
    		//Prepare information for passing to dialog
	    	Bundle args = new Bundle();
	    	args.putInt("dialogType", 1);
	    	args.putString("projectName", project.getProject());
	    	args.putString("projectNumber", project.getProjectNumber());
	    	FragmentManager fm = getSupportFragmentManager();
	        DialogFragmentProjectEntry dialogFragmentProjectEntry = new DialogFragmentProjectEntry();
	        dialogFragmentProjectEntry.setArguments(args);
	        dialogFragmentProjectEntry.show(fm, "dialog_fragment_project_entry");
    		break;
    	case 1:
    		DataSourceProjects datasource = new DataSourceProjects(this);
    		datasource.open();
    		List<SQLReport> reports = null;
    		DataSourceReports datasourceReports = new DataSourceReports(this);
    		datasourceReports.open();
    		reports = datasourceReports.getAllProjectReports(project.getId());
    		datasourceReports.close();
    		if (reports==null){
    			datasource.deleteProject(project);
    			ProjectsList = PopulateList();
    		} else {
    			Toast toast = Toast.makeText(this, "Cannot Delete Project.  Please Delete All Reports Associated With this Project First", Toast.LENGTH_LONG);
    			toast.show();
    		}
    		datasource.close();
    		break;
    	}
    	PopulateList();
    	return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.general_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		OptionsMenu options = new OptionsMenu(item,this);
		options.menuSelect();
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }

	@Override
    protected void onPause() {
        super.onPause();
    }
	
	private ListView PopulateList() {
        
		DataSourceProjects datasource = new DataSourceProjects(this);
		List<SQLProject> values = new ArrayList<SQLProject>();
		
        datasource.open();
        
        values = datasource.getAllProjects();
        datasource.close();
        ListView ProjectsList = (ListView)findViewById(R.id.projectsList);
        
        AdapterProjects adapter = new AdapterProjects(this, values);
        ProjectsList.setAdapter(adapter);
        
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    	int limit = sharedPref.getInt("ProjectLimit", 5);
    	if (adapter.getCount()<limit) {
    		Button newProjectButton = (Button) findViewById(R.id.newProjectButton);
    		newProjectButton.setVisibility(View.VISIBLE);
    	} else {
    		Button newProjectButton = (Button) findViewById(R.id.newProjectButton);
    		newProjectButton.setVisibility(View.INVISIBLE);
    	}
        
        return ProjectsList;
	}

	@Override
	public void onProjectEntryPositiveClick(DialogFragment dialog) {
		PopulateList();
	}
}
