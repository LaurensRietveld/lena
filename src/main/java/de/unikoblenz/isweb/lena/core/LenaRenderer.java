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
package de.unikoblenz.isweb.lena.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import fr.inria.jfresnel.Constants;
import fr.inria.jfresnel.ContentFormat;
import fr.inria.jfresnel.Format;
import fr.inria.jfresnel.FresnelDocument;
import fr.inria.jfresnel.Lens;
import fr.inria.jfresnel.StyleFormatContainer;
import fr.inria.jfresnel.fsl.FSLPath;
import fr.inria.jfresnel.fsl.sesame.FSLSesameEvaluator;
import fr.inria.jfresnel.sesame.FresnelSesameParser;
import fr.inria.jfresnel.sesame.SesameLens;
import fr.inria.jfresnel.sesame.SesameRenderer;
import fr.inria.jfresnel.sparql.SPARQLQuery;
import fr.inria.jfresnel.sparql.sesame.SPARQLSesameEvaluator;

/**
 * Renders a DOM document as created by a Fresnel-Engine
 * to HTML.
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class LenaRenderer extends SesameRenderer {

	public Document render(FresnelDocument fd, Repository dataRepo, Lens lens)
		 throws Exception {
			try {
				DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document out = db.newDocument();
				Element root = out.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "results");
				out.appendChild(root);
			
				render(fd, dataRepo, out, root, new Lens[]{lens});				

				return out;
			}
			catch (Exception e) {
				throw e;
			}
		}
	


Repository fresnelRepository;

public LenaRenderer(){}

public void setFresnelRepository(Repository fresnelRepo) {
	fresnelRepository = fresnelRepo;
}

public Document render(Repository fresnelRepo, String uri, Repository dataRepo) throws ParserConfigurationException {
	FresnelSesameParser parser = new FresnelSesameParser();
	FresnelDocument fd = parser.parse(fresnelRepo, uri);
	setFresnelRepository(fresnelRepo);
	return render(fd, dataRepo);
}

/**
 * Renders a Fresnel Display program to a DOM document.
 *@param fd parsed Fresnel Display program
 *@return org.w3c.dom.Document description of rendered results;
 */
public Document render(FresnelDocument fd, Repository dataRepo) throws ParserConfigurationException {
	try {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document out = db.newDocument();
		Element root = out.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "results");
		out.appendChild(root);
	
		Lens[] lenses = fd.getLenses(); 
		render(fd, dataRepo, out, root, lenses);

		return out;
	}
	catch (Exception e) {
		throw new ParserConfigurationException("Rendering only supported for Sesame");
	}
}

/**
 * Renders specified lens from a Fresnel Display program to a DOM document.
 *@param fd parsed Fresnel Display program
 *@param lensURI URI of lens to be rendered
 *@return org.w3c.dom.Document description of rendered results;
 */
public Document render(FresnelDocument fd, Repository dataRepo, String lensURI) throws ParserConfigurationException {
	try {
		String[] lURIA = new String[1];
		lURIA[0] = lensURI;
		return render(fd, dataRepo, lURIA);
	}
	catch (Exception e) {
		throw new ParserConfigurationException("Rendering only supported for Sesame");
	}
}

/**
 * Renders a set of lenses from a Fresnel Display program to a DOM document.
 *@param fd parsed Fresnel Display program
 *@param lensURI set of URI's of lenses to be rendered
 *@return org.w3c.dom.Document description of rendered results;
 */
public Document render(FresnelDocument fd, Repository dataRepo, String[] lensURI) throws ParserConfigurationException {
	try {
		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document out = db.newDocument();
		Element root = out.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "results");
		out.appendChild(root);
	
		Lens[] lenses = fd.getLenses(lensURI);
		if (lenses.length > 0) {
			render(fd, dataRepo, out, root, lenses );
		}

		return out;
	}
	catch (Exception e) {
		throw new ParserConfigurationException("Rendering only supported for Sesame");
	}
}

/*** end of public methods ***/

/**
 * Renders a set of lenses from a Fresnel Display program to a DOM document.
 *@param parsed Fresnel Display program
 *@param container for data to be rendered
 *@param org.w3c.dom.Document for output
 *@param DOM document element to which all elements are appended
 *@param set of URI's of lenses to be rendered
 */
