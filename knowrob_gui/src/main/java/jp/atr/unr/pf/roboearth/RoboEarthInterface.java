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
package jp.atr.unr.pf.roboearth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;

import roboearth.wp5.conn.REConnectionHadoop;

/**
 * @author Moritz Tenorth, tenorth@atr.jp
 */
public class RoboEarthInterface {

	static final String API_KEY = "6e6574726f6d40b699e442ebdca5850e7cb7486679768aec3c70";

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
	public static String requestActionRecipeFor(String command) {

		String[] recipes = searchActionRecipesFor(command);
		downloadRecipeFromUrl(recipes[0]);
		
		return null;
	}


	
	public static String[] searchActionRecipesFor(String command) {

		String res;
		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		try {

			res = conn.queryActionRecipeDB("SELECT source FROM CONTEXT source\n" +
					"{S} rdfs:label {\""+command.replace("'", "")+"\"^^xsd:string}\n" +
					"USING NAMESPACE\n" +
			"rdfs=<http://www.w3.org/2000/01/rdf-schema#>\n");
			
//String q = "SELECT source FROM CONTEXT source\n" +
//		"{S} rdfs:label {\""+command.replace("'", "")+"\"^^xsd:string}\n" +
//		"USING NAMESPACE\n" +
//"rdfs=<http://www.w3.org/2000/01/rdf-schema#>\n";
//CommunicationVisApplet.visualizeCommunication("Requesting action recipe for '"+command.replace("'", "")+"' from RoboEarth...\n"+q, "", "pr2.jpg", "roboearth.png");
			
			res = res.replace("\\n", "").replace("\\t", "");
			res = res.substring(1, res.length()-1);

			ArrayList<String> act_urls = new ArrayList<String>();
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(false);
			factory.newSAXParser().parse(new InputSource(new StringReader(res)), new SparqlReader(act_urls));

//			String act_names = "";
			
			ArrayList<String> recipes = new ArrayList<String>();
			for(String url : act_urls) {
				recipes.add(getFilenameFromURL(url));
				
//				act_names += getFilenameFromURL(url) + ", ";
			}
//CommunicationVisApplet.visualizeCommunication(null, "Received action recipe "+ act_names, "pr2.jpg", "roboearth.png");
			return recipes.toArray(new String[0]);
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			System.err.println("Parser problem while trying to parse XML response from RoboEarth");
			e.printStackTrace();
		} catch (SAXException e) {
			System.err.println("Could not parse XML response from RoboEarth");
			e.printStackTrace();
		}
		return null;
	}

	
	
	public static String downloadRecipeFromUrl(String url) {
		
		String filename=null;
		try{

//CommunicationVisApplet.visualizeCommunication("Downloading recipe from "+ Util.getFilenameFromURL(url), "", "pr2.jpg", "roboearth.png");
			
			REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
			new File("/tmp/re_comm_core/tmp/").mkdirs();
			filename = "/tmp/re_comm_core/tmp/" + getFilenameFromURL(url)+".owl";

			FileWriter out = new FileWriter(filename);
			out.write(conn.requestActionRecipeFromURL(url));
			out.close();
			
//CommunicationVisApplet.visualizeCommunication(null, "Received recipe "+ Util.getFilenameFromURL(url), "pr2.jpg", "roboearth.png");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return filename;
	}
	


	public static String[] searchObjectModelsFor(String query) {
	
		System.err.println("Object model download not implemented.");
		return null;
	}



	/**
	 * Sends a 'semantic query' to the RoboEarth DB to search for an environment model
	 * 
	 * @param roomNumber Environment for which to search (currently: room number)
	 * @return Array of URL strings pointing to the recipe specifications
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static String[] searchEnvironmentMapsFor(String[][] roomQuery) throws IOException, ParserConfigurationException, SAXException {
	
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
			"kr=<http://ias.cs.tum.edu/kb/knowrob.owl#> ";

//CommunicationVisApplet.visualizeCommunication("Requesting map from RoboEarth..." + q, "", "pr2.jpg", "roboearth.png");
		
		String res;
		REConnectionHadoop conn = new REConnectionHadoop(API_KEY);

		
		res = conn.queryEnvironmentDB(q);

		res = res.replace("\\n", "").replace("\\t", "");
		res = res.substring(1, res.length()-1);
				
		ArrayList<String> env_urls = new ArrayList<String>();
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.newSAXParser().parse(new InputSource(new StringReader(res)), new SparqlReader(env_urls));
		
//		String map_names = "";
		
		ArrayList<String> maps = new ArrayList<String>();
		for(String url : env_urls) {
			maps.add(getFilenameFromURL(url));
			
//			map_names += getFilenameFromURL(url) + ", ";
		}
//CommunicationVisApplet.visualizeCommunication(null, "Received environment maps "+ map_names, "pr2.jpg", "roboearth.png");
		
		return maps.toArray(new String[0]);
		
	}

	
	public static String downloadMapFromUrl(String url) {
		
		String filename=null;
		try{

//CommunicationVisApplet.visualizeCommunication("Downloading environment map from "+ Util.getFilenameFromURL(url), "", "pr2.jpg", "roboearth.png");

			
			REConnectionHadoop conn = new REConnectionHadoop(API_KEY);
			new File("/tmp/re_comm_core/tmp/").mkdirs();
			filename = "/tmp/re_comm_core/tmp/" + getFilenameFromURL(url)+".owl";

			FileWriter out = new FileWriter(filename);
			out.write(conn.requestEnvironmentFromURL(url));
			out.close();
			
//CommunicationVisApplet.visualizeCommunication("", "Received environment maps "+ Util.getFilenameFromURL(url), "pr2.jpg", "roboearth.png");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return filename;
	}
	
	
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




	/**
	 * Retrieves the last part of the given URL that starts at the index of the 
	 * last occurrence of '/' + 1. If no '/' was found, the input string gets 
	 * returned.
	 * @param url a URL
	 * @return the part of the URL after the last occurrence of '/'
	 */
	public static String getFilenameFromURL(String url) {

		if (url == null) {
			return null;
		} else {
			return url.substring(url.lastIndexOf("/")+1, url.length());	
		}

	}
}
