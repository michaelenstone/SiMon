package uk.co.simon.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import uk.co.simon.app.adapters.AdapterReportItems;
import uk.co.simon.app.sqllite.DataSourcePhotos;
import uk.co.simon.app.sqllite.DataSourceReportItems;
import uk.co.simon.app.sqllite.DataSourceReports;
import uk.co.simon.app.sqllite.SQLReport;
import uk.co.simon.app.sqllite.SQLReportItem;
import uk.co.simon.app.ui.customElements.ExpandableHeightListView;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class ActivityReport extends FragmentActivity implements uk.co.simon.app.DialogFragmentReportItem.onDialogResultListener, 
	uk.co.simon.app.FragmentProgressReportHeader.onSpinnerSelect, uk.co.simon.app.DialogFragmentProjectEntry.onDialogResultListener {

	DialogFragmentDate dateFragment;
	Calendar now = Calendar.getInstance();

	private DataSourceReportItems datasource;
	private DataSourceReports reportsDatasource;
	private DataSourcePhotos photosDatasource;
	private SQLReport thisReport;
    FragmentProgressReportHeader header = new FragmentProgressReportHeader();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        setContentView(R.layout.activity_report);
        
        reportsDatasource = new DataSourceReports(this);
        reportsDatasource.open();
        
        Bundle extras = getIntent().getExtras();
	    Bundle args = new Bundle();
        
        ExpandableHeightListView reportItemsList = (ExpandableHeightListView)findViewById(R.id.progressReportItemsList);
        reportItemsList.setVisibility(View.GONE);
        
        if (getIntent().hasExtra("reportId")) {
        	thisReport = reportsDatasource.getReport(extras.getLong("reportId"));
            args.putBoolean("isNew", false);
    		showListView();
        } else {
            //create empty report
            thisReport = new SQLReport();
            thisReport.setReportType(extras.getBoolean("reportType"));
        	thisReport = reportsDatasource.createReport(thisReport);
            args.putBoolean("isNew", true);      
        }
	    reportsDatasource.close();

	    args.putLong("reportId", thisReport.getId());
	    args.putBoolean("reportType", thisReport.getReportType());
	    
	    if (thisReport.getReportType()){
	    	setTitle(R.string.title_activity_site_visit_report);
	    } else {
	    	setTitle(R.string.title_activity_progress_report);	 		
	    }      
	    
        FragmentManager fm = getSupportFragmentManager();
        header.setArguments(args);
        fm.beginTransaction().add(R.id.reportHeader, header).commit();

    }
    
    public void showListView() {

    	ExpandableHeightListView ReportItemsList = (ExpandableHeightListView) PopulateList(thisReport.getId());
        registerForContextMenu(ReportItemsList);
        ReportItemsList.setVisibility(View.VISIBLE);
        
        ReportItemsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
        	    int position, long id) {
        		    if (position==0) {
          		        //Prepare information for passing to dialog
          		    	Bundle args = new Bundle();
          		    	args.putInt("dialogType", 0);
          			    args.putBoolean("reportType", thisReport.getReportType());
          		    	args.putLong("projectId", thisReport.getProjectId());
          		    	args.putLong("reportId", thisReport.getId());
        				Toast toast = Toast.makeText(getBaseContext(), "Project id: " + thisReport.getProjectId(), Toast.LENGTH_SHORT);
        				toast.show();
          		    	FragmentManager fm = getSupportFragmentManager();
          		    	DialogFragmentReportItem dialogFragmentProgressReportItem = new DialogFragmentReportItem();
          		    	dialogFragmentProgressReportItem.setArguments(args);
          		    	dialogFragmentProgressReportItem.show(fm, "dialog_fragment_progress_report_item");
          		    } else {
          			    ListView reportItemsList = (ListView)findViewById(R.id.progressReportItemsList);
          				AdapterReportItems adapter = (AdapterReportItems) reportItemsList.getAdapter();
          				SQLReportItem reportItem = adapter.getItem(position);
        				Toast toast = Toast.makeText(getBaseContext(), "Project id: " + thisReport.getProjectId(), Toast.LENGTH_SHORT);
        				toast.show();
          		    	Bundle args = new Bundle();
          		    	args.putInt("dialogType", 1);
          			    args.putBoolean("reportType", thisReport.getReportType());
          		    	args.putLong("projectId", thisReport.getProjectId());
          		    	args.putLong("reportId", thisReport.getId());
          		    	args.putLong("reportItemId", reportItem.getId());
          		    	FragmentManager fm = getSupportFragmentManager();
          		    	DialogFragmentReportItem dialogFragmentProgressReportItem = new DialogFragmentReportItem();
          		    	dialogFragmentProgressReportItem.setArguments(args);
          		    	dialogFragmentProgressReportItem.show(fm, "dialog_fragment_progress_report_item");          		    	
          		    }
        	  }
        	});
    }
    
	private ListView PopulateList(long reportId) {
        
		SQLReportItem firstRow = new SQLReportItem();
        firstRow.setReportItem(getString(R.string.dailyProgressAddItem));
        firstRow.setLocationId(-1);	

    	List<SQLReportItem> values = new ArrayList<SQLReportItem>();
        values.add(firstRow);
            
    	datasource = new DataSourceReportItems(this);
        datasource.open();
                   
        values.addAll(datasource.getReportItems(reportId));
        ExpandableHeightListView ReportItemsList = (ExpandableHeightListView)findViewById(R.id.progressReportItemsList);
        
        AdapterReportItems adapter = new AdapterReportItems(this, values);
        ReportItemsList.setAdapter(adapter);
        ReportItemsList.setExpanded(true);
        
        datasource.close();
        
        return ReportItemsList;
	}
	
	public void onSpinnerSelected(long data) {
		thisReport.setProjectId(data);
		showListView();
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    }

	@Override
    protected void onPause() {
        super.onPause();
    }
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	if (v.getId()==R.id.progressReportItemsList) {
    		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
    		if (info.position>0) {
    			ListView ReportsList = (ListView)findViewById(R.id.progressReportItemsList);
    			AdapterReportItems adapter = (AdapterReportItems) ReportsList.getAdapter();
    			SQLReportItem reportItem = adapter.getItem(info.position);
    			menu.setHeaderTitle(reportItem.getReportItem());
    			String[] menuItems = new String[] 
    					{getResources().getString(R.string.contextEdit), 
    					getResources().getString(R.string.contextDelete)};
    			for (int i = 0; i<menuItems.length; i++) {
    				menu.add(Menu.NONE, i, i, menuItems[i]);
    			}
    		}
    	}
    }

	@Override
    public boolean onContextItemSelected(MenuItem item) {
    	ListView ReportsList = (ListView)findViewById(R.id.progressReportItemsList);
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		AdapterReportItems adapter = (AdapterReportItems) ReportsList.getAdapter();
		SQLReportItem reportItem = adapter.getItem(info.position);
    	switch (item.getItemId()) {
    	case 0:
		    	Bundle args = new Bundle();
		    	args.putInt("dialogType", 1);
		    	args.putLong("projectId", thisReport.getProjectId());
		    	args.putLong("reportId", thisReport.getId());
		    	args.putLong("reportItemId", reportItem.getId());
		    	FragmentManager fm = getSupportFragmentManager();
		    	DialogFragmentReportItem dialogFragmentProgressReportItem = new DialogFragmentReportItem();
		    	dialogFragmentProgressReportItem.setArguments(args);
		    	dialogFragmentProgressReportItem.show(fm, "dialog_fragment_progress_report_item"); 
    		break;
    	case 1:
    	    datasource = new DataSourceReportItems(getBaseContext());
      	    datasource.open();
      	    datasource.deleteReportItem(reportItem);
      	    datasource.close();
      	    photosDatasource = new DataSourcePhotos(getBaseContext());
      	    photosDatasource.open();
      	    photosDatasource.deleteReportItemPhotos(reportItem.getId());
      	    photosDatasource.close();
    		showListView();
    		break;
    	}
        adapter.notifyDataSetChanged();
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
	public void onDialogPositiveClick(DialogFragment dialog) {
		PopulateList(thisReport.getId());		
	}

	@Override
	public void onProjectEntryPositiveClick(DialogFragment dialog) {
		// TODO Auto-generated method stub
		
	}


	}
