package com.example.photogallery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class ThumbnailDownloader<Token> extends HandlerThread {
	
	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD = 0;
	
	Handler mHandler;
	Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
	Handler mResponseHandler;
	Listener<Token> mListener;
	
	public interface Listener<Token> {
		void onThumbnailDownloader(Token token, Bitmap thumbnail);
	}
	
	public void setListener(Listener<Token> listener) {
		mListener = listener;
	}
	
	
	public ThumbnailDownloader(Handler respoHandler) {
		super(TAG);
		mResponseHandler = respoHandler;
	}
	
	@SuppressLint("Handlerleak")
	@Override
	protected void onLooperPrepared() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if(msg.what == MESSAGE_DOWNLOAD) {
					@SuppressWarnings("unchecked")
					Token token = (Token)msg.obj;
					//log that u got a request frim osmething
					handleRequest(token);
				}
			}
		};
	}
	
	
	
	public void queueThumbnail(Token token, String url) {
		//log that u got a new Url
		requestMap.put(token, url);
		
		mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
	}
	
	private void handleRequest(final Token token) {
		try {
			final String url = requestMap.get(token);
			if(url == null)
				return;
			
			byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
			final Bitmap bitmap = BitmapFactory
					.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
			//log that bitmap was just created
			mResponseHandler.post(new Runnable() {

				@Override
				public void run() {
					if(requestMap.get(token) != url) {
						return;
					}
					requestMap.remove(token);
					mListener.onThumbnailDownloader(token, bitmap);
				}
				
			});
			
			
			
		} catch (IOException e) {
			//log error dumbo
		}
	}
	
	public void clearQueue() {
		mHandler.removeMessages(MESSAGE_DOWNLOAD);
		requestMap.clear();
	}
	
	
}













