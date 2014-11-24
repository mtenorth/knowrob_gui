/* \file REClients.java
 * \brief RoboEarth Client code for being called from Prolog
 * 
 * This file is part of the RoboEarth ROS re_comm package.
 * 
 * It was originally created for <a href="http://www.roboearth.org/">RoboEarth</a>.
 * The research leading to these results has received funding from the 
 * European Union Seventh Framework Programme FP7/2007-2013 
 * under grant agreement no248942 RoboEarth.
 *
 * Copyright (C) 2010 by 
 * <a href=" mailto:tenorth@cs.tum.edu">Moritz Tenorth</a>
 * Technische Universitaet Muenchen
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    <UL>
 *     <LI> Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     <LI> Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     <LI> Neither the name of Willow Garage, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *    </UL>
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * \author Moritz Tenorth
 * \version 1.0
 * \date 2010
 * \image html http://www.roboearth.org/sites/default/files/RoboEarth.org_logo.gif
 * \image latex http://www.roboearth.org/sites/default/files/RoboEarth.org_logo.gif
 */
package roboearth.wp5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import roboearth.wp5.conn.REConnectionHadoop;
import roboearth.wp5.owl.OWLIO;
import roboearth.wp5.util.Util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;


public class REClients {

	static final String API_KEY = "6e6574726f6d40b699e442ebdca5850e7cb7486679768aec3c70";


	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// 
	// Download object information from the DB
	//


