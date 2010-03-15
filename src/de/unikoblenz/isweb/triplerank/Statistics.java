/**
 * 
 */
package de.unikoblenz.isweb.triplerank;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class Statistics {
	Map<String,ResourceStatistics> rs=new HashMap<String,ResourceStatistics>();
	
	ResourceStatistics getStatistics(String uri) {
		if (rs.containsKey(uri)) {
			return rs.get(uri);
		} else {
			ResourceStatistics r=new ResourceStatistics(uri);
			rs.put(uri,r);
			return r;
		}
	}
	
	public int getDegree(String resource){
		return rs.get(resource).getDegree();
	}
	
	public int getInDegree(String resource) {
		return rs.get(resource).getInDegree();
	}
	
	public int getOutDegree(String resource){
		return rs.get(resource).getOutDegree();
	}
	
	public void addlink(String subject,String predicate,String object) {
		ResourceStatistics sstat=getStatistics(subject);
		sstat.addOutLink(predicate);
		ResourceStatistics ostat=getStatistics(object);
		ostat.addInLink(predicate);				
	}
	
	
}
