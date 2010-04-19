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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.oryxeditor.server.diagram.Diagram;
import org.oryxeditor.server.diagram.StencilSet;
import org.oryxeditor.server.diagram.StencilType;

import de.hpi.bpmn.*;
import de.hpi.bpmn2_0.model.diagram.ProcessDiagram;
import de.hpi.bpmn2_0.model.diagram.activity.ActivityShape;
import de.hpi.util.Bounds;

public class BPMNBuilder {

	private ArrayList<PolygonStructure> tasks;
	private ArrayList<PolygonStructure> gateways;
	private ArrayList<PolygonStructure> dataObjects;
	private ArrayList<CircleStructure> events;	
	
	private BPMNFactory factory;
	private Diagram diagram;
	private String rootDir;
	
	private double ratio;
	private final int DEFAULT_WIDTH = 100;
	
	public BPMNBuilder() {
		
//		this.factory = new BPMNFactory();
//		this.diagram = this.factory.createBPMNDiagram();
		this.ratio = 1;
		
		this.tasks = new ArrayList<PolygonStructure>();
		this.gateways = new ArrayList<PolygonStructure>();
		this.dataObjects = new ArrayList<PolygonStructure>();
		this.events = new ArrayList<CircleStructure>();
		
	}

	public BPMNBuilder(ArrayList<PolygonStructure> tasks,
			ArrayList<PolygonStructure> gateways,
			ArrayList<PolygonStructure> dataObjects,
			ArrayList<CircleStructure> events, String rootDir) {		
		
		this.rootDir = rootDir;
//		this.factory = new BPMNFactory();
//		this.diagram = this.factory.createBPMNDiagram();
		
		// assuming the default size of a task is 100px (DEFAULT_WIDTH)
		// ratio is the resize factor the for further object creation
		if (tasks.size() > 0 ) {
			this.ratio = this.DEFAULT_WIDTH
				/ tasks.get(0).getPolygon().getBounds2D().getWidth();
		}
		
		else 
			this.ratio = 1;
			
		Comparator<Object> c = new XComparator();
		
		Object[] tmp = tasks.toArray();
		Arrays.sort(tmp, c);
		this.tasks = new ArrayList<PolygonStructure>();
		for ( int i = 0; i < tmp.length; i++)
			this.tasks.add( (PolygonStructure) tmp[i]);
		
		tmp = gateways.toArray();
		Arrays.sort(tmp, c);		
		this.gateways = new ArrayList<PolygonStructure>();
		for ( int i = 0; i < tmp.length; i++)
			this.gateways.add( (PolygonStructure) tmp[i]);
		
		tmp = dataObjects.toArray();
		Arrays.sort(tmp, c);		
		this.dataObjects = new ArrayList<PolygonStructure>();
		for ( int i = 0; i < tmp.length; i++)
			this.dataObjects.add( (PolygonStructure) tmp[i]);
		
		tmp = events.toArray();
		Arrays.sort(tmp, c);
		this.events = new ArrayList<CircleStructure>();
		for ( int i = 0; i < tmp.length; i++)
			this.events.add( (CircleStructure) tmp[i]);
		}

	
	public Diagram buildDiagram() {
		
		String resourceId = "oryx-canvas123";
		StencilType type = new StencilType("BPMNDiagram");
		String stencilSetNs = "http://b3mn.org/stencilset/bpmn2.0#";
		String url = rootDir + "stencilsets/bpmn2.0/bpmn2.0.json";
		StencilSet stencilSet = new StencilSet(url, stencilSetNs);
		this.diagram = new Diagram(resourceId, type, stencilSet);
		
//		this.diagram = new ProcessDiagram();
		
//		this.buildEvents();
//		this.buildTasks();
//		this.buildGateways();
//		this.buildDataObjects();
		
		return this.diagram;
	}
	
	/**
	 * the most left events will be start plain events
	 * the most right ones will be end plain  events
	 * all events in between will be intermediate plain events
	 * 
	 */
	public void buildEvents(){
		
		int variance = (int) (100 * this.ratio);
		int referenceStart = this.events.get(0).getX() + variance;
		int referenceEnd = this.events.get( this.events.size() - 1 ).getX() - variance;
		String currentType = "start";
		
		int firstIntermediate = 0;
		int firstEnd = this.events.size() - 1;
		// determine startevents
		// all events that are less than 100px away from the first event will be start events
		
		for ( int i = 0; i < this.events.size(); i++ ){
			Event e;
			// last start event not found yet
			if ( currentType.equals("start") ) {
				
				if ( this.events.get(i).getX() < referenceStart ){
					e = this.factory.createStartPlainEvent();
				}
				else {
					// first intermediate event is found 
					e = this.factory.createIntermediatePlainEvent();
					currentType = "intermediate";
					
				}
			}
			else if (currentType.equals("intermediate")){
				if ( this.events.get(i).getX() < referenceEnd ){
					e = this.factory.createIntermediatePlainEvent();
				}
				else {
					// first end event is found 
					e = this.factory.createEndPlainEvent();
					currentType = "end";
				}
			}
			else {
				e = this.factory.createEndPlainEvent();
			}
			e.setBounds( this.events.get(i).getBounds( this.ratio ) );
			//this.diagram.getChildShapes().add(e);
				
		}
	
	}
	
	public void buildTasks(){
		for ( int i = 0; i < this.tasks.size(); i++ ){
			//t.setBounds(this.tasks.get(i).getBounds(this.ratio));
			//this.diagram.getChildShapes().add(t);
		}
	}
	
	public void buildDataObjects(){
		
		for ( int i = 0; i < this.dataObjects.size(); i++ ){
			DataObject d = this.factory.createDataObject();
			
			d.setBounds(this.dataObjects.get(i).getBounds(this.ratio));
			//this.diagram.getDataObjects().add(d);
		}
	}
	
	public void buildGateways(){
		for ( int i = 0; i < this.gateways.size(); i++ ){
			XORDataBasedGateway g = this.factory.createXORDataBasedGateway();
			g.setBounds(this.gateways.get(i).getBounds(this.ratio));
			//this.diagram.getChildShapes().add(g);
		}
	}
	
	
	
}