	/**
	 * Sends a 'semantic query' to the RoboEarth DB to search for models that 
	 * re:providesModelFor the desired object class
	 * 
	 * @param objclass The object class a model is searched for
	 * @return Array of URL strings pointing to the object models
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static String[] requestModelFor(String objclass) throws IOException, ParserConfigurationException, SAXException {

		String q = "SELECT source FROM CONTEXT source\n" +
				"{S} roboearth:providesModelFor {"+objclass.replace("'", "")+"}\n" +
				"USING NAMESPACE\n" +
				"roboearth=<http://www.roboearth.org/kb/roboearth.owl#>,\n"+
				"knowrob=<http://knowrob.org/kb/knowrob.owl#>";

//		CommunicationVisApplet.visualizeCommunication("Requesting model for object '"+objclass.replace("'", "")+"' from RoboEarth..." + q, "", "pr2.jpg", "roboearth.png");
		//		System.err.println("\nRequesting model for: " + objclass.replace("'", ""));

		String res;
		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		res = conn.queryObjectDB(q);

		res = res.replace("\\n", "").replace("\\t", "");
		res = res.substring(1, res.length()-1);


		ArrayList<String> obj_urls = new ArrayList<String>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.newSAXParser().parse(new InputSource(new StringReader(res)), new SparqlReader(obj_urls));

		return obj_urls.toArray(new String[0]);
	}



	public static String downloadModelFrom(String url) throws IOException, ParserConfigurationException, SAXException {

		ArrayList<String> outFilenames = new ArrayList<String>();
		ArrayList<String> outFileURLs = new ArrayList<String>();

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		String objDir = Util.tmpDir + Util.getFilenameFromURL(url); 
		String filename = objDir +".owl";

		FileWriter out = new FileWriter(filename);
		String obj = conn.requestObjectFromURL(url, outFilenames, outFileURLs);
		out.write(obj==null?"":obj);
		out.close();

		if (outFileURLs.size() > 0) {
			File dir = new File(objDir);
			if (dir.exists()) {
				Util.deleteFolderRec(dir, false);	
			} else {
				dir.mkdir();	
			}

			for(int i=0;i<outFileURLs.size();i++) {
				conn.requestBinaryFile(new URL(outFileURLs.get(i)), objDir);
			}
		}

		//		System.err.print("Model downloaded\n\n\n");

//		CommunicationVisApplet.visualizeCommunication("", "Received object model "+ Util.getFilenameFromURL(url), null, "roboearth.png");
		return filename;
	}



	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// 
	// Download action recipes from the DB
	//

	/**
	 * Sends a 'semantic query' to the RoboEarth DB to search for action recipes that 
	 * perform the task specified as a rdfs:label
	 * 
	 * @param command String command for which a recipe is searched
	 * @return Array of URL strings pointing to the recipe specifications
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static String[] requestActionRecipeFor(String command) throws IOException, ParserConfigurationException, SAXException {


		String q = "SELECT source FROM CONTEXT source\n" +
				"{S} rdfs:label {\""+command.replace("'", "")+"\"^^xsd:string}\n" +
				"USING NAMESPACE\n" +
				"rdfs=<http://www.w3.org/2000/01/rdf-schema#>\n";

//		CommunicationVisApplet.visualizeCommunication("Requesting action recipe for '"+command.replace("'", "")+"' from RoboEarth...\n"+q, "", "pr2.jpg", "roboearth.png");

		String res;
		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		res = conn.queryActionRecipeDB(q);

		res = res.replace("\\n", "").replace("\\t", "");
		res = res.substring(1, res.length()-1);
		//System.out.println(res);


		ArrayList<String> act_urls = new ArrayList<String>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.newSAXParser().parse(new InputSource(new StringReader(res)), new SparqlReader(act_urls));

		return act_urls.toArray(new String[0]);
	}

	/**
	 * Sends a 'semantic query' to the RoboEarth DB to search for action recipes 
	 * having a certain class.
	 * 
	 * @param recipeClass OWL class of recipe
	 * @return Array of URL strings pointing to the recipe specifications
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static String[] requestActionRecipeClass(String recipeClass) throws IOException, ParserConfigurationException, SAXException {

		String q = "SELECT source FROM CONTEXT source\n" +
			"{<" + recipeClass + ">} rdf:type {owl:Class}\n" +
			"USING NAMESPACE\n" +
			"knowrob=<http://knowrob.org/kb/knowrob.owl#>,owl=<http://www.w3.org/2002/07/owl#>";

//		CommunicationVisApplet.visualizeCommunication("Requesting action recipe for '"+command.replace("'", "")+"' from RoboEarth...\n"+q, "", "pr2.jpg", "roboearth.png");
		String res;
		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		res = conn.queryActionRecipeDB(q);

		res = res.replace("\\n", "").replace("\\t", "");
		res = res.substring(1, res.length()-1);
		//System.out.println(res);


		ArrayList<String> act_urls = new ArrayList<String>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.newSAXParser().parse(new InputSource(new StringReader(res)), new SparqlReader(act_urls));

		return act_urls.toArray(new String[0]);
	}

	public static String downloadRecipeFrom(String url) throws IOException, ParserConfigurationException, SAXException {

		//System.err.println(url);

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
		String filename = Util.tmpDir + Util.getFilenameFromURL(url)+".owl";

		FileWriter out = new FileWriter(filename);
		out.write(conn.requestActionRecipeFromURL(url));
		out.close();

//		CommunicationVisApplet.visualizeCommunication(null, "Received recipe "+ Util.getFilenameFromURL(url), "pr2.jpg", "roboearth.png");

		return filename;
	}




	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// 
	// Download map information from the DB
	//


	/**
	 * Sends a 'semantic query' to the RoboEarth DB to search for an environment model
	 * 
	 * @param roomNumber Environment for which to search (currently: room number)
	 * @return Array of URL strings pointing to the recipe specifications
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static String[] requestEnvironmentMapFor(String[][] roomQuery) throws IOException, ParserConfigurationException, SAXException {

		String q = "SELECT source FROM CONTEXT source\n" +
				"{A} kr:describedInMap {Z} ,\n";

		ArrayList<String> constr = new ArrayList<String>();
		ArrayList<String> where  = new ArrayList<String>();

		char idx = 'A';
		for(String[] constraint : roomQuery) {

			if(idx!='A'){
				constr.add("{"+idx+"} kr:properPhysicalParts {"+(char)(idx-1)+"}");	
			}

			String var = constraint[0].split(":")[1]; 
			var = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, var);
			var = "V"+var;  // avoid problems with reserved words like 'label' 

			constr.add("{"+idx+"} " + constraint[0] + " {"+var+"}");
			where.add(var+" LIKE \""+constraint[1]+"\"");

			idx++;
		}

		q+= Joiner.on(" , \n").join(constr);
		q+="\nWHERE\n" + Joiner.on("\nAND ").join(where);

		q += "\nUSING NAMESPACE\n" +
				"re=<http://www.roboearth.org/kb/roboearth.owl#>,\n" + 
				"rdfs=<http://www.w3.org/2000/01/rdf-schema#>,\n" +
				"kr=<http://knowrob.org/kb/knowrob.owl#> ";


//		CommunicationVisApplet.visualizeCommunication("Requesting map from RoboEarth..." + q, "", "pr2.jpg", "roboearth.png");

		String res;
		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		System.err.println(q);

		res = conn.queryEnvironmentDB(q);

		res = res.replace("\\n", "").replace("\\t", "");
		res = res.substring(1, res.length()-1);


		ArrayList<String> env_urls = new ArrayList<String>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.newSAXParser().parse(new InputSource(new StringReader(res)), new SparqlReader(env_urls));

		return env_urls.toArray(new String[0]);
	}


	public static String downloadEnvironmentMapFrom(String url, String robotUID, ArrayList<String> outFilenames) throws IOException, ParserConfigurationException, SAXException {

		ArrayList<String> outFileURLs = new ArrayList<String>();

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		String envDir = Util.tmpDir + Util.getFilenameFromURL(url); 
		String filename = envDir + ".owl";

		FileWriter out = new FileWriter(filename);
		String env = conn.requestEnvironmentFromURL(url);
		if (env != null) {
			out.write(env);
			out.close();			
		} else {
			System.err.println("Error: environment '" + url + "' couldn't be found.");
			return null;
		}

		File dir = new File(envDir);
		if (dir.exists()) {
			Util.deleteFolderRec(dir, false);	
		} else {
			dir.mkdir();	
		}

		// read file names from OWL
		OWLOntology owlMap = OWLIO.loadOntologyFromFile(filename);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		DefaultPrefixManager pm = new DefaultPrefixManager("http://knowrob.org/kb/knowrob.owl#");
		pm.setPrefix("knowrob:", "http://knowrob.org/kb/knowrob.owl#");
		pm.setPrefix("roboearth:", "http://www.roboearth.org/kb/roboearth.owl#");

		OWLDataProperty linkToMapFile   = factory.getOWLDataProperty("roboearth:linkToMapFile", pm);
		OWLDataProperty linkToImageFile = factory.getOWLDataProperty("roboearth:linkToImageFile", pm);
		OWLClass octomapCls = factory.getOWLClass("roboearth:OctoMap", pm);

		for(OWLIndividual ind : owlMap.getIndividualsInSignature()) {

			Set<OWLClassExpression> classExpressions = ind.getTypes(owlMap);
			for (OWLClassExpression owlExpr : classExpressions) {

				// special treatment for octomaps (extract robot specific 2dmap)
				if (owlExpr.asOWLClass().equals(octomapCls)) {

					// download robot SRDL document
					String srdlString = conn.requestRobot(robotUID);
					if (srdlString != null && !srdlString.isEmpty()) {
						OWLOntology srdl = OWLIO.loadOntologyFromString(srdlString);

						// request 2d map
						String autoMapFilename = "auto_2d_loc_map";
						String baseLaserLink = "http://knowrob.org/kb/pr2.owl#pr2_base_laser"; // TODO auto-extract link name from SRDL?

						ArrayList<byte[]> mapBytes;
						mapBytes = conn.request2dMap(Util.getFilenameFromURL(url), srdl, baseLaserLink, autoMapFilename);
						if (Util.writeFile(envDir, autoMapFilename+".pgm", mapBytes.get(0))) {
							outFilenames.add(envDir + File.separator + autoMapFilename+".pgm");	
						}
						if (Util.writeFile(envDir, autoMapFilename+".yaml", mapBytes.get(1))) {
							outFilenames.add(envDir + File.separator + autoMapFilename+".yaml");	
						}						
					} else {
						System.err.println("Error: Couldn't find robot '" + robotUID + "' in RoboEarthDB ");
					}

				} 

			}

			for(OWLDataPropertyAssertionAxiom dataprop : owlMap.getDataPropertyAssertionAxioms(ind)) {
				if(dataprop.getProperty().equals(linkToMapFile) || dataprop.getProperty().equals(linkToImageFile)) {
					String linkUrl = dataprop.getObject().getLiteral().replaceAll("\\s", "");
					outFileURLs.add(linkUrl);
				}
			}

		}

		for(int i=0;i<outFileURLs.size();i++) {
			File outfile = conn.requestBinaryFile(new URL(outFileURLs.get(i)), envDir);
			outFilenames.add(outfile.getAbsolutePath());
		}

//		CommunicationVisApplet.visualizeCommunication("", "Received environment maps "+ Util.getFilenameFromURL(url), "pr2.jpg", "roboearth.png");
		return filename;

	}


	public static String downloadEnvironmentMapFrom(String url, ArrayList<String> outFilenames) throws IOException, ParserConfigurationException, SAXException {

		ArrayList<String> outFileURLs = new ArrayList<String>();

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		String envDir = Util.tmpDir + Util.getFilenameFromURL(url); 
		String filename = envDir + ".owl";

		FileWriter out = new FileWriter(filename);
		out.write(conn.requestEnvironmentFromURL(url));
		out.close();

		// read file names from OWL
		OWLOntology owlMap = OWLIO.loadOntologyFromFile(filename);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLDataFactory factory = manager.getOWLDataFactory();
		DefaultPrefixManager pm = new DefaultPrefixManager("http://knowrob.org/kb/knowrob.owl#");
		pm.setPrefix("knowrob:", "http://knowrob.org/kb/knowrob.owl#");
		pm.setPrefix("roboearth:", "http://www.roboearth.org/kb/roboearth.owl#");

		OWLDataProperty linkToMapFile   = factory.getOWLDataProperty("roboearth:linkToMapFile", pm);
		OWLDataProperty linkToImageFile = factory.getOWLDataProperty("roboearth:linkToImageFile", pm);

		for(OWLIndividual ind : owlMap.getIndividualsInSignature()) {
			for(OWLDataPropertyAssertionAxiom dataprop : owlMap.getDataPropertyAssertionAxioms(ind)) {

				if(dataprop.getProperty().equals(linkToMapFile) || dataprop.getProperty().equals(linkToImageFile)) {
					String linkUrl = dataprop.getObject().getLiteral().replaceAll("\\s", "");
					outFileURLs.add(linkUrl);
				}
			}
		}

		if (outFileURLs.size() > 0) {
			File dir = new File(envDir);
			if (dir.exists()) {
				Util.deleteFolderRec(dir, false);	
			} else {
				dir.mkdir();	
			}



			for(int i=0;i<outFileURLs.size();i++) {
				File outfile = conn.requestBinaryFile(new URL(outFileURLs.get(i)), envDir);
				outFilenames.add(outfile.getAbsolutePath());
			}
		}

//		CommunicationVisApplet.visualizeCommunication("", "Received environment maps "+ Util.getFilenameFromURL(url), "pr2.jpg", "roboearth.png");
		return filename;

	}




	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// 
	// Submit new information to the DB
	//
	public static void	submitObject(String owl_filename, String id, String cls, String description) {

//		CommunicationVisApplet.visualizeCommunication("Uploading information for object '"+id+"' in RoboEarth...", "", "pr2.jpg", "roboearth.png");

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
		conn.submitObject(readOWLtoString(owl_filename), cls, id, description);

//		CommunicationVisApplet.visualizeCommunication("", ""+ res, "pr2.jpg", "roboearth.png");
	}


	public static void submitActionRecipe(String owl_filename, String id, String cls, String description){

//		CommunicationVisApplet.visualizeCommunication("Uploading information for recipe '"+id+"' in RoboEarth...", "", "pr2.jpg", "roboearth.png");

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
		conn.submitActionRecipe(readOWLtoString(owl_filename), cls, id, description);

//		CommunicationVisApplet.visualizeCommunication("", ""+ res, "pr2.jpg", "roboearth.png");
	}


	public static void submitMap(String owl_filename, String id, String cls, String description){

//		CommunicationVisApplet.visualizeCommunication("Uploading information for map '"+id+"' in RoboEarth...", "", "pr2.jpg", "roboearth.png");

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
		conn.submitEnvironment(readOWLtoString(owl_filename), cls, id, description);

//		CommunicationVisApplet.visualizeCommunication("", ""+ res, "pr2.jpg", "roboearth.png");
	}



	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// 
	// Update information in the DB
	// 

	public static void updateObjectOWL(String owl_filename, String uid, String description) {

//		CommunicationVisApplet.visualizeCommunication("Updating information for '"+uid+"' in RoboEarth...", "", "pr2.jpg", "roboearth.png");

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
		conn.updateObject(uid, readOWLtoString(owl_filename), description);
//		CommunicationVisApplet.visualizeCommunication("", ""+ res, "pr2.jpg", "roboearth.png");

	}



	public static void updateActionRecipe(String owl_filename, String id, String description) {

//		CommunicationVisApplet.visualizeCommunication("Updating information for action recipe '"+id+"' in RoboEarth...", "", "pr2.jpg", "roboearth.png");

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
		conn.updateActionRecipe(id, readOWLtoString(owl_filename), description);

//		CommunicationVisApplet.visualizeCommunication("", ""+ res, "pr2.jpg", "roboearth.png");
	}


	public static void updateMap(String owl_filename, String id, String description) {

//		CommunicationVisApplet.visualizeCommunication("Updating information for map '"+id+"' in RoboEarth...", "", "pr2.jpg", "roboearth.png");

		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
		conn.updateEnvironment(id, readOWLtoString(owl_filename), description);

//		CommunicationVisApplet.visualizeCommunication("", ""+ res, "pr2.jpg", "roboearth.png");

	}





	// // // // // // // // // // // // // // // // // // // // // // // // // // // // // // 
	// 
	// SAX SPARQL reader methods
	// 

	/**
	 * Reader to extract URIs from the XML-encoded query result
	 */
	private static class SparqlReader extends DefaultHandler {

		private ArrayList<String> urls;

		public SparqlReader(ArrayList<String> urls) {
			this.urls = urls;
		} 

		protected boolean uri = false;
		public void startElement(String uri, String name, String qName, Attributes attrs) {
			if(qName.equals("uri")) {
				this.uri = true;
			}
		}

		@Override
		public void characters( char[] ch, int start, int length ) {

			if(uri) {
				urls.add(new String(ch).substring(start, start+length));
				this.uri=false;
			}
		}
	}




	private static String readOWLtoString(String owl_filename) {

		String objectOwl = "";
		try {
			BufferedReader reader = new BufferedReader( new FileReader (new File(owl_filename)));
			String line  = null;

			while( ( line = reader.readLine() ) != null ) {
				objectOwl+= line + System.getProperty("line.separator");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return objectOwl;
	}
}
