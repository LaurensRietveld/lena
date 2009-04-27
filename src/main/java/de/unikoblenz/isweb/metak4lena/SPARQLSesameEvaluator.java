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
package de.unikoblenz.isweb.metak4lena;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Value;
import org.xmedia.metak.eval.SesameSparqlEvaluator;
import org.xmedia.metak.prov.ComplexProvenance;

import fr.inria.jfresnel.sparql.SPARQLNSResolver;
import fr.inria.jfresnel.sparql.SPARQLQuery;



public class SPARQLSesameEvaluator extends fr.inria.jfresnel.sparql.sesame.SPARQLSesameEvaluator {

	
	public SPARQLSesameEvaluator(){
		super();
	}
	public SPARQLSesameEvaluator(SPARQLNSResolver nsr){
		super(nsr);
    }
	public Map<Value, ComplexProvenance> metaEvaluateQuery(String sparqlQuery){		
		Map<Set<Value>, ComplexProvenance> metadata = (new SesameSparqlEvaluator(super.getDataRepository())).evaluate(sparqlQuery);
				
		Map<Value, ComplexProvenance> provenance = new HashMap<Value, ComplexProvenance>();
		java.util.Iterator<Value> it;
		for (Set<Value> values: metadata.keySet()){
			it = values.iterator();
			provenance.put(it.next(), metadata.get(values));
		}
		return provenance; 
	}
}
