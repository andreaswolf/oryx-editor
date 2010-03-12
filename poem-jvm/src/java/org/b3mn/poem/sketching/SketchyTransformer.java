package org.b3mn.poem.sketching;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.apache.batik.dom.svg.*;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.util.*;
import org.apache.batik.svggen.*;
import org.apache.batik.dom.GenericCDATASection;
import org.apache.fop.svg.PDFTranscoder;

public class SketchyTransformer extends PDFTranscoder{

	private SVGDocument doc;
	private SVGSVGElement root;
	private SVGGeneratorContext ctx;
	private CSSStyleHandler styleHandler;
	private CDATASection styleSheet; 
	private String fontSize = "15px";
	private boolean createPDF;
	private OutputStream out;

	public SketchyTransformer(InputStream in, OutputStream out, boolean createPDF) {
		try {
			this.createPDF = createPDF;
			this.out = out;
			String parser = XMLResourceDescriptor.getXMLParserClassName();
			SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
			//this.doc = f.createSVGDocument(svg);
			this.doc = f.createSVGDocument(null, in);
			this.root = (SVGSVGElement) this.doc.getDocumentElement();

			this.ctx = SVGGeneratorContext.createDefault(this.doc);
			this.ctx.setEmbeddedFontsOn(true);
			this.styleSheet = this.doc.createCDATASection("");
			this.styleHandler = new CSSStyleHandler(this.styleSheet, this.ctx);
			this.ctx.setStyleHandler(this.styleHandler);
			

		} catch (IOException e) {
			// TODO: handle exception
		}
		
		this.createArrowEnd();
	}

	public SVGDocument getDoc() {
		return doc;
	}

	public void setDoc(SVGDocument doc) {
		this.doc = doc;
	}

	public SVGSVGElement getRoot() {
		return root;
	}

	public void setRoot(SVGSVGElement root) {
		this.root = root;
	}
	
	public SVGGeneratorContext getCtx() {
		return ctx;
	}

	public void setCtx(SVGGeneratorContext ctx) {
		this.ctx = ctx;
	}

	public CSSStyleHandler getStyleHandler() {
		return styleHandler;
	}

	public void setStyleHandler(CSSStyleHandler styleHandler) {
		this.styleHandler = styleHandler;
	}

	public CDATASection getStyleSheet() {
		return styleSheet;
	}

	public void setStyleSheet(CDATASection styleSheet) {
		this.styleSheet = styleSheet;
	}
	
	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isCreatePDF() {
		return createPDF;
	}

	public void setCreatePDF(boolean createPDF) {
		this.createPDF = createPDF;
	}
	
	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public void transform() throws IOException, TranscoderException{
		this.transformConnectors();
		this.transformRectangles();
		this.transformEllipses();
		this.setFont("PapaMano AOE", "18px");
		this.produceOutput();
	}

	public void produceOutput() throws IOException, TranscoderException{
		this.applyStyles();

		// Create an instance of the SVG Generator.
		SVGGraphics2D svgGenerator = new SVGGraphics2D(this.doc);
		boolean useCSS = true;
		boolean escaped = false;
		// Setup output
		OutputStream dest;
		if (this.createPDF)
			dest = new ByteArrayOutputStream();
		else 
			dest = new FileOutputStream("outcome.svg");
		Writer outWriter = new OutputStreamWriter(dest, "UTF-8");
		
		try {
			// stream to output
			svgGenerator.stream(this.root, outWriter, useCSS, escaped);
			outWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (this.createPDF)
			this.exportPdf( ((ByteArrayOutputStream)dest).toByteArray());
	}
	
	private void exportPdf(byte[] svg) throws IOException, TranscoderException{		
		
		// required for pdf creation otherwise default font-size is selected
		NodeList texts = this.doc.getElementsByTagName("tspan");
		for (int i = 0; i < texts.getLength(); i++)
			((Element)texts.item(i)).setAttribute("font-size", "15px");
		
		PDFTranscoder transcoder = new PDFTranscoder();
		try {
			// setup input
			InputStream in 			= new ByteArrayInputStream(svg);
			TranscoderInput input 	= new TranscoderInput(in);
	    	//Setup output
			TranscoderOutput output	= new TranscoderOutput(this.out); 
			
	    	try {
		    	// apply transformation
				transcoder.transcode(input, output);
	    	} finally {
	    		this.out.close();
				in.close();
	    	}
		} finally {}
	}

	private void applyStyles() {
		//this.doc.normalizeDocument();
		// append CDATASection for CSS styles
		Element defs = (Element)this.root.getElementsByTagName("defs").item(0);
		Element style = this.doc.createElementNS(SVGSyntax.SVG_NAMESPACE_URI, SVGSyntax.SVG_STYLE_TAG);
		style.setAttributeNS(null, SVGSyntax.SVG_TYPE_ATTRIBUTE, "text/css");
		style.appendChild(this.styleSheet);
		defs.appendChild(style);
	}
	
	private void createArrowEnd(){
		
		Element marker = this.doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "marker");
		marker.setAttribute("id", "oryx_arrow");
		marker.setAttribute("refX", "7");
		marker.setAttribute("refY", "6");
		marker.setAttribute("markerWidth", "7");
		marker.setAttribute("markerHeight", "12");
		marker.setAttribute("orient", "auto");
		
		Element path = this.doc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI, "path");
		path.setAttribute("d", "M 0 0 L 7 6 L 0 10");
		
