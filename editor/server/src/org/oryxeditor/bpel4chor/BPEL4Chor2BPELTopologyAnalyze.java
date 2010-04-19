package org.oryxeditor.bpel4chor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * It is the analyze of Topology, which was designed in the Studien Arbeit
 * of Peter Reimann(2008)
 */

public class BPEL4Chor2BPELTopologyAnalyze {
	
	final static String EMPTY = "";
	public Set<String> namespaceSet = new HashSet<String>();

	// 3.2: record all name space prefixes of QName
	public Set<String> namespacePrefixSet = new HashSet<String>();
	public HashMap<String, String> ns2prefixMap = new HashMap<String, String>();
	public String topologyNS;					// it will be used in conversion of PBD
	public HashMap<String, String> forEach2setMap = new HashMap<String, String>();

	/*************************ParticipantType variables***********************/
	public Set<String> paTypeSet = new HashSet<String>();

	// 3.5: process set
	public Set<String> processSet = new HashSet<String>();
	//private HashMap<String, String> paType2PBDMap = new HashMap<String, String>();
	
	// for the function 3.6 fprocessPaType
	public HashMap<String, String> paType2processMap = new HashMap<String, String>();

	/*************************Participants**************************/
	public Set<String> paSet = new HashSet<String>();

	// 3.10: scopes set
	public Set<String> scopeSet = new HashSet<String>();

	// for function 3.9 ftypePa
	public HashMap<String, String> pa2paTypeMap = new HashMap<String, String>();

	// for function 3.11 fscopePa
	public HashMap<String, Object> pa2scopeMap = new HashMap<String, Object>();
	private HashMap<String, String> pa2foreachInScopeMap = new HashMap<String, String>();


	/*************************MessageLink variables***************************/
	public Set<String> messageLinkSet = new HashSet<String>();

	public Set<String> messageConstructsSet = new HashSet<String>();

	public HashMap<String, Object> ml2mcMap = new HashMap<String, Object>();

	public HashMap<String, Object> ml2paMap = new HashMap<String, Object>();

	//in fbindSenderToML defined and for grounding Analyze
	public HashMap<String, String> ml2bindSenderToMap = new HashMap<String, String>(); 

	/**
	 * To analyze the name spaces of <topology> of topology.xml with the node name "topology"
	 * 
	 * @param {Document} currentDocument      The document of topology.xml 
	 */
	public void nsAnalyze (Document currentDocument){
		
		getNamespaceSet(currentDocument, "topology");
		
		//System.out.println("ns2prefixMap of topology is: " + ns2prefixMap);
		//System.out.println("namespaces prefix Set of topology is: " + namespacePrefixSet);
		//System.out.println("namespaceSet is: " + namespaceSet);			
	}
	
	/**
	 * To analyze the part <participantTypes> of topology.xml
	 * 
	 * @param {Document} currentDocument     The document of topology.xml 
	 */
	public void paTypeAnalyze (Document currentDocument){
		
		paTypeSet = getPaTypeSet((Element)currentDocument.getFirstChild());
		processSet = getProcessSet((Element)currentDocument.getFirstChild());
		paType2processMap = getPaType2ProcessMap((Element)currentDocument.getFirstChild());
		
		//System.out.println("paTypeSet" + paTypeSet);
		//System.out.println("processSet" + processSet);
		//System.out.println("paType2processMap is: " + paType2processMap);
	}
	
	/**
	 * To analyze the part <participants> of topology.xml
	 * 
	 * @param {Document} currentDocument     The document of topology.xml
	 */
	public void paAnalyze (Document currentDocument){
				
		paSet = getPaSet((Element)currentDocument.getFirstChild());
		paTypeSet = getPaTypeSet((Element)currentDocument.getFirstChild());
		
		getPa2PaTypeMap((Element)currentDocument.getFirstChild());

		scopeSet = getScopeSet((Element)currentDocument.getFirstChild());
		
		getPa2ScopeMap((Element)currentDocument.getFirstChild());
				
		//System.out.println("pa2scopeMap is: " + pa2scopeMap);
		//System.out.println("pa2paTypeMap is:" + pa2paTypeMap);
		//System.out.println("scopeSet is:" + scopeSet);
		//System.out.println("paTypeSet is: " + paTypeSet);
		//System.out.println("paSet is: " + paSet);
		//System.out.println("pa2foreachInScopeMap is: " + pa2foreachInScopeMap);
	}
	
