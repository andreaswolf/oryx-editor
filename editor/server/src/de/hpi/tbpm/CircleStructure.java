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

package de.hpi.tbpm;
import static name.audet.samuel.javacv.jna.cxcore.v21.*;
import de.hpi.util.Bounds;

public class CircleStructure implements ShapeStructure{
	
	private CvPoint center;
	private double radius;
	
	public CircleStructure(CvPoint center, double radius) {
		super();
		this.center = center;
		this.radius = radius;
	}
	
	public CircleStructure(CvPoint center) {
		super();
		this.center = center;
	}
	
	public void drawShape(IplImage img, CvScalar.ByValue color){
		if (color == null)
			color = CV_RGB(0,255,0);
		cvDrawCircle(img, this.center.byValue(), (int)Math.round(this.radius), color, 3, CV_AA, 0);
	}

	public CvPoint getCenter() {
		return center;
	}
	public void setCenter(CvPoint center) {
		this.center = center;
	}
	public double getRadius() {
		return radius;
	}
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public int[] getCoordinates(){
		int[] coordinates = new int[2];
		coordinates[0] = this.center.x - (int) this.radius;
		coordinates[1] = this.center.y - (int) this.radius;
		return coordinates;
	}
	
	public int getX(){
		return this.center.x - (int) this.radius;
	}
	public Bounds getBounds( double ratio ) {
		String[] bounds = new String[4];
		bounds[0] = ( new Float( (this.center.x - this.radius) * ratio) ).toString();
		bounds[1] = ( new Float( (this.center.y - this.radius) * ratio) ).toString();
		bounds[2] = ( new Float( (this.center.x + this.radius) * ratio) ).toString();
		bounds[3] = ( new Float( (this.center.y + this.radius) * ratio) ).toString();
		return new Bounds(bounds);
	}
}
