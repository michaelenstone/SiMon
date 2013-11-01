package uk.co.simon.app;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.bugsense.trace.BugSenseHandler;

public class ActivitySettings extends PreferenceActivity {
    @SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		BugSenseHandler.initAndStartSession(ActivitySettings.this, "6c6b0664");
        addPreferencesFromResource(R.xml.preferences);
    }
}