	/**
	 * To analyze the part <messageLinks> of topology.xml
	 * 
	 * @param {Document} currentDocument     The document of topology.xml
	 */
	public void mlAnalyze(Document currentDocument){
		
		messageLinkSet = new HashSet<String>();		  // ML Set
		messageConstructsSet = new HashSet<String>(); // MC Set
		String ml;                                    // ml is an Element of messageLinkSet
		String receiver, sender1 = ""; 				  // they are the Element of PaSet
		ArrayList<String> sendersList = new ArrayList<String>();  // to store Elements of Attribute "senders"
		String receiveActivity, sendActivity;         // NCName
		String receiverns, senderns;                  // name space prefixes receiver-ns and sender-ns
		String mc1, mc2;							  // QName, mc1 will be the sned activity and mc2 the receive activity of the message link
		
		// during the analyze of messageLink of topology to get the messageLinkSet, 
		// messageConstructsSet, ml2mcMap, ml2paMap, 
		NodeList childNodes = ((Element)currentDocument.getFirstChild()).getElementsByTagName("messageLink");
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if (child instanceof Element){
				ml = ((Element)child).getAttribute("name");
				messageLinkSet.add(ml);
				receiver = ((Element)child).getAttribute("receiver");
				if (((Element) child).hasAttribute("sender")){
					sender1 = ((Element)child).getAttribute("sender");
				}
				else {
					sendersList.clear();
					if (((Element) child).hasAttribute("senders")){
						String senders = ((Element)child).getAttribute("senders");
						String[] sendersSplit = senders.split(" ");
						for(int j=0; j<sendersSplit.length; j++){
							sendersList.add(sendersSplit[j]);
							sender1 = (String)sendersList.get(0);
						}
					}
				}
				receiveActivity = ((Element)child).getAttribute("receiveActivity");
				sendActivity = ((Element)child).getAttribute("sendActivity");
				receiverns = fnsprefixProcess(fprocessPaType(ftypePa(receiver)));
				senderns = fnsprefixProcess(fprocessPaType(ftypePa(sender1)));
				mc2 = buildQName(receiverns, receiveActivity);
				mc1 = buildQName(senderns, sendActivity);
				messageConstructsSet.add(mc2);
				messageConstructsSet.add(mc1);
				// deal with the senders attribute
				if(sendersList.size() >= 2){
					for(int k=1; k<sendersList.size(); k++){
						senderns = fnsprefixProcess(fprocessPaType(ftypePa(sendersList.get(k))));
						mc2 = buildQName(receiverns, receiveActivity);
						mc1 = buildQName(senderns, sendActivity);
						messageConstructsSet.add(mc2);
						messageConstructsSet.add(mc1);
					}
				}
				// create ml2mcMap for function fconstructsML
				ArrayList<String> mcSenderReceiverList = new ArrayList<String>();
				if (!sendActivity.isEmpty() && !receiveActivity.isEmpty()){
					mcSenderReceiverList.clear();
					mcSenderReceiverList.add(mc1);
					mcSenderReceiverList.add(mc2);
					ml2mcMap.put(ml, mcSenderReceiverList);
				}
				// create ml2paMap for function fparefsML
				ArrayList<Object> senderReceiverList = new ArrayList<Object>();
				//senderReceiverList.clear();
				if (((Element) child).hasAttribute("senders")){
					senderReceiverList.add(sendersList);
					senderReceiverList.add(receiver);
				}
				else {
					ArrayList<String> senderList = new ArrayList<String>();
					senderList.add(sender1);
					senderReceiverList.add(senderList);
					senderReceiverList.add(receiver);
				}
				ml2paMap.put(ml, senderReceiverList);
			}
		}
		