void render(FresnelDocument fd, Repository dataRepo, Document doc, Element parent, Lens[] lens) throws ParserConfigurationException {
	Lens renderLens = SesameLens.selectRenderLens(lens);
	if (renderLens == null)
		return;
	if (renderLens.getSPARQLInstanceDomains() != null) {
		SPARQLQuery[] sid = renderLens.getSPARQLInstanceDomains();
		SesameLens sl = (SesameLens)renderLens;
		SPARQLSesameEvaluator sse = new SPARQLSesameEvaluator();
		sse.setDataRepository(dataRepo);
		for (int i = 0; i < sid.length; i++)
		{
			Vector queryResults = sse.evaluateQuery(sid[i]);
			for (int k=0; k<queryResults.size(); k++) {
			Object end = queryResults.get(k);
			if (end instanceof Resource)
			{
				Resource r = (Resource)end;
				Element eR = createResourceElement(fd, dataRepo, doc, sl, null, r, Constants.DEFAULT_MAX_DEPTH);
				parent.appendChild(eR);
			}
			else if (end instanceof Literal)
			{
				/*
				Literal l = (Literal)end;
				
				ValueFactory f = dataRepo.getValueFactory();
				URI r = f.createURI(l.getLabel());
				if (r == null)
					continue;
				Element eR = createResourceElement(fd, dataRepo, doc, sl, null, r, Constants.DEFAULT_MAX_DEPTH);
								
				parent.appendChild(eR);
				*/
			}
			else if (end instanceof Statement)
			{
				String error = "Render Error: SPARQL query must end on node";
				throw new ParserConfigurationException(error);
			}
		}
		}
	}
	else if (renderLens.getFSLInstanceDomains() != null)
	{
		FSLPath[] fid = renderLens.getFSLInstanceDomains();
		SesameLens sl = (SesameLens)renderLens;
		FSLSesameEvaluator fse = new FSLSesameEvaluator();
		fse.setDataRepository(dataRepo);
		for (int i = 0; i < fid.length; i++)
		{
			Vector pathInstances = fse.evaluatePath(fid[i]);
			for (int k=0; k<pathInstances.size(); k++) { 
			Object end = ((Vector)pathInstances.get(k)).get(0);
			if (end instanceof Resource)
			{
				Resource r = (Resource)end;
				Element eR = createResourceElement(fd, dataRepo, doc, sl, null, r, Constants.DEFAULT_MAX_DEPTH);
				parent.appendChild(eR);
			}
			
			else if (end instanceof Literal)
			{
				Literal l = (Literal)end;
				ValueFactory f = dataRepo.getValueFactory();
				URI r = f.createURI(l.getLabel());
				if (r == null)
					continue;
				Element eR = createResourceElement(fd, dataRepo, doc, sl, null, r, Constants.DEFAULT_MAX_DEPTH);
				parent.appendChild(eR);
			}
			else if (end instanceof Statement)
			{
				String error = "Render Error: FSL path must end on node";
				throw new ParserConfigurationException(error);
			}
			}
		}
	}
	else if (renderLens.getBasicInstanceDomains() != null)
	{
		java.lang.String[] bid = renderLens.getBasicInstanceDomains();
		SesameLens sl = (SesameLens)renderLens;
		HashMap resSet = new HashMap();

		for (int j = 0; j < bid.length; j++)
		{
			ValueFactory f = dataRepo.getValueFactory();
			URI r = f.createURI(bid[j]);
			if (r == null)
				continue;

			// Don't render resource twice (???)
			if (resSet.containsKey(r))
					continue;
			resSet.put(r, null);
			
			Element eR = createResourceElement(fd, dataRepo, doc, sl, null, r, Constants.DEFAULT_MAX_DEPTH);
			parent.appendChild(eR);
		}
	}
	else if (renderLens.getBasicClassDomains() != null)
	{
		java.lang.String[] cd = renderLens.getBasicClassDomains();
		SesameLens sl = (SesameLens)renderLens;
		ValueFactory f = dataRepo.getValueFactory();
		URI pRDFType = f.createURI(Constants.RDF_NAMESPACE_URI + "type");	
		HashMap resSet = new HashMap();

		for (int j = 0; j < cd.length; j++)
		{
			try {
				RepositoryConnection connection = dataRepo.getConnection();
				RepositoryResult<Statement> si = connection.getStatements(null, RDF.TYPE, f.createURI(cd[j]), true);
				Statement s;
				Resource r;
				while (si.hasNext()){
					s = si.next();
					r = s.getSubject();
					// Don't render resource twice
					if (resSet.containsKey(r))
						continue;
					resSet.put(r, null);
					Element eR = createResourceElement(fd, dataRepo, doc, sl, null, r, Constants.DEFAULT_MAX_DEPTH);
					// TODO don't append if no property elements
					parent.appendChild(eR);
				}
				si.close();
			}
			catch (Exception ex){
				System.err.println("Error while getting statements from repository:");
			}
		}
	}
}


