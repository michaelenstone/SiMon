package uk.co.simon.app;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

@SuppressLint("ValidFragment")
public class DialogFragmentDate extends DialogFragment {
 
	public static String TAG = "DateDialogFragment";
	static int mYear;
	static int mMonth;
	static int mDay;
	private Fragment mFragment;
	private Calendar c;
	
	public DialogFragmentDate(Fragment callback, Calendar cal) {
		mFragment = callback;
		this.c = cal;
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH);
		mDay = c.get(Calendar.DAY_OF_MONTH);
		
		return new DatePickerDialog(getActivity(), (OnDateSetListener) mFragment, mYear, mMonth, mDay);
	}
}