		//System.out.println("MessageLinkSet is: " + messageLinkSet);
		//System.out.println("MessageConstructsSet is: " + messageConstructsSet);
		//System.out.println("ml2mcMap is: " + ml2mcMap);
		//System.out.println("ml2paMap is: " + ml2paMap);
	}
	

	/**********************Method of Name Space******************************/
	/**
	 * to create the Sets: namespaceSet, namespaceprefixSet and Mapping: ns2prefixMap
	 * 
	 * @param {Node}   currentNode     The current node of the XML file
	 * @param {String} nodeName        The name of the Node
	 */
	private void getNamespaceSet(Node currentNode, String nodeName){
		if(!(currentNode instanceof Element || currentNode instanceof Document)){
			return;
		}

		String str;
		String[] strSplit, prefixSplit;

		if(currentNode.getNodeName().equals(nodeName)){
			for(int i=0; i<currentNode.getAttributes().getLength(); i++){
				str = currentNode.getAttributes().item(i).toString();
				strSplit = str.split("=");
				if(strSplit[0].contains("xmlns") || (strSplit[0].equals("targetNamespace")) 
						|| (strSplit[0].equals("topology"))){
					if(strSplit[0].equals("targetNamespace")){
						ns2prefixMap.put(strSplit[0], strSplit[1].replaceAll("\"", ""));
						String valueOfTopologyNS = strSplit[1].replaceAll("\"", "");
						topologyNS = strSplit[1].replaceAll("\"", "");
						ns2prefixMap.put("topologyNS", valueOfTopologyNS);
						String targetNS = "targetNamespace";
						namespacePrefixSet.add(targetNS);
						namespaceSet.add(valueOfTopologyNS);
					}
					if(strSplit[0].contains("xmlns:")){
						prefixSplit = strSplit[0].split(":");
						namespacePrefixSet.add(prefixSplit[1]);
						namespaceSet.add(strSplit[1].replaceAll("\"", ""));
						ns2prefixMap.put(prefixSplit[1],strSplit[1].replaceAll("\"", ""));
					}
					if(strSplit[0].equals("xmlns")){
						namespaceSet.add(strSplit[1].replaceAll("\"", ""));
						ns2prefixMap.put(strSplit[0],strSplit[1].replaceAll("\"", ""));
					}
					if(strSplit[0].equals("topology")){
						ns2prefixMap.put(strSplit[0], strSplit[1].replaceAll("\"", ""));
						String valueOfTopologyInGrounding = strSplit[1].replaceAll("\"", "");
						namespacePrefixSet.add(strSplit[0]);
						namespaceSet.add(valueOfTopologyInGrounding);
					}
				}
			}
		}
		// recursive to search name space 
		NodeList childNodes = currentNode.getChildNodes();
		Node child;
		for(int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getNamespaceSet(child, nodeName);
			}	
		}
	}


	/***********************Method of ParticipantType*************************/
	/**
	 * function 3.4: To create the participantTypeSet (create the mapping between "name" and "participantBehaviorDescription")
	 * 
	 * @param {Element} currentElement     The current Element
	 * @return {Set}    paTypeSet          The participantType set
	 */
	private Set<String> getPaTypeSet(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)){
			return null;
		}

		if(currentElement.getNodeName().equals("participantType")){
			// analyze name space of participantType node
			String paType = currentElement.getAttribute("name");
			paTypeSet.add(paType);
//			String paTypePBD = currentElement.getAttribute("participantBehaviorDescription");
//			paType2PBDMap.put(paType, paTypePBD);
		}

		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getPaTypeSet((Element)child);
			}	
		}
		return paTypeSet;
	}
	
	/**
	 * function 3.5: To create the processSet
	 * 
	 * @param {Element} currentElement     The current element
	 * @return {Set}    processSet         The process set
	 */
	private Set<String> getProcessSet(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)){
			return null;
		}

/*		if(currentElement.getNodeName().equals("participant")){
			String processLocalName = currentElement.getAttribute("name");
			String processType = currentElement.getAttribute("type");
			String prefixOfProcess = fnsprefixProcess(paType2PBDMap.get(processType));
			String processName = prefixOfProcess + ":" + processLocalName;
			processSet.add(processName);
		}
*/
		
		if(currentElement.getNodeName().equals("participantType")){
			// analyze namespace of participantType node
			String pbd = currentElement.getAttribute("participantBehaviorDescription");
			processSet.add(pbd);
		}
		
		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getProcessSet((Element)child);
			}	
		}
		return processSet;
	}

	/**
	 * To create the paType2processMap for function 3.6
	 * 
	 * @param {Element} currentElement     The current element
	 * @return {HashMap} paType2processMap The mapping of paType and process
	 */
	private HashMap<String, String> getPaType2ProcessMap(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)) {
			return null;
		}