/**
 * Creates a DOM document element describing a single resource. Can recurse...
 *@param parsed Fresnel Display program
 *@param container for data to be rendered
 *@param org.w3c.dom.Document for output
 *@param Lens to use for rendering
 *@param data resource to process
 *@param depth to limit recursion
 *@return org.w3c.dom.Document element describing specified resource;
 */
Element createResourceElement(FresnelDocument fd, Repository dataRepo, Document doc, SesameLens sl, String useURI, Resource r, int depth) {
	if (depth < 0)
		return null;
	
	// TODO: fresnel:primaryClasses? What to do with this?

	/*
	 *    <xsd:complexType name="FresnelResource">
	 *     <xsd:sequence>
	 *       <xsd:element name="content" type="FresnelContents" minOccurs="0" maxOccurs="1"/>
	 *       <xsd:element name="title" type="xsd:string" minOccurs="1" maxOccurs="1"/>
	 *       <xsd:element name="property" type="FresnelProperty" minOccurs="1" maxOccurs="unbounded"/>
	 *     </xsd:sequence>
	 *     <xsd:attribute name="class" type="xsd:string"/>
	 *     <xsd:attribute name="uri" type="xsd:string"/>
	 *   </xsd:complexType>
	 */
	Element eR = doc.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "resource");
	
	if (!(r instanceof BNode))
		eR.setAttribute("uri", r.toString());

	// TODO: SIMILE adds link element for css link
	// TODO: add optional content element
	// TODO: what is the title of the resource?
	eR.appendChild(createTextElement(doc, "title", null));
	
	Vector vResourceStyle = new Vector();
	appendResourceProperties(fd, dataRepo, doc, eR, sl, useURI, r, vResourceStyle, depth);
	setClassAttribute(eR, vResourceStyle);
	
	return eR;

}


/**
 * Appends DOM document elements describing properties of specified resource.
 * @param parsed Fresnel Display program
 * @param container for data to be rendered
 * @param org.w3c.dom.Document for output
 * @param parent org.w3c.dom.Document element for output
 * @param vector of styles to apply to current resource element
 * @param Lens to use for rendering
 * @param data resource to process
 */
