package org.b3mn.poem.sketching;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.*;
import org.apache.batik.dom.svg.*;

public class SVGRectangle {
	
	private CSSStyleHandler styleHandler;
	private Element rectangle;
	private double x;
	private double y;
	private double height;
	private double width;
	private Document doc;
	private static int DISTANCE = 30;
	
	public SVGRectangle(Element e, Document doc, CSSStyleHandler styleHandler){
		java.util.Locale.setDefault(java.util.Locale.US);
		
		this.styleHandler 	= styleHandler;
		this.rectangle 		= e;
		this.doc 			= doc;
		this.x 				= Double.parseDouble(e.getAttribute("x"));
		this.y 				= Double.parseDouble(e.getAttribute("y"));
		this.height 		= Double.parseDouble(e.getAttribute("height"));
		this.width 			= Double.parseDouble(e.getAttribute("width"));
		
	}
	
	public void changeColor(String color){
		//this.rectangle.getAttributes().getNamedItem("fill").setNodeValue(color);
		((Element) this.rectangle).setAttribute("fill", color);
	}
	
	public Element transform(){
		if ((this.width < 30 && this.height < 30)
				|| this.rectangle.getAttribute("display").equals("none"))
			return null;
		// enable for round corners
		boolean round = this.rectangle.hasAttribute("rx");
		double radius = round ? Double.parseDouble(this.rectangle.getAttribute("rx")) : 0;
		if (radius <= 10)
			round = false;
		
		Element path = this.doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
		path.setAttribute("display", this.rectangle.getAttribute("display"));
		String d = "";
		
		if (round)
			d += String.format("M %.2f 0", radius);
		else if (Math.random() > 0.5)	// crossing edge	
			d += "M 0 0 ";
		else
			d += "M -5 0 ";
		
		int distX = (this.width < 200) ? DISTANCE : 100;
		int distY = (this.height < 200) ? DISTANCE : 100;
		
		// top left to top right
		d += this.topLine(round, radius, distX);
		
		// top right to bottom right
		d += rightLine(round, radius, distY);
		
		// bottom right to bottom left
		d += bottomLine(round, radius, distX);
		
		// bottom left to top left
		d += leftLine(round, radius, distY);
		
		//System.out.println(d);
		path.setAttribute("d", d);
		
		this.changeStyle(path);
		
		return path;
	}

	private String leftLine(boolean round, double radius, int distY) {
		double destX	= 0;
		double refX		= 0;
		String d = "";
		for (double y = this.height - distY; y > (round ? radius : 0); y -= distY){
			// less variance for destination than for the reference
			destX 	+= (destX < 0) ? Math.random() : -Math.random();
			refX 	+= (refX < 0) ? Math.random()*3 : -Math.random()*3;
			
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", refX + this.x, y + 15 + this.y, destX + this.x, y + this.y);
		}
		if (round)
			d += String.format("L %.2f, %.2f Q %.2f, %.2f, %.2f, %.2f", 
					this.x, this.y + 15, this.x, this.y, this.x + 20, this.y + 3);
		else
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", this.x, this.y, this.x, 
				this.y - ((this.height < 200 && Math.random() < 0.5) ? 5 : 0) );
		return d;
	}

	private String bottomLine(boolean round, double radius, int distX) {
		double destY 	= this.height;
		double refY  	= this.height;
		String d = "";
		for (double x = this.width - distX; x > (round ? radius : 0); x -= distX){
			// less variance for destination than for the reference
			destY 	+= (destY < this.height) ? Math.random() : -Math.random();
			refY 	+= (refY < this.height) ? Math.random()*3 : -Math.random()*3;
			
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", x + 15 + this.x, refY + this.y, x + this.x, destY + this.y);
		}
		if (round)
			d += String.format("L %.2f, %.2f Q %.2f, %.2f, %.2f, %.2f", 
					this.x + radius, this.y + this.height, this.x, this.y + this.height, this.x, this.y + this.height - radius);
		else if (this.width < 30 || this.width > 200 || Math.random() > 0.5)	// crossing edge
			d += d += String.format("L %.2f, %.2f M %.2f %.2f ", this.x - 5, this.height + this.y, this.x, this.height + this.y - 5);
		else	// round edge
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", this.x, this.height + this.y, this.x, this.height + this.y - 5);
		return d;
	}

	private String rightLine(boolean round, double radius, int distY) {
		double destX	= this.width;
		double refX		= this.width;
		String d = "";
		for (double y = distY; y < (round ? this.height - radius : this.height); y += distY){
			// less variance for destination than for the reference
			destX 	+= (destX < this.width) ? Math.random() : -Math.random();
			refX 	+= (refX < this.width) ? Math.random()*3 : -Math.random()*3;
			
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", refX + this.x, y - 15 + this.y, destX + this.x, y + this.y);
		}
		if (round)
			d += String.format("L %.2f, %.2f Q %.2f, %.2f, %.2f, %.2f", 
					this.width + this.x, this.y + this.height - radius, this.width + this.x, this.y + this.height, this.width + this.x - radius, this.y + this.height);
		else if (this.height < 30 || this.height > 200 || Math.random() < 0.3)		//crossing edge
			d += String.format("L %.2f, %.2f M %.2f %.2f ", this.width + this.x, this.height + this.y + 5, this.width + this.x + 5, this.height + this.y);
		else	// round edge
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", this.width + this.x, this.y + this.height, this.width + this.x - 5, this.y + this.height);
		return d;
	}

	private String topLine(boolean round, double radius, int distX) {
		double destY 	= 0;
		double refY  	= 0;
		String d = "";
		for (double x = distX; x < (round ? this.width - radius : this.width); x += distX){
			// less variance for destination than for the reference
			destY 	+= (destY < 0) ? Math.random() : -Math.random();
			refY 	+= (refY < 0) ? Math.random()*3 : -Math.random()*3;
			
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", x - 15 + this.x, refY + this.y, x + this.x, destY + this.y);
		}
		if (round)
			d += String.format("L %.2f, %.2f Q %.2f, %.2f, %.2f, %.2f", 
					this.width + this.x - radius, this.y, this.width + this.x, this.y, this.width + this.x, this.y + radius);
		else if (this.width < 30 || this.width > 200 || Math.random() < 0.3)	// crossing edge
			d += String.format("L %.2f, %.2f M %.2f %.2f ", this.width + this.x + 5, this.y, this.width + this.x, this.y - 5);
		else //round edge
			d += String.format("Q %.2f, %.2f, %.2f, %.2f ", this.width + this.x, this.y, this.width + this.x, this.y + 5);
		return d;
	}
	
	private void changeStyle(Element path) {
		
		Map<String, String> styles = new HashMap<String, String>();
		styles.put("fill", "white");
		styles.put("stroke-width", "2");
		
		this.styleHandler.setStyle(path, styles);
		
		if (path.getAttribute("display").equals(""))
			path.removeAttribute("display");
	}

	public CSSStyleHandler getStyleHandler() {
		return styleHandler;
	}

	public void setStyleHandler(CSSStyleHandler styleHandler) {
		this.styleHandler = styleHandler;
	}

	public Element getRectangle() {
		return rectangle;
	}

	public void setRectangle(Element rectangle) {
		this.rectangle = rectangle;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}	
	

}
