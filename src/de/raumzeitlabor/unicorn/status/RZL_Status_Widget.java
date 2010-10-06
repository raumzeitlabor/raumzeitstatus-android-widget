package de.raumzeitlabor.unicorn.status;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.raumzeitlabor.unicorn.status.R;

/**
 * Define a simple widget that shows the Wiktionary "Word of the day." To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class RZL_Status_Widget extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// Log.d("widgetdreck", "OnUpdate");
		// To prevent any ANR timeouts, we perform the update in a service
		context.startService(new Intent(context, UpdateService.class));
	}

	public static class UpdateService extends Service {
		@Override
		public void onStart(Intent intent, int startId) {
			// Build the widget update for today
//			Log.d("widgetdreck", "OnStart");

			RemoteViews updateViews = buildUpdate(this);

			// Push update for this widget to the home screen
			ComponentName thisWidget = new ComponentName(this,
					RZL_Status_Widget.class);
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			manager.updateAppWidget(thisWidget, updateViews);

		}

		/**
		 * Build a widget update to show the current Wiktionary
		 * "Word of the day." Will block until the online API returns.
		 */
		public static RemoteViews buildUpdate(Context context) {
			// Log.d("widgetdreck", "buildUpdate");
			RemoteViews updateViews = null;
			String pageContent = "";

			pageContent = fetcher();
			int imgSrc;
			if (pageContent.startsWith("0"))
				imgSrc = 0x7f020003;

			else if (pageContent.startsWith("1"))
				imgSrc = 0x7f020001;

			else if (pageContent.startsWith("?"))
				imgSrc = 0x7f020002;
			else {
				imgSrc = 0x7f020000;
			}

			// Build an update that holds the updated widget contents
			updateViews = new RemoteViews(context.getPackageName(),
					R.layout.widget_word);
			updateViews.setImageViewResource(R.id.icon, imgSrc);

			// When user clicks on widget
		//Intent defineIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://status.raumzeitlabor.de/mobile.php"));
		//PendingIntent pendingIntent = PendingIntent.getActivity(context,0 /* no requestCode */, defineIntent, 0 /* no flags */);
					
            Intent defineIntent = new Intent(context, UpdateService.class);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, defineIntent, 0);

		
			updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);
			return updateViews;
		}

		@Override
		public IBinder onBind(Intent intent) {
			// We don't need to bind to this service
			return null;
		}
	}

	public static String fetcher() {
		// Log.d("widgetdreck", "Fetcher");
		URL myConnection = null;
		String read = "0";
		try {
			myConnection = new URL(
					"http://status.raumzeitlabor.de/api/simple");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			return "?";
		}
		URLConnection connectMe = null;
		try {
			connectMe = myConnection.openConnection();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			return "?";
		}

		InputStreamReader lineReader = null;
		try {
			lineReader = new InputStreamReader(connectMe.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			return "?";
		}
		BufferedReader buffer = new BufferedReader(lineReader);

		try {
			read = buffer.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "?";
		}
		return read;
	}
}
