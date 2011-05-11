package com.embeddedmicro.branch;

import android.content.SharedPreferences;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Android Live Wallpaper Archetype
 * 
 * @author Justin Rajewski
 */
public class BranchWallpaper extends WallpaperService {

	public static final String PREFERENCES = "com.embeddedmicro.branch";

	@Override
	public Engine onCreateEngine() {
		return new BranchEngine();
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class BranchEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		private LiveWallpaperPainting painting;
		private SharedPreferences prefs;

		BranchEngine() {
			SurfaceHolder holder = getSurfaceHolder();
			painting = new LiveWallpaperPainting(holder,
					getApplicationContext());
			prefs = BranchWallpaper.this.getSharedPreferences(PREFERENCES, 0);
			prefs.registerOnSharedPreferenceChangeListener(this);
			onSharedPreferenceChanged(prefs, null);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
			// setTouchEventsEnabled(false);
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
			painting.stopPainting();
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			boolean rainbow = prefs.getBoolean("rainbow", false);
			painting.setRainbow(rainbow);
			if (rainbow == false) {
				painting.setBranchColor(prefs
						.getInt("branch_color", 0xffffffff));
			}
			boolean wire = prefs.getBoolean("wire", false);
			painting.setWire(wire);
			if (wire) {
				painting.setFill(prefs.getBoolean("fill", false));
			} else {
				painting.setFill(false);
			}
			painting.setBackgroundColor(prefs.getInt("background_color",
					0xff000000));
			painting.setAntiAlias(prefs.getBoolean("antialias", true));

			painting.setFPS(prefs.getInt("fps", 45));
			painting.setZoom(prefs.getInt("zoom", 5));
			painting.setCrook(prefs.getInt("crook", 20));
			painting.setVert(prefs.getInt("vert", 10));
			painting.setTwigs(prefs.getInt("twigs", 7));
			painting.setBranches(prefs.getInt("branches", 50));
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			if (visible) {
				// register listeners and callbacks here
				painting.resumePainting();
			} else {
				// remove listeners and callbacks here
				painting.pausePainting();
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			painting.setSurfaceSize(width, height);
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			// start painting
			painting.start();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			boolean retry = true;
			painting.stopPainting();
			while (retry) {
				try {
					painting.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels) {
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			super.onTouchEvent(event);
			painting.doTouchEvent(event);
		}

	}

}