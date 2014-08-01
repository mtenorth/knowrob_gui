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

import java.awt.Frame;
import java.io.File;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlListener;
import controlP5.ControlP5;
import controlP5.ControlWindow;
import controlP5.Textarea;
import controlP5.Textfield;
import processing.core.PApplet;
import roboearth.wp5.REClients;

import org.knowrob.map.SemanticMapEditorForms;
import org.knowrob.owl.OWLThing;
import org.knowrob.prolog.PrologInterface;
import org.knowrob.vis.applets.MapObjClickListener;
import org.knowrob.vis.applets.PlanVisAppletFsm;
import org.knowrob.vis.applets.SemanticMapVisApplet;
import org.knowrob.vis.themes.GreyTheme;


/**
 * Graphical user interface of the teleoperation application.
 * 
 * @author Moritz Tenorth, tenorth@atr.jp
 *
 */
public class KnowRobGuiApplet extends PApplet implements MapObjClickListener, NotificationListener {

	private static final long serialVersionUID = -284448276454939406L;

	private KnowRobGuiMain op;
	protected ControlP5 controlP5;

	public PlanVisAppletFsm planvis;
	public SemanticMapVisApplet mapvis;
	public SemanticMapEditorForms map_forms;

//	private HashMap<Integer, ListBoxItem> propId2item;
//	private ArrayList<ListBoxItem> currentTypes;

	private ControlWindow notification_window;
	private ControlWindow open_recipe_dialog;
	private String recipeFilePath = null;

	private org.knowrob.gui.KnowRobGuiApplet.NotificationAckEventListener notificationAckListener;
	
	
	/**
	 * Initialize the applet and load the GUI elements and embedded applets.
	 */
	@Override
	public void setup() {
		
//		currentTypes = new ArrayList<ListBoxItem>();
		
		size(1250, 750, P2D);
	    textMode(SCREEN);
	    background(color(20));
		initControlP5();
		frameRate(20);
	}


	/**
	 * Set up the GUI elements, mainly the different tabs and the 
	 * controller groups on each of them.
	 */
	void initControlP5() {
		

	    controlP5 = new ControlP5(this);
	    controlP5.getTab("default").remove();
	    GreyTheme.applyStyle(controlP5);
	    
	    
//	    GreyTheme.applyStyle(controlP5.addTab("files").setLabel("Load files")).setHeight(40).setWidth(180);
//		GreyTheme.applyStyle(controlP5.addGroup("load file", 850, 80, 300).moveTo("files"));
		
		
		GreyTheme.applyStyle(controlP5.addTab("map").setLabel("Environment map")).setHeight(40).setWidth(180);
		GreyTheme.applyStyle(controlP5.addGroup("load map", 810, 30, 170).moveTo("map"));
		GreyTheme.applyStyle(controlP5.addButton("load map from roboearth", 42f, 10, 10, 150, 20).setGroup("load map"));
		GreyTheme.applyStyle(controlP5.addButton("load map from file"     , 42f, 10, 40, 150, 20).setGroup("load map"));
		
		GreyTheme.applyStyle(controlP5.addGroup("save map", 1015, 30, 170).moveTo("map"));
		GreyTheme.applyStyle(controlP5.addButton("save map to roboearth", 42f, 10, 10, 150, 20).setGroup("save map"));
		GreyTheme.applyStyle(controlP5.addButton("save map to file",      42f, 10, 40, 150, 20).setGroup("save map"));

		GreyTheme.applyStyle(controlP5.addTab("action").setLabel("Action Recipe")).setHeight(40).setWidth(180);
		GreyTheme.applyStyle(controlP5.addGroup("load recipe", 50, 80, 170).moveTo("action"));
		GreyTheme.applyStyle(controlP5.addButton("load recipe from roboearth", 44f, 10, 10, 150, 20).setGroup("load recipe"));
		GreyTheme.applyStyle(controlP5.addButton("load recipe from file",      43f, 10, 40, 150, 20).setGroup("load recipe"));

		GreyTheme.applyStyle(controlP5.addGroup("save recipe", 300, 80, 170).moveTo("action"));
		GreyTheme.applyStyle(controlP5.addButton("save recipe to roboearth",   46f, 10, 10, 150, 20).setGroup("save recipe"));
		GreyTheme.applyStyle(controlP5.addButton("save recipe to file",        45f, 10, 40, 150, 20).setGroup("save recipe"));
		
//		GreyTheme.applyStyle(controlP5.addGroup("execute recipe", 550, 80, 170).moveTo("action"));
//		GreyTheme.applyStyle(controlP5.addButton("start execution engine",     47f, 10, 10, 150, 20).setGroup("execute recipe"));
//		GreyTheme.applyStyle(controlP5.addButton("execute on UNR platform",    48f, 10, 40, 150, 20).setGroup("execute recipe"));
		
//
//		GreyTheme.applyStyle(controlP5.addTab("operator").setLabel("Operator console")).setHeight(40).setWidth(180);
//		GreyTheme.applyStyle(controlP5.addGroup("connect as operator", 40, 90, 220).moveTo("operator")).setBackgroundHeight(120);
//		ListBox op_type = GreyTheme.applyStyle(controlP5.addListBox("Operator type", 10, 30, 200, 60), 15).setGroup("connect as operator");
//		GreyTheme.applyStyle(controlP5.addButton("connect", 42f, 168, 90, 42, 20).setGroup("connect as operator"));
		
//		int props_idx = 0;
//		propId2item = new HashMap<Integer, ListBoxItem>();
//		propId2item.put(props_idx, op_type.addItem("general_operator", props_idx++).setText("General operator"));
//		propId2item.put(props_idx, op_type.addItem("map_operator", props_idx++).setText("Environment map operator"));
//		propId2item.put(props_idx, op_type.addItem("recipe_operator", props_idx++).setText("Action recipe operator"));
		
		
		
		planvis = new PlanVisAppletFsm();
		planvis.init();
		planvis.setBounds(0,140,850,700);
		planvis.noLoop();
		
		mapvis = new SemanticMapVisApplet();
		mapvis.init();
		mapvis.setBounds(80, 130, 800,600);
		mapvis.setMapObjClickListener(this);
		mapvis.noLoop();

		
		map_forms = new SemanticMapEditorForms();
		map_forms.frame = this.frame;
		map_forms.setMapVisApplet(mapvis);
		map_forms.init();
		map_forms.setBounds(920, 130, 1200,600);
		map_forms.noLoop();
	}
	

