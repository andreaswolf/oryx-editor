package org.oryxeditor.bpel4chor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Copyright (c) 2009-2010 
 * 
 * Changhua Li
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


/**
 * !!!!!!Attention!!!!!!
 * Now this files works isolated from the other files, which outside of this directory.
 * But it should be added into oryx as a plugin in the further.
 * 
 * It will be used for the Transformation of the BPEL4Chor to BPEL.
 * 
 * It was designed for the Diplom Arbeit of Changhua Li(student of uni. stuttgart), 
 * It is the creation of WSDL file of BPEL, which was designed in the Studien Arbeit
 * of Peter Reimann(2008)
 */

public class BPEL4Chor2BPELWSDLCreate extends BPEL4Chor2BPELPBDConversion{
	
	public Document currentDocument;
	public Set<String> nsprefixSet;
	
	final static String EMPTY = "";
	public Set<String> messageLinkSet = new HashSet<String>();

	// 3.20: fportTypeMC()
	public HashMap<String, String> ml2ptMap = new HashMap<String, String>();

	// 3.21: foperationMC()
	public HashMap<String, String> ml2opMap = new HashMap<String, String>();

	// 3.22: partnerLink Set
	public Set<String> plSet = new HashSet<String>();

	// 3.23: messageConstruct --> partnerLink Mapping
	public HashMap<String, PartnerLink> mc2plMap = new HashMap<String, PartnerLink>();

	// 3.24: scope --> partnerLinkSet Mapping
	public HashMap<String, Set<PartnerLink>> sc2plMap = new HashMap<String, Set<PartnerLink>>();

	// 3.25: partnerLinkType Set
	public Set<String> plTypeSet = new HashSet<String>();

	// 3.26: partnerLink --> partnerLinkType
	public HashMap<String, String> pl2plTypeMap = new HashMap<String, String>();

	// 3.29: communication --> partnerLinkType
	public HashMap<Comm, String> comm2pltMap = new HashMap<Comm, String>();

	// 3.30: partnerLink --> myRole 
	public HashMap<String, String> pl2myRoleMap = new HashMap<String, String>();

	// 3.31: partnerLink --> partnerRole
	public HashMap<String, String> pl2partnerRoleMap = new HashMap<String, String>();

	// for function 3.11 fscopePa
	public HashMap<String, Object> pa2scopeMap = new HashMap<String, Object>();

	// used by 3.34
	public HashMap<String, String> corrPropName2propertyMap = new HashMap<String, String>();

	// used by 3.35
	public HashMap<String, String> property2nsprefixOfPropMap = new HashMap<String, String>();

	// used by 3.36
	public Set<String> namespaceSet = new HashSet<String>();

	// 3.2: record all name space prefixes of QName
	public Set<String> namespacePrefixSet = new HashSet<String>();
	public HashMap<String, String> ns2prefixMap = new HashMap<String, String>();
	public String topologyNS;					// it will be used in conversion of PBD

	// 3.4: participant types set
	public Set<String> paTypeSet = new HashSet<String>();

	// for the function 3.6 fprocessPaType
	public HashMap<String, String> paType2processMap = new HashMap<String, String>();


	// for function 3.9 ftypePa
	public HashMap<String, String> pa2paTypeMap = new HashMap<String, String>();

	public HashMap<String, Object> ml2mcMap = new HashMap<String, Object>();

	public HashMap<String, Object> ml2paMap = new HashMap<String, Object>();


