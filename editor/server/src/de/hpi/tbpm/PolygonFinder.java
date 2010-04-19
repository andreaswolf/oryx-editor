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
import static name.audet.samuel.javacv.jna.cv.v21.*;
import static name.audet.samuel.javacv.jna.cvaux.v21.*;
import static name.audet.samuel.javacv.jna.highgui.v21.*;
import name.audet.samuel.javacv.*;

import com.sun.jna.Native;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashSet;

public class PolygonFinder {

	private IplImage img0;
	private IplImage img;

	private static int thresh = 50;
	private CvMemStorage storage;
	private ArrayList<PolygonStructure> polygons;

	public PolygonFinder(String imgSource) {
		this.img0 = cvLoadImage(imgSource, 1);
		if (this.img0 == null) {
			System.out.println(imgSource);
		}		
		this.storage = cvCreateMemStorage(0);
		this.img = cvCloneImage(this.img0);
	}


	/**
	 * finds cosine of angle between vectors pt0->pt1 and from pt0->pt2
	 * @param pt1
	 * @param pt2
	 * @param pt0
	 * @return
	 */
	private double angle(CvPoint pt1, CvPoint pt2, CvPoint pt0) {
		double dx1 = pt1.x - pt0.x;
		double dy1 = pt1.y - pt0.y;
		double dx2 = pt2.x - pt0.x;
		double dy2 = pt2.y - pt0.y;
		
		return (dx1 * dx2 + dy1 * dy2)
			/ Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10 );
		
	}

	/**
	 * Finds polygons with the specified number of vertices
	 * @param n
	 */
	public ArrayList<PolygonStructure> findPolygons(int n) {
		int N = 3;
		CvSize imgSize = cvSize(this.img.width & -2, this.img.height & -2);

		IplImage timg 	= cvCloneImage( this.img0 ); // make a copy of input image
		IplImage gray 	= cvCreateImage( imgSize.byValue(), 8, 1);
		IplImage pyr 	= cvCreateImage(cvSize(imgSize.width / 2, imgSize.height / 2), 8, 3);
		this.polygons	= new ArrayList<PolygonStructure>();
		
		cvSetImageROI(timg, cvRect(0, 0, imgSize.width, imgSize.height));

		// down-scale and upscale the image to filter out the noise
		cvPyrDown(timg, pyr, 7);
		cvPyrUp(pyr, timg, 7);
		IplImage tgray = cvCreateImage(imgSize.byValue(), 8, 1);

		// find squares in every color plane of the image
		for (int c = 0; c < 3; c++) {
			// extract the c-th color plane
			cvSetImageCOI(timg, c + 1);
			cvCopy(timg, tgray, null);			
			// try several threshold levels
			for (int l = 1; l < N; l++) {		
				
				//cvCopy(timg, tgray, null);
				// System.out.println("c: " + c + " l: " + l);
				// hack: use Canny instead of zero threshold level.
				// Canny helps to catch squares with gradient shading
				if ( l == 0) {
					cvCanny(tgray, gray, 0, this.thresh, 5);

					// dilate canny output to remove potential
					// holes between edge segments
					cvDilate(gray, gray, null, 1);
				} else {
					cvThreshold(tgray, gray, (l) * 255 / 10, 255,
							CV_THRESH_BINARY);
				}

				processContours(n, gray);
			}
		}

		// release all the temporary images
		cvReleaseImage(gray.pointerByReference());
		cvReleaseImage(pyr.pointerByReference());
		cvReleaseImage(tgray.pointerByReference());
		cvReleaseImage(timg.pointerByReference());
		
		System.out.println(this.polygons.size());
		this.filterOuterBorder(n);
		this.filterRedundancy(n);
		System.out.println(this.polygons.size());
		
		// clear memory storage - reset free space position
		cvClearMemStorage(this.storage);
		
		return this.polygons;

	}

	/**
	 * Collects contours of current filter modi via cvFindCountours
	 * checks if they have the right number of edges and are convex
	 * @param n
	 * @param gray
	 */
	private void processContours(int n, IplImage gray) {
		
		CvSeq.PointerByReference contoursPointer = new CvSeq.PointerByReference();
		cvFindContours(gray, this.storage, contoursPointer, 
				Native.getNativeSize(CvContour.ByValue.class), CV_RETR_LIST,
				CV_CHAIN_APPROX_SIMPLE, cvPoint(0, 0));
		CvSeq contours = contoursPointer.getStructure();
		System.out.println("contours: " + contours.total);
		// test each contour
		int count = contours.total;
		while ( count > 0 ) {
			
			CvSeq result = cvApproxPoly(contours.getPointer(),
					CV_SEQ_CONTOUR, this.storage, CV_POLY_APPROX_DP,
					cvContourPerimeter(contours.getPointer()) * 0.02, 0);
			
			// approximation relatively large area (filter out noisy contours)
			if ((result.total == n)
					&& Math.abs(cvContourArea(result, CV_WHOLE_SEQ, 0)) > 1000
					&& cvCheckContourConvexity(result) != 0) {

				double s = 0;

				for (int j = 0; j < result.total; j++) {
					// find minimum angle between joint
					if (j >= 2) {
						CvPoint p1 = new CvPoint(cvGetSeqElem(result, j));
						CvPoint p2 = new CvPoint(cvGetSeqElem(result, j - 2));
						CvPoint p3 = new CvPoint(cvGetSeqElem(result, j - 1));
						double t = Math.abs(angle(p1, p2, p3));
						s = s > t ? s : t;
					}
				}
				// if cosines of all angles are small
				// (all angles are ~90 degree) then write quandrange
				// vertices to resultant sequence
				//if (s < 0.8) {
				if (true) {
					Polygon p = new Polygon();
					CvPoint[] vertices = new CvPoint[n];
					for (int j = 0; j < result.total; j++){
						CvPoint v = new CvPoint(cvGetSeqElem(result, j));
						p.addPoint(v.x, v.y);
						vertices[j] = v;
					}
					this.polygons.add( new PolygonStructure(p, vertices, n) );
				}
			}

			// take the next contour
			contours = contours.h_next;
			count--;
			if (contours == null)
				break;
		}
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
	 * Removes Polygons at the border of the image (outer frame)
	 * @param n
	 */
	private void filterOuterBorder(int n){
		
		HashSet<PolygonStructure> borders = new HashSet<PolygonStructure>();
		for ( int i = 0; i < this.polygons.size(); i++ ){
			if ( this.polygons.get(i).getPolygon().getBounds2D().getHeight() > this.img.height - 50 ){
				borders.add(this.polygons.get(i));
			}
		}
		this.polygons.removeAll( borders );
	}
	
	/**
	 * removes overlapping polygons that were found due to
	 * trying several thresholds and colors 
	 * @param n
	 */
	private void filterRedundancy(int n){
		// remove rectangle at the border of the image
		HashSet<PolygonStructure> duplicates = new HashSet<PolygonStructure>();
		for ( int i = 0; i < this.polygons.size() - 1; i++ ){
			duplicates = new HashSet<PolygonStructure>();
			// check for all remaining polygons if they include one of the vertices 
			// or the center of the i-th polygon
			for ( int j = i + 1; j < this.polygons.size(); j++ ){

				if ( this.polygons.get(i).getPolygon().intersects( this.polygons.get(j).getPolygon().getBounds2D() ) )
					duplicates.add( this.polygons.get(j) );
			}
			// remove duplicates from list of polygons and from list of polygon vertices
			this.polygons.removeAll( duplicates );
			
		}
	
	}

}
