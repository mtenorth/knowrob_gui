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

import org.knowrob.vis.themes.GreyTheme;

import jp.atr.unr.pf.roboearth.RoboEarthInterface;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ControlWindow;
import controlP5.Knob;
import controlP5.ListBox;
import controlP5.Textfield;


/**
 * Displays a dialog for searching for and downloading action
 * recipes from RoboEarth.
 *  
 * @author Moritz Tenorth, tenorth@atr.jp
 *
 */
public class ActionRecipeDialog {

	KnowRobGuiMain op;
	KnowRobGuiApplet gui;
	public ControlP5 controlP5;
	ControlWindow controlWindow;
	DialogListener dialogListener;
	
	ListBox res;
	Textfield q;
	Knob progress;
	String recipe_owl_file = "";

	
	
	/**
	 * Constructor
	 * 
	 * @param op Reference to the TeleopInterfaceMain instance
	 * @param gui Reference to the TeleopInterfaceGui instance 
	 */
	public ActionRecipeDialog(KnowRobGuiMain op, KnowRobGuiApplet gui) {
		
		controlP5 = new ControlP5(gui);
		this.op = op;
		
		controlWindow = controlP5.addControlWindow("controlP5window",200,200,450,250)
								 .setBackground(gui.color(50))
								 .setUpdateMode(ControlWindow.NORMAL)
								 .setTitle("Load action recipe from RoboEarth");


		q = GreyTheme.applyStyle(controlP5.addTextfield("search_query",20,30,300,20))
					 .setValue("ubimart recommendation")
					 .moveTo(controlWindow);

		
		Button b = GreyTheme.applyStyle(controlP5.addButton("submit", 23, 340, 30, 80, 20))
				 .moveTo(controlWindow);
		
		progress = GreyTheme.applyStyle(controlP5.addKnob("", 0, 200, 100, 200, 25))
							.moveTo(controlWindow)
							.hide();
		
		res = GreyTheme.applyStyle(controlP5.addListBox("Action Recipes"), 20)
						.setPosition(20, 100)
                        .setSize(400, 220)
 			            .moveTo(controlWindow);
		
		
		dialogListener = new DialogListener();

		b.addListener(dialogListener);
		q.addListener(dialogListener);
		res.addListener(dialogListener);

	}


	
	/** 
	 * Sends a query for action recipes to the RoboEarth database. The 
	 * resulting set of recipes is added to the ListBox for the user 
	 * to select one.
	 * 
	 * @param query A command such as "serve a drink"
	 */
	protected void queryRoboEarth(String query) {

		String[] q_res = RoboEarthInterface.searchActionRecipesFor(query);

		this.progress.show();
		controlP5.draw();
		
		this.progress.setVisible(true);
		ProgressIndicatorUpdator prog_indicator = new ProgressIndicatorUpdator();
		prog_indicator.setKnob(progress);
		new Thread(prog_indicator).start();
		
		if(q_res!=null) {
			int i=0;
			this.res.clear();
			for (String r : q_res) {
				this.res.addItem(r, i++);
			}
		}

		this.progress.hide();
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
					
					if(theEvent.getGroup().getName().equals("Action Recipes")) {
						
						String url = "http://api.roboearth.org/api/recipe/" + res.getItem((int)theEvent.getGroup().getValue()).getName();
						op.setRecipeOwlFile(RoboEarthInterface.downloadRecipeFromUrl(url), q.getText(), url);
						controlWindow.hide();
					}
				} 
				else if (theEvent.isController()) {

					if(theEvent.getController().getName().equals("submit")) {
						queryRoboEarth(q.getText());

					} else if(theEvent.getController().getName().equals("search_query")) {
						queryRoboEarth(q.getText());
					}
				}
				
			} catch (Exception e ) {
				e.printStackTrace();
			}
		}
	}
	
	public class ProgressIndicatorUpdator implements Runnable {

		Knob progress;
		private float min;
		private float max;
		private float val=0;

		@Override
		public void run() {

			try {
				progress.setValue(val++);

				if(val>max) 
					val=min;

				controlP5.draw();
				Thread.sleep(10);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void setKnob(Knob k) {
			this.progress = k;
			this.min = k.getMin();
			this.max = k.getMax();
			this.val = min;
		}
	}
}