	/**************************create a matched WSDL file to current PBD***************************/
	/** 
	 * Algorithm 3.19 Procedure declarePartnerLinkTypes
	 * 
	 * @param {Element} definitions  The Tag <wsdl:definitions>
	 */
	public void declarePartnerLinkTypes(Element definitions){
		// the input definitions points on the <wsdl:definitions> tag of the newly created WSDL file
		// remove the first Child of currentDocument
		currentDocument.removeChild(definitions);
		
		String plt;												// partnerLinkType, inherits of NCName
		Element partnerLinkType, role;							// single BPEL constructs
		Set<Object> aSet = new HashSet<Object>();				// a list of participant references
		String b;												// a participant, inherits of NCName
		String c,d;												// portType, inherits of QName
		Set<String> nsprefixSet = this.nsprefixSet;				// a list of name space prefixes referencing to the name
																// spaces of the WSDL definitions of port types used by
		// create a new child node of currentDocument
		definitions = currentDocument.createElement("wsdl:definitions");
		currentDocument.appendChild(definitions);
		
		// add a name attribute to the <definitions> tag
		definitions.setAttribute("name", "partnerLinkTypes");
		// declare the target name space for the WSDL definitions
		String targetNS = topologyNS + "/partnerLinkTypes";
		// topologyNS is the target name space of the participant topology
		definitions.setAttribute("targetNamespace", targetNS);	// algorithm 3.5
		// add the name space declarations for the name space prefixes wsdl and plnk
		definitions.setAttribute("xmlns:wsdl", "http://schemas.xmlsoap.org/wsdl/");
		String plnkNS = "http://docs.oasis-open.org/wsbpel/2.0/plnktype";
		definitions.setAttribute("xmlns:plnk", plnkNS);
		
		// add a partner link type declaration for each element of PLType
		//System.out.println("plTypeSet is: " + plTypeSet);
		if(!plTypeSet.isEmpty()){
			Iterator<String> it = plTypeSet.iterator();
			while(it.hasNext()){
				// create a new <plnk:partnerLinkType> construct
				plt = (String)it.next();
				partnerLinkType = currentDocument.createElement("plnk:partnerLinkType");
				// add a name attribute to the new partner link type declaration
				partnerLinkType.setAttribute("name", plt);
				// get the element of the relation Comm of the current partner link type
				Comm comm = fpltCommReverse(plt);
				aSet = comm.getPa1();
				c 	 = comm.getPt1();
				b	 = comm.getPa2();
				d	 = comm.getPt2();
				//System.out.println(comm.getElement());
				// create the first role element and add it to the partner link type declaration
				role = declareNewRole(b,d);											// algorithm 3.20
				partnerLinkType.appendChild(role);
				// the second role must not be declared if we have c = EMPTY
				if(!c.equals(EMPTY)){
					role = declareNewRole(aSet.iterator().next().toString(), c);	// algorithm 3.20
					// in this case, A includes only one participant reference
					partnerLinkType.appendChild(role);
				}
				// add the partner link type declaration to the <definitions> construct
				definitions.appendChild(partnerLinkType);
			}
		}
		// add the declarations of the name spaces of the port type definitions
		declareNameSpaces(definitions, nsprefixSet);								// algorithm 3.8
	}
	
	/**
	 * Algorithm 3.20 Function declareNewRole
	 * 
	 * @param  {String} pa           The participant of this role
	 * @param  {String} pt           The portType of this role
	 * @return {Element} role        The tag <plnk:role>
	 */
	private Element declareNewRole(String pa, String pt){
		// the input pa is the participant reference which may be interpreted as the new role, the input pt is the port
		// type of this role
		// this function returns the new <plnk:role> construct
		Element role = currentDocument.createElement("plnk:role");		// a new <plnk:role> construct
		String pt_nsprefix = fnsprefixPT(pt);							// the name space prefix of the port type pt
		
		// add the name attribute to the role
		role.setAttribute("name", pa);									// pa is an NCName
		// add the portType attribute to the role
		role.setAttribute("portType", pt);								// pt is a QName
		
		// add the name space prefix of pt to the global list nsprefixList if it has not been added before
		if(!nsprefixSet.contains(pt_nsprefix) && !pt_nsprefix.equals(EMPTY)){
			nsprefixSet.add(pt_nsprefix);
		}
		
		// return the new <plnk:role> construct
		return role;
	}

	/**
	 * function 3.18: nsprefixPT: PT -> NSPrefix
	 * 
	 * @param {String} portType     The port type
	 * @return {String} nsprefix    The name space prefix
	 */
	private String fnsprefixPT(String portType){
		String[] nsprefixSplit;
		if(portType.contains(":")){
			nsprefixSplit = portType.split(":");
			return nsprefixSplit[0];
		}
		return EMPTY;
	}

	/**
	 * function 3.29: pltComm: Comm -> PLType
	 * 
	 * @param {Comm}    comm            The communication((A,c),(b,d))
	 * @return {String} partnerLinkType The partner link type
	 */
	private String fpltComm(Comm comm){
		if(comm2pltMap.containsKey(comm)){
			return comm2pltMap.get(comm);
		}
		return EMPTY;
	}
	
	/**
	 * function pltCommReverse: 
	 * according the input partnerLinkType of comm2pltMap to output the communication
	 * 
	 * @param {String} plt       The partner link type
	 * @return {Comm}  comm      The communication
	 */
	private Comm fpltCommReverse(String plt){
		if(comm2pltMap.containsValue(plt)){
			Set<Comm> ks = comm2pltMap.keySet();
			Iterator<Comm> it = ks.iterator();
			while(it.hasNext()){
				Comm comm = (Comm)it.next();
				if(fpltComm(comm).equals(plt)){
					return comm;
				}
			}
		}
		return null;
	}

