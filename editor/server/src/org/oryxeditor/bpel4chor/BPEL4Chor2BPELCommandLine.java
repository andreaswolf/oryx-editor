package org.oryxeditor.bpel4chor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BPEL4Chor2BPELCommandLine {

	//private static Logger log = Logger.getLogger("BPEL4Chor2BPEL");

	/**
	 * Main method
	 * can be used to call the transformation from the command line instead of calling it via Oryx
	 * 
	 * @param argv
	 */
	public static void main(String argv[]) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		
		/*
		File file = new File(".");
		File[] toppology = file.listFiles(new FilenameFilter() {@Override public boolean accept(File dir, String name) {return name.endsWith("topology.xml");}});
		if (toppology.length != 1) {
			throw new Exception("topology error");
		}
		Document docTopo = docBuilder.parse(toppology[0]);

		File[] grounding = file.listFiles(new FilenameFilter() {@Override public boolean accept(File dir, String name) {return name.endsWith("grounding.xml");}});
		if (grounding.length != 1) {
			throw new Exception("groudning error");
		}
		Document docGround = docBuilder.parse(grounding[0]);
		
		File[] pbds = file.listFiles(new FilenameFilter() {@Override public boolean accept(File dir, String name) {return name.endsWith(".bpel");}});
		if (pbds.length == 0) {
			throw new Exception("No PBDs found");
		}
		ArrayList<Document> pbdDocs = new ArrayList<Document>(pbds.length);
		for (File f: pbds) {
			Document res = docBuilder.parse(f);
			pbdDocs.add(res);
		}
		*/
		

		// assumption: working directory is the directory where the files are included
		// This is ensured by the Eclipse Debug configuration -..-> Arguments -> Working directory
		Document docGround = docBuilder.parse("groundingSA.bpel");
		Document docTopo = docBuilder.parse("topologySA.xml");
		Document docPBD = docBuilder.parse("processSA.bpel");

		/*
		Document docGround = docBuilder.parse("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/groundingSA.bpel");
		Document docTopo = docBuilder.parse("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/topologySA.xml");
		Document docPBD = docBuilder.parse("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/processSA.bpel");
		*/
		
		ArrayList<Document> pbdDocs = new ArrayList<Document>();
		pbdDocs.add(docPBD);
		
		BPEL4Chor2BPEL t = new BPEL4Chor2BPEL();
		List<Document> res = t.convert((Element) docGround.getFirstChild(), (Element) docTopo.getFirstChild(), pbdDocs);

		/**************************output of the converted PBD******************************/
		for (Document currentPBD: res) {
			Source sourceBPEL = new DOMSource(currentPBD);
			
			//File bpelFile = new File("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/PBDConvertion.bpel");
			File bpelFile = new File("processSA-with-WSD-info.bpel");
			Result resultBPEL = new StreamResult(bpelFile);
			 
			// Write the converted docPBD to the file
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(sourceBPEL, resultBPEL);
		}
	}
	

	
}
