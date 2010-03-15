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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

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
import org.xmedia.kernel.dto.XMUser;
import org.xmedia.kernel.handlers.InvalidUserException;
import org.xmedia.kernel.scratchpad.Application;

import de.uni_hildesheim.ismll.integration.ConfiguringException;
import de.unikoblenz.isweb.lena.util.Clock;
import de.unikoblenz.isweb.metak4lena.SesameRenderer;
import de.unikoblenz.isweb.triplerank.LODCrawler;
import de.unikoblenz.isweb.triplerank.Matlab2Java;
import de.unikoblenz.isweb.triplerank.RDF2Matlab;
import fr.inria.jfresnel.FresnelDocument;
import fr.inria.jfresnel.Lens;
import fr.inria.jfresnel.sesame.FresnelSesameParser;


/**
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class LenaController extends Thread implements HttpSessionBindingListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8740417816942219390L;
	
	private String resource;
	private String lens;
	private String clazz;
	private String location;
	private String meta;
	private String page;
	private String bookmark;
	
	FresnelDocument fd;
	FresnelDocument defaultLenses;
	
	LenaConfig lenaConfig;
	String defaultResourceLens="http://isweb.uni-koblenz.de/lena/defaultResourceLens";
	String defaultClassLens="http://isweb.uni-koblenz.de/lena/defaultClassLens";
	
	private XMUser assumedUser; 
	private SesameRenderer sesameRenderer;	
	private LenaRenderer lenaRenderer;
	
	private StringBuffer errorMessage=new StringBuffer();
	
	private Logger lenaLogger;
	
	private Float stoptimeCrawl=-1.0F;
	private Float stoptimeRDF2Matlab=-1.0F;
	private Float stoptimeGetURIsFromMatlab=-1.0F;
	
	private boolean wasSignaledStoptimeCrawl = false;
	private boolean wasSignaledStoptimeRDF2Matlab = false;
	private boolean wasSignaledStoptimeGetURIsFromMatlab = false;
	
	private String syncStoptimeCrawl = "";
	private String syncStoptimeRDF2Matlab = "";
	private String syncStoptimeGetURIsFromMatlab = "";
	
	private String path;
	
	public LenaController(LenaConfig lenaConfig, Logger lenaLogger, String path) {
		System.out.println("Initialising JFresnelEngine");
		System.out.println("LINKEDDATA: LenaServlet resourcePath: " + path);
		this.lenaConfig=lenaConfig;
		this.lenaLogger=lenaLogger;
		this.path = path;
		
		lenaRenderer = new LenaRenderer();
		sesameRenderer = new de.unikoblenz.isweb.metak4lena.SesameRenderer(this);
		
		fd = new FresnelDocument();
		defaultLenses = new FresnelDocument();
		
		FresnelSesameParser fp = new FresnelSesameParser();
		fd = fp.parse(lenaConfig.getLensRepository(), "");	
		defaultLenses=fp.parse(lenaConfig.getDefaultLensRepository(),"");		
		
		init();
		
		System.out.println("Initialization of JFresnelEngine successfully finished!");		
	}
	
	private void init() {
		errorMessage = new StringBuffer();
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		//System.setProperty("org.openrdf.repository.debug", "true");
		System.setProperty("org.openrdf.repository.warn", "false");
	}
	
	/*File lensFile=lenaConfig.getLensFile();
	fd = fp.parse(lensFile, Constants.N3_READER);*/	
	/*public FresnelDocument getFresnelDocument(){ 
		return fd;
	}*/
	
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
		System.out.println("AJAX IV: Loading Classes...");
		String out = "";
		String dataLocation = "";
		String dataMeta = "";
		String dataRank = "&rank=true";
		RepositoryConnection con=null;
		try {			
			if (location == "local") {
				System.out.println("AJAX IV: Local Repository");
				out = "<div id='lensesAndClasses'><h3>Local Classes: </h3><hr><menu id='lclasses'>";				
				con=lenaConfig.getLocalRepository().getConnection();
				dataLocation = "&location=local";				
			} else if (location=="remote"){
				System.out.println("AJAX IV: Remote Repository");
				//out = "<div id='lensesAndClasses'><h3>Remote Classes: </h3><hr><menu id='rclasses'>";
				out = "<div id='lensesAndClasses'><h3>Classes: </h3><hr><menu id='rclasses'>";
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
								(!uri.toString().contains(Namespaces.TRIX))&&
								(!uri.toString().contains(Namespaces.OWL))&&
								(!uri.toString().contains(Namespaces.RDF))&&
								(!uri.toString().contains(Namespaces.RDFS))){
							
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
									out = out.concat("<li><a onClick='setHrefs()' class='tooltip' title='" + uriString + "' rel='' href='?class="
											+ URLEncoder.encode(uriString, "UTF-8") + dataLocation + dataMeta + dataRank+"'>" + before
											+ uriString.subSequence(uriString.lastIndexOf("/") + 1, uriString.length()) + " " + classInfo + after + "</a></li>");
								} else {
									out = out.concat("<li><a onClick='setHrefs()' class='tooltip' title='" + uriString + "' rel='' href='?class="
											+ URLEncoder.encode(uriString, "UTF-8") + dataLocation + dataMeta + dataRank+"'>" + before
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
					.concat("</a><img src='public/images/lens_icon.png' style='border: 0px; margin-left: 5px; vertical-align: bottom;' class='tooltip' title='Lens(es) available!' rel='' alt='Icon.' />");
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
		//rendererSetFresnelRepository(lenaConfig.getLensRepository());
		
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
				labelLenses = labelLenses.concat("<li><a onClick='setHrefs()' class='tooltip' title='" + lensID + "' rel='' href='?lens="
						+ URLEncoder.encode(lensID, "UTF-8") + "&location=local"+dataMeta+"'>" + before + lensID.subSequence(lensID.lastIndexOf("#") + 1, lensID.length())
						//+ " (" + conf.getNumberOfResources(data, lensObj) + ")" 
						+ after 
						+ "</a></li>");
			} else {
				defaultLenses = defaultLenses.concat("<li><a onClick='setHrefs()' class='tooltip' title='" + lensID + "' rel='' href='?lens="
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
			String pageString, String locationString, String metaString, String bookmarkString) {
		this.resource = resourceURIString;
		this.lens = fresnelLensString;	
		this.clazz =  classURIString;
		this.location = locationString;
		this.meta = metaString;
		this.page = pageString;
		this.bookmark = bookmarkString;
		
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
						classURIString, locationString, metaString, pageString, bookmarkString));				
				
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
				System.out.println("AJAX V: SET LOCAL Repo.!");
			} else {
				repository = lenaConfig.getRemoteRepository();
				System.out.println("AJAX V: SET REMOTE Repo.!");
			}
			if (lensGiven && resourceGiven) {
				System.out.println("AJAX V: *** LENA: lens and resource given");
				Lens lens = fd.getLens(fresnelLensString);
				 rendererSetFresnelRepository(lenaConfig.getLensRepository());
				lens.setInstanceDomain(resourceURIString);
				document = rendererRender(repository, lens, offset, limit);	
			} else if (resourceGiven){
				System.out.println(String.format("AJAX V: *** LENA: no lens, but resource %s",resourceURIString));							
				Lens defaultLens=defaultLenses.getLens(defaultResourceLens);
				rendererSetFresnelRepository(lenaConfig.getDefaultLensRepository());
				defaultLens.setInstanceDomain(resourceURIString);
				System.out.println("AJAX V: Print Resource Lens:" + defaultLenses.getLens(defaultResourceLens));	
				document = rendererRender(repository, defaultLens, offset, limit);				
			} else if (classGiven) {
				System.out.println(String.format("AJAX V: *** LENA: no lens, no resource, but class %s",classURIString));
				Lens defaultLens=defaultLenses.getLens(defaultClassLens);
				rendererSetFresnelRepository(lenaConfig.getDefaultLensRepository());
				defaultLens.setClassDomain(classURIString);
				System.out.println("AJAX V: Print Classes Lens:" + defaultLenses.getLens(defaultClassLens));				
				document =  rendererRender(repository, defaultLens, offset, limit);				
			} else if (lensGiven) {				 
				System.out.println(String.format("AJAX V: *** LENA: lens %s, no resource given", fresnelLensString));
				Lens lens = fd.getLens(fresnelLensString);
				rendererSetFresnelRepository(lenaConfig.getLensRepository());
				document =  rendererRender(repository, lens, offset, limit);				
			} else { //if (!lensGiven && !resourceGiven && !classGiven) {							
				System.out.println("AJAX V: No lens and/or resource and class parameter set!");
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
	
	public XMUser setXMUser(String userName, String userToken,	String userURI) throws UnsupportedEncodingException{
		assumedUser = new XMUser(userName, java.net.URI.create(URLDecoder.decode(userURI, "UTF-8")), userToken);
		return assumedUser;		
	}
	public XMUser getXMUser(){
		return assumedUser;
	}
	public String getIntegrationMenu(final java.net.URI uri, String meta) throws ConfiguringException, UnsupportedEncodingException, MalformedURLException {
		if((uri!=null)&&(getXMUser()!=null)){
			System.out.println("User: " + getXMUser());
			System.out.println("URI: " + uri);
			Application[] applications = lenaConfig.getApplicationHandler().getApplications(uri, getXMUser());
			String out = "<div id='lensesAndClasses'><h3>Applications: </h3><hr><menu id='applications'>";
			
			
			out = out.concat("<li><a title='Scratchpad' href='http://localhost:8080/Scratchpad'> Scratchpad </a></li>");			
			
			
			String dataLocation = "";
			String dataMeta = "";
			String before = "";
			String after = "";
	
			if (location == "local") {
				System.out.println("Local Repository");			
				dataLocation = "&location=local";				
			} else if (location=="remote"){
				System.out.println("Remote Repository");
				dataLocation = "&location=remote";
			}
			if (meta.equalsIgnoreCase("true")) {			
				dataMeta = "&meta=true";
			} else if (meta.equalsIgnoreCase("false")){
				dataMeta = "&meta=false";
			}
			
			for (final Application app : applications) {			
				System.out.println("Creating Menu Item for " + app.getName() + "::src=" + app.getSrc() + ";type=" + app.getType() + ";parameters:" + app.getParameters().toString());
				
				URL u = uri.toURL(); //XMediaIntegration.configureForRedirect(app, lenaConfig.getSessionHandler(), getXMUser(), uri);
				
				System.out.println("+++++++++++++++++++Integration: "+ u);
				
				
				out = out.concat("<li><a title='::" + app.getName() + "' href='" + u + "'>" 
						+ before + app.getName().subSequence(app.getName().lastIndexOf("#") + 1, app.getName().length())
						+ after	+ "</a></li>");			
				
				
								
				//XMediaIntegration.configureForRedirect(application, sessionHandler, user, uri);
			}
			out = out.concat("</menu></div>");
			return out;
		}
		else return "";
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
		LenaController jfe = new LenaController(new LenaConfig(null), null, null);
		
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
		//String xhtml=jfe.selectAsXHTML(null, "http://www.xmedia.org/infrastructure#Document", null, "1","local","true", "false");
		String xhtml=jfe.selectAsXHTML(null, null, "http://kmi.open.ac.uk/RR/modules/vib.owl#ZmodS", "1","local","false", "false");
		
		//String xhtml=jfe.selectAsXHTML(null, "http://www.x-media-project.org/fiat#News", null, "1","local","true");
		System.out.println(xhtml);		
		
					
	}
	public void insertBookmarks(java.net.URI[] bookmarkURI) throws InvalidUserException{
		if(getXMUser()!=null)
			lenaConfig.getBookmarksHandler().insertBookmarks(bookmarkURI, getXMUser());
	}
	public void deleteBookmarks(java.net.URI bookmarkURI) throws InvalidUserException{
		if(getXMUser()!=null)
			lenaConfig.getBookmarksHandler().deleteBookmark(bookmarkURI, getXMUser());		
	}
	public boolean isBookmarkInScratchpad(java.net.URI bookmark) throws InvalidUserException{
		return lenaConfig.getBookmarksHandler().isBookmarkInScratchpad(bookmark, getXMUser());
	} 	
	public Application[] getApplications(java.net.URI resource){ /* for the Spring-bean, and */
		if(getXMUser()!=null)
			return lenaConfig.getApplicationHandler().getApplications(resource, getXMUser());
		return null;
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
	
	public float getStoptimeCrawl() {
		System.out.println("AJAX: wasSignaledStoptimeCrawl: "+wasSignaledStoptimeCrawl);
		 synchronized (syncStoptimeCrawl) {
			 while (!wasSignaledStoptimeCrawl){
				 try{
					 System.out.println("AJAX: wasSignaledStoptimeCrawl: Waiting...");
					 syncStoptimeCrawl.wait();
					 System.out.println("AJAX: wasSignaledStoptimeCrawl: Waiting... FINISHED!");
	        	 }catch(InterruptedException e) {}
			 }
		 }
		 return stoptimeCrawl.floatValue();
	}
	
	public float getStoptimeRDF2Matlab() {
		System.out.println("AJAX: wasSignaledStoptimeRDF2Matlab: "+wasSignaledStoptimeRDF2Matlab);
		 synchronized (syncStoptimeRDF2Matlab) {
			 while (!wasSignaledStoptimeRDF2Matlab){
				 try{
					 System.out.println("AJAX: wasSignaledStoptimeRDF2Matlab: Waiting...");
					 syncStoptimeRDF2Matlab.wait();
					 System.out.println("AJAX: wasSignaledStoptimeRDF2Matlab: Waiting... FINISHED!");
	        	 }catch(InterruptedException e) {}
			 }
		 }
		return stoptimeRDF2Matlab.floatValue();
	}
	
	public float getStoptimeGetURIsFromMatlab() {
		System.out.println("AJAX: wasSignaledStoptimeGetURIsFromMatlab: "+wasSignaledStoptimeGetURIsFromMatlab);
		 synchronized (syncStoptimeGetURIsFromMatlab) {
			 while (!wasSignaledStoptimeGetURIsFromMatlab){
				 try{
					 System.out.println("AJAX: syncStoptimeGetURIsFromMatlab: Waiting...");
					 syncStoptimeGetURIsFromMatlab.wait();
					 System.out.println("AJAX: syncStoptimeGetURIsFromMatlab: Waiting... FINISHED!");
	        	 }catch(InterruptedException e) {}
			 }
		 }
		return stoptimeGetURIsFromMatlab.floatValue();
	}
	
	public void resetStoptimes() {
		stoptimeCrawl=-1.0F;
		stoptimeRDF2Matlab=-1.0F;
		stoptimeGetURIsFromMatlab=-1.0F;
	}
	
	public void resetStoptimeBools() {
		wasSignaledStoptimeCrawl = false;
		wasSignaledStoptimeRDF2Matlab = false;
		wasSignaledStoptimeGetURIsFromMatlab = false;
	}
	
	/**
	 * Delete files from data directory.
	 * 
	 * @param directory
	 */
	private void deleteFiles(File directory){
		File[] files = directory.listFiles();
		for (File file : files) {
			if(file.isDirectory()){
				deleteFiles(file);
			}else if (!file.delete()) {
				// Failed to delete file
				System.out.println("Failed to delete " + file);
			}
		}
	}

	@Override
	public void valueBound(HttpSessionBindingEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void valueUnbound(HttpSessionBindingEvent event) {
		// TODO Auto-generated method stub
		
	}
}
