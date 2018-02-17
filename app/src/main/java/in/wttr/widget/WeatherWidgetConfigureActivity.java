package in.wttr.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

/**
 * The configuration screen for the {@link WeatherWidget WeatherWidget} AppWidget.
 */
public class WeatherWidgetConfigureActivity extends Activity {

    private static final String PREFS_NAME = "in.wttr.widget.WeatherWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = WeatherWidgetConfigureActivity.this;

            EditText locationName = (EditText) findViewById(R.id.settings_location);
            String locationPart = locationName.getText().toString();
            if (locationPart.isEmpty()) return;
            locationPart = locationPart.replace(" ", "%20");
            locationPart = locationPart.replace("/", "%2F");

            Switch todaySwitch = (Switch) findViewById(R.id.settings_today);
            Switch msSwitch = (Switch) findViewById(R.id.settings_use_ms);
            Switch captionSwitch = (Switch) findViewById(R.id.settings_caption);
            Switch paddingSwitch = (Switch) findViewById(R.id.settings_padding);

            String todayPart = todaySwitch.isChecked()?"0":"";
            String msPart = msSwitch.isChecked()?"M":"";
            String captionPart = captionSwitch.isChecked()?"":"q";
            String paddingPart = paddingSwitch.isChecked()?"p":"";
            String settingsPart = msPart + captionPart + paddingPart + todayPart;
            if (!settingsPart.isEmpty()) settingsPart = "_" + settingsPart;

            String url = "http://wttr.in/" + locationPart + settingsPart + ".png";

            // When the button is clicked, store the string locally
            saveURLPref(context, mAppWidgetId, url);

            // It is the responsibility of the configuration activity to update the app widget
            WeatherWidget.startAppWidgetUpdate(context, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    public WeatherWidgetConfigureActivity() {
        super();
    }

    // Write the prefix to the SharedPreferences object for this widget
    static void saveURLPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadURLPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteURLPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.weather_widget_configure);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
    }
}

