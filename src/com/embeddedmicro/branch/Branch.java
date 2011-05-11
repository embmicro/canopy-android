package com.embeddedmicro.branch;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

public class Branch {
	private long animationTime;
	private Vector2D[] center;
	private Vector2D[] nVec, wVec;
	private Bezier[] c_bez;
	private int nVert;
	private long genTime;
	private long growDur;
	private float width, base, length;
	private Rectangle bounds;
	private float start_angle;
	public boolean grown, split;
	public long fadeTime;
	private Path draw;
	private Matrix matrix;
	public Paint paint;
	public boolean active;

	Branch() {
		genTime = System.currentTimeMillis();
		fadeTime = 0;
		grown = false;
		split = false;
		active = false;
		bounds = new Rectangle();
		draw = new Path();
		matrix = new Matrix();
	}

	public long get_age() {
		return animationTime;
	}

	public float get_length() {
		return length;
	}

	public void set_length(float l) {
		length = l;
	}

	public float get_percent_grown() {
		return animationTime / growDur;
	}

	public void set_growDur(long time) {
		growDur = time;
	}

	public void set_start_angle(float a) {
		start_angle = a;
	}

	public void set_nVert(int v) {
		nVert = v;
		nVec = new Vector2D[nVert]; // Normal Vectors
		wVec = new Vector2D[nVert];
		for (int i = 0; i < nVert; i++) {
			wVec[i] = new Vector2D();
		}
		c_bez = new Bezier[nVert - 1];
	}

	public Vector2D get_point(float t) {
		float u = t * (nVert - 1);
		int uint = (int) Math.floor(u);
		float uf = u - uint;
		return c_bez[uint].getPoint(uf);
	}

	public float get_angle(float t) {
		float u = t * (nVert - 1);
		int uint = (int) Math.floor(u);
		float uf = u - uint;
		return c_bez[uint].getnAngle(uf);
	}

	public void set_center_path(Vector2D[] vec) {
		center = vec;
		update_bounds();
	}

	public Vector2D[] get_center_path() {
		return center;
	}

	private void update_bounds() {
		float minx, miny, maxx, maxy;
		minx = maxx = center[0].x;
		miny = maxy = center[0].y;
		for (int i = 1; i < nVert; i++) {
			if (center[i].x > maxx)
				maxx = center[i].x;
			if (center[i].x < minx)
				minx = center[i].x;
			if (center[i].y > maxy)
				maxy = center[i].y;
			if (center[i].y < miny)
				miny = center[i].y;
		}
		bounds.min.x = minx;
		bounds.min.y = miny;
		bounds.max.x = maxx;
		bounds.max.y = maxy;
	}

	public Rectangle get_bounds() {
		return bounds;
	}

	public int get_nVert() {
		return nVert;
	}

	public Vector2D get_cv(int i) {
		return center[i];
	}

	public void set_width(float w) {
		width = w;
	}

	public float get_width() {
		return width;
	}

	public void generate_center_curve() {
		// The first curve uses the same point for the first and second points
		// because there is no previous point
		c_bez[0] = Bezier.getCurve(center[0], center[0], center[1], center[2]);
		for (int i = 1; i < nVert - 2; i++) {
			c_bez[i] = Bezier.getCurve(center[i - 1], center[i], center[i + 1],
					center[i + 2]);
		}
		// The last curve uses the same point for the third and forth points
		// because there is no next point
		c_bez[nVert - 2] = Bezier.getCurve(center[nVert - 3],
				center[nVert - 2], center[nVert - 1], center[nVert - 1]);
	}

	public void generate_normals() {
		// Generate unit vectors for the normal so they can be scaled later

		// You can't find the starting angle based on the vertices because when
		// you take the derivative you end up with a 0/0 situation. So we use
		// the starting angle instead.
		nVec[0] = Vector2D.getPtDA(1.0f, (float) (start_angle - Math.PI / 2));

		for (int i = 1; i < nVert - 1; i++) {
			nVec[i] = Vector2D.getPtDA(1.0f, c_bez[i].getiAngle());
		}
		nVec[nVert - 1] = new Vector2D(0.0f, 0.0f);
	}

