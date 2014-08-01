/*
 *    Copyright (C) 2012
 *      ATR Intelligent Robotics and Communication Laboratories, Japan
 *
 *    Permission is hereby granted, free of charge, to any person obtaining a copy
 *    of this software and associated documentation files (the "Software"), to deal
 *    in the Software without restriction, including without limitation the rights
 *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 *    of the Software, and to permit persons to whom the Software is furnished to do so,
 *    subject to the following conditions:
 *
 *    The above copyright notice and this permission notice shall be included in all
 *    copies or substantial portions of the Software.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 *    PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *    HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *    OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 *    SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.knowrob.gui;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.knowrob.vis.themes.GreyTheme;
import org.xml.sax.SAXException;

import org.knowrob.roboearth.RoboEarthInterface;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ControlWindow;
import controlP5.ListBox;
import controlP5.Textfield;


/**
 * Displays a dialog for searching for and downloading environment
 * maps from RoboEarth.
 *  
 * @author Moritz Tenorth, tenorth@atr.jp
 *
 */
public class EnvironmentMapDialog {


	KnowRobGuiMain op;
	public ControlP5 controlP5;
	ControlWindow controlWindow;
	DialogListener dialogListener;
	
	ListBox res;
	Textfield room_number;
	Textfield floor_number;
	Textfield street_number;
	Textfield street_name;
	String map_owl_file = "";


	/**
	 * Constructor
	 * 
	 * @param op Reference to the TeleopInterfaceMain instance
	 * @param gui Reference to the TeleopInterfaceGui instance 
	 */
	public EnvironmentMapDialog(KnowRobGuiMain op, KnowRobGuiApplet gui) {
		
		controlP5 = new ControlP5(gui);
		this.op = op;

		controlWindow = controlP5.addControlWindow("controlP5window",200,200,450,250)
								 .setBackground(gui.color(50))
								 .setUpdateMode(ControlWindow.NORMAL)
								 .setTitle("Load environment map from RoboEarth");

		Button b = GreyTheme.applyStyle(controlP5.addButton("submit", 23, 340, 30, 80, 20))
				 .moveTo(controlWindow);

		room_number = GreyTheme.applyStyle(controlP5.addTextfield("room number",20,30,140,20))
					 .setValue("3001")
					 .moveTo(controlWindow);

		
		floor_number = GreyTheme.applyStyle(controlP5.addTextfield("floor number",180,30,140,20))
					 .moveTo(controlWindow);

		
		street_number = GreyTheme.applyStyle(controlP5.addTextfield("street number",20,70,140,20))
					 .moveTo(controlWindow);

		
		street_name = GreyTheme.applyStyle(controlP5.addTextfield("street name",180,70,140,20))
					 .moveTo(controlWindow);
		
		
		res = GreyTheme.applyStyle(controlP5.addListBox("Environment Maps"), 20)
			           .setPosition(20, 130)
			           .setSize(400, 150)
			           .moveTo(controlWindow);

		dialogListener = new DialogListener();

		b.addListener(dialogListener);
		room_number.addListener(dialogListener);
		floor_number.addListener(dialogListener);
		street_number.addListener(dialogListener);
		street_name.addListener(dialogListener);
		res.addListener(dialogListener);
	}

	
	
	
	/**
	 * Sends a query for environment maps to the RoboEarth database. The 
	 * resulting set of maps is added to the ListBox for the user to select 
	 * one. The query is assembled out of the non-empty form fields. 
	 * 
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	protected void queryRoboEarth() throws IOException, ParserConfigurationException, SAXException {

		ArrayList<String[]> constraints = new ArrayList<String[]>();
		
		if(!room_number.getText().isEmpty()) {
			constraints.add(new String[]{"kr:roomNumber", room_number.getText()});
		}

		if(!floor_number.getText().isEmpty()) {
			constraints.add(new String[]{"kr:floorNumber", floor_number.getText()});
		}

		if(!street_number.getText().isEmpty()) {
			constraints.add(new String[]{"kr:streetNumber", street_number.getText()});
		}

		if(!street_name.getText().isEmpty()) {
			constraints.add(new String[]{"rdfs:label", street_name.getText()});
		}
		
		String[] q_res = RoboEarthInterface.searchEnvironmentMapsFor(constraints.toArray(new String[0][0]));

		if(q_res!=null) {
			int i=0;
			this.res.clear();
			for (String r : q_res) {
				this.res.addItem(r, i++);
			}
		}
	}


	
	/** 
	 * Custom control event listener attached to the control elements in 
	 * this window.
	 * 
	 * Otherwise, the control events would be sent to the listener that
	 * is part of the main window.
	 * 
	 * @author tenorth
	 *
	 */
	public class DialogListener implements ControlListener {

		public void controlEvent(ControlEvent theEvent) {
			try {

				if (theEvent != null && theEvent.isGroup()) {

					if(theEvent.getGroup().getName().equals("Environment Maps")) {

						String url = "http://api.roboearth.org/api/environment/" + res.getItem((int)theEvent.getGroup().getValue()).getName();
						op.setMapOwlFile(RoboEarthInterface.downloadMapFromUrl(url), url, room_number.getText(), floor_number.getText(), street_number.getText(), street_name.getText());
						controlWindow.hide();
						
					}
				} 
				else if (theEvent.isController()) {

					if(theEvent.getController().getName().equals("submit")) {
						queryRoboEarth();

					} else if(theEvent.getController().getName().equals("room number") || 
							theEvent.getController().getName().equals("floor number") || 
							theEvent.getController().getName().equals("street number") || 
							theEvent.getController().getName().equals("street name")) {
						queryRoboEarth();
					}
				}

			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
	}
}