	/**************************main*******************************/
	/**
	 * FIXME: include in BPEL4Chor2BPEL
	 */
	public static void main(String argv[]) throws ParserConfigurationException, 
										SAXException, IOException, TransformerFactoryConfigurationError, TransformerException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setIgnoringComments(true);
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder docBuilder = factory.newDocumentBuilder();
		if(docBuilder.isNamespaceAware()){
			Document docGround = docBuilder.parse("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/groundingSA.bpel");
			Document docTopo = docBuilder.parse("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/topologySA.xml");
			Document docPBD = docBuilder.parse("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/processSA.bpel");
			
			BPEL4Chor2BPELTopologyAnalyze topoAnaly = new BPEL4Chor2BPELTopologyAnalyze();
			BPEL4Chor2BPELGroundingAnalyze groundAnaly = new BPEL4Chor2BPELGroundingAnalyze();
			BPEL4Chor2BPELPBDConversion pbdCon = new BPEL4Chor2BPELPBDConversion();
			BPEL4Chor2BPELWSDLCreate wsdlCreate = new BPEL4Chor2BPELWSDLCreate();

			//topology analyze
			topoAnaly.nsAnalyze((Element) docTopo.getFirstChild());
			topoAnaly.paTypeAnalyze((Element) docTopo.getFirstChild());
			topoAnaly.paAnalyze((Element) docTopo.getFirstChild());
			topoAnaly.mlAnalyze((Element) docTopo.getFirstChild());
			topoAnaly.getMl2BindSenderToMap(((Element)docTopo.getFirstChild()));
			
			groundAnaly.namespacePrefixSet = topoAnaly.namespacePrefixSet;    // will be used in grounding nsAnalyze
			groundAnaly.namespaceSet = topoAnaly.namespaceSet;				// will be used in grounding nsAnalyze
			groundAnaly.ns2prefixMap = topoAnaly.ns2prefixMap;				// will be used in grounding nsAnalyze
			groundAnaly.messageConstructsSet = topoAnaly.messageConstructsSet;
			groundAnaly.messageLinkSet = topoAnaly.messageLinkSet;
			groundAnaly.ml2mcMap = topoAnaly.ml2mcMap;
			groundAnaly.ml2paMap = topoAnaly.ml2paMap; 						// will be used in fparefsML() and in Alg. 3.4
			groundAnaly.ml2bindSenderToMap = topoAnaly.ml2bindSenderToMap; 	// will be used in mlAnalyze
			groundAnaly.pa2scopeMap = topoAnaly.pa2scopeMap; 					// will be used in Alg. 3.4 createPartnerLinkDeclarations
			groundAnaly.paTypeSet = topoAnaly.paTypeSet;                      // will be used in Alg. 3.4 createPartnerLinkDeclarations
			groundAnaly.pa2paTypeMap = topoAnaly.pa2paTypeMap;              	// will be used in Alg. 3.4 createPartnerLinkDeclarations
			groundAnaly.paType2processMap = topoAnaly.paType2processMap;      // will be used in Alg. 3.4 createPartnerLinkDeclarations
			
			//grounding analyze
			groundAnaly.nsAnalyze((Element) docGround.getFirstChild());
			groundAnaly.mlAnalyze((Element) docGround.getFirstChild());
			groundAnaly.propertyAnalyze((Element) docGround.getFirstChild());

			//WSDL creation
			wsdlCreate.currentDocument = docPBD;
			wsdlCreate.topologyNS = topoAnaly.topologyNS;
			wsdlCreate.plTypeSet = groundAnaly.plTypeSet;
			wsdlCreate.comm2pltMap = groundAnaly.comm2pltMap;
			wsdlCreate.nsprefixSet = pbdCon.namespacePrefixSet;
			wsdlCreate.declarePartnerLinkTypes((Element)docPBD.getFirstChild());

			/**************************output of the converted PBD******************************/
			Source sourceWSDL = new DOMSource(docPBD);
			File wsdlFile = new File("/home/eysler/work/DiplomArbeit/oryx-editor/editor/server/src/org/oryxeditor/bpel4chor/testFiles/PBDConvertion.wsdl");
			Result resultWSDL = new StreamResult(wsdlFile);
			 
			// Write the converted docPBD to the file
			Transformer xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(sourceWSDL, resultWSDL);
		}
	}
}
