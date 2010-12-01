package com.embeddedmicro.branch;

import java.util.ArrayList;

import com.android.vending.licensing.AESObfuscator;
import com.android.vending.licensing.LicenseChecker;
import com.android.vending.licensing.LicenseCheckerCallback;
import com.android.vending.licensing.ServerManagedPolicy;
import com.android.vending.licensing.LicenseCheckerCallback.ApplicationErrorCode;

import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Android Live Wallpaper Archetype
 * 
 * @author Justin Rajewski
 */
public class BranchWallpaper extends WallpaperService {

	private static final String BASE64_PUBLIC_KEY = "":
	private static final byte[] SALT = new byte[] {  };
	private LicenseChecker mChecker;

	private LicenseCheckerCallback mLicenseCheckerCallback;

	public static final String PREFERENCES = "com.embeddedmicro.branch";

	private boolean licensed = true;

	private ArrayList<BranchEngine> engines = new ArrayList<BranchEngine>();

	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
		@Override
		public void allow() {
			licensed = true;
			updateEngines();
		}

		@Override
		public void applicationError(ApplicationErrorCode errorCode) {
			licensed = false;
			updateEngines();
		}

		@Override
		public void dontAllow() {
			licensed = false;
			updateEngines();
		}
	}

	private void updateEngines() {
		for (int i = 0; i < engines.size(); i++) {
			engines.get(i).enable(licensed);
		}
	}

	@Override
	public Engine onCreateEngine() {
		engines.add(new BranchEngine(licensed));
		return engines.get(engines.size() - 1);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Try to use more data here. ANDROID_ID is a single point of attack.
		String deviceId = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);

		// Library calls this when it's done.
		mLicenseCheckerCallback = new MyLicenseCheckerCallback();
		// Construct the LicenseChecker with a policy.
		mChecker = new LicenseChecker(this, new ServerManagedPolicy(this,
				new AESObfuscator(SALT, getPackageName(), deviceId)),
				BASE64_PUBLIC_KEY);
		mChecker.checkAccess(mLicenseCheckerCallback);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public class BranchEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener {

		private LiveWallpaperPainting painting;
		private SharedPreferences prefs;

		BranchEngine(boolean run) {
			SurfaceHolder holder = getSurfaceHolder();
			painting = new LiveWallpaperPainting(holder,
					getApplicationContext(), run);
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

		public void enable(boolean set) {
			painting.enable(set);
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs,
				String key) {
			boolean rainbow = prefs.getBoolean("rainbow", false);
			painting.setRainbow(rainbow);
			if (rainbow == false) {
				painting.setBranchColor(prefs
						.getInt("branch_color", 0xffffffff));
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