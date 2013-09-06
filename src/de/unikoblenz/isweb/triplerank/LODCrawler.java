/**
 * 
 */
package de.unikoblenz.isweb.triplerank;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.sail.SailException;

import de.unikoblenz.isweb.lena.util.Clock;

/**
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class LODCrawler {
	boolean continueCrawling=true;
	Logger mylog=Logger.getLogger("de.unikoblenz.isweb.triplerank.LODCrawler");
	String startResource;
	String endpoint;
	HTTPRepository rep;
	String dumpFile;
	List<String> resourceLimitExceeders=new LinkedList<String>();
	
	long statementLimit=15000; //15000;
	int depthLimit=4; //8;
	//limit for statements followed for a link type
	int linkLimit=1000;//100;
	//int resourceStatementLimit=1000;
	
	int statementCount=0;
	Map<String,Integer> predicateCount=new HashMap<String, Integer>();
	Set<String> resources=new HashSet<String>();
	
	int retryLimit=5;//5;
	int pause=100;//10000;
	int reachedDepth=1;
	
	Clock clock;
	
	static String virtuosoEndpoint="http://lod.openlinksw.com/sparql";	
	static String dblpEndpoint="http://dblp.l3s.de/d2r/sparql";	
	static String imdbEndpoint="http://data.linkedmdb.org/sparql";
	static String dbpedia="http://dbpedia.org/sparql";
	static String bbc="http://bbc.openlinksw.com/sparql";
	static String umbel="http://umbel.structureddynamics.com/sparql";
	
	static String swetodblp="http://localhost:8080/openrdf-sesame/repositories/swetodblp";
	static String beatles2="http://localhost:8080/openrdf-sesame/repositories/beatles2";	
	static String start1="http://dblp.l3s.de/d2r/resource/authors/Thomas_Franz";
	
	String placeHolder="%%%";
	//String query1="construct {?s ?p ?o} WHERE { {?s ?p ?o. Filter(?s=<%%%>)} UNION {?s ?p ?o. Filter(?o=<%%%>)} }";
	String query1="DESCRIBE <%%%>";
	//String query1 = "CONSTRUCT {?s ?p ?o} WHERE {?s ?p ?o}";
	//String queryOutlink="construct {<%%%> ?p ?o} WHERE {<%%%> ?p ?o)}";
	//String queryInlink="construct {?s ?p ?o} WHERE {?s ?p ?o. Filter(?o=<%%%>)}";
	
	Queue<String> todo;
	RDFXMLWriter xmlwriter;

	public LODCrawler(){
		todo=new LinkedList<String>();
	}
	
	public void setLODURI(String LODURI){
		this.startResource=LODURI;
	}
	
	public void setEndpoint(String endpoint) throws RepositoryException{
		if(this.rep!=null){
			this.rep.shutDown();
			this.rep=connect(endpoint);
		}else{
			this.rep=connect(endpoint);
		}
	}
	
	public void setDumpFile(String dumpFile){
		this.dumpFile=dumpFile;
	}
	
	public void setStatementLimit(long statementLimit) {
		this.statementLimit=statementLimit; 
	}
	
	public void setDepthLimit(int depthLimit) {
		this.depthLimit=depthLimit; 
	}
	
	public void setLinkLimit(int linkLimit) {
		this.linkLimit=linkLimit; 
	}
	
	public void setRetryLimit(int retryLimit) {
		this.retryLimit=retryLimit; 
	}
	
	public void setPause(int pause) {
		this.pause = pause;
	}
	
	
	
	void countPredicate(String predicate){
		if (predicateCount.containsKey(predicate)){
			predicateCount.put(predicate, predicateCount.get(predicate)+1);
		} else {
			predicateCount.put(predicate, 1);
		}
	}
	
	HTTPRepository connect(String sparqlEndpoint) throws RepositoryException {		
		HTTPRepository endpoint = new HTTPRepository(sparqlEndpoint);
		endpoint.initialize();
		System.out.println("HTTPRepository initialized.");
		return endpoint;				
	}

	public boolean crawl() throws SailException, RDFHandlerException, IOException {
		todo.clear();
		todo.add(startResource);
		//N3Writer n3writer=new N3Writer(new FileOutputStream(dumpFile+".n3"));
		xmlwriter=new RDFXMLWriter(new FileOutputStream(dumpFile+".rdf"));
		//n3writer.startRDF();
		xmlwriter.startRDF();
		continueCrawling=true;
		RepositoryConnection con=null;
		try {
			con=rep.getConnection();
			clock=new Clock();
			clock.start();
			addData(con,xmlwriter,0,todo,new HashSet<String>());
		} catch (RepositoryException e) {
			e.printStackTrace();
		} finally {
			if (con!=null)
				try {
					con.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}			
		}		
		//n3writer.endRDF();
		xmlwriter.endRDF();
		printStatistics(dumpFile+"_crawl.txt");
		
		mylog.log(Level.INFO, "Crawled "+statementCount+" statements.");
		
		if(statementCount<300)
			return false;
		return true;
	}
	
	void printStatistics(String file) throws IOException {
		FileWriter fw=new FileWriter(file);		
		String s="== Crawl Statistics ==\n"
		+String.format( "Start resource:          %s\n",startResource)
		+String.format( "Statement count (limit): %d (%d)\n", statementCount,statementLimit)
		+String.format( "Resource count:          %d \n", resources.size())
		+String.format( "Depth count (limit):     %d (%d)\n", reachedDepth,depthLimit)
		//+String.format( "Resource limit:          %d \n",resourceStatementLimit)
		+String.format( "Limit exceeded by:       %s\n",resourceLimitExceeders);
		fw.write(s);
		List<Integer> numPredicates=new LinkedList<Integer>(predicateCount.values());
		Collections.sort(numPredicates);
		System.out.println("numPredicates.size(): "+numPredicates.size());
		
		if(numPredicates.size()>=1){
			fw.write(String.format( "Max(predicateCount):     %d\n", numPredicates.get(numPredicates.size()-1)));
		}
		fw.flush();
		fw.close();
	}
	
	void query(RepositoryConnection con,
			RDFHandler writer,			
			String query,
			String currentResource,Queue<String> todo, Set<String> done) {
		GraphQueryResult result=null;			
		int tries=0;
		while (tries<retryLimit) {					
			try {
				Thread.sleep(pause);
				result=con.prepareGraphQuery(QueryLanguage.SPARQL, query).evaluate();										
			} catch (Exception e) {						
				mylog.log(Level.WARNING,String.format("Exception during query evaluation of query \"%s\":\n\"%s\"\n", query,e.getMessage()));				
				mylog.log(Level.WARNING,String.format("... starting retry %d ...\n",tries));				
			} finally {
				tries++;
			}
		}	
		if (result!=null) {
			//int rsCount=0;
			// add link counting
			LinkCounter lc=new LinkCounter();
			
			try {
				while (result.hasNext()) {
					/*
					if (++rsCount==resourceStatementLimit) {
						System.out.printf("Resource statement limit of %d reached for resource %s.\n", resourceStatementLimit,currentResource);
						resourceLimitExceeders.add(currentResource);						
						break;
					}*/					
					Statement s=result.next();
					String predicate=s.getPredicate().stringValue();
					
					// Break when link Limit is reached!
					//if (lc.getCount(predicate)<linkLimit)
					//	break;
					writer.handleStatement(s);
					lc.addLink(predicate);
					statementCount++;
					countPredicate(s.getPredicate().stringValue());
					resources.add(s.getSubject().stringValue());
					resources.add(s.getObject().stringValue());
					if (lc.getCount(predicate)>linkLimit) {
						//System.out.print("LL ");
						//System.out.printf("Link limit exceeded for link %s on resource %s\n",predicate,s.getSubject().stringValue());
						//break;
					} else {
						if (s.getSubject().stringValue().equals(currentResource)) { // outlink
							if (s.getObject() instanceof URI && !done.contains(((URI)s.getObject()).stringValue())){
								todo.add(((URI)s.getObject()).stringValue());
								//System.out.print("+ ");
							}
						} else { //in(verse) link
							if (!done.contains(s.getSubject().stringValue())){
								todo.add(s.getSubject().stringValue());
								//System.out.print("- ");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	void addData(RepositoryConnection con,RDFHandler writer,int depthCount,Queue<String> todo, Set<String> done) {
		System.out.print("TODO: "+!todo.isEmpty()+" -> "+todo.size()+"; ");
		System.out.print("SC: "+statementCount+" ");
		System.out.print("< SL: "+(statementCount<statementLimit)+"; ");
		System.out.print("DC: "+depthCount+" ");
		System.out.println("< Depth Limit: "+(depthCount<depthLimit)+";");
		mylog.log(Level.INFO, "Time elapsed: "+clock.timeElapsed());
		
		while ((!todo.isEmpty() && statementCount<statementLimit && clock.timeElapsed()<60) || 
				statementCount<500) {			
			String currentResource=todo.poll();			
			done.add(currentResource);
			try {
				String query=query1.replaceAll(placeHolder, currentResource);						
				query(con,writer,query,currentResource,todo,done);
				addData(con,writer,++depthCount,todo,done);
			} catch (Exception e){
				mylog.log(Level.SEVERE,"Some error ocurred while querying ...",e);
			}
		}
		System.out.println("TODO: "+!todo.isEmpty()+" SC<SL: "+(statementCount<statementLimit)+" depthcount<depthlimit: "+(depthCount<depthLimit));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		try {
			
			/*
			LODCrawler crawler=new LODCrawler(
					"http://dblp.l3s.de/d2r/resource/authors/Steffen_Staab",
					virtuosoEndpoint,
					"data/rdf/steffen-2");
		
			*/
			LODCrawler crawler=new LODCrawler();
			crawler.setLODURI("http://dbpedia.org/resource/Berlin");
			crawler.setEndpoint(virtuosoEndpoint);
			crawler.setDumpFile("data/rdf/berlin");
			
			/*
			LODCrawler crawler=new LODCrawler(
					"http://dblp.l3s.de/d2r/resource/authors/Thomas_Franz",
					virtuosoEndpoint,
					"data/rdf/thomas");
			
			/*
			LODCrawler crawler=new LODCrawler(
					"http://dbpedia.org/resource/HITS_algorithm",
					virtuosoEndpoint,
					"data/rdf/hits");
			LODCrawler crawler=new LODCrawler(
					"",
					virtuosoEndpoint,
					"data/rdf/");		

			LODCrawler crawler=new LODCrawler(
					"http://dbpedia.org/resource/Semantic_Web",
					virtuosoEndpoint,
					"data/rdf/semantic_web");
			LODCrawler crawler=new LODCrawler(
					"http://tw.rpi.edu/wiki/index.php/Special:URIResolver/The_Semantic_Web-2C_6th_International_Semantic_Web_Conference-2C_2nd_Asian_Semantic_Web_Conference-2C_ISWC_2007_-2B_ASWC_2007-2C_Busan-2C_Korea-2C_November_11-2D15-2C_2007",
					virtuosoEndpoint,
					"data/rdf/iswc");															
								
			LODCrawler crawler=new LODCrawler(
					"http://www4.wiwiss.fu-berlin.de/dblp/resource/person/100007",
					virtuosoEndpoint,
					"data/rdf/timbl");
			LODCrawler crawler=new LODCrawler(
					//"http://dblp.l3s.de/Authors/Steffen+Staab",
					//"http://dblp.l3s.de/d2r/page/authors/Steffen_Staab",
					"http://dblp.l3s.de/d2r/resource/authors/Steffen_Staab",
					dblpEndpoint,
					"data/rdf/steffen");
			
			LODCrawler crawler=new LODCrawler(
					"http://data.linkedmdb.org/resource/film/2014",
					imdbEndpoint,
					"data/rdf/shining");
			
			LODCrawler crawler=new LODCrawler(
					"http://dbpedia.org/resource/The_Beatles",
					virtuosoEndpoint,
					"data/rdf/beatles2");
																				
			LODCrawler crawler=new LODCrawler(
					"http://dblp.uni-trier.de/rec/bibtex/conf/semweb/2007",
					swetodblp,
					"data/rdf/semweb07");	
			
			LODCrawler crawler=new LODCrawler(
					"http://dbpedia.org/resource/The_Lord_of_the_Rings",
					dbpedia,
					"data/rdf/lotr2");
			
			LODCrawler crawler=new LODCrawler(
					"http://data.semanticweb.org/conference/eswc/2008",
					"http://data.semanticweb.org/conference/eswc/2008",
					"data/rdf/eswc08");*/
			
			/*
			LODCrawler crawler=new LODCrawler(
					"http://dbpedia.org/resource/The_Beatles",
					dbpedia,
					"data/rdf/test");*/
			
			crawler.crawl();
		} catch (Exception e) {
			e.printStackTrace();
		} 		
	}
	
	
}