		Map<String, String> styles = new HashMap<String, String>();
		styles.put("fill", "none");
		styles.put("stroke", "black");
		styles.put("stroke-width", "1");
		this.styleHandler.setStyle(path, styles);
		
		marker.appendChild(path);
		
		this.root.getFirstChild().appendChild(marker);

	}
	
	public void transformConnectors() {
		NodeList paths = this.doc.getElementsByTagName("path");
		for (int i = 0; i < paths.getLength(); i++) {
			Element e = (Element) paths.item(i);
			if (!((Element) e.getParentNode()).getAttribute("display").equals("none")
					&& !e.getParentNode().getNodeName().equals("marker")
					&& !((Element)e.getParentNode()).hasAttribute("oryx:anchors")) {
				
				SVGPath path = new SVGPath(e, this.doc, this.styleHandler);
				path.transform();
			}
		}
	}

	public void transformRectangles() {
		NodeList rectangles = this.doc.getElementsByTagName("rect");
		HashMap<Element, Element> replaceMap = new HashMap<Element, Element>();

		for (int i = 0; i < rectangles.getLength(); i++) {
			Element e = (Element) rectangles.item(i);
			if (e.getAttribute("stroke").equals("none"))
				continue;
			SVGRectangle rect = new SVGRectangle(e, this.doc, this.styleHandler);
			replaceMap.put(e, rect.transform());
		}

		// replace rectangles by sketchy paths
		for (Element rect : replaceMap.keySet()){
			if (replaceMap.get(rect) != null)
				rect.getParentNode().replaceChild(replaceMap.get(rect), rect);
		}
	}
	
	public void transformEllipses() {
		HashMap<Element, ArrayList<Element>> replaceMap = new HashMap<Element, ArrayList<Element>>();
		
		NodeList circles = this.doc.getElementsByTagName("circle");
		for (int i = 0; i < circles.getLength(); i++) {
			Element e = (Element) circles.item(i);
			if (e.getAttribute("stroke").equals("none"))
				continue;
			SVGEllipse ellipse = new SVGEllipse(e, this.doc, this.styleHandler);
			replaceMap.put(e, ellipse.transform());
		}
		
		NodeList ellipses = this.doc.getElementsByTagName("ellipse");
		for (int i = 0; i < ellipses.getLength(); i++) {
			Element e = (Element) ellipses.item(i);
			if (e.getAttribute("stroke").equals("none"))
				continue;
			SVGEllipse ellipse = new SVGEllipse(e, this.doc, this.styleHandler);
			replaceMap.put(e, ellipse.transform());
		}

		// replace rectangles by sketchy paths
		for (Element ellipse : replaceMap.keySet()){
			if (replaceMap.get(ellipse) != null){
				// insert stroke before old element
				ellipse.getParentNode().insertBefore(replaceMap.get(ellipse).get(1), ellipse);
				// replce old element by new ellipse
				ellipse.getParentNode().replaceChild(replaceMap.get(ellipse).get(0), ellipse);
			}
		}
	}

	public void setFont(String font, String size) {
		
		this.fontSize = size;
		((Element)this.root.getLastChild()).setAttribute("font-family", font);
		((Element)this.root.getLastChild()).setAttribute("font-size", size);
		
		Map<String, String> styles = new HashMap<String, String>();
		styles.put("font-size", size);
		
		if (this.createPDF) {
			NodeList texts = this.doc.getElementsByTagName("tspan");
			for (int i = 0; i < texts.getLength(); i++)
				this.styleHandler.setStyle((Element) texts.item(i), styles);
		}
		
		
	}

}
