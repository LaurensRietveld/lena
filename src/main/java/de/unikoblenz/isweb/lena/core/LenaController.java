/*
*
* LENA - a Fresnel LEns based RDF/Linked Data NAvigator with SPARQL selector support.
* Copyright (C) 2009 Thomas Franz, Joerg Koch, Renata Dividino
* This file is part of Lena
*
* LENA is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* LENA is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with LENA. If not, see <http://www.gnu.org/licenses/>.
*
* LENA uses libraries from the Sesame Project for license details
* see http://www.openrdf.org/license.jsp
*/ 
package de.unikoblenz.isweb.lena.core;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.unikoblenz.isweb.metak4lena.SesameRenderer;
import fr.inria.jfresnel.Constants;
import fr.inria.jfresnel.FresnelDocument;
import fr.inria.jfresnel.Lens;
import fr.inria.jfresnel.fsl.sesame.FSLSesameEvaluator;
import fr.inria.jfresnel.sesame.FresnelSesameParser;


/**
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class LenaController {
	
	private String resource;
	private String lens;
	private String clazz;
	private String location;
	private String meta;
	private String page;
	
	private File xslFile;
	private File xslFileRemote;
	//FSLNSResolver nsr;
	//FSLHierarchyStore fhs;
	FSLSesameEvaluator fje;
	FresnelDocument fd;
	FresnelDocument defaultLenses;
	LenaConfig lenaConfig;
	String defaultResourceLens="http://isweb.uni-koblenz.de/lena/defaultResourceLens";
	String defaultClassLens="http://isweb.uni-koblenz.de/lena/defaultClassLens";
	
	private SesameRenderer sesameRenderer;	
	private LenaRenderer lenaRenderer;
	
	private StringBuffer errorMessage=new StringBuffer();
	private int totalResources  = 0;
	
	public LenaController(LenaConfig lenaConfig) {
		System.out.println("Initialising JFresnelEngine");
		this.lenaConfig=lenaConfig;
		
		lenaRenderer = new LenaRenderer();
		sesameRenderer = new de.unikoblenz.isweb.metak4lena.SesameRenderer();
		
		//nsr = new FSLNSResolver();
		// add namespaces ...
		/*nsr.addPrefixBinding("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"); 
		nsr.addPrefixBinding("rdfs", "http://www.w3.org/2000/01/rdf-schema#"); 
		nsr.addPrefixBinding("foaf", "http://xmlns.com/foaf/0.1/");
		nsr.addPrefixBinding("fresnel", "<http://www.w3.org/2004/09/fresnel#>");
		nsr.addPrefixBinding("rss","<http://purl.org/rss/1.0/>");
		nsr.addPrefixBinding("ex","<http://example.org/>");
		nsr.addPrefixBinding("content","<http://purl.org/rss/1.0/modules/content/>");
		nsr.addPrefixBinding("xsd","<http://www.w3.org/2001/XMLSchema#>");
		nsr.addPrefixBinding("edns","<http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#>");
		nsr.addPrefixBinding("dolite","<http://www.loa-cnr.it/ontologies/DOLCE-Lite.owl#>");
			*/
		//fhs = new FSLSesameHierarchyStore();
		// add ontologies that should be considered in fsl evaluation
		//fhs.addOntology(docURI, locationURL)
		
		//fje = new FSLSesameEvaluator(nsr, fhs);
		//fje = new FSLSesameEvaluator();
		
		// instantiate a Fresnel parser that will create lenses and formats to be applied on a Sesame model 
		//FresnelSesameParser fp = new FresnelSesameParser(nsr, fhs); 
		
		FresnelSesameParser fp = new FresnelSesameParser();
		fd = new FresnelDocument();
		defaultLenses = new FresnelDocument();
		
		// actually parse a Fresnel document, written in Notation 3 

	
		/*loading the RDF data from f file in the Sesame store*/
		/*System.out.println("here!!");
		Repository fresnelRepository = new SailRepository(new MemoryStore());
		System.out.println("here**!");
		try {
		    fresnelRepository.initialize();
		    System.out.println("here1*!!");
		    RepositoryConnection connection = fresnelRepository.getConnection();
		    System.out.println("here1**!!");
		    connection.add(lensFile, lensFile.toURL().toString(), RDFFormat.N3);
		    System.out.println("here1***!!");
		    FSLNSResolver nsr = null;
			if (nsr == null) {
				RepositoryResult<Namespace> nsi = connection.getNamespaces();
				Namespace ns;
				nsr = new FSLNSResolver();
				while (nsi.hasNext()){
					ns = nsi.next();
					nsr.addPrefixBinding(ns.getPrefix(), ns.getName());
				}
			}
			System.out.println("here2!!");
		}
		catch (Exception ex){
		    System.out.println("Fresnel: Error: Failed to load RDF data from " + lensFile.toString());
		    ex.printStackTrace();
		}	*/
		
		fd = fp.parse(lenaConfig.getLensFile(), Constants.N3_READER);	

		
		//The lenses, formats and groups can then be obtained as follows:
		/*
		Lens[] lenses = fd.getLenses();
		System.out.println("LENSES found:");		
		for (Lens l:lenses){
			System.out.println(l);
		}
		fr.inria.jfresnel.Format[] formats = fd.getFormats();
		System.out.println("FORMATS found:\n");
		for (fr.inria.jfresnel.Format f:formats) {
			System.out.println(f);
		}
		*/
		
		// init default lens fresnel document
		defaultLenses=fp.parse(lenaConfig.getDefaultLensFile(), Constants.N3_READER);
		
		
		//renderer=new SesameRenderer();
		//renderer=new LenaRenderer();
		rendererSetFresnelRepository(fp.getFresnelRepository());
		init();
		
		System.out.println("Initialization of JFresnelEngine successfully finished!");		
	}

	

	
	private void init() {
		errorMessage = new StringBuffer();

		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		System.setProperty("org.openrdf.repository.debug", "true");

		// XSL files.

		try {
			xslFile = new File(this.getClass().getResource("/de/unikoblenz/isweb/lena/transform.xsl").toURI());
			xslFileRemote = new File(this.getClass().getResource("/de/unikoblenz/isweb/lena/transformRemote.xsl").toURI());
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			errorMessage.append(e.toString() + "<br/>");
			e.printStackTrace();
		}
	}
	
	/*File lensFile=lenaConfig.getLensFile();
	fd = fp.parse(lensFile, Constants.N3_READER);*/	
	public FresnelDocument getFresnelDocument(){ 
		return fd;
	}
	
	/**
	 * Get all existing classes of the data repository.
	 * 
	 * @return String A string representing the complete classes menu.
	 * @throws UnsupportedEncodingException
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public String getClasses(String location, String meta) throws UnsupportedEncodingException {
		System.out.println("Loading Classes...");
		String out = "";
		String dataLocation = "";
		String dataMeta = "";
		RepositoryConnection con=null;
		try {

			if (location == "local") {
				out = "<div id='lensesAndClasses'><h3>Local Classes: </h3><hr><menu id='lclasses'>";				
				con=lenaConfig.getLocalRepository().getConnection();
				dataLocation = "&location=local";				
			} else if (location=="remote"){
				out = "<div id='lensesAndClasses'><h3>Remote Classes: </h3><hr><menu id='rclasses'>";
				con=lenaConfig.getRemoteRepository().getConnection();
				dataLocation = "&location=remote";
			}
			if (meta.equalsIgnoreCase("true")) {			
				dataMeta = "&meta=true";
			} else if (meta.equalsIgnoreCase("false")){
				dataMeta = "&meta=false";
			}
			if (con!=null) {
				try {
					String query = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?o WHERE { ?s rdf:type ?o } ORDER BY ?o";
	
					TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
	
					TupleQueryResult result = tupleQuery.evaluate();
					try {
						String firstBindingName = result.getBindingNames().get(0);
						while (result.hasNext()) {
							Value uri = result.next().getBinding(firstBindingName).getValue();
							if ((uri instanceof URI) && 
								(!uri.toString().contains("http://www.w3.org/2004/03/trix"))){
							
								String uriString = uri.toString();								
								String classInfo = getClassInfo(uriString, location);
								String before = "";
								String after = "";
	
								/*
								if (uriString == resource) {
									before = "<b>";
									after = "</b>";
								}*/
	
								if (uriString.lastIndexOf("#") == -1) {
									out = out.concat("<li><a onClick='setHrefs()' class='tooltip' title='::" + uriString + "' href='?class="
											+ URLEncoder.encode(uriString, "UTF-8") + dataLocation + dataMeta +"'>" + before
											+ uriString.subSequence(uriString.lastIndexOf("/") + 1, uriString.length()) + " " + classInfo + after + "</a></li>");
								} else {
									out = out.concat("<li><a onClick='setHrefs()' class='tooltip' title='::" + uriString + "' href='?class="
											+ URLEncoder.encode(uriString, "UTF-8") + dataLocation + dataMeta +"'>" + before
											+ uriString.subSequence(uriString.lastIndexOf("#") + 1, uriString.length()) + " " + classInfo + after + "</a></li>");
								}
							}
						}
					} finally {
						result.close();
					}
				} catch (QueryEvaluationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedQueryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (con!=null)
				try {
					con.close();
				} catch (RepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		out = out.concat("</menu></div>");
		System.out.println(out);
		return out;
	}	
	
	
	
	/**
	 * Gets the number of instances of a class and adds an icon when at least one
	 * lens is available for one of the instances.
	 * 
	 * @param uri
	 * @return String A string showing the number of instances and an icon if a
	 *         lens is available.
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	/**
	 * @param uri
	 * @param location
	 * @return
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	public String getClassInfo(String uri, String location) throws RepositoryException, MalformedQueryException, QueryEvaluationException {
		String out = "";
		boolean isLensAvailable = false;
		int i = 0;
		RepositoryConnection con=null;
		Repository repository=null;

		if (location == "remote") {
			repository =lenaConfig.getRemoteRepository();
			con = repository.getConnection();
		} else if (location == "local") {
			repository = lenaConfig.getLocalRepository();
			con = repository.getConnection();
		}
		try {

			String query = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?s WHERE { ?s rdf:type <" + uri + "> }";

			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
			TupleQueryResult result = tupleQuery.evaluate();

			try {

				String firstBindingName = result.getBindingNames().get(0);
				while (result.hasNext()) {
					
					Value instance = result.next().getBinding(firstBindingName).getValue();
					//if (instance instanceof Resource) {						
						//if (isLensForResourceAvailable(((Resource) instance).stringValue(),location)) {
							//isLensAvailable = true;
						//}						
					//}
					i++;
				}
			} finally {
				result.close();
			}
		} finally {
			con.close();
		}
		int numberOfResources = 0;
		//NEW: Metaknowledge counts as instance. Take them out
		/*if(lenaConfig.getMetaknowledge())
			numberOfResources = i/2;
		else
			numberOfResources = i;*/
		
		out = out.concat("(" + i + ")");
		if (isLensAvailable) {
			out = out
					.concat("</a><img src='public/images/lens_icon.png' style='border: 0px; margin-left: 5px; vertical-align: bottom;' class='tooltip' title='Lens(es) available!' alt='Icon.' />");
		}
		return out;
	}	

	
	
	
	/**
	 * Gets the lenses defined in the configuration files.
	 * 
	 * @return String A string representing the complete lenses menu.
	 * @throws UnsupportedEncodingException 
	 * @throws UnsupportedEncodingException
	 */
	public String getLenses(String meta) throws UnsupportedEncodingException  {
		System.out.println("Loading Lenses...");
		String labelLenses = "<div id='lensesAndClasses'><h3>Label Lenses: </h3><hr><menu id='lenses'>";
		String defaultLenses = "<div id='lensesAndClasses'><h3>Lenses: </h3><hr><menu id='lenses'>";

		String dataMeta="";
		
		if(meta.equalsIgnoreCase("true"))
			dataMeta = "&meta=true";
		else
			dataMeta = "&meta=false";
		
		Lens[] lenses= fd.getLenses();
		
		for (Lens lens:lenses) {
			String lensID = lens.getURI();//lensObj.getIdentifier().toString();
			String before = "";
			String after = "";

			/*if (lensID == lens) {
				before = "<b>";
				after = "</b>";
			}*/
			if (lens.getPurpose()==Lens.PURPOSE_LABEL) {
				labelLenses = labelLenses.concat("<li><a onClick='setHrefs()' class='tooltip' title='::" + lensID + "' href='?lens="
						+ URLEncoder.encode(lensID, "UTF-8") + "&location=local"+dataMeta+"'>" + before + lensID.subSequence(lensID.lastIndexOf("#") + 1, lensID.length())
						//+ " (" + conf.getNumberOfResources(data, lensObj) + ")" 
						+ after 
						+ "</a></li>");
			} else {
				defaultLenses = defaultLenses.concat("<li><a onClick='setHrefs()' class='tooltip' title='::" + lensID + "' href='?lens="
						+ URLEncoder.encode(lensID, "UTF-8") + "&location=local"+dataMeta+"'>" + before + lensID.subSequence(lensID.lastIndexOf("#") + 1, lensID.length())
						//+ " (" + conf.getNumberOfResources(data, lensObj) + ")" 
						+ after + "</a></li>");
			}
		
		}		
		labelLenses = labelLenses.concat("</menu></div>");
		defaultLenses = defaultLenses.concat("</menu></div>");
		System.out.println(defaultLenses);
		return defaultLenses;
	}

	/**
	 * Creates the pages view.
	 * 
	 * @return The pages view
	 * @throws UnsupportedEncodingException
	 */
	
	public String getPages() throws UnsupportedEncodingException {
		//if(getNumberOfResources() >0){
			
			int pageInt = Integer.valueOf(page).intValue();
			StringBuffer pages = new StringBuffer();
			
			pages.append("<b>Page: </b>");
			pages.append(pageInt);
			//pages.append(" | " + getNumberOfResources() + " Instances");
			pages.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			
			
			
			if(pageInt >= 1){
				if ((this.lens !=null) && (this.resource != null)&& (this.lens.trim() != "") && (this.resource.trim() != "")) {			
					pages.append("<a onClick='setHrefs()' href='?lens=" + URLEncoder.encode(this.lens, "UTF-8") + "&resource=" + URLEncoder.encode(this.resource, "UTF-8")
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=1'><b><< </b></a>");
					int i = 1;
					while(i<=pageInt){
						pages.append("<a onClick='setHrefs()' href='?lens=" + URLEncoder.encode(this.lens, "UTF-8") + "&resource=" + URLEncoder.encode(this.resource, "UTF-8")
								+ "&location=" + this.location +"&meta=" + this.meta+"&page="+i+"'><b> "+ i +" </b></a>");
						i++;
					}					
					pages.append("<a onClick='setHrefs()' href='?lens=" + URLEncoder.encode(this.lens, "UTF-8") + "&resource=" + URLEncoder.encode(this.resource, "UTF-8")
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=" + (pageInt+1) + "'><b> next page...</b></a>");
				} else if ((this.resource != null) && (this.resource.trim() != "")){
					pages.append("<a onClick='setHrefs()' href='?resource=" + URLEncoder.encode(this.resource, "UTF-8")
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=1'><b><< </b></a>");
					int i = 1;
					while(i <=pageInt){
						pages.append("<a onClick='setHrefs()' href='?resource=" + URLEncoder.encode(this.resource, "UTF-8")
							+ "&location=" + this.location +"&meta=" + this.meta+"&page="+i+"'><b> "+i+" </b></a>");
						i++;
					}
					pages.append("<a onClick='setHrefs()' href='?resource=" + URLEncoder.encode(this.resource, "UTF-8")
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=" + (pageInt+1) + "'><b> next page...</b></a>");									
				} else if ((this.clazz != null)&&(this.clazz.trim() != "")) {
					pages.append("<a onClick='setHrefs()' href='?class=" + URLEncoder.encode(this.clazz, "UTF-8") 
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=1'><b><< </b></a>");					
					int i = 1;
					while(i <=pageInt){
						pages.append("<a onClick='setHrefs()' href='?class=" + URLEncoder.encode(this.clazz, "UTF-8") 
							+ "&location=" + this.location +"&meta=" + this.meta+"&page="+i+"'><b> "+i+" </b></a>");
						i++;
					}
					pages.append("<a onClick='setHrefs()' href='?class=" + URLEncoder.encode(this.clazz, "UTF-8") 
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=" + (pageInt+1) + "'><b> next page...</b></a>");							
				} else if ((this.lens !=null)&&(this.lens.trim() !="")) {	
					pages.append("<a onClick='setHrefs()' href='?lens=" + URLEncoder.encode(this.lens, "UTF-8") 
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=1'><b><< </b></a>");					
					int i =1;
					while(i <=pageInt){
						pages.append("<a onClick='setHrefs()' href='?lens=" + URLEncoder.encode(this.lens, "UTF-8") 
							+ "&location=" + this.location +"&meta=" + this.meta+"&page="+i+"'><b> "+i+" </b></a>");
						i++;
					}
					pages.append("<a onClick='setHrefs()' href='?lens=" + URLEncoder.encode(this.lens, "UTF-8") 
							+ "&location=" + this.location +"&meta=" + this.meta+"&page=" + (pageInt+1) + "'><b> next page...</b></a>");					
				} else { //if (!lensGiven && !resourceGiven && !classGiven) {							
					System.out.println("No lens and/or resource and class parameter set!");				
				}
			}
			return pages.toString();
		}
		/*else{
			System.out.println("Error getting pages");
			return null;
		}*/
	//}	
	
	/**
	 * Transforms selection to XHTML
	 * 
	 * @param fresnelGroupString
	 * @param resourceURIString
	 * @param fresnelPurposeString
	 * @return selection The selection as XHTML
	 * @throws InvalidResultSetException
	 * @throws NoResultsException
	 */
	public String selectAsXHTML(String resourceURIString, String classURIString, String fresnelLensString, 
			String pageString, String locationString, String metaString) {
		
		
		
		this.resource = resourceURIString;
		this.lens = fresnelLensString;	
		this.clazz =  classURIString;
		this.location = locationString;
		this.meta = metaString;
		this.page = pageString;
		
		String xhtml = "";
		
		try {
				
			Document selection=makeSelection(
						resourceURIString,
						classURIString,
						fresnelLensString,
						pageString,
						locationString);
				
			
				//System.out.println("Class:" + classURIString);
				//getClasses(locationString);
		    //printDoc(selection,System.out);
			
		
			if (selection.hasChildNodes()) {
				XMLOutputter out=new XMLOutputter(Format.getPrettyFormat());
				
				xhtml=out.outputString(LenaTransformer.transform(selection,resourceURIString, 
						classURIString, locationString, metaString, pageString));				
				
			} else {				
				errorMessage.append("Could not perform any selection.");
				xhtml = errorMessage.toString();
			}
		
		} catch (Exception e) {			
			// TODO Auto-generated catch block
			errorMessage.append(e.toString() + "<br/>");
			e.printStackTrace();
		} 	
		return xhtml;
	}	
	
	private void printDoc(Document doc, PrintStream out){
		DOMDemoPrint.printNode(doc.getFirstChild(), "");
	}
	
	/**
	 * Performs the selection.
	 * 
	 * @param fresnelGroupString
	 * @param resourceURIString
	 * @return The selection.
	 */
	private Document makeSelection(String resourceURIString, String classURIString, String fresnelLensString, 
			String pageNumber, String locationString) {
		
		boolean remoteRepo = false;
		boolean lensGiven;
		boolean resourceGiven;
		boolean classGiven;
		
		FresnelSesameParser fp = new FresnelSesameParser();
		fd = fp.parse(lenaConfig.getLensFile(), Constants.N3_READER);	
		defaultLenses=fp.parse(lenaConfig.getDefaultLensFile(), Constants.N3_READER);
		
		

		
		int offset = ((Integer.parseInt(pageNumber))* lenaConfig.getMaxResourcesPage()) - lenaConfig.getMaxResourcesPage();
		int limit = lenaConfig.getMaxResourcesPage();
		
		if (locationString!=null && locationString.equalsIgnoreCase("local")) {
			remoteRepo=false;
		} else {
			remoteRepo=true;
		}
		
		if (fresnelLensString != null && fresnelLensString.trim() != ""){
			lensGiven=true;
		} else {
			lensGiven=false;
		}
		
		if (resourceURIString != null && resourceURIString.trim() != "") {
			resourceGiven=true;
		} else {
			resourceGiven=false;
		}						
		
		if (classURIString != null && classURIString.trim() != "") {
			classGiven=true;
		} else {
			classGiven=false;
		}
		
	
		Repository repository = null;
		Document document=null;
		try {
			if (!remoteRepo) {
				repository = lenaConfig.getLocalRepository();
				//System.out.println("SET REMOTE");
			} else {
				repository = lenaConfig.getRemoteRepository();
				//System.out.println("SET LOCAL");
			}
			if (lensGiven && resourceGiven) {
				System.out.println("*** LENA: lens and resource given");
				Lens lens = fd.getLens(fresnelLensString);
				lens.setInstanceDomain(resourceURIString);
				document = rendererRender(repository, lens, offset, limit);	
			} else if (resourceGiven){
				System.out.println(String.format("*** LENA: no lens, but resource %s",resourceURIString));							
				Lens defaultLens=defaultLenses.getLens(defaultResourceLens);				
				defaultLens.setInstanceDomain(resourceURIString);
				System.out.println("Print Resource Lens:" + defaultLenses.getLens(defaultResourceLens));	
				document =  rendererRender(repository, defaultLens, offset, limit);				
			} else if (classGiven) {
				System.out.println(String.format("*** LENA: no lens, no resource, but class %s",classURIString));
				Lens defaultLens=defaultLenses.getLens(defaultClassLens);				
				defaultLens.setClassDomain(classURIString);
				System.out.println("Print Classes Lens:" + defaultLenses.getLens(defaultClassLens));				
				document =  rendererRender(repository, defaultLens, offset, limit);				
			} else if (lensGiven) {				 
				System.out.println(String.format("*** LENA: lens %s, no resource given", fresnelLensString));
				Lens lens = fd.getLens(fresnelLensString);
				document =  rendererRender(repository, lens, offset, limit);				
			} else { //if (!lensGiven && !resourceGiven && !classGiven) {							
				System.out.println("No lens and/or resource and class parameter set!");
				errorMessage.append("No lens and/or resource and class parameter set!");			
				//return document;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			errorMessage.append(e.toString() + "<br/>");
			e.printStackTrace();
		}		
		return document;
	}	
	
	private void rendererSetFresnelRepository(Repository repository){
		if(lenaConfig.getMetaknowledge())			
			sesameRenderer.setFresnelRepository(repository);		
		else
			lenaRenderer.setFresnelRepository(repository);
	}
	
	private Document rendererRender(Repository repository, Lens defaultLens, int offset, int limit){
		try {
			if(lenaConfig.getMetaknowledge())			
				return sesameRenderer.render(fd, repository, defaultLens, offset, limit);			
			else
				return lenaRenderer.render(fd, repository, defaultLens);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	/*private int getNumberOfResources(){
		if(lenaConfig.getMetaknowledge())			
			return sesameRenderer.getNumberOfResources();		
		else{
			//TODO:: add getNumberOfResources() in LenaRenderer
			return 2;
		}
	}*/
	
	/*private Document rendererRender(Repository repository, String lensString){
		try {
			if(lenaConfig.getMetaknowledge())		
			return sesameRenderer.render(fd, repository, lensString);
		else
			return lenaRenderer.render(fd, repository, lensString);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}*/
	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		LenaController jfe=new LenaController(new LenaConfig(null));
		
		String classes = jfe.getClasses("local", "false");
		
		String classString="http://www.loa-cnr.it/ontologies/ExtendedDnS.owl#agent";
		String resource="http://xmlns.com/foaf/0.1/Thomas";
		String fresnelLensString="http://isweb/RSS_planetRDF";		
		fresnelLensString="http://isweb/FOAF_person";
		//fresnelLensString="http://isweb/default";
		
		//String xhtml=jfe.selectAsXHTML(null, "http://xmlns.com/foaf/0.1/Person", fresnelLensString, "1","local", "true");
		//String xhtml=jfe.selectAsXHTML(null, null, fresnelLensString, "1","local");
		//String xhtml=jfe.selectAsXHTML(resource, null, null, "1","local");
		//String xhtml=jfe.selectAsXHTML(null, null, fresnelLensString, "1","local","true");
		//String xhtml=jfe.selectAsXHTML(resource, null, null, "1","local","true");
		//String xhtml=jfe.selectAsXHTML(null, null, fresnelLensString, "1","local","true");
		//String xhtml=jfe.selectAsXHTML(null, classString, null, "1","local","true");

		//String xhtml=jfe.selectAsXHTML(null, "http://www.lehigh.edu/%7Ezhp2/2004/0401/univ-bench.owl#Department", null, "1","local","true");
		
		//String xhtml=jfe.selectAsXHTML(null, "http://www.x-media-project.org/fiat#PerformanceInfluenceMeasurement", null, "1","local","true");
		//String xhtml=jfe.selectAsXHTML(null, "http://www.x-media-project.org/fiat#Component", null, "1","local","true");
		String xhtml=jfe.selectAsXHTML(null, null, "http://www.x-media-project.org/fiat#FIAT_TrimForecasted", "1","local","true");
		
		//String xhtml=jfe.selectAsXHTML(null, "http://www.x-media-project.org/fiat#News", null, "1","local","true");
		System.out.println(xhtml);		
		
					
	}

	public void print(PrintStream out, Document doc) {		
		NodeList l=doc.getChildNodes();
		for (int i=0;i<l.getLength();i++) {
			print(out,l.item(i));			
		}	
	}
	
	public void print(PrintStream out, Node n) {		
		String s=n.getNodeValue()+n.getNodeName()+n.getBaseURI()+n.getLocalName()+n.getTextContent();
		out.println(s);
		NodeList l=n.getChildNodes();
		for (int i=0;i<l.getLength();i++) {
			print(out,l.item(i));			
		}
	}	
	
}
