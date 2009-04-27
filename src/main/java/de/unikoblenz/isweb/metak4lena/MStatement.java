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

import org.openrdf.model.Statement;
import org.xmedia.metak.prov.ComplexProvenance;

public class MStatement {

	private Statement statement;
	private ComplexProvenance provenance;
	
	public MStatement(Statement s, ComplexProvenance p){
		statement = s;
		provenance = p;
	}
	
	Statement getStatement(){
		return statement;
	}
	
	ComplexProvenance getProvenance(){
		return provenance;
	}
}
