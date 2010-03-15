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
public class LinkCounter {
	Map<String,Integer> linkCount=new HashMap<String, Integer>();
		
	public void addLink(String predicate){
		if (linkCount.containsKey(predicate)){
			linkCount.put(predicate, linkCount.get(predicate)+1);
		} else {
			linkCount.put(predicate,1);
		}		
	}
	
	int getCount(String predicate) {
		if (linkCount.containsKey(predicate))
			return linkCount.get(predicate);
		return 0;
	}
	
		
}
