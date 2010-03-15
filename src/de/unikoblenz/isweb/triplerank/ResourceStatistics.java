/**
 * 
 */
package de.unikoblenz.isweb.triplerank;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class ResourceStatistics implements Comparable<ResourceStatistics> {
	private String uri;
	private Map<String,Integer> outLinks=new HashMap<String,Integer>();
	private Map<String,Integer> inLinks=new HashMap<String,Integer>();	
	private int inoutSum=0;
	private int inlinks=0;
	private int outlinks=0;
	
	public ResourceStatistics (String uri){
		this.uri=uri;
	}
	
	public void addOutLink(String predicate){
		if (outLinks.containsKey(predicate)){
			outLinks.put(predicate, outLinks.get(predicate)+1);
		} else {
			outLinks.put(predicate,1);
		}
		outlinks++;
		inoutSum++;
	}
	
	public String getUri(){
		return uri;
	}
	
	public void addInLink(String predicate){
		if (inLinks.containsKey(predicate)){
			inLinks.put(predicate, inLinks.get(predicate)+1);
		} else {
			inLinks.put(predicate,1);
		}
		inlinks++;
		inoutSum++;
	}
	
	public int getDegree(){
		return inoutSum;
	}
	
	public int getInDegree(){
		return inlinks;
	}
	
	public int getOutDegree() {
		return outlinks;
	}
	
	public String printString() {
		String s="* "+uri+" "+inoutSum+" (In+Out) *\n";
		for (Entry<String,Integer> entry:outLinks.entrySet()) {
			int in=0;
			if (inLinks.containsKey(entry.getKey())) {
				in=inLinks.get(entry.getKey());
			}
			s+=String.format("%-70s  In: %4d Out: %4d\n",entry.getKey(),in,entry.getValue());
		}
		inLinks.keySet().removeAll(outLinks.keySet());
		for (Entry<String,Integer> entry:inLinks.entrySet()) {			
			s+=String.format("%-70s  In: %4d Out: %4d\n",entry.getKey(),entry.getValue(),0);
		}
		return s;
	}

	@Override
	public int compareTo(ResourceStatistics other) {
		Integer otherSum=((ResourceStatistics) other).inoutSum;
		Integer mySum=inoutSum;
		return mySum.compareTo(otherSum);
	}
	
	
	
}
