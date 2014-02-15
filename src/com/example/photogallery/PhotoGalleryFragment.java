package com.example.photogallery;

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

public class PhotoGalleryFragment extends Fragment {
	GridView myGrid;
	ArrayList<GalleryItem> items;
	ThumbnailDownloader<ImageView> mThumbnailThread;
	
	private static final String TAG = "PhotogalleryFragment";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		updateItems();
		
		PollService.setServiceAlarm(getActivity(), true);
		
		mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
				if(isVisible()) {
					imageView.setImageBitmap(thumbnail);
				}
 			}

			@Override
			public void onThumbnailDownloader(ImageView token, Bitmap thumbnail) {
				if(isVisible()) {
					token.setImageBitmap(thumbnail);
				}
				
			}
		});
		mThumbnailThread.start();
		mThumbnailThread.getLooper();
		//log that background thread started
	}
	
	public void updateItems() {
		new FetchItemTask().execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
		myGrid = (GridView)v.findViewById(R.id.gridView);
		setAdapter();
		return v;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_photo_gallery, menu);
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			MenuItem searchItem = menu.findItem(R.id.menu_item_search);
			SearchView searchView = (SearchView)searchItem.getActionView();
			
			SearchManager searchManager = (SearchManager)getActivity()
					.getSystemService(Context.SEARCH_SERVICE);
			ComponentName name = getActivity().getComponentName();
			SearchableInfo searchInfo = searchManager.getSearchableInfo(name);
			
			searchView.setSearchableInfo(searchInfo);
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menu_item_search:
				getActivity().onSearchRequested();
		case R.id.menu_item_clear:
				PreferenceManager.getDefaultSharedPreferences(getActivity())
							.edit()
							.putString(FlickrFetchr.PREF_SEARCH_QUERY, null)
							.commit();
				updateItems();
				return true;
		default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		mThumbnailThread.quit();
		//log that the backthread was desrroyed
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mThumbnailThread.clearQueue();
	}
	
	private void setAdapter() {
		if(getActivity() == null || myGrid == null) return;
		
		if(items != null) {
			myGrid.setAdapter(new GalleryItemAdapter(items));
		} else {
			myGrid.setAdapter(null);
		}
	}


	private class FetchItemTask extends AsyncTask<Void, Void, ArrayList<GalleryItem>> {
		@SuppressWarnings("unused")
		@Override
		protected ArrayList<GalleryItem> doInBackground(Void... params) {
			
			Activity act = getActivity();
			if(act == null)
				return new ArrayList<GalleryItem>();
			
			String query = PreferenceManager.getDefaultSharedPreferences(act)
					.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
			
			try {
				if(query != null) {
					return new FlickrFetchr().search(query);
				} else {
					return new FlickrFetchr().fetchItems();
				}
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
				
		}
		
		@Override
		protected void onPostExecute(ArrayList<GalleryItem> items2) {
			items = items2;
			setAdapter();
		}
	}
	
	private class GalleryItemAdapter extends ArrayAdapter<GalleryItem> {
		public GalleryItemAdapter(ArrayList<GalleryItem> items) {
			super(getActivity(), 0 , items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null) {
				convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent, false);
			}
			
			ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_imageView);
			imageView.setImageResource(R.drawable.brian_up_close);
			GalleryItem item = getItem(position);
			mThumbnailThread.queueThumbnail(imageView, item.getUrl());
			return convertView;
		}
	}
	
}
















