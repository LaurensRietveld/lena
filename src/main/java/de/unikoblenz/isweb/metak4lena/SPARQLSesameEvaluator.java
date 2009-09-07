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
* LENA uses blicense.jsp
*/ 
package de.unikoblenz.isweb.metak4lena;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Value;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.xmedia.metak.eval.SesameSparqlEvaluator;
import org.xmedia.metak.prov.ComplexProvenance;

import fr.inria.jfresnel.sparql.SPARQLNSResolver;



public class SPARQLSesameEvaluator extends fr.inria.jfresnel.sparql.sesame.SPARQLSesameEvaluator {

	public static final String _PREFIX = "PREFIX";
	public static final String _SELECT = "SELECT";
	public static final String _PREFIX_END = ":";
	
	Repository dataRepository = null;
	SPARQLNSResolver nsr = null;
	static public final String NL = System.getProperty("line.separator");
	public SPARQLSesameEvaluator(){
		
	}	
	 public void setDataRepository(Repository dataRepo){
		dataRepository = dataRepo;
	 }
	 public Repository getDataRepository(){
		 return dataRepository;
	 }
				    
	public Map<Value, ComplexProvenance> metaEvaluateQuery(String queryString) throws RepositoryException{
		SesameSparqlEvaluator eval = new SesameSparqlEvaluator(dataRepository);
		
		if (this.nsr == null) {
			RepositoryResult<Namespace> nsi = dataRepository.getConnection().getNamespaces();
			Namespace ns;
			this.nsr = new SPARQLNSResolver();
			while (nsi.hasNext()){
				ns = nsi.next();
				this.nsr.addPrefixBinding(ns.getPrefix(), ns.getName());
			}
		}
		
		Hashtable prologPrefixTable = new Hashtable();		
		if (queryString.contains(_PREFIX)){
			int i = queryString.indexOf(_SELECT);
			if (i != -1){
				parsePrologPrefixDeclarations(queryString.substring(0, i), prologPrefixTable);				
			}
		}
		Hashtable defaultPrefixTable = nsr.getPrefixTable();
		Iterator iter = defaultPrefixTable.keySet().iterator();
		while (iter.hasNext()) {
			String prefix =  (String)iter.next();
			if (!prologPrefixTable.containsKey(prefix)){
				queryString = "PREFIX " + prefix + ": <" + (String)defaultPrefixTable.get(prefix) + ">" + NL + queryString;				
			}
		}
		
		Map<Set<Value>, ComplexProvenance> metadata = eval.evaluate(queryString);
				
		Map<Value, ComplexProvenance> provenance = new HashMap<Value, ComplexProvenance>();
		java.util.Iterator<Value> it;
		for (Set<Value> values: metadata.keySet()){
			it = values.iterator();
			provenance.put(it.next(), metadata.get(values));
		}
		return provenance; 
	}
	public static void parsePrologPrefixDeclarations(String prolog, Hashtable res){
		String[] bindings = prolog.split(_PREFIX);
		for (int i=0;i<bindings.length;i++){
			bindings[i] = bindings[i].trim();
			if (bindings[i].length() > 0){
				int j = bindings[i].indexOf(_PREFIX_END);
				if (j != -1){
					String prefix = bindings[i].substring(0, j);
					String namespace = bindings[i].substring(j+1).trim();
					namespace = namespace.substring(1, namespace.length()-1);
					res.put(prefix, namespace);
				}
			}
		}
	}
}