void appendResourceProperties(FresnelDocument fd, Repository dataRepo, Document doc, Element eR, SesameLens sl, String useURI, Resource r, Vector vResourceStyle, int depth) {		
	// For JenaLens, getPropertyValuesToDisplay generates instances of Jena statement objects. 
	// The predicate and object of these statements are the property-value pairs of interest.
	//
	// The property Selection Vocabulary from the Fresnel Ontology:
	//
	//	:showProperties a owl:ObjectProperty ;
	//	    rdfs:label "show properties"@en ;
	//	    rdfs:comment "List of all properties which should be shown."@en ;
	//	    rdfs:isDefinedBy <http://www.w3.org/2004/09/fresnel> ;
	//	    rdfs:domain :Lens ;
	//	    rdfs:range [ a owl:Class ;
	//		        owl:unionOf (
	//		            rdf:Property
	//		            :PropertyDescription
	//		            :ConvenienceToken
	//		            :ShowPropertyList
	//		        )
	//	    ]
	//	    .
	//
	// rdf:Property & :ShowPropertyList are returned as 
	// Jena Statements with s=resource,p=property,o=value
	// :PropertyDescription are returned as 
	// Jena Statements with s=PropertyDescription_resource,p=?,o=?
	//
	// TODO: ConvenienceToken????
	
	Element lastValues = null;
	URI lastPredicate = null;
	
	Vector rsv = sl.getPropertyValuesToDisplay(r, dataRepo, fresnelRepository);
	Iterator rsi = rsv.iterator();
	while (rsi.hasNext())
	{
		Statement rs = (Statement)rsi.next();
		String rsSID = rs.getSubject().toString();
		String rID = r.toString();
		URI rp = rs.getPredicate();
		
		// keep track of previous predicate to re-use "values" element in doc
		if ((lastPredicate == null) || !(lastPredicate.toString().equals(rp.toString())))
		{
			lastPredicate = null;
			lastValues = null;
		}
	
		if (rsSID.equals(rID))
		{
			// "normal" property specification
			Format rf = sl.getBestFormatForProperty(fd, dataRepo, rs, useURI);

			String pName = rp.toString();
			Value o = rs.getObject();
			String oName = null;
			if (o instanceof Literal)
				oName = ((Literal)o).getLabel();
			else
				oName = o.toString();
			
			StyleFormatContainer sfc = new StyleFormatContainer(sl, rf, vResourceStyle, null);
			if (lastValues == null)
			{
				// re-use values node where possible
				Element eP = createPropertyElement(dataRepo, doc, sl, rf, pName, oName, sfc );
				eR.appendChild(eP);
				lastValues = getValuesElement(eP);
				lastPredicate = rp;
			}
			String valueType = (rf == null) ? null : rf.getValueTypeName();
	        Element eV = createValueElement(doc, oName, valueType, sfc.vValueStyle);  
	        lastValues.appendChild(eV);
		}
		else
		{
			// if subject of statement not current lens, 
			// then a PropertyDescription resource 
			//		:PropertyDescription a owl:Class ;
			//	    rdfs:subClassOf rdfs:Resource ;
			//	    rdfs:label "Property Description"@en ;
			//	    rdfs:comment "More detailed description of the property, e.g. for specifing sublenses or merging properties."@en ;
			//	    rdfs:subClassOf [
			//	        a owl:Restriction ;
			//	        owl:onProperty :property ;
			//	        owl:allValuesFrom rdf:Property
			//	    ] ;
			// minCardinality = 0, maxCardinality = 1
			//	    rdfs:subClassOf [
			//	        a owl:Restriction ;
			//	        owl:onProperty :sublens ;
			//	        owl:minCardinality "0"^^dtype:nonNegativeInteger
			//	    ] ;
			// minCardinality = 0
			//	    rdfs:subClassOf [
			//	        a owl:Restriction ;
			//	        owl:onProperty :depth ;
			//	        owl:allValuesFrom dtype:nonNegativeInteger
			//	    ] ;
			// minCardinality = 0, maxCardinality = 1
			//		rdfs:subClassOf [
			//		    a owl:Restriction ;
			//		    owl:onProperty :use ;
			//		    owl:allValuesFrom [ a owl:Class ;
			//		           owl:unionOf ( :Group :Format ) ]
			//		]
			// minCardinality = 0, maxCardinality = 1
			//		.
			
		    SesameLens.PropertyDescriptionProperties pdp = sl.getPropertyDescriptionProperties(rs.getSubject(), fresnelRepository);
		    if (pdp.property == null)
		    	continue;
			// re-use values node where possible
			Element eP = null;					
	    	Lens[] sublens = fd.getLenses(pdp.sublens);

	    	// Target resource is object from original 
	    	// resource : fresnel:property : object statements
			try {
				RepositoryConnection connection = dataRepo.getConnection();
				RepositoryResult<Statement> msl = connection.getStatements(r, null, null, true);
				Statement sr;
				while (msl.hasNext()){
					sr = msl.next();
					URI pr = sr.getPredicate();
					if (pr.toString().equals(pdp.property)) {
						Value no = sr.getObject();
						String valueType = null;
						StyleFormatContainer sfc = null;
	
						if (eP == null){
							// TODO: Is this right for getting styles?????
							// TODO: Fresnel:use - should this be "used" here?
							Format rf = sl.getBestFormatForProperty(fd, dataRepo, sr, pdp.use);
							sfc = new StyleFormatContainer(sl, rf, vResourceStyle, null);
							if (rf != null) valueType = rf.getValueTypeName();
							eP = createPropertyElement(dataRepo, doc, sl, rf, pdp.property, null, sfc);
							eR.appendChild(eP);
							lastValues = getValuesElement(eP);
							lastPredicate = rp;
						}
						
						if (no instanceof Resource){
							// recurse!
							// TODO: Is this right for getting styles?????
							Resource rr = (Resource)no;
							SesameLens jlr = (SesameLens)sl.selectRenderLens(dataRepo, sublens, rr);
							Format rf = sl.getBestFormatForProperty(fd, dataRepo, sr, pdp.use);
							sfc = new StyleFormatContainer(sl, rf, vResourceStyle, null);
							Element eV = createValueElement(doc, null, valueType, sfc.vValueStyle);
							lastValues.appendChild(eV);
							if (jlr != null)
							{
								// TODO: fix depth stuff??? (or is this right???)
								int dr = depth;
								if (pdp.depth >= 0 && pdp.depth < depth) 
									dr = pdp.depth;
								Element nre = createResourceElement(fd, dataRepo, doc, jlr, pdp.use, rr, dr);
								if (nre != null)
									eV.appendChild(nre);
							}
						}
						else {
							String oName = null;
							if (no instanceof Literal)
								oName = ((Literal)no).getLabel();
							else
								oName = no.toString();
							Element eV = createValueElement(doc, oName, valueType, sfc.vValueStyle);  
							lastValues.appendChild(eV);
						}
						
					}
			    }
			    msl.close();
			}
			catch (RepositoryException ex){
			    System.err.println("Error while getting statements from repository:");
			}
		}
	}
}