	private void generate_widths() {
		float tgen = (float) animationTime / growDur;
		if (tgen > 1.0f)
			tgen = 1.0f;

		tgen = (tgen - 1) * (tgen - 1) * (tgen - 1) + 1;

		base = width * tgen;
		float ratio = base / ((nVert - 1) * tgen);

		for (int i = 0; i < nVert; i++) {
			wVec[i].x = nVec[i].x;
			wVec[i].y = nVec[i].y;
			wVec[i].scale(base - (ratio * i));
		}
	}

	public void transform(Vector2D origin, Vector2D move, float scale) {
		for (int i = 0; i < nVert; i++) {
			center[i].transform(origin, move, scale);
		}
		for (int i = 0; i < nVert - 1; i++) {
			c_bez[i].transform(origin, move, scale);
		}
		bounds.transform(origin, move, scale);
		width *= scale;
		length *= scale;
		if (grown) {
			matrix.setScale(scale, scale, origin.x, origin.y);
			draw.transform(matrix);
		}
	}

	public Path generate_path() {
		animationTime = System.currentTimeMillis() - genTime;

		float tgen = (float) animationTime / growDur;
		tgen = (tgen - 1) * (tgen - 1) * (tgen - 1) + 1;
		if (grown == false) {
			if (tgen > 1.0f) {
				tgen = 1.0f;
				grown = true;
			}
			float u = tgen * (nVert - 1);
			int uint = (int) Math.floor(u);
			float uf = u - uint;

			generate_widths();

			draw.reset();
			try {
				// Side A
				draw.moveTo(c_bez[0].a.x + wVec[0].x, c_bez[0].a.y + wVec[0].y);

				for (int i = 0; i < uint; i++) {
					draw.cubicTo(c_bez[i].b.x + wVec[i].x, c_bez[i].b.y
							+ wVec[i].y, c_bez[i].c.x + wVec[i + 1].x,
							c_bez[i].c.y + wVec[i + 1].y, c_bez[i].d.x
									+ wVec[i + 1].x, c_bez[i].d.y
									+ wVec[i + 1].y);
				}

				if (uf > 0 && uint < c_bez.length) {
					Vector2D[] tip = Bezier.semiBezier(uf, c_bez[uint].a.x,
							c_bez[uint].a.y, c_bez[uint].b.x, c_bez[uint].b.y,
							c_bez[uint].c.x, c_bez[uint].c.y, c_bez[uint].d.x,
							c_bez[uint].d.y);
					draw.cubicTo(tip[0].x + wVec[uint].x, tip[0].y
							+ wVec[uint].y, tip[1].x, tip[1].y, tip[2].x,
							tip[2].y);
					draw.cubicTo(tip[1].x, tip[1].y, tip[0].x - wVec[uint].x,
							tip[0].y - wVec[uint].y, c_bez[uint].a.x
									- wVec[uint].x, c_bez[uint].a.y
									- wVec[uint].y);
				}

				for (int i = uint - 1; i >= 0; i--) {
					draw.cubicTo(c_bez[i].c.x - wVec[i + 1].x, c_bez[i].c.y
							- wVec[i + 1].y, c_bez[i].b.x - wVec[i].x,
							c_bez[i].b.y - wVec[i].y, c_bez[i].a.x - wVec[i].x,
							c_bez[i].a.y - wVec[i].y);
				}

				// Draw a smooth end
				draw.cubicTo(c_bez[0].a.x - wVec[0].x + wVec[0].y, c_bez[0].a.y
						- wVec[0].y - wVec[0].x, c_bez[0].a.x + wVec[0].x
						+ wVec[0].y, c_bez[0].a.y + wVec[0].y - wVec[0].x,
						c_bez[0].a.x + wVec[0].x, c_bez[0].a.y + wVec[0].y);

				draw.close();
			} catch (ArrayIndexOutOfBoundsException e) {

			}
		}

		return draw;
	}

}
