/**
 * 
 */
package de.unikoblenz.isweb.triplerank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailException;

/**
 * @author jmkoch
 *
 */
public class Matlab2Java {
	
	//static String virtuosoEndpoint="http://lod.openlinksw.com/sparql";
	static String endpoint="http://dbpedia.org/sparql";

	String path = "./";
	int factor = 15;
	Process process;
	
	// Stream-Reader
	BufferedWriter out;
	BufferedReader in;
	BufferedReader error;
	
	public Matlab2Java(String path) {
		this.path = path;
		String command = "matlab -nodisplay -nosplash -nodesktop -nojvm";
		System.out.println("*******---------------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>><");
		System.out.println("Matlab2Java: Matlab command: "+command+" CALLED!");
		
		try {
			process = Runtime.getRuntime().exec(command);
			
			out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void getURIs(String lodURI) {
		try {
			LODCrawler crawler=new LODCrawler();
			crawler.setLODURI(lodURI);
			crawler.setEndpoint(endpoint);
			crawler.setDumpFile(path + "data/rdf/" + lodURI.substring(lodURI.lastIndexOf("/") + 1).toLowerCase());
			crawler.crawl();
			System.out.println("Matlab2Java: crawl READY!!!");
			System.out.println("Matlab2Java: Matlab2Java path:" + path);
			RDF2Matlab transform = new RDF2Matlab();
			transform.init(lodURI.substring(lodURI.lastIndexOf("/") + 1).toLowerCase(),lodURI, path);
			System.out.println("Matlab2Java: RDF2Matlab READY!!!");
			
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SailException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RDFHandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public LinkedList<Object> getURIsFromMatlab(String matlabPath, String resourcePath, String lodURI) {
		LinkedList<Object> result = new LinkedList<Object>();
		
		try {
			String changePath = "cd " + matlabPath + "\n";
			String commandBegin = "[ktensor newspt newpstat newrstat resultmix evaldata]=myParafac('";
			String commandEnd = "',true,false,"+factor+",'"+resourcePath+"');";
			
			// Write a script to be loaded as Matlab parameter
			FileWriter fstream = new FileWriter(matlabPath + "matlab.sh");
	        BufferedWriter outW = new BufferedWriter(fstream);
	        outW.write(changePath);
	        //out.write(commandBegin + lodURI + "','" + lodURI.substring(lodURI.lastIndexOf("/") + 1).toLowerCase() + commandEnd);
	        outW.write(commandBegin + lodURI + "','" + "dump" + commandEnd);
	        outW.close();
	        File script = new File(matlabPath + "matlab.sh");
	        
	        System.out.println("Matlab2Java: Matlab script READY!!!");
	        System.out.println("Matlab2Java: Matlab path: " + matlabPath);
	        System.out.println("Matlab2Java: Resource path: " + resourcePath);
	        System.out.println("Matlab2Java: LOD URI: " + lodURI);
	        
	        // Start process
			//String command = "matlab -nodisplay -nosplash -nodesktop -nojvm";// -r cd " + matlabPath + ";" + commandBegin + lodURI + "','" + "dump" + commandEnd;
			//System.out.println("Matlab2Java: Matlab command: "+command);
			//String command = "matlab -nodisplay -nosplash -nodesktop -nojvm < " + script;
			//Process process = Runtime.getRuntime().exec(command);
			//System.out.println("LINKEDDATA: Matlab command called!!!");
			
			out.write("cd " + matlabPath);
			out.write("\n");
			out.write(commandBegin + lodURI + "','" + "dump" + commandEnd);
			out.write("exit;");
			out.close();
			
			LinkedList<String> weight = new LinkedList<String>();
			
			LinkedList<Object> groupResultScore = new LinkedList<Object>();
			LinkedList<Object> groupResultId = new LinkedList<Object>();
			LinkedList<Object> groupResultName = new LinkedList<Object>();
			
			LinkedList<Object> groupResultScorePredicate = new LinkedList<Object>();
			LinkedList<Object> groupResultIdPredicate = new LinkedList<Object>();
			LinkedList<Object> groupResultNamePredicate = new LinkedList<Object>();
			
			LinkedList<String> score = new LinkedList<String>();
			LinkedList<String> id = new LinkedList<String>();
			LinkedList<String> name = new LinkedList<String>();
			
			String returnLine;
			int count = 0;
			
			while ((returnLine = in.readLine()) != null) {
				
				//System.out.println(returnLine);
				
				if (returnLine.startsWith("Weight")) {
					weight.add(returnLine.substring(9));
				} else if (returnLine.startsWith("Score") && count == 0) {
					count++;
				} else if (returnLine.startsWith("Score") && count == 1) {
					count++;
				} else if (returnLine.startsWith("Score") && count == 2) {
					groupResultScore.add(score);
					groupResultId.add(id);
					groupResultName.add(name);
					
					score = new LinkedList<String>();
					id = new LinkedList<String>();
					name = new LinkedList<String>();
					
					count++;
				} else if ((returnLine.startsWith(" 0.") || returnLine.startsWith(" 1.")) && count == 2) {
					//System.out.println("ENTERING Res.: " + returnLine);
					score.add(returnLine.substring(1, 10));
					id.add(returnLine.substring(12, 17));
					name.add(returnLine.substring(17));
				} else if ((returnLine.startsWith(" 0.") || returnLine.startsWith(" 1.")) && count == 3) {
					//System.out.println("ENTERING Pre.: " + returnLine);
					score.add(returnLine.substring(1, 10));
					id.add(returnLine.substring(12, 17));
					name.add(returnLine.substring(17));
				} else if (count == 3) {
					groupResultScorePredicate.add(score);
					groupResultIdPredicate.add(id);
					groupResultNamePredicate.add(name);
					
					score = new LinkedList<String>();
					id = new LinkedList<String>();
					name = new LinkedList<String>();
					
					count = 0;
				}
			}
			
			groupResultScorePredicate.add(score);
			groupResultIdPredicate.add(id);
			groupResultNamePredicate.add(name);
			
			while ((returnLine = error.readLine()) != null) {
				System.out.println(returnLine);
			}
			
			result.add(weight);
			result.add(groupResultScore);
			result.add(groupResultId);
			result.add(groupResultName);
			result.add(groupResultScorePredicate);
			result.add(groupResultIdPredicate);
			result.add(groupResultNamePredicate);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
	public void setFactor(int factor) {
		this.factor = factor;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String lodURI = "http://dbpedia.org/resource/Berlin";
		//String lodURI = "http://dbpedia.org/resource/The_Beatles";
		
		Matlab2Java rankStore = new Matlab2Java("./");//relative dir
		rankStore.getURIs(lodURI);
		LinkedList<Object> result = rankStore.getURIsFromMatlab("WebContent/matlab/","./",lodURI);
		//LinkedList<Object> result = rankStore.getURIsFromMatlab("/home/jmkoch/workspace/svn/src/main/webapp/matlab/","/home/jmkoch/workspace/svn/src/main/webapp/public/resources/",lodURI);
		
		LinkedList<String> weight = (LinkedList<String>) result.get(0);
		
		LinkedList<Object> groupResultScore = (LinkedList<Object>) result.get(1);
		LinkedList<Object> groupResultId = (LinkedList<Object>) result.get(2);
		LinkedList<Object> groupResultName = (LinkedList<Object>) result.get(3);
		
		LinkedList<Object> groupResultScorePredicate = (LinkedList<Object>) result.get(4);
		LinkedList<Object> groupResultIdPredicate = (LinkedList<Object>) result.get(5);
		LinkedList<Object> groupResultNamePredicate = (LinkedList<Object>) result.get(6);
		
		// Output results
		for (int i = 0; i < weight.size(); i++) {
			System.out.println("Weight for Group " + i + ": " + weight.get(i));
			
			LinkedList<String> groupScore = (LinkedList<String>) groupResultScore.get(i);
			LinkedList<String> groupId = (LinkedList<String>) groupResultId.get(i);
			LinkedList<String> groupName = (LinkedList<String>) groupResultName.get(i);
			
			LinkedList<String> groupScorePredicate = (LinkedList<String>) groupResultScorePredicate.get(i);
			LinkedList<String> groupIdPredicate = (LinkedList<String>) groupResultIdPredicate.get(i);
			LinkedList<String> groupNamePredicate = (LinkedList<String>) groupResultNamePredicate.get(i);
			
			for (int j = 0; j < groupScore.size(); j++) {
				System.out.println("Resource Ranks " + j + ": Score: " + groupScore.get(j) + " Id: " + groupId.get(j) + " Name: " + groupName.get(j));
			}
			for (int j = 0; j < groupScorePredicate.size(); j++) {
				System.out.println("Predicate Ranks " + j + ": Score: " + groupScorePredicate.get(j) + " Id: " + groupIdPredicate.get(j) + " Name: " + groupNamePredicate.get(j));
			}
		}
	}
		
}
