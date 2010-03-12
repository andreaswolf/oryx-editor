package org.b3mn.poem.sketching;

public class PathSection {
	
	private String type;
	private double[] point;
	
	public PathSection(String type, double[] point) {
		this.type = type;
		this.point = point;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double[] getPoint() {
		return point;
	}

	public void setPoint(double[] point) {
		this.point = point;
	}
	
	public double getX() {
		return point[0];
	}
	
	public double getY() {
		return point[1];
	}
	
	public void setX(double x) {
		this.point[0] = x;
	}
	
	public void setY(double y) {
		this.point[1] = y;
	}
	

}
