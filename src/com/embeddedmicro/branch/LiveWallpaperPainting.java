package com.embeddedmicro.branch;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LiveWallpaperPainting extends Thread {
	/** Reference to the View and the context */
	private SurfaceHolder surfaceHolder;

	/** State */
	private boolean wait;
	private boolean run;
	private long time;
	private int frame_rate;
	private int height, width;

	private Tree tree;

	public LiveWallpaperPainting(SurfaceHolder surfaceHolder, Context context) {
		// keep a reference of the context and the surface
		// the context is needed if you want to inflate
		// some resources from your livewallpaper .apk
		this.surfaceHolder = surfaceHolder;
		// don't animate until surface is created and displayed
		this.wait = true;
		tree = new Tree();
		time = System.currentTimeMillis();
		frame_rate = 45;
	}

	/**
	 * Pauses the live wallpaper animation
	 */
	public void pausePainting() {
		this.wait = true;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Resume the live wallpaper animation
	 */
	public void resumePainting() {
		this.wait = false;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Stop the live wallpaper animation
	 */
	public void stopPainting() {
		this.run = false;
		synchronized (this) {
			this.notify();
		}
	}

	@Override
	public void run() {
		this.run = true;
		Canvas c = null;
		long diff;
		while (run) {
			try {
				c = this.surfaceHolder.lockCanvas(null);
				synchronized (this.surfaceHolder) {
					time = System.currentTimeMillis();
					updatePhysics();
					doDraw(c);
				}
			} finally {
				if (c != null) {
					this.surfaceHolder.unlockCanvasAndPost(c);
				}
			}

			// pause if no need to animate
			synchronized (this) {
				if (wait) {
					try {
						wait();
					} catch (Exception e) {
					}
				}
				diff = (1000 / frame_rate)
						- (System.currentTimeMillis() - time);
				if (diff > 0) {
					try {
						wait(diff);
					} catch (Exception e) {
					}
				}
			}
		}
	}

	/**
	 * Invoke when the surface dimension change
	 */
	public void setSurfaceSize(int w, int h) {
		tree.set_dimentions(w, h);
		width = w;
		height = h;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Invoke while the screen is touched
	 */
	public void doTouchEvent(MotionEvent event) {
		// handle the event here
		// if there is something to animate
		// then wake up
		this.wait = false;
		synchronized (this) {
			notify();
		}
	}

	public void setFPS(int fps) {
		frame_rate = fps;
	}

	public void setRainbow(boolean rainbow) {
		tree.set_rainbow(rainbow);
	}

	public void setWire(boolean wire) {
		tree.set_wire(wire);
	}

	public void setFill(boolean fill) {
		tree.set_wirefill(fill);
	}

	public void setZoom(int z) {
		tree.set_zoom(z);
	}

	public void setCrook(int c) {
		tree.set_crook(c);
	}

	public void setTwigs(int t) {
		tree.set_twigs(t);
	}

	public void setBranches(int b) {
		tree.set_branches(b);
	}

	public void setVert(int v) {
		tree.set_vert(v);
	}

	public void setBranchColor(int color) {
		tree.set_color(color);
	}

	public void setBackgroundColor(int color) {
		tree.set_background(color);
	}

	public void setAntiAlias(boolean aa) {
		tree.set_antialias(aa);
	}

	/**
	 * Do the actual drawing stuff
	 */
	private void doDraw(Canvas canvas) {
		tree.draw(canvas);
	}

	/**
	 * Update the animation, sprites or whatever. If there is nothing to animate
	 * set the wait attribute of the thread to true
	 */
	private void updatePhysics() {
		tree.update();
	}

}