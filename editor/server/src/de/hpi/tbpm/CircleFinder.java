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

import static name.audet.samuel.javacv.jna.cxcore.cvClearMemStorage;
import static name.audet.samuel.javacv.jna.cxcore.v21.*;
import static name.audet.samuel.javacv.jna.cv.v21.*;
import static name.audet.samuel.javacv.jna.cvaux.v21.*;
import static name.audet.samuel.javacv.jna.highgui.v21.*;
import name.audet.samuel.javacv.*;
import com.sun.jna.Native;

import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashSet;

public class CircleFinder {
	private IplImage img0;
	private IplImage img;

	private static int thresh = 50;
	private CvMemStorage storage;
	private ArrayList<CircleStructure> circles;

	public CircleFinder(String imgSource) {
		this.img0 = cvLoadImage(imgSource, 1);
		if (this.img0 == null) {
			System.out.println(imgSource);
		}
		this.img = cvCloneImage(this.img0);

		this.storage = cvCreateMemStorage(0);
		this.circles = new ArrayList<CircleStructure>();
	}

	public ArrayList<CircleStructure> findCircles() {
		
		double edgeThresh = 1;

		IplImage gray = cvCreateImage(cvGetSize(this.img), 8, 1);
		IplImage edge = cvCreateImage(cvGetSize(this.img), 8, 1 );
		IplImage tgray = cvCreateImage(cvGetSize(this.img), 8, 1);
		IplImage timg = cvCloneImage( this.img0 );
		
		cvSetImageCOI(timg, 1);
		cvCopy(timg, tgray, null);

		//cvCvtColor(this.img, gray, CV_BGR2GRAY);
		cvThreshold(tgray, gray, 100, 255, CV_THRESH_BINARY);
		cvSmooth( gray, gray, CV_GAUSSIAN, 11, 11, 0, 0 );
		cvCanny(gray, edge, edgeThresh, edgeThresh * 3, 5);
	
//		CanvasFrame canvas = new CanvasFrame(false, "pic");
//		canvas.showImage( gray );
		
		CvSeq results = cvHoughCircles(gray, this.storage.getPointer(),
				CV_HOUGH_GRADIENT, 1, 5, 5, 35, 1, 100000);

		float[] p;
		CvPoint center;
		for (int i = 0; i < results.total; i++) {
			p = cvGetSeqElem(results, i).getFloatArray(0, 3);
			center = cvPoint(Math.round(p[0]), Math.round(p[1]));
			this.circles.add(new CircleStructure(center, p[2]));
		}

		// release all the temporary images
		cvReleaseImage(gray.pointerByReference());
		cvReleaseImage(edge.pointerByReference());
		cvReleaseImage(tgray.pointerByReference());
		cvReleaseImage(timg.pointerByReference());
		
		System.out.println("circles total: " + this.circles.size());
		this.filterRedundancy();
		System.out.println("circles filtered: " + this.circles.size());
		
		// clear memory storage - reset free space position
		cvClearMemStorage(this.storage);
		
		return this.circles;

	}
	
	/**
	 * Open Image in pop up using canvas facility
	 */
	public void showCanvas(){
		CanvasFrame canvas = new CanvasFrame("pic");
		canvas.showImage( this.img );

		// release both image
		cvReleaseImage(this.img.pointerByReference());
		cvReleaseImage(this.img0.pointerByReference());
	}

	/**
	 * removes overlapping circles that were found due to
	 * applying several filters
	 * @param n
	 */
	private void filterRedundancy() {
		for ( int i = 0; i < this.circles.size() - 1; i++ ) {
			ArrayList<CircleStructure> duplicates = new ArrayList<CircleStructure>();
			CircleStructure c1 = this.circles.get(i);
			// instantiate Ellipse with width = height to have a circle
			Ellipse2D.Double e1 = new Ellipse2D.Double(c1.getCenter().x, c1.getCenter().y, 
					c1.getRadius(), c1.getRadius());
			// check for all other circles if their center is enclosed by th i-th circle))
			for ( int j = i+1; j < this.circles.size(); j++) {
				CircleStructure c2 = this.circles.get(j);
				// instantiate Ellipse with width = height to have a circle
				Ellipse2D.Double e2 = new Ellipse2D.Double(c2.getCenter().x, c2.getCenter().y, 
						c2.getRadius(), c2.getRadius());
				if (e1.intersects(e2.getBounds2D()))
					duplicates.add(c2);
			}
			this.circles.removeAll(duplicates);
		}

	}
}
