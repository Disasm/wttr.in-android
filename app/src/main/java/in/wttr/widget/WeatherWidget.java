package in.wttr.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link WeatherWidgetConfigureActivity WeatherWidgetConfigureActivity}
 */
public class WeatherWidget extends AppWidgetProvider {
    public static final String DATA_FETCHED = "in.wttr.widget.DATA_FETCHED";

    static Intent constructUpdateIntent(Context context, int appWidgetId) {
        CharSequence url = WeatherWidgetConfigureActivity.loadURLPref(context, appWidgetId);

        Intent serviceIntent = new Intent(context, WeatherFetchService.class);
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        serviceIntent.putExtra(WeatherFetchService.EXTRA_URL, url.toString());
        return serviceIntent;
    }

    static void startAppWidgetUpdate(Context context, int appWidgetId) {
        Log.i("WeatherWidget", "startAppWidgetUpdate() called");
        context.startService(constructUpdateIntent(context, appWidgetId));
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bitmap bmp) {
        Log.i("WeatherWidget", "updateAppWidget() called");
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.weather_widget);
        //views.setTextViewText(R.id.appwidget_text, widgetText);
        Log.i("WeatherWidget", "updateAppWidget(): bitmap: " + ((bmp==null)?"null":"not null"));
        if (bmp == null) {
            bmp = constructErrorBitmap(context);
        } else {
            Bitmap newBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth() * 4, bmp.getHeight() * 4, true);
            bmp = newBitmap;
        }
        views.setImageViewBitmap(R.id.weather_image, bmp);

        Intent intent = constructUpdateIntent(context, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0 , intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.weather_image, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static Bitmap constructErrorBitmap(Context context) {
        Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);

        String text = "No data";

        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(40 * context.getResources().getDisplayMetrics().density);
        textPaint.setColor(0xFFcccccc);

        int width = (int) textPaint.measureText(text);
        StaticLayout staticLayout = new StaticLayout(text, textPaint, (int) width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
        canvas.save();
        canvas.translate((canvas.getWidth() - width) / 2, canvas.getHeight() / 2);
        staticLayout.draw(canvas);
        canvas.restore();

        //paint.setTextSize(40);
        //paint.setColor(Color.LTGRAY);
        //canvas.drawText("No data", 0, 0, paint);

        return bitmap;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("WeatherWidget", "onUpdate() called");
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            startAppWidgetUpdate(context, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(DATA_FETCHED)) {
            Log.i("WeatherWidget", "Got DATA_FETCHED intent");
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            Bitmap bmp = intent.getParcelableExtra(WeatherFetchService.EXTRA_BITMAP);
            updateAppWidget(context, appWidgetManager, appWidgetId, bmp);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            WeatherWidgetConfigureActivity.deleteURLPref(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

