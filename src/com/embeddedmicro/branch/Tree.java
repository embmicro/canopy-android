package com.embeddedmicro.branch;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Tree {
	private ArrayList<Branch> branches;
	private Rectangle cullbounds;
	private int width, height;
	private Vector2D origin, center, zero;
	private Vector2D[] core;
	private Paint paint, fillPaint;
	private int clear_color;
	private long time, inital_time, inital_delay;
	private int zoom_factor;
	private float crook_factor;
	private int vert_factor, twigs, max_branches, grow_speed;
	private boolean rainbow, wireFill;
	private int n_branches;
	private Rectangle bounds;

	Tree() {
		branches = new ArrayList<Branch>();
		paint = new Paint();
		paint.setColor(0xffff0000);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStyle(Paint.Style.FILL);
		fillPaint = new Paint(paint);
		fillPaint.setAntiAlias(false);
		fillPaint.setColor(0xff000000);
		zero = new Vector2D(0, 0);
		time = inital_time = System.currentTimeMillis();
		reset();
		wireFill = true;
		cullbounds = new Rectangle();
		origin = new Vector2D();
		center = new Vector2D();
		bounds = new Rectangle();
	}

	public void reset() {
		inital_time = System.currentTimeMillis();
		branches.clear();
		for (int i = 0; i < max_branches; i++)
			branches.add(new Branch(vert_factor));
		n_branches = 0;
	}

	public void set_dimentions(int w, int h) {
		width = w;
		height = h;
		cullbounds.set(-75, -75, width + 75, height + 75);
		origin.set(w / 2, h / 2);
		center.set(w / 2, h / 2);
	}

	private Vector2D segIntersection(float x1, float y1, float x2, float y2, Vector2D p3,
			Vector2D p4) {
		float bx = x2 - x1;
		float by = y2 - y1;
		float dx = p4.x - p3.x;
		float dy = p4.y - p3.y;

		float b_dot_d_perp = bx * dy - by * dx;

		if (b_dot_d_perp == 0.0f)
			return null;

		float cx = p3.x - x1;
		float cy = p3.y - y1;

		float t = (cx * dy - cy * dx) / b_dot_d_perp;
		if (t < 0.0f || t > 1.0f)
			return null;

		float u = (cx * by - cy * bx) / b_dot_d_perp;
		if (u < 0.0f || u > 1.0f)
			return null;

		return new Vector2D(x1 + t * bx, y1 + t * by);
	}

	private Vector2D findIntersection(float x1, float y1, float x2, float y2) {
		bounds.set(x1, y1, x2, y2);
		Vector2D poi;

		for (int i = 0; i < branches.size() - 1; i++) {
			if (branches.get(i).active) {
				if (bounds.intersects(branches.get(i).get_bounds())) {
					for (int j = 0; j < branches.get(i).get_nVert() - 1; j++) {
						poi = segIntersection(x1, y1, x2, y2,
								branches.get(i).get_cv(j), branches.get(i)
										.get_cv(j + 1));
						if (poi != null)
							return poi;
					}
				}
			}
		}
		return null;
	}


	public void set_color(int color) {
		paint.setColor(color);
	}

	public void set_background(int color) {
		clear_color = color;
		fillPaint.setColor(color);
	}

	public void set_rainbow(boolean set) {
		rainbow = set;
	}

	public void set_wirefill(boolean set) {
		wireFill = set;
	}

	public void set_wire(boolean set) {
		if (set) {
			paint.setStyle(Paint.Style.STROKE);
		} else {
			paint.setStyle(Paint.Style.FILL);
		}
	}

	public void set_antialias(boolean aa) {
		paint.setAntiAlias(aa);
	}

	public void set_zoom(int z) {
		zoom_factor = z;
		grow_speed = -100 * z + 1500;
		inital_delay = grow_speed * 10;
	}

	public void set_crook(int c) {
		crook_factor = (float) c / 100.0f;
	}

	public void set_vert(int v) {
		vert_factor = v;
		if (v < 5)
			v = 5;
		for (int i = 0; i < branches.size(); i++){
			branches.get(i).set_maxVert(v);
		}
		
		core = new Vector2D[vert_factor];
		for (int i = 0; i < vert_factor; i++){
			core[i] = new Vector2D();
		}
	}

	public void set_twigs(int t) {
		twigs = t;
	}

	public void set_branches(int b) {
		max_branches = b;
		reset();
	}

	private int get_unactive() {
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active == false)
				return i;
		}
		return -1;
	}

	private boolean all_unactive() {
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active)
				return false;
		}
		return true;
	}

	private int count_unactive() {
		int c = 0;
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active == false)
				c++;
		}
		return c;
	}

	private boolean all_active() {
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active == false)
				return false;
		}
		return true;
	}
	
	private void move_forward(int z) {
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active && branches.get(i).zbuf >= z)
				branches.get(i).zbuf--;
		}
	}
	
	private int get_z_index(int z){
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active && branches.get(i).zbuf == z)
				return i;
		}
		return -1;
	}

	public void add_branch(Vector2D orig, float sangle, float length,
			float width, float crook, int vert, long grow) {
		if (!all_active()) {
			n_branches++;
			int next = get_unactive();
			branches.get(next).reset();
			branches.get(next).zbuf = n_branches - 1;
			branches.get(next).set_growDur(grow);
			
			float angle, anglev = 0;
			float ox, oy, nx, ny;
			int nv = 1;
			
			
			ox = orig.x;
			oy = orig.y;
			core[0].set(orig);

			angle = sangle;

			for (int i = 1; i < vert; i++) {
				nx = (float) (ox + Math.cos(angle) * length / vert);
				ny = (float) (oy + Math.sin(angle) * length / vert);
				anglev += Math.random() * 2 * crook - crook;
				angle += anglev;

				Vector2D poi = findIntersection(ox, oy, nx, ny);

				if (poi != null && i != 1) {
					core[nv].set(poi);
					nv++;
					ox = poi.x;
					oy = poi.y;
					break;
				} else {
					core[nv].set(nx, ny);
					nv++;
					ox = nx;
					oy = ny;
				}
			}
			
			branches.get(next).set_nVert(nv);
			branches.get(next).set_width(width * nv / vert);
			branches.get(next).set_length(length * nv / vert);
			branches.get(next).set_center_path(core);
			branches.get(next).set_start_angle(sangle);
			branches.get(next).generate_center_curve();
			branches.get(next).generate_normals();
			branches.get(next).paint.set(paint);
			if (rainbow) {
				branches.get(next).paint.setColor(generate_color());
			}
			branches.get(next).active = true;
		}
	}

	private int generate_color() {
		float hue = (float) (Math.random() * 6.0f);
		float x = (1 - Math.abs(hue % 2 - 1)) * 255;
		int r = 0, g = 0, b = 0;
		if (hue >= 0.0f && hue < 1.0f) {
			r = 255;
			g = (int) x;
			b = 0;
		} else if (hue >= 1.0f && hue < 2.0f) {
			r = (int) x;
			g = 255;
			b = 0;
		} else if (hue >= 2.0f && hue < 3.0f) {
			r = 0;
			g = 255;
			b = (int) x;
		} else if (hue >= 3.0f && hue < 4.0f) {
			r = 0;
			g = (int) x;
			b = 255;
		} else if (hue >= 4.0f && hue < 5.0f) {
			r = (int) x;
			g = 0;
			b = 255;
		} else if (hue >= 5.0f && hue < 6.0f) {
			r = 255;
			g = 0;
			b = (int) x;
		}
		return 0xff000000 | ((r & 255) << 16) | ((g & 255) << 8) | ((b & 255));
	}

	public void draw(Canvas canvas) {
		int i;
		try {
			canvas.drawColor(clear_color);
			for (int ct = branches.size() - 1; ct >= 0; ct--) {
				if (rainbow){
					i = get_z_index(ct);
				} else {
					i = ct;
				}
				if (i != -1 && branches.get(i).active) {
					branches.get(i).paint.setAlpha(0xff);
					if (branches.get(i).fadeTime == 0
							&& branches.get(i).get_length() > height * 10) {
						branches.get(i).fadeTime = branches.get(i).get_age();
					} else if (branches.get(i).fadeTime != 0) {
						long age = branches.get(i).get_age()
								- branches.get(i).fadeTime;
						if (age > 1500) {
							branches.get(i).active = false;
							n_branches--;
							move_forward(branches.get(i).zbuf);
							i--;
							continue;
						} else {
							branches.get(i).paint
									.setAlpha((int) (0xff - ((float) (age) * 0xff / 1500)));
						}
					}

					if (wireFill) {
						fillPaint.setAlpha(branches.get(i).paint.getAlpha());
						canvas.drawPath(branches.get(i).generate_path(),
								fillPaint);
					}

					canvas.drawPath(branches.get(i).generate_path(), branches
							.get(i).paint);
				}
			}
		} catch (IndexOutOfBoundsException e) {

		}
	}

	private void cullOffscreen() {
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active) {
				if (!cullbounds.intersects(branches.get(i).get_bounds())) {
					branches.get(i).active = false;
					n_branches--;
					move_forward(branches.get(i).zbuf);
				}
			}
		}
	}

	private void split(int i) {

		if (branches.get(i).split == false
				&& branches.get(i).get_percent_grown() >= 1.0f
				&& count_unactive() >= twigs
				&& branches.get(i).get_length() > height / 20) {
			for (int j = 0; j < twigs; j++) {
				int side = j % 2;
				float t = ((float) j + 1) / ((float) twigs + 1);
				Vector2D pt = branches.get(i).get_point(t);
				float ang = branches.get(i).get_angle(t);
				float w = branches.get(i).get_width() * (1 - t);
				float h = w * 40;
				if (side == 1)
					ang += Math.PI;
				add_branch(pt, ang, h, w, crook_factor, vert_factor, grow_speed);
			}
			branches.get(i).split = true;
		}
	}

	public void update_origin() {
		float dx = 0, dy = 0, tx, ty;
		Vector2D[] points;
		float m;
		int v;
		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active) {
				points = branches.get(i).get_center_path();
				v = branches.get(i).get_nVert();
				for (int j = 0; j < v; j++) {
					tx = points[j].x;
					ty = points[j].y;
					tx -= center.x;
					ty -= center.y;
					if (tx != 0.0f || ty != 0.0f) {
						m = (float) Math.sqrt(tx * tx + ty * ty);
						tx /= m;
						ty /= m;
						tx *= 1000.0f / (float) (vert_factor * max_branches);
						ty *= 1000.0f / (float) (vert_factor * max_branches);
						dx += tx;
						dy += ty;
					}
				}
			}
		}
		origin.x += ((center.x + dx) - origin.x) / 4;
		origin.y += ((center.y + dy) - origin.y) / 4;
	}

	public void update() {
		update_origin();

		float scale = (float) (System.currentTimeMillis() - time);
		scale /= 10000.0f;
		scale *= zoom_factor;

		long t_rem = System.currentTimeMillis() - inital_time;

		if (t_rem < inital_delay) {
			scale *= ((float) t_rem / inital_delay);
		}

		for (int i = 0; i < branches.size(); i++) {
			if (branches.get(i).active) {
				branches.get(i).transform(origin, zero, 1.0f + scale);
			}
		}
		cullOffscreen();

		for (int i = branches.size() - 1; i >= 0; i--) {
			if (branches.get(i).active) {
				split(i);
			}
		}

		if (all_unactive())
			add_branch(new Vector2D((float) (width / 2 + width * (Math.random() - 0.5f) * 0.75f), height),
					(float) (Math.PI * 3 / 2), height * 0.8f, height / 50,
					0.2f, 5, grow_speed);

		time = System.currentTimeMillis();
	}
}
