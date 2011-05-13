package com.embeddedmicro.branch;

public class Vector2D {
	float x;
	float y;

	Vector2D() {
	}

	Vector2D(Vector2D v) {
		x = v.x;
		y = v.y;
	}

	Vector2D(float px, float py) {
		x = px;
		y = py;
	}

	public void set(float px, float py) {
		x = px;
		y = py;
	}

	public void set(Vector2D vec) {
		x = vec.x;
		y = vec.y;
	}

	public void add(Vector2D vec) {
		x += vec.x;
		y += vec.y;
	}

	public void sub(Vector2D vec) {
		x -= vec.x;
		y -= vec.y;
	}

	public void scale(float s) {
		x *= s;
		y *= s;
	}

	public float mag() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public float angle() {
		if (x == 0 && y == 0)
			return 0;
		float ang = (float) (x / Math.sqrt(x * x + y * y));
		if (ang < -1.0f)
			ang = -1.0f;
		else if (ang > 1.0f)
			ang = 1.0f;
		ang = (float) Math.acos(ang);
		if (y < 0.0f)
			ang = -ang;
		return ang;
	}

	public void normalize() {
		if (x == 0 && y == 0)
			return;
		float m = mag();
		x /= m;
		y /= m;
	}

	public void transform(Vector2D origin, Vector2D move, float scale) {
		float xo = (x - origin.x + move.x) * scale;
		float yo = (y - origin.y + move.y) * scale;
		x = origin.x + xo;
		y = origin.y + yo;
	}
	
	void setPtDA(float dist, float angle) {
		x = (float) (dist * Math.cos(angle));
		y = (float) (dist * Math.sin(angle));
	}

	static Vector2D getPtDA(float dist, float angle) {
		return new Vector2D((float) (dist * Math.cos(angle)),
				(float) (dist * Math.sin(angle)));
	}

}
