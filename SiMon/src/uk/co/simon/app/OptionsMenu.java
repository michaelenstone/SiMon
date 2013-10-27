package uk.co.simon.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class OptionsMenu {
	
	private MenuItem item;
	private Context context;
	private Activity activity;
	
	public OptionsMenu (MenuItem item, Activity activity) {
		this.item = item;
		this.activity = activity;
		this.context = activity.getBaseContext();
	}

	public boolean menuSelect() {
		switch (item.getItemId()) {
		case R.id.menuHome:
			if (!activity.getClass().getSimpleName().contains("ActivityHome")) {
				Intent home = new Intent(activity, ActivityHome.class);
				activity.startActivity(home);
			}
			return true;
		case R.id.menuSettings:
			Intent openSettings = new Intent(activity, ActivitySettings.class);
			activity.startActivity(openSettings);
			return true;
		case R.id.menuLogout:
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString("PasswordPref", null);
			editor.commit();
			Intent login = new Intent(activity, ActivityLogin.class);
			activity.startActivity(login);
			return true;
		}
		return false;
	}
}
