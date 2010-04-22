/**
 * Copyright (c) 2009-2010 Changhua Li
 * 				 2010 Oliver Kopp
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

package org.oryxeditor.bpel4chor;

import java.util.List;

import javax.jws.WebService;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BPEL4Chor2BPEL {
	
	/**
	 * Adds WSDL specific elements to the given BPEL4Chor choreography
	 * 
	 * FIXME add WSDLs to result set (see BPEL4Chor2BPELExport)
	 * 
	 * @param docPBD the BPEL process definitions to modify - These documents are MODIFIED!
	 * @return docPBD with WSDL specific elements 
	 */
	public List<Document> convert(Element elGround, Element elTopo, List<Document> docPBD) {
		BPEL4Chor2BPELTopologyAnalyze topoAnaly = new BPEL4Chor2BPELTopologyAnalyze();
		BPEL4Chor2BPELGroundingAnalyze grouAnaly = new BPEL4Chor2BPELGroundingAnalyze();
		BPEL4Chor2BPELPBDConversion pbdCon = new BPEL4Chor2BPELPBDConversion();

		//topology analyze
		topoAnaly.nsAnalyze(elTopo);
		topoAnaly.paTypeAnalyze(elTopo);
		topoAnaly.paAnalyze(elTopo);
		topoAnaly.mlAnalyze(elTopo);
		topoAnaly.getMl2BindSenderToMap(elTopo);
			
		grouAnaly.namespacePrefixSet = topoAnaly.namespacePrefixSet;    // will be used in grounding nsAnalyze
		grouAnaly.namespaceSet = topoAnaly.namespaceSet;				// will be used in grounding nsAnalyze
		grouAnaly.ns2prefixMap = topoAnaly.ns2prefixMap;				// will be used in grounding nsAnalyze
		grouAnaly.messageConstructsSet = topoAnaly.messageConstructsSet;
		grouAnaly.messageLinkSet = topoAnaly.messageLinkSet;
		grouAnaly.ml2mcMap = topoAnaly.ml2mcMap;
		grouAnaly.ml2paMap = topoAnaly.ml2paMap; 						// will be used in fparefsML() and in Alg. 3.4
		grouAnaly.ml2bindSenderToMap = topoAnaly.ml2bindSenderToMap; 	// will be used in mlAnalyze
		grouAnaly.pa2scopeMap = topoAnaly.pa2scopeMap; 					// will be used in Alg. 3.4 createPartnerLinkDeclarations
		grouAnaly.paTypeSet = topoAnaly.paTypeSet;                      // will be used in Alg. 3.4 createPartnerLinkDeclarations
		grouAnaly.pa2paTypeMap = topoAnaly.pa2paTypeMap;              	// will be used in Alg. 3.4 createPartnerLinkDeclarations
		grouAnaly.paType2processMap = topoAnaly.paType2processMap;      // will be used in Alg. 3.4 createPartnerLinkDeclarations
			
		//grounding analyze
		grouAnaly.nsAnalyze(elGround);
		grouAnaly.mlAnalyze(elGround);
		grouAnaly.propertyAnalyze(elGround);
			
		pbdCon.scopeSet = topoAnaly.scopeSet;							// will be used in Conversion of PBD
		pbdCon.processSet = topoAnaly.processSet;						// will be used in Conversion of PBD
		pbdCon.topologyNS = topoAnaly.topologyNS;						// will be used in Conversion of PBD
		pbdCon.forEach2setMap = topoAnaly.forEach2setMap;				// will be used in Conversion of PBD
		pbdCon.paSet = topoAnaly.paSet;									// will be used in Conversion of PBD
		pbdCon.pa2scopeMap = topoAnaly.pa2scopeMap; 					// will be used in Conversion of PBD
		pbdCon.ns2prefixMap = grouAnaly.ns2prefixMap;					// will be used in Conversion of PBD
		pbdCon.namespacePrefixSet = grouAnaly.namespacePrefixSet;		// will be used in Conversion of PBD
		pbdCon.plSet = grouAnaly.plSet;									// will be used in Conversion of PBD
		pbdCon.sc2plMap = grouAnaly.sc2plMap;							// will be used in Conversion of PBD
		pbdCon.pl2plTypeMap = grouAnaly.pl2plTypeMap;					// will be used in Conversion of PBD
		pbdCon.pl2myRoleMap = grouAnaly.pl2myRoleMap;					// will be used in Conversion of PBD
		pbdCon.pl2partnerRoleMap = grouAnaly.pl2partnerRoleMap;			// will be used in Conversion of PBD
		pbdCon.messageConstructsSet = grouAnaly.messageConstructsSet;	// will be used in Conversion of PBD
		pbdCon.mc2plMap = grouAnaly.mc2plMap;							// will be used in Conversion of PBD
		pbdCon.ml2mcMap = grouAnaly.ml2mcMap;							// will be used in Conversion of PBD
		pbdCon.messageLinkSet = grouAnaly.messageLinkSet;				// will be used in Conversion of PBD
		pbdCon.ml2ptMap = grouAnaly.ml2ptMap;							// will be used in Conversion of PBD
		pbdCon.ml2opMap = grouAnaly.ml2opMap;							// will be used in Conversion of PBD
		pbdCon.corrPropName2propertyMap = grouAnaly.corrPropName2propertyMap;  // will be used in Conversion of PBD
		pbdCon.property2nsprefixOfPropMap = grouAnaly.property2nsprefixOfPropMap; // will be used in Conversion of PBD
			
		//PBD conversion
		for (Document currentDoc: docPBD) {
			pbdCon.convertPBD(currentDoc);
		}
		
		return docPBD;
	}
}
