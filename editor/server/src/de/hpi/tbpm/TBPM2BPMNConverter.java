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

import static name.audet.samuel.javacv.jna.cxcore.v21.CV_RGB;
import static name.audet.samuel.javacv.jna.highgui.v21.cvLoadImage;
import static name.audet.samuel.javacv.jna.highgui.v21.cvSaveImage;
import static name.audet.samuel.javacv.jna.cxcore.v21.cvReleaseImage;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.oryxeditor.server.diagram.Diagram;

import com.sun.image.codec.jpeg.ImageFormatException;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import name.audet.samuel.javacv.jna.cxcore.IplImage;

public class TBPM2BPMNConverter {

	private ArrayList<PolygonStructure> tasks;
	private ArrayList<PolygonStructure> gateways;
	private ArrayList<PolygonStructure> dataObjects;
	private ArrayList<CircleStructure> events;
	private String rootDir;

	private IplImage image;
	private String imgPath;
	private File img0; // original uploaded picture
	
	private double FACADE_WIDTH = 1300;

	public TBPM2BPMNConverter(String imagePath, File img0, String rootDir) {
		this.rootDir = rootDir;
		this.imgPath = imagePath;
		this.image = cvLoadImage(imagePath, 1);
		this.img0 = img0;

		this.tasks = new ArrayList<PolygonStructure>();
		this.gateways = new ArrayList<PolygonStructure>();
		this.dataObjects = new ArrayList<PolygonStructure>();
		this.events = new ArrayList<CircleStructure>();
	}

	public Diagram convertImage() {

		PolygonFinder polygonFinder = new PolygonFinder(this.imgPath);

		ArrayList<PolygonStructure> rectangles = polygonFinder.findPolygons(4);

		// distinguish between tasks and gateways via square test
		for (int i = 0; i < rectangles.size(); i++) {
			if (rectangles.get(i).isSquare())
				this.gateways.add(rectangles.get(i));

			else
				this.tasks.add(rectangles.get(i));
		}

		this.dataObjects = polygonFinder.findPolygons(5);

		CircleFinder circleFinder = new CircleFinder(this.imgPath);
		this.events = circleFinder.findCircles();

		this.drawShapes();
		
		//this.imgPath = "C:\\Dokumente und Einstellungen\\Helen\\Desktop\\img\\img.png";
		cvSaveImage(this.imgPath, this.image);		
		
		double ratio = this.FACADE_WIDTH / this.image.width;
		scaleImage((int) this.FACADE_WIDTH , (int) (this.image.height * ratio) );

		BPMNBuilder builder = new BPMNBuilder(this.tasks, this.gateways,
				this.dataObjects, this.events, this.rootDir);
		Diagram diagram = builder.buildDiagram();
		// diagram.identifyProcesses();

		cvReleaseImage(this.image.pointerByReference());
		return diagram;
	}

	/**
	 * draw borders around detected shapes
	 */
	public void drawShapes() {

		for (int i = 0; i < this.tasks.size(); i++) {
			this.tasks.get(i).drawShape(this.image, CV_RGB(255, 0, 0));
		}

		for (int i = 0; i < this.gateways.size(); i++) {
			this.gateways.get(i).drawShape(this.image, CV_RGB(0, 255, 0));
		}

		for (int i = 0; i < this.dataObjects.size(); i++) {
			this.dataObjects.get(i).drawShape(this.image, CV_RGB(0, 0, 255));
		}

		for (int i = 0; i < this.events.size(); i++) {
			this.events.get(i).drawShape(this.image, CV_RGB(255, 255, 0));
		}

	}

	public File scaleImage( int pWidth, int pHeight){

		
		BufferedImage image = this.image.getBufferedImage(0.3);

		int thumbWidth = pWidth;
		int thumbHeight = pHeight;

		// Make sure the aspect ratio is maintained, so the image is not skewed
		double thumbRatio = (double) thumbWidth / (double) thumbHeight;
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		double imageRatio = (double) imageWidth / (double) imageHeight;
		if (thumbRatio < imageRatio) {
			thumbHeight = (int) (thumbWidth / imageRatio);
		} else {
			thumbWidth = (int) (thumbHeight * imageRatio);
		}

		// Draw the scaled image
		BufferedImage thumbImage = new BufferedImage(thumbWidth, thumbHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = thumbImage.createGraphics();
		graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2D.drawImage(image, 0, 0, thumbWidth, thumbHeight, null);		
		graphics2D.dispose();
		// Write the scaled image to the outputstream
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
		JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(thumbImage);

		// Use between 1 and 100, with 100 being highest quality
		int quality = 100;
		quality = Math.max(0, Math.min(quality, 100));
		param.setQuality((float) quality / 100.0f, false);
		encoder.setJPEGEncodeParam(param);
		try {
			encoder.encode(thumbImage);
			ImageIO.write(thumbImage, "JPEG", out);
			ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());
			File f = new File( this.imgPath.replaceAll("png", "jpg") );
			
			FileOutputStream fos = new FileOutputStream(f);
			int data;
			while ((data = bis.read()) != -1) {
				char ch = (char) data;
				fos.write(ch);
			}
			fos.flush();
			fos.close();
			
			return f;
		} catch (ImageFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