/*		if(currentElement.getNodeName().equals("participant") && 
				!currentElement.getParentNode().getNodeName().equals("participantSet")){

			// analyze namespace of participantType node
			String processLocalName = currentElement.getAttribute("name");
			String processType = currentElement.getAttribute("type");
			String processPrefix = fnsprefixProcess(paType2PBDMap.get(processType));
			String process = processPrefix + ":" + processLocalName;
			//String pbd = currentElement.getAttribute("participantBehaviorDescription");
			paType2processMap.put(processType, process);
		}
*/
		
		if(currentElement.getNodeName().equals("participantType")){

			// analyze namespace of participantType node
			String paName = currentElement.getAttribute("name");
			String pbd = currentElement.getAttribute("participantBehaviorDescription");
			paType2processMap.put(paName, pbd);
		}
		
		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getPaType2ProcessMap((Element)child);
			}	
		}
		return paType2processMap;
	}

	/**
	 * function 3.6: processPaType: PaType -> Process
	 * 
	 * @param {String} paType     The participant type
	 * @return {String} process   The process 
	 */
	private String fprocessPaType(String paType){
		if(!paType.isEmpty() && paType2processMap.containsKey(paType)){
			return paType2processMap.get(paType);
		}
		return EMPTY;
	}

	/**
	 * function 3.7: nsprefixProcess: Process -> NSPrefix
	 * 
	 * @param {String} process     The process
	 * @return {String} nsprefix   The name space prefix
	 */
	private String fnsprefixProcess(String process){
		String[] nsprefixSplit;
		if(process.contains(":")){
			nsprefixSplit = process.split(":");
			return nsprefixSplit[0];
		}
		return EMPTY;
	}


	/********************Method of Participant*************************/
	/**
	 * function 3.8: To create a set of participants
	 * 
	 * @param {Element} currentElement     The current element
	 * @return {Set}    paSet              The participant set
	 */
	private Set<String> getPaSet(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)){
			return null;
		}

		if(currentElement.getNodeName().equals("participant") || 
			currentElement.getNodeName().equals("participantSet")){
				String pa = currentElement.getAttribute("name");
				paSet.add(pa);
		}

		// make forEach2setMap for PBDConvertion (base for function 3.36)
		if(currentElement.getNodeName().equals("participantSet")){
			String paSetName = currentElement.getAttribute("name");
			if(currentElement.hasAttribute("scope")){
				String scContent = currentElement.getAttribute("scope");
				// it allow just one scope for scope attribute
				fsetForEach(scContent, EMPTY);
			}
		    if(currentElement.hasAttribute("forEach")){
				String scContent = currentElement.getAttribute("forEach");
				// it allow many forEachs for forEach attribute
				if(scContent.contains(" ")){
					String[] scArray = scContent.split(" ");
					for(int i=0;i<scArray.length;i++){
						fsetForEach(scArray[i].toString(), paSetName);
					}
				}
				else{
					fsetForEach(scContent, paSetName);
				}
			}
		}

		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for(int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getPaSet((Element)child);
			}	
		}
		return paSet;
	}

	/**
	 * To create pa2paTypeMap for 3.9
	 * 
	 * @param {Element} currentElement     The current element
	 */
	private void getPa2PaTypeMap(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)){
			return;
		}

		if(currentElement.getNodeName().equals("participant")){
			try{
				if(!(currentElement.getAttribute("name") == "") &&
						!(currentElement.getAttribute("type") == "")){
					String pa = currentElement.getAttribute("name");
					String paType = currentElement.getAttribute("type");
					pa2paTypeMap.put(pa, paType);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		if(currentElement.getNodeName().equals("participantSet")){
			try{
				String pa = currentElement.getAttribute("name");
				String paType = currentElement.getAttribute("type");
				pa2paTypeMap.put(pa, paType);
				if(currentElement.hasChildNodes()){
					NodeList childNodes = currentElement.getChildNodes();
					Node child;
					for(int i = 0; i < childNodes.getLength(); i++){
						child = childNodes.item(i);
						if(child instanceof Element){
							String childPa = child.getAttributes().getNamedItem("name").getNodeValue();
							String childPaType = paType;
							pa2paTypeMap.put(childPa, childPaType);
						}
					}
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}

		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getPa2PaTypeMap((Element)child);
			}	
		}
	}

	/**
	 * function 3.9: typePa: Pa -> paType
	 * 
	 * @param {String} participant       The participant
	 * @return {String} participantType  The participantType
	 */
	private String ftypePa (String participant){
		if(!paTypeSet.isEmpty()){
			Iterator<String> it = paTypeSet.iterator();
			while (it.hasNext()){
				String participantType = (String)it.next();
				try{
					if(pa2paTypeMap.containsKey(participant) && pa2paTypeMap.get(participant).equals(participantType)){
						return participantType;
					}
				}
				catch (Exception e){
					e.printStackTrace();
					System.out.println(participant + " has problem");
				}
			}
		}
		return EMPTY;
	}

	/**
	 * function 3.10: getScopeSet for <scope> and <forEach> attribute of participant 
	 * 
	 * @param {Element} currentElement     The current element
	 * @return {Set}    scopeSet           The scope set
	 */
	private Set<String> getScopeSet(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)){
			return null;
		}

		if(currentElement.getNodeName().equals("participant") || currentElement.getNodeName().equals("participantSet")){
			try{
				// it is might be many elements in forEach attribute
				if(!(currentElement.getAttribute("forEach") == "")){
					String forEachAttribute = currentElement.getAttribute("forEach");
					if(forEachAttribute.contains(" ")){
						String[] forEachArray = forEachAttribute.split(" ");
						for(int i=0;i<forEachArray.length;i++){
							scopeSet.add(forEachArray[i]);
						}
					}
					else{
						scopeSet.add(forEachAttribute);
					}
				}

				// it could be just one element in scope attribute 
				if(!(currentElement.getAttribute("scope") == "")){
					String scopeAttribute = currentElement.getAttribute("scope");
					scopeSet.add(scopeAttribute);
				}
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
			}
		}

		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getScopeSet((Element)child);
			}	
		}
		return scopeSet;
	}

	/**
	 * To create mapping{participant, scope||forEach} for function 3.11
	 * 
	 * @param {Element} currentElement     The current element
	 */
	private void getPa2ScopeMap(Element currentElement){
		if(!((currentElement instanceof Node) || (currentElement instanceof Document))){
			return;
		}

		if(currentElement.getNodeName().equals("participant")){
			try{
				if((currentElement.getAttribute("name") != "") &&
						(currentElement.getAttribute("scope") != "")){
					String pa = currentElement.getAttribute("name");
					String paScope = currentElement.getAttribute("scope");
					pa2scopeMap.put(pa, paScope);
				}
				else if((currentElement.getAttribute("name") != "") &&
						(currentElement.getAttribute("forEach") != "")){
					String pa = currentElement.getAttribute("name");
					String paForEach = currentElement.getAttribute("forEach");
					if(paForEach.contains(" ")){
						String[] paForEachArray = paForEach.split(" ");
						for(int i=0;i<paForEachArray.length;i++){
							//TODO:: to be refined with many forEachs in forEach attribute of a single participant of
							//       participantSet
							pa2foreachInScopeMap.put(paForEachArray[i], "<ForEach>");
							pa2scopeMap.put(pa, pa2foreachInScopeMap);
							//pa2scopeMap.put(paNameAttribute, paForEachArray[i]);
							scopeSet.add(paForEachArray[i]);
						}
					}
					else{
						scopeSet.add(paForEach);
						pa2scopeMap.put(pa, paForEach);
					}
				}
				else{
					String pa = currentElement.getAttribute("name");
					pa2scopeMap.put(pa, EMPTY);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		else if(currentElement.getNodeName().equals("participantSet")){
			try{
				if(currentElement.getAttribute("name") != ""){
					String pa = currentElement.getAttribute("name");
					pa2scopeMap.put(pa, EMPTY);
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	
		// recursive to search
		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getPa2ScopeMap((Element)child);
			}	
		}
	}


	/***********************Method of MessageLink***********************/
	/**
	 * function 3.12: create message construct set
	 * 
	 * @param {Node} currentNode          The current node
	 * @return {Set} messageConstructsSet The message constructs set
	 */
/*	private Set<String> getMessageConstructsSet (Node currentNode){
		NodeList childNodes = ((Element)currentNode).getElementsByTagName("messageLink");
		Node child;
		
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				String receiver = ((Element)child).getAttribute("receiver");
				String sender1 = "";
				ArrayList<String> sendersList = new ArrayList<String>();
				if(((Element)child).hasAttribute("sender")){
					sender1 = ((Element)child).getAttribute("sender");
				}
				else if(((Element)child).hasAttribute("senders")){
					String senders = ((Element)child).getAttribute("senders");
					String[] sendersSplit = senders.split(" ");
					for(int j=0; j<sendersSplit.length; j++){
						sendersList.add(sendersSplit[j]);
						sender1 = (String)sendersList.get(0);
					}
				}
				String receiveActivity = ((Element)child).getAttribute("receiveActivity");
				String sendActivity = ((Element)child).getAttribute("sendActivity");
				String receiverns = fnsprefixProcess(fprocessPaType(ftypePa(receiver)));
				String senderns = fnsprefixProcess(fprocessPaType(ftypePa(sender1)));
				String mc2 = buildQName(receiverns, receiveActivity);
				String mc1 = buildQName(senderns, sendActivity);
				messageConstructsSet.add(mc2);
				messageConstructsSet.add(mc1);
				if(sendersList.size() >= 2){
					for(int k=1; k<sendersList.size(); k++){
						senderns = fnsprefixProcess(fprocessPaType(ftypePa(sendersList.get(k))));
						mc2 = buildQName(receiverns, receiveActivity);
						mc1 = buildQName(senderns, sendActivity);
						messageConstructsSet.add(mc2);
						messageConstructsSet.add(mc1);
					}
				}
			}
		}
		return messageConstructsSet;
	}
*/
	/**
	 * function: To build QName for function 3.12 
	 * 
	 * @param {String} prefix     The prefix
	 * @param {String} NCName     The NCName
	 * @return {String} QName     The QName
	 */
	private static String buildQName(String prefix, String NCName){
		return prefix + ":" + NCName;
	}

	/**
	 * function 3.13: To create message link set
	 * 
	 * @param {Element} currentElement     The current element
	 * @return {Set}    messageLinkSet     The message link set
	 */
/*	private Set<String> getMessageLinkSet(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)){
			return null;
		}

		if(currentElement.getNodeName().equals("messageLink")){
			try{
				String mlString = currentElement.getAttribute("name");
				messageLinkSet.add(mlString);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		NodeList childNodes = currentElement.getChildNodes();
		Node child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = childNodes.item(i);
			if(child instanceof Element){
				getMessageLinkSet((Element)child);
			}	
		}
		return messageLinkSet;
	}
*/

	/**
	 * To create mapping[messageLink, bindSenderTo]
	 * will be used in fbindSenderToML and Grounding analyze
	 * 
	 * @param {Element} currentElement      The current element
	 */
	public void getMl2BindSenderToMap(Element currentElement){
		if(!(currentElement instanceof Node || currentElement instanceof Document)){
			return;
		}

		NodeList childNodes = currentElement.getElementsByTagName("messageLink");
		Element child;
		for (int i=0; i<childNodes.getLength(); i++){
			child = (Element)childNodes.item(i);
			if(child.hasAttribute("bindSenderTo")){
				String key = child.getAttribute("name");
				String value = child.getAttribute("bindSenderTo");
				ml2bindSenderToMap.put(key, value);
			}
			else{
				String key = child.getAttribute("name");
				String value = EMPTY;
				ml2bindSenderToMap.put(key, value);
			}
		}
	}
	
	/**
	 * function 3.36: assigning a set of participant references to each <forEach> activity
	 * Attention: the following functions of 3.36 will be just used within the tag <participantSet> of XML files.
	 * create a mapping forEach2setMap[sc, paSetName]
	 * 
	 * @param {String} sc         The QName of <scope> or <forEach> activity
	 * @param {String} paSetName  The name of <participantSet>
	 */
	private void fsetForEach(String sc, String paSetName){
			if(pa2scopeMap.containsValue(sc)){
				forEach2setMap.put(sc, EMPTY);
			}
			else{
				forEach2setMap.put(sc, paSetName);
			}
	}
}
