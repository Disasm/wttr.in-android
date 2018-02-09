package in.wttr.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WeatherFetchService extends Service {
    public static final String EXTRA_URL = "in.wttr.widget.URL";
    public static final String EXTRA_BITMAP = "in.wttr.widget.BITMAP";
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private String url = null;

    public WeatherFetchService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
            appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                                             AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (intent.hasExtra(EXTRA_URL)) {
            Log.i("WeatherFetchService", "Got fetch intent");
            url = intent.getStringExtra(EXTRA_URL);
            new DownloadFileTask().execute(url);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private class DownloadFileTask extends AsyncTask<String, Integer, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                InputStream URLcontent = (InputStream) new URL(params[0]).getContent();
                return BitmapFactory.decodeStream(URLcontent);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                Log.i("DownloadFileTask", "Bitmap fetched");
            } else {
                Log.i("DownloadFileTask", "Error fetching bitmap");
            }
            Intent widgetUpdateIntent = new Intent();
            widgetUpdateIntent.setAction(WeatherWidget.DATA_FETCHED);
            widgetUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            widgetUpdateIntent.putExtra(EXTRA_BITMAP, bitmap);
            sendBroadcast(widgetUpdateIntent);
            stopSelf();
        }
    }
}
