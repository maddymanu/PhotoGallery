package com.example.photogallery;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;

public class PollService extends IntentService {
	
	private static final String TAG = "PollService";
	
	private static final int POLL_INTERVAL = 1000*15;
	
	public PollService() {
		super(TAG);
	}
	

	@Override
	protected void onHandleIntent(Intent intent) {
		ConnectivityManager cm = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		
		@SuppressWarnings("deprecation")
		boolean isNetworkAvail = cm.getBackgroundDataSetting() &&
					cm.getActiveNetworkInfo() != null;
		if(!isNetworkAvail)
			return;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String query = prefs.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
		String lastResultId = prefs.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);
		
		ArrayList<GalleryItem> items = null;
		if(query != null) {
			try {
				items = new FlickrFetchr().search(query);
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			try {
				items = new FlickrFetchr().fetchItems();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(items.size() == 0)
			return;
		
		String resultId = items.get(0).getId();
		
		if(!resultId.equals(resultId)) {
			//this is a NEW id
		}
		
		
		prefs.edit()
			.putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId)
			.commit();
		

	}
	
	public static void setServiceAlarm(Context context, boolean isOn) {
		Intent i = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		if(isOn) {
			alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
		
	}
	
	public static boolean isServiceAlarmOn(Context context) {
		Intent i = new Intent(context, PollService.class);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
		return pi != null;
	}

}













