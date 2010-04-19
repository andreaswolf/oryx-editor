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

package org.b3mn.poem.sketching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.*;
import org.apache.batik.dom.svg.*;

public class SVGEllipse {
	
	private CSSStyleHandler styleHandler;
	private Element circle;
	private Double x;
	private Double y;
	private Double radius;
	private Document doc;
	
	public SVGEllipse(Element e, Document doc, CSSStyleHandler styleHandler){
		java.util.Locale.setDefault(java.util.Locale.US);
		
		this.styleHandler 	= styleHandler;
		this.circle 		= e;
		this.doc 			= doc;
		this.x 				= Double.parseDouble(e.getAttribute("cx"));
		this.y 				= Double.parseDouble(e.getAttribute("cy"));
		
		if (this.circle.getNodeName().equals("circle"))
			this.radius = Double.parseDouble(this.circle.getAttribute("r"));
		else 
			this.radius = (Double.parseDouble(e.getAttribute("rx")) + Double.parseDouble(e.getAttribute("ry"))) * 0.5;
	}
	
	public ArrayList<Element> transform(){
		// skip too small circles/ellipses
		if (this.radius <= 5)
			return null;

		ArrayList<Element> elements = new ArrayList<Element>();
		
		Element ellipse = this.doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "ellipse");
		ellipse.setAttribute("cx", this.x.toString());
		ellipse.setAttribute("cy", this.y.toString());
		double variance = Math.random() * 2 + 2;	
		ellipse.setAttribute("rx", ((Double)(this.radius + variance)).toString());
		ellipse.setAttribute("ry", ((Double)(this.radius - variance/2)).toString());		
		elements.add(ellipse);
		
		elements.add(this.addStroke(this.radius - variance/2));
		
		this.changeStyle(ellipse);

		return elements;
	}
	
	private Element addStroke(double ry){
		Element path = this.doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
		Map<String, String> styles = new HashMap<String, String>();
		styles.put("fill", "none");
		styles.put("stroke-width", "2");
		styles.put("stroke", "black");
		styles.put("stroke-linejoin", "round");
		
		this.styleHandler.setStyle(path, styles);
		
//		path.setAttribute("d", String.format("M %.2f %.2f Q %.2f %.2f %.2f %.2f", 
//				this.x - 5, this.y - ry, this.x - 15, this.y - ry, this.x - 20, this.y - ry + 5));
//		
		return path;
	}
	
	private void changeStyle(Element ellipse) {
		
		Map<String, String> styles = new HashMap<String, String>();
		styles.put("fill", "white");
		if ( ((Element)this.circle.getParentNode()).getAttribute("title").equals("End Event"))
			styles.put("stroke-width", "4");
		else 
			styles.put("stroke-width", "2");
		
		this.styleHandler.setStyle(ellipse, styles);
	}
}