	/**
	 * Draw the background of the window. Other elements are drawn by 
	 * controlP5 and the embedded applets.
	 */
	@Override
	public void draw() {

		background(40);
		controlP5.draw();
	}

	
	/**
	 * Event listener for GUI events
	 * 
	 * @param theEvent ControlEvent triggered by some user interaction
	 */
	public void controlEvent(ControlEvent theEvent) {

		try {
			
			if (theEvent !=null && theEvent.isTab()) {
				
				if(theEvent.getTab().getName().equals("map")) {
					this.remove(planvis);
					planvis.noLoop();
					this.add(mapvis);
					mapvis.loop();
					this.add(map_forms);
					map_forms.loop();
				}
				
				if(theEvent.getTab().getName().equals("action")) {
					this.add(planvis);
					planvis.loop();
					this.remove(mapvis);
					mapvis.noLoop();
					this.remove(map_forms);
					map_forms.noLoop();
				}
//				
//				if(theEvent.getTab().getName().equals("operator")) {
//					this.remove(planvis);
//					planvis.noLoop();
//					this.remove(mapvis);
//					mapvis.noLoop();
//					this.remove(map_forms);
//					map_forms.noLoop();
//				}
				
				
			} else if(theEvent !=null && theEvent.isController()) {
				
				if(theEvent.getController().getName().equals("load recipe from roboearth")) {
					new ActionRecipeDialog(op, this);
				}

				if(theEvent.getController().getName().equals("load recipe from file")) {
					
					recipeFilePath = selectInput();  // opens file chooser
					if (recipeFilePath == null) {
						// no file selected
						return;
						
					} else {
						open_recipe_dialog = controlP5.addControlWindow("Select command", 320, 120).setBackground(50);
						controlP5.addTextlabel("enter command", "Please enter the command or the main OWL class of the recipe:", 6, 20).moveTo(open_recipe_dialog);
						controlP5.addTextfield("command or owl class", 10, 40, 290, 20).moveTo(open_recipe_dialog);
						controlP5.addButton("select_command", 123, 220, 70, 80, 20).moveTo(open_recipe_dialog);
					}
				}
				
				if(theEvent.getController().getName().equals("select_command") || theEvent.getController().getName().equals("command or owl class")) {
					op.setRecipeOwlFile(recipeFilePath, ((Textfield) controlP5.getController("command or owl class")).getText(), null);
					recipeFilePath = null;
					open_recipe_dialog.hide();
					open_recipe_dialog=null;
				}
				
				if(theEvent.getController().getName().equals("load map from roboearth")) {
					new EnvironmentMapDialog(op, this);
				}
				
				if(theEvent.getController().getName().equals("load map from file")) {
					map_forms.selectAndLoadInputFile();
					op.setMapURL(null);
				}
				
				if(theEvent.getController().getName().equals("save map to file")) {
					map_forms.saveMapToFile(map_forms.t_filename.getText());
				}
				
				if(theEvent.getController().getName().equals("save map to roboearth")) {
					
					// save map to temp file
					map_forms.saveMapToFile("/tmp/" + OWLThing.getFilenameOfIRI(op.getMapURL()));
					
					if(op.getMapURL()!=null) { // update existing map
						
						REClients.updateMap("/tmp/" + OWLThing.getFilenameOfIRI(op.getMapURL()),
								OWLThing.getFilenameOfIRI(op.getMapURL()),
								"Map created by semantic map editor" );
						
												
					} else { // create new map
						
						// TODO: explicitly create new map (have a button for this) and set the map's name and room/street/etc properties
						REClients.submitMap("/tmp/" + OWLThing.getFilenameOfIRI(op.getMapURL()),
								"sem_map",
								OWLThing.getShortNameOfIRI(op.getMapInstance()),
								"Map created by semantic map editor" );
					}
				}

				if(theEvent.getController().getName().equals("save recipe to roboearth")) {

					// update action in Prolog
					planvis.syncWithProlog();

					// update recipe in RoboEarth
					if(op.getRecipeURL()!=null) { // update existing recipe

						PrologInterface.executeQuery("re_update_action_recipe('" + 	planvis.getCurrTask().getIRI() + "', '"+
								OWLThing.getFilenameOfIRI(op.getRecipeURL()) 
								+"', 'Recipe created by recipe editor')");

					} else { // create new recipe

						
						String shortname = planvis.getCurrTask().getShortName().toLowerCase();
						PrologInterface.executeQuery("re_submit_action_recipe('" + 	planvis.getCurrTask().getIRI() + "', '"+ 
								shortname + "', '"+ 
								shortname +"', 'Recipe created by recipe editor')");
						
						op.setRecipeURL("http://api.roboearth.org/api/recipe/" + shortname + "." + shortname);
					}
				}

				if(theEvent.getController().getName().equals("save recipe to file")) {

					String savePath = selectOutput();  // opens file chooser
					if (savePath == null) {
						// no file selected
						return;
						
					} else {
						
						// update action in Prolog
						planvis.syncWithProlog();

						if(new File(savePath).isDirectory()) {
							savePath += File.separator + planvis.getCurrTask().getShortName() + ".owl";
						}
						
						// export as file
						PrologInterface.executeQuery("export_action('" + planvis.getCurrTask().getIRI() + "', '"+savePath+"')");
					}
				}
				
//				if(theEvent.getController().getName().equals("execute on UNR platform")) {
//					op.executeRecipeClass();
//				}
//				
//				
//			} else if(theEvent !=null && theEvent.isGroup()) {
//
//				if(theEvent.getGroup().getName().equals("Operator type")) {
//
//					ListBoxItem item = propId2item.get((int)theEvent.getValue());
//					if(item!=null) {
//						if(currentTypes.contains(item)) {
//							
//							// deactivate
//							item.setColorBackground(color(80));
//							currentTypes.remove(item);
//							
//						} else {
//							
//							// activate
//							item.setColorBackground(color(120));
//							currentTypes.add(item);
//						}
//					}
//				}
			}
			
		} catch (Exception e ) {
			e.printStackTrace();
		}
	}
	