/**
 * Create DOM doc element describing specified property.
 *@param container for data to be rendered
 *@param org.w3c.dom.Document for output
 *@param Lens to use for rendering
 *@param Format to use for rendering
 *@param property URI
 *@param value string
 *@param vector of property styles from current lens, group and format
 *@param vector of label styles from current lens, group and format
 *@param vector of value styles from current lens, group and format
 *@return DOM doc element describing specified property
 */
Element createPropertyElement(Repository dataRepo, Document doc, SesameLens sl, Format sf, String pName, String oText, StyleFormatContainer sfc) {
/*
     * <xsd:complexType name="FresnelProperty">
     *  <xsd:sequence>
     *   <xsd:element name="content" type="FresnelContents" minOccurs="0" maxOccurs="1"/>
     *   <xsd:element name="label" type="FresnelLabel" minOccurs="1" maxOccurs="1"/>
     *   <xsd:element name="values" type="FresnelValues" minOccurs="1" maxOccurs="1"/>
     *  </xsd:sequence>
     *  <xsd:attribute name="class" type="xsd:string"/>
     *  <xsd:attribute name="uri" type="xsd:string"/>
     * </xsd:complexType>
     */

	Element eP = doc.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "property");
	eP.setAttribute("uri", pName );
	setClassAttribute(eP, sfc.vPropertyStyle);
    
	// Create optional content element
	Element eC = createContentElement(doc, sfc.propertyFormattingInstructions);
	if (eC != null)
		eP.appendChild(eC);

	// Create required label element
	String labelText = (sf == null) ? "" : sf.getValueLabel();
    Element eL = createLabelElement(doc, labelText, sfc.labelFormattingInstructions, sfc.vLabelStyle);
	eP.appendChild(eL);

	// Create required values element
	ContentFormat cf = (sf == null) ? null : sf.getValueFormattingInstructions();
	Element eVS = createValuesElement(doc, cf);
	eP.appendChild(eVS);

    return eP;

}

/**
 * Create DOM doc element describing specified label.
 * @param org.w3c.dom.Document for output
 * @param label text
 * @param label style
 * @return DOM doc element describing specified label
 */
Element createLabelElement(Document doc, String labelText, ContentFormat cf, Vector vLabelStyle) {
	/*
	 * <xsd:complexType name="FresnelLabel">
	 *   <xsd:sequence>
	 *     <xsd:element name="contents" type="FresnelContents" minOccurs="0" maxOccurs="1"/>
	 *     <xsd:element name="title" type="xsd:string" minOccurs="1" maxOccurs="1"/>
	 *   </xsd:sequence>
	 *   <xsd:attribute name="class" type="xsd:string"/>
	 * </xsd:complexType>
	 */
    Element eL = doc.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "label");
    
    // Create optional content element
	Element eC = createContentElement(doc, cf);
	if (eC != null)
		eL.appendChild(eC);

    // Create required title element
	eL.appendChild(createTextElement(doc, "title", labelText));
	
	// optional class attribute
	setClassAttribute(eL, vLabelStyle);

	return eL;
}

/**
 * Create DOM doc element describing specified values.
 * @param org.w3c.dom.Document for output
 * @param values content
 * @return DOM doc element describing specified values
 */
