package uk.co.simon.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import uk.co.simon.app.sqllite.DataSourceProjects;
import uk.co.simon.app.sqllite.DataSourceReports;
import uk.co.simon.app.sqllite.SQLProject;
import uk.co.simon.app.sqllite.SQLReport;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class FragmentProgressReportHeader extends Fragment {

	DialogFragmentDate dateFragment;
	Calendar now = Calendar.getInstance();

	public static String TAG = "FragmentProgressReportHeader";
	private DataSourceProjects datasource;
	private DataSourceReports reportsDatasource;
	onSpinnerSelect spinnerSelect;
	private boolean isNew;
	private long reportId;
	private String reportTypeRef;
	SQLReport thisReport = new SQLReport();
	Spinner projectsSpinner;
	int noProjects;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_progress_report_header, container, false);
		return view;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		reportsDatasource = new DataSourceReports(getActivity());
		reportsDatasource.open();

		isNew = getArguments().getBoolean("isNew");
		reportId = getArguments().getLong("reportId");

		thisReport = reportsDatasource.getReport(reportId);

		reportsDatasource.close();

		projectsSpinner = (Spinner) getView().findViewById(R.id.dailyProgressProjectSpinner); 
		final EditText refText = (EditText) getView().findViewById(R.id.dailyProgressReportRefEditText); 
		final Button dateButton = (Button) getView().findViewById(R.id.dailyProgressDateButton);

		updateSpinner(projectsSpinner);

		if (thisReport.getReportType()) {
			reportTypeRef = this.getString(R.string.dailyProgressSVRRef);
		} else {
			reportTypeRef = this.getString(R.string.dailyProgressPRRef);
			LinearLayout weather = (LinearLayout) getView().findViewById(R.id.dailyProgressWeatherLayout);
			weather.setVisibility(View.GONE);
		}

		final EditText supervisorText = (EditText) getView().findViewById(R.id.dailyProgressSupervisor);

		if (!isNew) {
			final EditText weatherText = (EditText) getView().findViewById(R.id.dailyProgressWeatherEditText);
			final EditText tempText = (EditText) getView().findViewById(R.id.dailyProgressTempEditText);
			final Spinner tempTypeSpinner = (Spinner) getView().findViewById(R.id.dailyProgressTempSpinner);

			dateButton.setText(thisReport.getReportDate());
			supervisorText.setText(thisReport.getSupervisor());
			weatherText.setText(thisReport.getWeather());
			tempText.setText(thisReport.getTemp());
			refText.setText(thisReport.getReportRef());

			tempTypeSpinner.setSelection((int) thisReport.getTempType());
		} else {
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
			String name = sharedPref.getString("NamePref", "xyd324");
			if (name!="xyd324") {
				thisReport.setSupervisor(name);
				supervisorText.setText(thisReport.getSupervisor());
			}
		}

		projectsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parentView, View view, int position, long id) {
				if (position!=0) {
					SQLProject spinnerproject = (SQLProject) projectsSpinner.getAdapter().getItem(position);
					datasource.open();
					SQLProject project = datasource.getProject(spinnerproject.getId());
					String projectNumber = project.getProjectNumber();
					thisReport.setProjectId(project.getId());
					String ref = projectNumber + reportTypeRef;
					//get reports for particular project
					reportsDatasource = new DataSourceReports(getActivity());
					reportsDatasource.open();
					List<SQLReport> reports = new ArrayList<SQLReport>();
					reports = reportsDatasource.getAllProjectReports(spinnerproject.getId());
					int i;
					int thisRefNo = 1;
					if (reports != null) {
						if (thisReport.getReportType()) {	
							for (i=0; i<reports.size(); i++) {
								if (reports.get(i).getReportType()){
									thisRefNo++;
								}
							}
						} else {
							for (i=0; i<reports.size(); i++) {
								if (!reports.get(i).getReportType()){
									thisRefNo++;
								}
							}
						}
					}
					ref = ref + Integer.toString(thisRefNo);
					refText.setText(ref);
					thisReport.setReportRef(ref);
					datasource.close();
					reportsDatasource.close();
					spinnerSelect.onSpinnerSelected(thisReport.getProjectId());
				}
			}
			public void onNothingSelected(AdapterView<?> parentView) {

			}
		});

		dateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				FragmentTransaction ft = getFragmentManager().beginTransaction(); //get the fragment
				dateFragment = DialogFragmentDate.newInstance(getActivity(), new DialogFragmentDateListener(){
					public void updateChangedDate(int year, int month, int day){
						final Button dateButton = (Button) getView().findViewById(R.id.dailyProgressDateButton);
						dateButton.setText(String.valueOf(day)+"/"+String.valueOf(month+1)+"/"+String.valueOf(year));
						now.set(year, month, day);
						thisReport.setReportDate(String.valueOf(day)+"/"+String.valueOf(month+1)+"/"+String.valueOf(year));
					}
				}, now);

				dateFragment.show(ft, "DateDialogFragment");
			}
		});

	}

	public interface DialogFragmentDateListener{
		//this interface is a listener between the Date Dialog fragment and the activity to update the buttons date
		public void updateChangedDate(int year, int month, int day);
	}

	public void updateSpinner(Spinner projectsSpinner) {

		datasource = new DataSourceProjects(getActivity());
		datasource.open();

		List<SQLProject> projects = new ArrayList<SQLProject>();
		projects = datasource.getAllProjects();

		List<SQLProject> values = new ArrayList<SQLProject>();
		SQLProject firstRow = new SQLProject();
		if (isNew) {
			firstRow.setId(-1);
			firstRow.setProject(" ");
		} else {      	
			SQLProject project = datasource.getProject(thisReport.getProjectId());
			firstRow = project;
		}
		values.add(firstRow);
		values.addAll(projects);
		SpinnerAdapter adapter = new ArrayAdapter<SQLProject> (getActivity(), R.layout.spinner_row, values);
		projectsSpinner.setAdapter(adapter);

		datasource.close();
	}

	public void packageReport() {

		final EditText supervisorText = (EditText) getView().findViewById(R.id.dailyProgressSupervisor);
		final EditText weatherText = (EditText) getView().findViewById(R.id.dailyProgressWeatherEditText);
		final EditText tempText = (EditText) getView().findViewById(R.id.dailyProgressTempEditText);
		final Spinner tempTypeSpinner = (Spinner) getView().findViewById(R.id.dailyProgressTempSpinner);
		final Spinner projectsSpinner = (Spinner) getView().findViewById(R.id.dailyProgressProjectSpinner);
		final EditText refText = (EditText) getView().findViewById(R.id.dailyProgressReportRefEditText); 

		SpinnerAdapter adapter = projectsSpinner.getAdapter();
		SQLProject project = (SQLProject) adapter.getItem(projectsSpinner.getSelectedItemPosition());

		thisReport.setProjectId(project.getId());
		thisReport.setSupervisor(supervisorText.getText().toString());
		try  {  
			thisReport.setTemp(tempText.getText().toString());
		} catch( Exception e ) {  			  
			thisReport.setTemp("0"); 
		} 
		thisReport.setTempType(tempTypeSpinner.getSelectedItemPosition());
		thisReport.setWeather(weatherText.getText().toString());
		thisReport.setReportRef(refText.getText().toString());
	}

	public interface onSpinnerSelect {
		public void onSpinnerSelected(long data);
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		spinnerSelect = (onSpinnerSelect) a;
	}

	public void onPause(){
		super.onPause();
		packageReport();
		reportsDatasource = new DataSourceReports(getActivity());
		reportsDatasource.open();
		reportsDatasource.updateReport(thisReport);
		reportsDatasource.close();
	}

	public void onDetach(){
		super.onDetach();
		if (isNew && projectsSpinner.getSelectedItemPosition()<=1) {
			reportsDatasource = new DataSourceReports(getActivity());
			reportsDatasource.open();
			reportsDatasource.deleteReport(thisReport);
			reportsDatasource.close();
		} else {
			reportsDatasource = new DataSourceReports(getActivity());
			reportsDatasource.open();
			reportsDatasource.updateReport(thisReport);
			reportsDatasource.close();			
		}
	}

}