	public void setFrame(Frame frame) {
		this.frame = frame;
	}
	
	public KnowRobGuiMain getTeleopMain() {
		return op;
	}

	public void setOperatorMain(KnowRobGuiMain op) {
		this.op = op;
	}
	
	
	

	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// HUMAN OPERATOR WINDOW
	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 


	public void showNotification(String note) {
		
		if(notification_window==null) {
			notification_window = controlP5.addControlWindow("controlP5window",200,200,550,230)
						 .setBackground(color(40))
						 .setUpdateMode(ControlWindow.NORMAL)
						 .setTitle("Notification");
		}
		
		if(controlP5.getController("notification")==null)
			controlP5.addTextlabel("notification", "A notification from the UNR Platform:", 20, 20).moveTo(notification_window);
		
		if(controlP5.getGroup("note")==null) {
			controlP5.addTextarea("note",  "" , 20, 40, 510, 140).moveTo(notification_window).setText(note).setColorBackground(color(80));
		
		} else {
			((Textarea) controlP5.get("note")).setText(((Textarea) controlP5.get("note")).getText() + "\n\n" + note);
		}
		
		if(controlP5.getController("notification")==null) {
			Button b = controlP5.addButton("notification-ack", 1, 20, 190, 30, 20).setCaptionLabel(" OK").moveTo(notification_window);
			notificationAckListener = new NotificationAckEventListener();
			b.addListener(notificationAckListener);
		}
		
		notification_window.show();
		System.err.println(note);
		
	}
	
	public class NotificationAckEventListener implements ControlListener {

		public void controlEvent(ControlEvent ev) {
			if(ev.getController().getName().equals("notification-ack")) {
				((Textarea) controlP5.get("note")).setText("");
				notification_window.hide();
			}
		}
	}


	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// HUMAN OPERATOR WINDOW
	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 



	@Override
	public void mapObjectClicked(String identifier) {
		
		if(controlP5.getTab("map").isActive()) {
			this.map_forms.editObject(OWLThing.removeSingleQuotes(OWLThing.getShortNameOfIRI(identifier)));
		}
		
	}
	

}
