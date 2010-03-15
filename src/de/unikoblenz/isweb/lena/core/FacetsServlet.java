package de.unikoblenz.isweb.lena.core;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.unikoblenz.isweb.lena.util.Clock;
import de.unikoblenz.isweb.triplerank.LODCrawler;
import de.unikoblenz.isweb.triplerank.Matlab2Java;
import de.unikoblenz.isweb.triplerank.RDF2Matlab;

/**
 * Servlet implementation class FacetsServlet
 */
public class FacetsServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	Logger mylog=Logger.getLogger("de.unikoblenz.isweb.lena.FacetsServlet");
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FacetsServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("AJAX VI: AJAX Rank call initiated!!!");
		String menuFacets = "";
		String paramURI,paramMeta;
		if (request.getParameter("resource") != null) {
			paramURI = URLDecoder.decode(request.getParameter("resource"), "UTF-8");
		} else {
			paramURI = "";
		}
		if (request.getParameter("meta") != null) {
			paramMeta = request.getParameter("meta");
		} else {
			paramMeta = "false";
		}
		menuFacets = getFacets(paramMeta, paramURI);
		PrintWriter writer = response.getWriter();
		writer.println(menuFacets);
		writer.flush();
		writer.close();	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}

	private Float stoptimeCrawl=-1.0F;
	private Float stoptimeRDF2Matlab=-1.0F;
	private Float stoptimeGetURIsFromMatlab=-1.0F;
	
	/**
	 * Gets the crawled facets.
	 * 
	 * @return String A string representing the complete facet menu.
	 * @throws UnsupportedEncodingException 
	 * @throws UnsupportedEncodingException
	 */
	public String getFacets(
			String meta, 
			String lodURI) {
		
		
		
		System.out.println("AJAX VI: Loading Facets...");
		String facets = "";
		String titleTop = "";
		String titleBottom = "";
		String dumpName="";
		String dataMeta="";
		
		boolean isSuccessfulCrawl = true;
		
		long statementLimit = 8000;// Original: 10000 Good: 10000
		//int depthLimit = 8;// Original: 8 Good: 3
		int linkLimit = 1000;// Original: 100 Good: 100
		int retryLimit = 3;// Original: 5 Good: 2
		int pause = 100;// Original: 10000 Good: 1000
		int matlabFactor = 9;// Original: 15 Good: 9
		
		String path=getServletContext().getRealPath("");
		
		mylog.log(Level.INFO, 
				"LENA TR: TripleRank Params: Statement Limit: "+statementLimit+ " Link Limit: "+linkLimit+" Retry Limit: "+retryLimit+" Pause: "+pause+" Matlab factor: "+matlabFactor);
		//" Depth Limit: "+depthLimit+
		if(meta.equalsIgnoreCase("true"))
			dataMeta = "&meta=true";
		else
			dataMeta = "&meta=false";
		
		// SPARQL endpoints:
		String endpoint="http://lod.openlinksw.com/sparql";
		endpoint="http://cb.semsol.org/sparql";
		endpoint="http://dbpedia.org/sparql";
		//String endpoint="http://www.sparql.org/sparql";
		
		if(lodURI.substring(lodURI.lastIndexOf("/") + 1).contains(":")){
			dumpName=lodURI.substring(lodURI.lastIndexOf(":") + 1).toLowerCase();
		}else{
			dumpName=lodURI.substring(lodURI.lastIndexOf("/") + 1).toLowerCase();
		}
		
		// Dumpname is always the same... change this for keeping results.
		dumpName="dump";
		
		LODCrawler lenaCrawler=new LODCrawler();
		RDF2Matlab lenaRankTransformer = new RDF2Matlab();
		Matlab2Java lenaRankResult = new Matlab2Java(path + "/public/resources/");
		
		lenaRankResult.setFactor(matlabFactor);
		mylog.log(Level.INFO, "LENA TR: Matlab Param: Factor: "+matlabFactor);
		
		try {
			// Data paths
			File rdfPath = new File(path + "/public/resources/" + "data/rdf/");
			File matricesPath = new File(path + "/public/resources/" + "data/matrices/");
			File resultsPath = new File(path + "/public/resources/" + "data/results/");
			
			// TODO: Save data in repository instead of files
			deleteFiles(rdfPath);
			deleteFiles(matricesPath);
			deleteFiles(resultsPath);
			
			// Check if already crawled -> skip crawl.
			//File crawlFile = new File(path + "/public/resources/" + "data/rdf/" + dumpName.toLowerCase() + ".rdf");
			//File statsFile = new File(path + "/public/resources/" + "data/rdf/" + dumpName.toLowerCase() + "_crawl.txt");
			//System.out.println("LINKEDDATA: crawlFolder: "+ crawlFile);
			
			Clock clock = new Clock();
			clock.start();
			
			// Crawl
			lenaCrawler.setLODURI(lodURI);
			lenaCrawler.setEndpoint(endpoint);
			lenaCrawler.setDumpFile(path + "/public/resources/" + "data/rdf/" + dumpName);
			lenaCrawler.setStatementLimit(statementLimit);
			System.out.println("AJAX VI: Crawler: Statement Limit (SL): "+statementLimit);
			//lenaCrawler.setDepthLimit(depthLimit);
			//System.out.println("AJAX VI: Crawler: Depth Limit (DL): "+depthLimit);
			lenaCrawler.setLinkLimit(linkLimit);
			System.out.println("AJAX VI: Crawler: Link Limit (LL): "+linkLimit);
			lenaCrawler.setRetryLimit(retryLimit);
			lenaCrawler.setPause(pause);
			
			System.out.println("AJAX VI: Crawler crawl...");
			isSuccessfulCrawl = lenaCrawler.crawl();
			System.out.println("AJAX VI: Crawler crawl... READY!!!");
			
			stoptimeCrawl = clock.timeElapsed().floatValue();
			System.out.println("AJAX VI: LODCrawler.crawl(); Time: "+Float.toString(stoptimeCrawl));
			mylog.log(Level.INFO, "LENA TR: LODCrawler.crawl(); Time: "+Float.toString(stoptimeCrawl));
			
			// Synchronize stoptime
			/*
			synchronized (syncStoptimeCrawl) {
				System.out.println("AJAX VI: syncStoptimeCrawl!");
				wasSignaledStoptimeCrawl = true;
				syncStoptimeCrawl.notify();
			}*/
			
			if (!isSuccessfulCrawl){
				//resetStoptimeBools();
				//resetStoptimes();
				//synchronized (syncStoptimeRDF2Matlab) {
					//wasSignaledStoptimeRDF2Matlab = true;
					//syncStoptimeRDF2Matlab.notify();
				//}
				//synchronized (syncStoptimeGetURIsFromMatlab) {
					//wasSignaledStoptimeGetURIsFromMatlab = true;
					//syncStoptimeGetURIsFromMatlab.notify();
				//}
				return "<div id='lensesAndClasses'><div style='margin-left:5px;padding-top:2px'>Problems while crawling!</div></div>";
			}
			
			clock.start();
			
			System.out.println("AJAX VI: RDF2Matlab transform...");
			boolean matricesCreated = lenaRankTransformer.init(dumpName,lodURI,path+"/public/resources/");
			System.out.println("AJAX VI: RDF2Matlab transform... READY!!!");
			
			stoptimeRDF2Matlab = clock.timeElapsed().floatValue();
			System.out.println("AJAX VI: RDF2Matlab() Time: "+Float.toString(stoptimeRDF2Matlab));
			mylog.log(Level.INFO, "LENA TR: RDF2Matlab(); Time: "+Float.toString(stoptimeRDF2Matlab));
			
			// Synchronize stoptime
			//synchronized (syncStoptimeRDF2Matlab) {
				System.out.println("AJAX VI: syncStoptimeRDF2Matlab!");
				//wasSignaledStoptimeRDF2Matlab = true;
				//syncStoptimeRDF2Matlab.notify();
			//}
			
			if(!matricesCreated) {
				/*resetStoptimeBools();
				resetStoptimes();
				synchronized (syncStoptimeGetURIsFromMatlab) {
					wasSignaledStoptimeGetURIsFromMatlab = true;
					syncStoptimeGetURIsFromMatlab.notify();
				}*/
				return "<div id='lensesAndClasses'><div style='margin-left:5px;padding-top:2px'>No Matrices created!</div></div>";
			}
			
			clock.start();
			
			System.out.println("AJAX VI: Matlab2Java rank...");
			LinkedList<Object> result = lenaRankResult.getURIsFromMatlab(path + "/matlab/",path+"/public/resources/",lodURI);
			System.out.println("AJAX VI: Matlab2Java rank... READY!!!");
			
			stoptimeGetURIsFromMatlab = clock.timeElapsed().floatValue();
			System.out.println("AJAX VI: getURIsFromMatlab() Time: "+Float.toString(stoptimeGetURIsFromMatlab));
			mylog.log(Level.INFO, "LENA TR: getURIsFromMatlab(); Time: "+Float.toString(stoptimeGetURIsFromMatlab));
			
			// Synchronize stoptime
			//synchronized (syncStoptimeGetURIsFromMatlab) {
				System.out.println("AJAX VI: syncStoptimeGetURIsFromMatlab!");
				//wasSignaledStoptimeGetURIsFromMatlab = true;
				//syncStoptimeGetURIsFromMatlab.notify();
			//}
			
			// Results
			if (result.size()==0){
				//resetStoptimeBools();
				//resetStoptimes();
				return "<div id='lensesAndClasses'><div style='margin-left:5px;padding-top:2px'>Problems while creating Facets!</div></div>";
			}
			
			LinkedList<String> weight = (LinkedList<String>) result.get(0);
			LinkedList<Object> groupResultScore = (LinkedList<Object>) result.get(1);
			LinkedList<Object> groupResultId = (LinkedList<Object>) result.get(2);
			LinkedList<Object> groupResultName = (LinkedList<Object>) result.get(3);
			LinkedList<Object> groupResultScorePredicate = (LinkedList<Object>) result.get(4);
			LinkedList<Object> groupResultIdPredicate = (LinkedList<Object>) result.get(5);
			LinkedList<Object> groupResultNamePredicate = (LinkedList<Object>) result.get(6);
						
			// Ranked results for each group (facet).  
			for (int i = 0; i < weight.size(); i++) {
				System.out.println("AJAX VI: Weight for Group " + i + ": " + weight.get(i));
				// Ranked results (resources)
				LinkedList<String> groupScore = (LinkedList<String>) groupResultScore.get(i);
				LinkedList<String> groupId = (LinkedList<String>) groupResultId.get(i);
				LinkedList<String> groupName = (LinkedList<String>) groupResultName.get(i);
				// Ranked results (predicates)				
				LinkedList<String> groupScorePredicate = (LinkedList<String>) groupResultScorePredicate.get(i);
				LinkedList<String> groupIdPredicate = (LinkedList<String>) groupResultIdPredicate.get(i);
				LinkedList<String> groupNamePredicate = (LinkedList<String>) groupResultNamePredicate.get(i);
				
				// Create facet only if there is at least one resource and one predicate.
				if (groupName.size()==0||groupNamePredicate.size()==0){
					continue;
				}
				
				// Highest ranked predicate of a facet
				String predicateURITop = groupNamePredicate.get(0).toString();
				
				// Title for facet heading
				titleTop = "<b>All predicates describing the facet: </b><br/>";
				for (int j = 0; j < groupScorePredicate.size(); j++) {
					titleBottom += groupNamePredicate.get(j).toString() + " (Score: " + groupScorePredicate.get(j) + ")<br/>";
					//System.out.println("Predicate Ranks " + j + ": Score: " + groupScorePredicate.get(j) + " Id: " + groupIdPredicate.get(j) + " Name: " + groupNamePredicate.get(j));
				}
				
				// Write facet div for each facet
				facets = facets.concat("<div id='lensesAndClasses'><p style='padding: 0px 0px 0px 5px; margin: 0px;'class='tooltip' title='" + titleTop + "' rel='"+titleBottom+"'>" + predicateURITop.subSequence(predicateURITop.lastIndexOf("/") + 1, predicateURITop.length()) + "<br/><i style='font-size: 8px;'>Weight: " + weight.get(i) + "</i></p><hr/><menu id='lenses'>");
				
				// Reset title
				titleTop = "";
				titleBottom = "";
				
				// Add resources to facet
				for (int j = 0; j < groupScore.size(); j++) {
					String resourceURI = groupName.get(j).toString();
					String subURI = resourceURI.substring(resourceURI.lastIndexOf("/") + 1);
					if (resourceURI.endsWith("/")){
						resourceURI = resourceURI.substring(0,resourceURI.length()-1);
						subURI = resourceURI.substring(resourceURI.lastIndexOf("/") + 1);
					}
					String resourceScore = groupScore.get(j).toString();
					facets = facets.concat("<li><a onClick='setHrefs()' class='tooltip' title='Resource: " + resourceURI + "' rel='Score: " + resourceScore + "' href='?resource="
					+ URLEncoder.encode(resourceURI, "UTF-8") + "&location=remote&rank=true"+dataMeta+"'>" + subURI + "</a></li>");
					//System.out.println("Resource Ranks " + j + ": Score: " + groupScore.get(j) + " Id: " + groupId.get(j) + " Name: " + groupName.get(j));
				}
				facets = facets.concat("</menu></div>");
			}					
		} catch (Exception e) {
			e.printStackTrace();
		}
		return facets;
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
	
}