Element createValuesElement(Document doc, ContentFormat cf) {
	/*
	 *   <xsd:complexType name="FresnelValues">
	 *    <xsd:sequence>
	 *      <xsd:element name="contents" type="FresnelContents" minOccurs="0" maxOccurs="1"/>
	 *      <xsd:element name="value" type="FresnelValue" minOccurs="1" maxOccurs="unbounded"/>
	 *    </xsd:sequence>
	 *   </xsd:complexType>
	 */
    Element eV = doc.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "values");
    
    // create optional content element
	Element eC = createContentElement(doc, cf);
	if (eC != null)
		eV.appendChild(eC);
	
	// required value element(s) added by caller....

	return eV;
}

/**
 * Create DOM doc element describing specified value.
 * @param org.w3c.dom.Document for output
 * @param value text
 * @param value style
 * @return DOM doc element describing specified value
 */
Element createValueElement(Document doc, String valueText, String valueType, Vector vValueStyle) {
	/*
	 *   <xsd:complexType name="FresnelValue">
	 *   <xsd:choice>
	 *     <xsd:element name="title" type="xsd:string" maxOccurs="1"/>
	 *     <xsd:element name="resource" type="FresnelResource" maxOccurs="1"/>
	 *   </xsd:choice>
	 *   <xsd:attribute name="class" type="xsd:string"/>
	 *   <xsd:attribute name="output-type" type="xsd:string"/>
	 *  </xsd:complexType>
	 */
    Element eV = doc.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "value");
    if (valueText != null)
    	eV.appendChild(createTextElement(doc, "title", valueText));
	if (valueType != null)
		eV.setAttribute("output-type", valueType);
	setClassAttribute(eV, vValueStyle);

	return eV;
}

/**
 * Create DOM doc element describing specified property.
 * @param org.w3c.dom.Document for output
 * @param element name
 * @param value string
 * @return DOM doc element describing specified property
 */
Element createTextElement(Document doc, String eName, String textVal) {
	Element e = doc.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, eName);
	if (textVal != null && !textVal.equals(""))
		e.appendChild(doc.createTextNode(textVal));
	return e;
}

/**
 * Create DOM doc element describing specified content.
 * @param org.w3c.dom.Document for output
 * @param content format
 * @return DOM doc element describing specified content
 */
Element createContentElement(Document doc, ContentFormat cf) {
	/*
	 *  <xsd:complexType name="FresnelContents">
	 *    <xsd:sequence>
	 *      <xsd:element name="before" type="xsd:string" minOccurs="0" maxOccurs="1"/>
	 *      <xsd:element name="after" type="xsd:string" minOccurs="0" maxOccurs="1"/>
	 *      <xsd:element name="first" type="xsd:string" minOccurs="0" maxOccurs="1"/>
		 *     <xsd:element name="last" type="xsd:string" minOccurs="0" maxOccurs="1"/>
	 *    </xsd:sequence>
	 *  </xsd:complexType>
	 */
	Element eC = null;
    if (cf != null && cf.hasFormattingInstructions()) {
        eC = doc.createElementNS(Constants.FRESNEL_OUTPUT_SCHEMA_NAMESPACE_URI, "content");
		if (cf.before != null) eC.appendChild(createTextElement(doc, "before", cf.before));	                        	
		if (cf.after != null) eC.appendChild(createTextElement(doc, "after", cf.after));	                        	
		if (cf.first != null) eC.appendChild(createTextElement(doc, "first", cf.first));	                        	
		if (cf.last != null) eC.appendChild(createTextElement(doc, "last", cf.last));
    }
	return eC;
}

/**
 * Create DOM doc element describing specified content.
 * @param org.w3c.dom.Document for output
 * @param content format
 * @return DOM doc element describing specified content
 */
void setClassAttribute(Element el, Vector style) {
	if (el != null && style != null && style.size() > 0) {
		String oStyle =  "";
		for (Iterator it = style.iterator(); it != null && it.hasNext();) {
			oStyle += (String)it.next();
			if (it.hasNext())
				oStyle += ",";
		}
		el.setAttribute("class", oStyle);
	}
}


/**
 * Search specified DOM doc node for "values" child.
 * @param parent node to search
 * @return DOM doc "values" element
 */
Element getValuesElement(Element propertyNode) {
	// re-use values node where possible
	NodeList nl = propertyNode.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++) {
		if (nl.item(i).getNodeName().equals("values")) {
			return(Element)nl.item(i);
		}
	}
	return null;
}	


}
