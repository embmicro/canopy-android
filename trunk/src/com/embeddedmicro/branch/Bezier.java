package com.embeddedmicro.branch;

public class Bezier {
	Vector2D a, b, c, d;

	Bezier() {
		a = new Vector2D();
		b = new Vector2D();
		c = new Vector2D();
		d = new Vector2D();
	}

	Bezier(Bezier bez) {
		a = bez.a;
		b = bez.b;
		c = bez.c;
		d = bez.d;
	}

	Bezier(Vector2D pa, Vector2D pb, Vector2D pc, Vector2D pd) {
		a = pa;
		b = pb;
		c = pc;
		d = pd;
	}
	
	public void setCurve(Vector2D v1, Vector2D v2, Vector2D v3,
			Vector2D v4) {
		a.x = v2.x;
		b.x = v2.x + (v3.x - v1.x) / 6;
		c.x = v3.x + (v2.x - v4.x) / 6;
		d.x = v3.x;
		a.y = v2.y;
		b.y = v2.y + (v3.y - v1.y) / 6;
		c.y = v3.y + (v2.y - v4.y) / 6;
		d.y = v3.y;
	}

	public static Bezier getCurve(Vector2D v1, Vector2D v2, Vector2D v3,
			Vector2D v4) {
		Bezier curve = new Bezier();

		curve.a.x = v2.x;
		curve.b.x = v2.x + (v3.x - v1.x) / 6;
		curve.c.x = v3.x + (v2.x - v4.x) / 6;
		curve.d.x = v3.x;
		curve.a.y = v2.y;
		curve.b.y = v2.y + (v3.y - v1.y) / 6;
		curve.c.y = v3.y + (v2.y - v4.y) / 6;
		curve.d.y = v3.y;
		return curve;
	}

	public static Vector2D[] semiBezier(float t, float x0, float y0, float x1,
			float y1, float x2, float y2, float x3, float y3) {
		Vector2D[] ret = new Vector2D[3];

		if (t == 0.0f) {
			ret[0] = ret[1] = ret[2] = new Vector2D(x0, y0);
			return ret;
		} else if (t == 1.0f) {
			ret[0] = new Vector2D(x1, y1);
			ret[1] = new Vector2D(x2, y2);
			ret[2] = new Vector2D(x3, y3);
			return ret;
		}

		float qx1 = x0 + (x1 - x0) * t;
		float qy1 = y0 + (y1 - y0) * t;
		float qx2 = x1 + (x2 - x1) * t;
		float qy2 = y1 + (y2 - y1) * t;
		float qx3 = x2 + (x3 - x2) * t;
		float qy3 = y2 + (y3 - y2) * t;
		float rx2 = qx1 + (qx2 - qx1) * t;
		float ry2 = qy1 + (qy2 - qy1) * t;
		float rx3 = qx2 + (qx3 - qx2) * t;
		float ry3 = qy2 + (qy3 - qy2) * t;
		float bx3 = rx2 + (rx3 - rx2) * t;
		float by3 = ry2 + (ry3 - ry2) * t;

		ret[0] = new Vector2D(qx1, qy1);
		ret[1] = new Vector2D(rx2, ry2);
		ret[2] = new Vector2D(bx3, by3);
		return ret;
	}

	public float getnAngle(float t) {
		float ax, bx, cx, ay, by, cy, mx, my;
		cx = 3 * (b.x - a.x);
		bx = 3 * (c.x - b.x) - cx;
		ax = d.x - a.x - cx - bx;
		cy = 3 * (b.y - a.y);
		by = 3 * (c.y - b.y) - cy;
		ay = d.y - a.y - cy - by;
		mx = (3 * ax * t * t + 2 * bx * t + cx);
		my = (3 * ay * t * t + 2 * by * t + cy);

		if (mx == 0.0f && my == 0.0f)
			return 0.0f;

		float ang = (float) (mx / Math.sqrt(mx * mx + my * my));
		
		if (ang < -1.0f)
			ang = -1.0f;
		else if (ang > 1.0f)
			ang = 1.0f;
		
		ang = (float) Math.acos(ang);
		
		if (my < 0.0f)
			ang = -ang;
		
		return (float) (ang - Math.PI / 2);
	}

	public float getiAngle() {
		float mx, my, ang;
		mx = 3 * (b.x - a.x);
		my = 3 * (b.y - a.y);
		if (mx == 0.0f && my == 0.0f)
			return 0.0f;
		ang = (float) (mx / Math.sqrt(mx * mx + my * my));

		// Cap numbers to limits of acos
		// Theoretically they should never go over but floating point rounding
		// can cause it to
		if (ang < -1.0f)
			ang = -1.0f;
		else if (ang > 1.0f)
			ang = 1.0f;

		ang = (float) Math.acos(ang);
		if (my < 0.0f)
			ang = -ang;
		return (float) (ang - Math.PI / 2);
	}

	public void transform(Vector2D origin, Vector2D move, float scale) {
		a.transform(origin, move, scale);
		b.transform(origin, move, scale);
		c.transform(origin, move, scale);
		d.transform(origin, move, scale);
	}

	public Vector2D getPoint(float t) {
		Vector2D v = new Vector2D();
		float mt = 1 - t;
		v.x = mt * mt * mt * a.x + 3 * mt * mt * t * b.x + 3 * mt * t * t * c.x
				+ t * t * t * d.x;
		v.y = mt * mt * mt * a.y + 3 * mt * mt * t * b.y + 3 * mt * t * t * c.y
				+ t * t * t * d.y;
		return v;
	}
}
