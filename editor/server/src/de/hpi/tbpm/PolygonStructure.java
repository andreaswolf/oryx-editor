package de.hpi.tbpm;
/***************************************
 * Copyright (c) 2008
 * Helen Kaltegaertner
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ****************************************/

import static name.audet.samuel.javacv.jna.cxcore.v21.*;

import java.awt.Polygon;

import de.hpi.util.Bounds;



public class PolygonStructure implements ShapeStructure{
	private Polygon polygon;
	private CvPoint[] vertices;
	private int n;
	private int size;
	
	
	public PolygonStructure(Polygon polygon, CvPoint[] vertices, int n) {
		if (vertices.length != n)
			throw new PolygonException("Numer of given vertices is incorrrect!");
		this.polygon = polygon;
		this.vertices = vertices;
		this.n = n;
		this.size = n;
	}
	
	public PolygonStructure(Polygon polygon, int n) {
		super();
		this.polygon = polygon;
		this.n = n;
		this.vertices = new CvPoint[n];
		this.size = 0;
	}
	
	public PolygonStructure(int n) {
		super();
		this.n = n;
		this.size = 0;
		this.vertices = new CvPoint[n];
	}
	
	public void drawShape(IplImage img, CvScalar.ByValue color){
		int[] count = {this.n};
		if ( color == null )
			color = CV_RGB(0,255,0);
		cvDrawPolyLine( img, this.vertices[0].pointerByReference(), count, 1, 1, color, 3, CV_AA, 0 );
		
	}
	
	public Polygon getPolygon() {
		return polygon;
	}
	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}
	public CvPoint[] getVertices() {
		return vertices;
	}
	public void setVertices(CvPoint[] vertices) {
		this.vertices = vertices;
	}
	
	public CvPoint getVertex(int index){
		return this.vertices[index];
	}
	
	public void addVertex(CvPoint vertex){
		if ( this.size == this.n)
			throw new PolygonException("Number of specified vertices exceeded for this polygon!");
		this.vertices[this.size++] = vertex;
	}
	
	/**
	 * checks if two adjacent edges are of equal length
	 * returns false if the polygon does not have exactly 4 vertices
	 * @return
	 */
	public boolean isSquare(){
		if (n != 4)
			return false;
		CvPoint p0 = this.vertices[0];
		CvPoint p1 = this.vertices[1];
		CvPoint p2 = this.vertices[2];
		double d1 = Math.sqrt( Math.pow(p0.x - p1.x, 2) + Math.pow(p0.y - p1.y, 2) );
		double d2 = Math.sqrt( Math.pow(p2.x - p1.x, 2) + Math.pow(p2.y - p1.y, 2) );
		double ratio = d1/d2;
		if ( ratio > 0.8 && ratio < 1.2)
			return true;
		return false;
	}
	
	public int[] getCoordinates(){
		int[] coordinates = new int[2];
		coordinates[0] = (int) this.polygon.getBounds2D().getMinX();
		coordinates[1] = (int) this.polygon.getBounds2D().getMinY();
		return coordinates;
	}
	
	public int getX(){
		return (int) this.polygon.getBounds2D().getMinX();
	}
	
	public Bounds getBounds( double ratio ) {
		String[] bounds = new String[4];
		bounds[0] = ( new Float(this.polygon.getBounds2D().getMinX() * ratio) ).toString();
		bounds[1] = ( new Float(this.polygon.getBounds2D().getMinY() * ratio) ).toString();
		bounds[2] = ( new Float(this.polygon.getBounds2D().getMaxX() * ratio) ).toString();
		bounds[3] = ( new Float(this.polygon.getBounds2D().getMaxY() * ratio) ).toString();
		return new Bounds(bounds);
	}

}
