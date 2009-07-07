package org.oryxeditor.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.hpi.bpmn.BPMNDiagram;
import de.hpi.bpmn.extract.CommonActivities;
import de.hpi.bpmn.extract.CommonMandatoryActivities;
import de.hpi.bpmn.extract.ExtractLargestCommon;
import de.hpi.bpmn.extract.ExtractLowestCommon;
import de.hpi.bpmn.extract.ExtractProcessConfiguration;
import de.hpi.bpmn.extract.exceptions.IsNotWorkflowNetException;
import de.hpi.bpmn.extract.exceptions.NoEndNodeException;
import de.hpi.bpmn.extract.exceptions.NoStartNodeException;
import de.hpi.bpmn.layout.BPMNLayouter;
import de.hpi.bpmn.rdf.BPMN11RDFImporter;
import de.hpi.bpmn.serialization.erdf.BPMNeRDFSerializer;
import de.hpi.util.JsonErdfTransformation;

/**
 * Copyright (c) 2009 Willi Tscheschner
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


public class ExtractCoreProcessServlet extends HttpServlet {
	
	private static final long serialVersionUID = -1;

	private static ServletContext servletContext;
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		servletContext = this.getServletContext();
		
		try {

			String modelA = req.getParameter("modelA");
			String modelB = req.getParameter("modelB");
			String algorithm = req.getParameter("algorithm");

			//CONST.EQUIVALENCE = "equivalence";
			//CONST.ARBITRARY_EQUIVALENCE = "arbitrary";
			//CONST.LOWEST_COMMON = "lowest";
			//CONST.LARGEST_COMMON = "largest";
			//CONST.COMBINED = "combined";
			
			BPMNDiagram extractModel = null;
			try {
				
				if ("combined".equals(algorithm)) {
					extractModel = new ExtractProcessConfiguration(getDiagram(modelA), getDiagram(modelB)).extract();
				} else if ("equivalence".equals(algorithm)) {
					extractModel = new CommonActivities(getDiagram(modelA), getDiagram(modelB)).extract();					
				} else if ("arbitrary".equals(algorithm)) {
					extractModel = new CommonMandatoryActivities(getDiagram(modelA), getDiagram(modelB)).extract();					
				} else if ("largest".equals(algorithm)) {
					extractModel = new ExtractLargestCommon(getDiagram(modelA), getDiagram(modelB)).extract();					
				} else if ("lowest".equals(algorithm)) {
					extractModel = new ExtractLowestCommon(getDiagram(modelA), getDiagram(modelB)).extract();					
				} else {
					//throw new MethodNotFoundException();
				}

				res.setContentType("text/json");
		    	res.setStatus(200);
				res.getWriter().print(getOutputFormat(extractModel, req));
				
			} catch (NoStartNodeException e) {
		    	res.setStatus(404);
				this.printError("One model has no start node.", res.getWriter());
			} catch (NoEndNodeException e) {
		    	res.setStatus(404);
				this.printError("One model has no end node.", res.getWriter());
			} catch (TransformerException e) {
		    	res.setStatus(404);
				this.printError("Model can not transfer to the RDF representation.", res.getWriter());
			} catch (IsNotWorkflowNetException e) {
		    	res.setStatus(404);
				this.printError("Diagram has to be an BPMN diagram which can be mapped to an workflow net.", res.getWriter());
			}
			
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	private void printError(String error, Writer writer){
		
		try {

			JSONObject er = new JSONObject();
			er.put("error", error);
			er.write(writer);
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private String getOutputFormat(BPMNDiagram diagram, HttpServletRequest req){
		
		BPMNeRDFSerializer serializer = new BPMNeRDFSerializer();
    	String eRDF = serializer.serializeBPMNDiagram(diagram);

		try {
			URL serverUrl = new URL( req.getScheme(),
			        req.getServerName(),
			        req.getServerPort(),
			        "" );

			BPMNLayouter layouter = new BPMNLayouter(erdfToJson(eRDF, serverUrl.toString()));
			StringWriter writer = new StringWriter();
			layouter.write(writer);
			
			eRDF = writer.toString().substring(38);
			
	    	return erdfToJson(eRDF, serverUrl.toString());
	    	
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	
	}
	
	private BPMNDiagram getDiagram(String json) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, TransformerException{
	 
		// Get eRDF
		String erdf;
		String rdf;
		if (json.startsWith("\"<")) {
			rdf = json.substring(1, json.length()-2).replace("\\\\", "\\");
		} else {
			erdf = new JsonErdfTransformation(json).toString();
			rdf = erdfToRdf(erdf);
		}
		
		
		DocumentBuilder builder;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(rdf.getBytes("UTF-8")));
		
		
		return new BPMN11RDFImporter(doc).loadBPMN();
		
	}
	
	
	protected static String erdfToRdf(String erdf) throws TransformerException{
		String serializedDOM = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
		"<html xmlns=\"http://www.w3.org/1999/xhtml\" " +
		"xmlns:b3mn=\"http://b3mn.org/2007/b3mn\" " +
		"xmlns:ext=\"http://b3mn.org/2007/ext\" " +
		"xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" "  +
		"xmlns:atom=\"http://b3mn.org/2007/atom+xhtml\">" +
		"<head profile=\"http://purl.org/NET/erdf/profile\">" +
		"<link rel=\"schema.dc\" href=\"http://purl.org/dc/elements/1.1/\" />" +
		"<link rel=\"schema.dcTerms\" href=\"http://purl.org/dc/terms/ \" />" +
		"<link rel=\"schema.b3mn\" href=\"http://b3mn.org\" />" +
		"<link rel=\"schema.oryx\" href=\"http://oryx-editor.org/\" />" +
		"<link rel=\"schema.raziel\" href=\"http://raziel.org/\" />" +
		"</head><body>" + erdf + "</body></html>";
        
		InputStream xsltStream = servletContext.getResourceAsStream("/WEB-INF/lib/extract-rdf.xsl");
        Source xsltSource = new StreamSource(xsltStream);
        Source erdfSource = new StreamSource(new StringReader(serializedDOM));

        TransformerFactory transFact =
                TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);
        StringWriter output = new StringWriter();
        trans.transform(erdfSource, new StreamResult(output));
		return output.toString();
	}
	
	protected static String erdfToJson(String erdf, String serverUrl){
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document rdfDoc = builder.parse(new ByteArrayInputStream(erdfToRdf(erdf).getBytes()));
			return RdfJsonTransformation.toJson(rdfDoc, serverUrl).toString();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}	
}
