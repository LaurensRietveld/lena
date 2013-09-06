/**
 * 
 */
package de.unikoblenz.isweb.triplerank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class RDF2Matlab {

	File rdfFile;
	int resCount=-1;
	RepositoryConnection con=null;
	//<URI,Integer>
	DualHashBidiMap uri2index=new DualHashBidiMap();
	Map<URI,String> prop2file=new HashMap<URI,String>();
	String filePrefix;
	int maxIndex=0;
	FileWriter allWriter;
	FileWriter statisticsWriter;
	FileWriter matricesWriter;
	//FileWriter rStatisticsWriter;
	Statistics stat=new Statistics();
	//Map<String,ResourceStatistics> stat=new HashMap<String,ResourceStatistics>();
	//String dataDir;
	
	// limit for statements followed for a link type
	//int linkLimit=500;
	int statementCounter=0;
	int predicateCounter=0;
	
	String countQuery=
		"SELECT distinct ?s WHERE {{?s ?p ?o. FILTER isIRI(?s)} UNION {?o ?p ?s. Filter isIRI(?s)}}";
		//order by ?s";	
	String predicateQuery=
		"SELECT distinct ?p WHERE {?s ?p ?o. Filter isURI(?s). Filter isURI(?o)}";
	
	URI sameAs=new URIImpl("http://www.w3.org/2002/07/owl#sameAs");
	URI type=new URIImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
	URI origin=new URIImpl("http://dbpedia.org/property/origin");

	String path;
	
	String rdfDir="data/rdf/";
	String matrixDir="data/matrices/";
	String resultsDir="data/results/";
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//new RDF2Matlab("beatles1","http://dbpedia.org/resource/The_Beatles");
		//new RDF2Matlab("hits","http://dbpedia.org/resource/HITS_algorithm");
		//new RDF2Matlab("test");
		RDF2Matlab transform = new RDF2Matlab();
		transform.init("berlin","http://dbpedia.org/resource/Berlin", "./");
		//new RDF2Matlab("iswc");
		//new RDF2Matlab("lord","http://dbpedia.org/resource/The_Lord_of_the_Rings");
		//new RDF2Matlab("semweb07","http://dblp.uni-trier.de/rec/bibtex/conf/semweb/2007");
		//new RDF2Matlab("eswc08","http://data.semanticweb.org/conference/eswc/2008");
		//new RDF2Matlab("james","http://dbpedia.org/resource/James_Bond");
		//new RDF2Matlab("sparql","http://dbpedia.org/resource/SPARQL");
	}
	
	public RDF2Matlab(){}
	
	public boolean init(String filePrefix,String startResource, String path) throws Exception {
		this.filePrefix=filePrefix;
		this.path=path;
		System.out.println("RDF2Matlab path:" + path);
		cleanOrCreateDataDir();
		allWriter=new FileWriter(path+matrixDir+filePrefix+"/"+filePrefix+"_alldata.dat");
		statisticsWriter=new FileWriter(path+resultsDir+filePrefix+"_statistics.txt");
		matricesWriter=new FileWriter(path+matrixDir+filePrefix+"/"+filePrefix+"_matrices.csv");
		//rStatisticsWriter=new FileWriter(path+matrixDir+filePrefix+"/"+filePrefix+"_r_statistics.csv");
		//statisticsWriter.write()
		String header = String.format("*** Statistics for Crawl \"%s\" ***\n",filePrefix);
		header+="    RDF Predicate            Links \n" +
				"-----------------------------------\n";
		statisticsWriter.write(header);
		
		// init repository		
		Repository rep = new SailRepository(new MemoryStore());
		//new HTTPRepository("http://localhost:8080/openrdf-sesame/repositories/srank");
		//new HTTPRepository("http://localhost:8080/openrdf-sesame/repositories/beatles2");

		try {
			rep.initialize();
				
		
		// load data from file
		con=rep.getConnection();
		con.add(new File(path+rdfDir+filePrefix+".rdf"), null, RDFFormat.RDFXML);
		
		// transform
		//Transformer t=new Transformer(startResource,con);
		//t.run();		
				
		createMatrices();		
		// store mapping in some file
		storeMapping();
		// write
		
		if(predicateCounter!=0){
			writeTopResources();
		
			matricesWriter.flush();
			matricesWriter.close();
			allWriter.flush();
			allWriter.close();
			
			String info=String.format("\n************************\n" +
					"Distinct resources: %d\n"+
					"Distinct properties: %d\n" +
					"Statements: %d"
					,getResourceCount(),predicateCounter,statementCounter);
			statisticsWriter.write(info);
			
			statisticsWriter.flush();
			statisticsWriter.close();
			
			return true;
		}else{
			return false;
		}
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				con.close();
			} catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}


	private void writeTopResources() throws IOException {
		statisticsWriter.write("\n\n************************ Top Resources *************************\n\n");
		List<ResourceStatistics> top=new LinkedList<ResourceStatistics>(stat.rs.values());
		Collections.sort(top);
		for (int i=top.size()-1;i>top.size()-11&&i>=0;i--){			
			statisticsWriter.write(top.get(i).printString());
			statisticsWriter.write("\n");
		}
	}

	void cleanOrCreateDataDir() {
		File dataDir=new File(path+matrixDir+filePrefix+"/");
		if (dataDir.exists() && dataDir.isDirectory()) {
			// clean, i.e. remove any file
			File[] files=dataDir.listFiles();
			for (File f:files){
				f.delete();
			}
		} else {
			// create dir
			dataDir.mkdir();
		}
	}
	/*
	ResourceStatistics getStatistics(String uri) {
		if (stat.containsKey(uri)) {
			return stat.get(uri);
		} else {
			ResourceStatistics rs=new ResourceStatistics(uri);
			stat.put(uri,rs);
			return rs;
		}
	}*/
	
	/**
	 * create arrays for different RDF link types
	 * @throws IOException 
	 * @throws Exception 
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 */
	void createMatrices() throws Exception {
			
		// writer for matrix representing linkage by any property (used for hits computation)		
		allWriter.write(getResourceCount()+" "+getResourceCount()+" 0\n");
		// for each property, create a link matrix		
		try {
			// query for different RDF properties
			TupleQueryResult result=con.prepareTupleQuery(QueryLanguage.SPARQL, predicateQuery).evaluate();
			System.out.println("Adding data linked by RDF property:");
			while (result.hasNext()){
				BindingSet binding=result.next();
				Value v=binding.getValue("p");
				if (v instanceof URI) { 
					writeMatrix((URI)v);
					//System.out.printf("%s\n", v.toString());
					predicateCounter++;					
				}				
			}				
		} catch (Exception e) {
			e.printStackTrace();			
		} finally {
			System.out.println("\n"+predicateCounter+" matrices created!");
		}
	}
	

	
	int getResourceCount() throws QueryEvaluationException, RepositoryException, MalformedQueryException{
		if (resCount==-1){
			resCount=0;
			//long size=con.size();
			TupleQueryResult result=con.prepareTupleQuery(QueryLanguage.SPARQL, countQuery).evaluate();
			while (result.hasNext()) {
				BindingSet bs=result.next();
				resCount++;
				if (resCount % 100 == 0)
					System.out.print(".");				
				getIndex((URI) bs.getValue("s"));
			}
			System.out.println("");
			result.close();
		}
		return resCount;
	}
	
	void storeMapping() {
		try {
			//Create file output stream.
			FileWriter fw=new FileWriter(path+matrixDir+filePrefix+"/"+filePrefix+"_mapping.csv");			
			for (int i=1;i<=uri2index.size();i++){	
				URI uri=(URI) uri2index.getKey(i);
				ResourceStatistics s=stat.getStatistics(uri.stringValue());
				fw.write(String.format("%d;%s;%d;%d\n",i,uri,s.getInDegree(),s.getOutDegree()));
			}
			fw.flush();
			fw.close();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	
	/** Map resource URI to index and assign indices.
	 * @param resource
	 * @return
	 */
	int getIndex(URI resource){
		Integer index=(Integer) uri2index.get(resource);
		if (index==null){
			++maxIndex;
			index=maxIndex;
			//System.out.printf("Adding new index %d for resource %s",index,resource.getLocalName());			
			uri2index.put(resource,index);			
		}
		return index;
	}
		
	void writeMatrix(URI link) throws Exception  {
		String fileName=getMatrixFileName(link);
		File file=new File(fileName);
		FileWriter fw=new FileWriter(file);	
		fw.write(getResourceCount()+" "+getResourceCount()+" 0\n");
		RepositoryResult<Statement> result=con.getStatements(null,link,null,false);
		int linkCounter=0;		
		while (result.hasNext() ) { //&& linkCounter<linkLimit
			Statement s=result.next();	
			if (s.getSubject() instanceof URI && s.getObject() instanceof URI) {
				URI subject=(URI) s.getSubject();
				URI object=(URI) s.getObject();			
				int m=getIndex(subject);
				int n=getIndex(object);		
				fw.write(m+" "+n+" "+1.0+"\n");
				allWriter.write(m+" "+n+" "+1.0+"\n");
				linkCounter++;				
				stat.addlink(subject.stringValue(), link.stringValue(), object.stringValue());				
			}			
		}		
		statementCounter+=linkCounter;
		fw.flush();
		fw.close();
		System.out.print("+"+linkCounter+"("+link.getLocalName()+") ");
		//if (result.hasNext())	System.out.println("Link limit exceeded for link type: "+link.stringValue());
		statisticsWriter.write(String.format("%-70s   %4d \n", link.stringValue(),linkCounter));
		matricesWriter.write(file.getName()+","+link.stringValue()+","+linkCounter+"\n");		
	}		
	
	String getMatrixFileName(URI link){
		if (!prop2file.containsKey(link)) {
			int i=0;
			String filename=path+matrixDir+filePrefix+"/"+filePrefix+"_"+link.getLocalName().replace("-", "")+i+".dat";
			while (prop2file.containsValue(filename)) {
				filename=path+matrixDir+filePrefix+"/"+filePrefix+"_"+link.getLocalName().replace("-", "")+ ++i+".dat";
			} 
			prop2file.put(link,filename);
			return filename;
		}
		return prop2file.get(link);
	}
}
