package com.embeddedmicro.branch;

public class Rectangle {
	Vector2D min, max;

	Rectangle(){
		min = new Vector2D();
		max = new Vector2D();
	}
	
	Rectangle(Vector2D a, Vector2D b) {
		bounds(a.x, a.y, b.x, b.y);
	}

	Rectangle(float x1, float y1, float x2, float y2) {
		bounds(x1, y1, x2, y2);
	}

	private void bounds(float x1, float y1, float x2, float y2) {
		min = new Vector2D(Math.min(x1, x2), Math.min(y1, y2));
		max = new Vector2D(Math.max(x1, x2), Math.max(y1, y2));
	}
	
	public void set(Vector2D a, Vector2D b) {
		set(a.x, a.y, b.x, b.y);
	}
	
	public void set(float x1, float y1, float x2, float y2) {
		min.x = Math.min(x1, x2);
		min.y = Math.min(y1, y2);
		max.x = Math.max(x1, x2);
		max.y = Math.max(y1, y2);
	}

	public boolean contains(Vector2D p) {
		return p.x > min.x && p.x < max.x && p.y > min.y && p.y < max.y;
	}

	public boolean intersects(Rectangle rec) {
		return min.x < rec.max.x && max.x > rec.min.x && min.y < rec.max.y
				&& max.y > rec.min.y;
	}

	public void transform(Vector2D origin, Vector2D move, float scale) {
		min.transform(origin, move, scale);
		max.transform(origin, move, scale);
	}
}
