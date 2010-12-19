package com.embeddedmicro.branch;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Tree {
	private ArrayList<Branch> branches;
	private Rectangle cullbounds;
	private int width, height;
	private Vector2D origin, center, zero;
	private Paint paint, fillPaint;
	private int clear_color;
	private long time, inital_time, inital_delay;
	private int zoom_factor;
	private float crook_factor;
	private int vert_factor, twigs, max_branches, grow_speed;
	private boolean rainbow, wireFill;

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
		wireFill = true;
	}

	public void reset() {
		inital_time = System.currentTimeMillis();
		branches.clear();
	}

	public void set_dimentions(int w, int h) {
		width = w;
		height = h;
		cullbounds = new Rectangle(-75, -75, width + 75, height + 75);
		origin = new Vector2D(w / 2, h / 2);
		center = new Vector2D(w / 2, h / 2);
	}

	private Vector2D segIntersection(Vector2D p1, Vector2D p2, Vector2D p3,
			Vector2D p4) {
		float bx = p2.x - p1.x;
		float by = p2.y - p1.y;
		float dx = p4.x - p3.x;
		float dy = p4.y - p3.y;

		float b_dot_d_perp = bx * dy - by * dx;

		if (b_dot_d_perp == 0.0f)
			return null;

		float cx = p3.x - p1.x;
		float cy = p3.y - p1.y;

		float t = (cx * dy - cy * dx) / b_dot_d_perp;
		if (t < 0.0f || t > 1.0f)
			return null;

		float u = (cx * by - cy * bx) / b_dot_d_perp;
		if (u < 0.0f || u > 1.0f)
			return null;

		return new Vector2D(p1.x + t * bx, p1.y + t * by);
	}

	private Vector2D findIntersection(Vector2D p1, Vector2D p2) {
		Rectangle bounds = new Rectangle(p1, p2);
		Vector2D poi;

		for (int i = 0; i < branches.size() - 1; i++) {
			if (bounds.intersects(branches.get(i).get_bounds())) {
				for (int j = 0; j < branches.get(i).get_nVert() - 1; j++) {
					poi = segIntersection(p1, p2, branches.get(i).get_cv(j),
							branches.get(i).get_cv(j + 1));
					if (poi != null)
						return poi;
				}
			}
		}
		return null;
	}

	private ArrayList<Vector2D> generate_center(Vector2D orig, float sangle,
			float length, float crook, int vert) {
		float angle, anglev = 0;
		Vector2D old_pt, new_pt;

		ArrayList<Vector2D> center = new ArrayList<Vector2D>();
		old_pt = new Vector2D(orig);
		center.add(old_pt);
		new_pt = new Vector2D();
		angle = sangle;

		for (int i = 1; i < vert; i++) {
			new_pt.x = (float) (old_pt.x + Math.cos(angle) * length / vert);
			new_pt.y = (float) (old_pt.y + Math.sin(angle) * length / vert);
			anglev += Math.random() * 2 * crook - crook;
			angle += anglev;

			Vector2D poi = findIntersection(old_pt, new_pt);

			if (poi != null && i != 1) {
				center.add(poi);
				old_pt = new Vector2D(poi);
				break;
			} else {
				center.add(new Vector2D(new_pt));
				old_pt = new Vector2D(new_pt);
			}

		}
		return center;
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
	}

	public void set_twigs(int t) {
		twigs = t;
	}

	public void set_branches(int b) {
		max_branches = b;
		reset();
	}

	public void add_branch(Vector2D orig, float sangle, float length,
			float width, float crook, int vert, long grow) {
		Vector2D[] center = null;
		branches.add(new Branch());
		int last = branches.size() - 1;
		branches.get(last).set_growDur(grow);
		ArrayList<Vector2D> cvert = generate_center(orig, sangle, length,
				crook, vert);
		branches.get(last).set_nVert(cvert.size());
		center = new Vector2D[cvert.size()];
		for (int i = 0; i < cvert.size(); i++) {
			center[i] = cvert.get(i);
		}
		branches.get(last).set_width(width * cvert.size() / vert);
		branches.get(last).set_length(length * cvert.size() / vert);
		branches.get(last).set_center_path(center);
		branches.get(last).set_start_angle(sangle);
		branches.get(last).generate_center_curve();
		branches.get(last).generate_normals();
		branches.get(last).paint = new Paint(paint);
		if (rainbow) {
			branches.get(last).paint.setColor(generate_color());
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
		try {
			canvas.drawColor(clear_color);
			for (int i = branches.size() - 1; i >= 0; i--) {
				branches.get(i).paint.setAlpha(0xff);
				if (branches.get(i).fadeTime == 0
						&& branches.get(i).get_length() > height * 10) {
					branches.get(i).fadeTime = branches.get(i).get_age();
				} else if (branches.get(i).fadeTime != 0) {
					long age = branches.get(i).get_age()
							- branches.get(i).fadeTime;
					if (age > 1500) {
						branches.remove(i);
						i--;
						continue;
					} else {
						branches.get(i).paint
								.setAlpha((int) (0xff - ((float) (age) * 0xff / 1500)));
					}
				}

				if (wireFill) {
					fillPaint.setAlpha(branches.get(i).paint.getAlpha());
					canvas.drawPath(branches.get(i).generate_path(), fillPaint);
				}

				canvas.drawPath(branches.get(i).generate_path(), branches
						.get(i).paint);
			}
		} catch (IndexOutOfBoundsException e) {

		}
	}

	private void cullOffscreen() {
		for (int i = 0; i < branches.size(); i++) {
			if (!cullbounds.intersects(branches.get(i).get_bounds())) {
				branches.remove(i);
				i--;
			}
		}
	}

	private void split(int i) {

		if (branches.get(i).split == false
				&& branches.get(i).get_percent_grown() >= 1.0f
				&& branches.size() + twigs < max_branches
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
			branches.get(i).transform(origin, zero, 1.0f + scale);
		}
		cullOffscreen();

		for (int i = branches.size() - 1; i >= 0; i--) {
			split(i);
		}

		if (branches.size() == 0)
			add_branch(new Vector2D(width / 2, height),
					(float) (Math.PI * 3 / 2), height * 0.8f, height / 50,
					0.2f, 5, grow_speed);

		time = System.currentTimeMillis();
	}
}
