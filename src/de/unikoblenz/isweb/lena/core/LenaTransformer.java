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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.DOMBuilder;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;



/**
 * @author Thomas Franz, http://isweb.uni-koblenz.de
 *
 */
public class LenaTransformer {
	
	public static Namespace fresnelNS=Namespace.getNamespace("http://www.w3.org/2004/09/fresnel-tree");
	private static LenaController controller;
	
	private static String resourceURIString = "";
	private static String classURIString = "";
	private static String locationString ="";
	private static String metaString = "";
	private static String pageString = "";
	private static String bookmarkString = "";
	
	public static Element transform(org.w3c.dom.Document w3cdoc, String _resourceURIString, String _classURIString, 
			String _locationString, String _metaString, String _pageString, String _bookmarkString){
		//controller = _controller;
		resourceURIString = _resourceURIString;
		classURIString = _classURIString;
		locationString = _locationString;
		metaString = _metaString;
		pageString = _pageString;
		bookmarkString = _bookmarkString;
		
		
		DOMBuilder builder=new DOMBuilder();
		Document doc=builder.build(w3cdoc);

		Element selection = new Element("div")
							.setAttribute("id", "selection");
		List<Element> resources=doc.getRootElement().getChildren("resource",fresnelNS);
		
		for (Element e:resources) {
			selection.addContent(handleResourceElement(e));
		}
		return selection;
	}
	
	private static Element handleResourceElement(Element resource) {
		Element resourceElement=new Element("div")
									.setAttribute("class", "resource")
									.setAttribute("id", "resource");
		
		String classAttribute=resource.getAttributeValue("class","");
		if (classAttribute!=null) {
			resourceElement.setAttribute("style",classAttribute);
		}		
		resourceElement.addContent(handleResourceTitle(resource));
		List<Element> properties=resource.getChildren("property", fresnelNS);
		for (Element propertyElement:properties) {
			resourceElement.addContent(handleProperty(propertyElement));
		}
		
		//NEW: Node for metaknowledge (Renata Dividino - 02.04.2009)
		//if (sidebar == true) {
	
		/*Element metaknowledgeElement=new Element("div")
		.setAttribute("id", "metaknowledge")
		.addContent(new Element("div")
						.setAttribute("id", "mtitle")
						.setText("Meta Knowledge")
						);

		List<Element> metaproperties=resource.getChildren("metaproperty", fresnelNS);
		for (Element metapropertyElement:metaproperties) {
			metaknowledgeElement.addContent(handleMetaproperty(metapropertyElement));
		}
		if(metaknowledgeElement.getChildren().size() > 1){
			resourceElement.addContent(metaknowledgeElement);						
		}*/
		
		return resourceElement;
	}
	
	/**
	 * @param resource
	 * @return
	 */
	private static Element handleResourceTitle(Element resource){
		String uri="Unknown URI";
		String uriEncoded="URI not given";
		//String lens="notYetImplemented";
		if (resource.getAttributeValue("uri")!=null) {
			uri=resource.getAttributeValue("uri");	
			try {
				uriEncoded = URLEncoder.encode(uri,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				uriEncoded="encodingErrorOccurred";
			}
		}
		
		
		Element title=new Element("span")
							.setAttribute("style", "cursor: help; padding: 0px 0px 0px 5px;")
							.setAttribute("class","tooltip")
							.setAttribute("title", "Resource: "+uri)
							.setAttribute("rel","");

		Element table = new Element("table")
		.setAttribute("style","border: 0px; width: 100%; padding: 0px; margin: 0px; border-spacing: 0px;");
	
		Element tableRow=new Element("tr")
			.setAttribute("style","padding: 0px; margin: 0px; border: 0px;");
		
		
		table.addContent(tableRow);
		
		Element tableElement1=new Element("td")
			.setAttribute("style","width: 35%; vertical-align: top; padding: 0px; margin: 0px; border: 0px;");
			
		
		String label=resource.getChildTextTrim("title", fresnelNS);
		if (label!=null && !label.trim().equals("")) {
			tableElement1.addContent(new Element("div")
				.setText(label)
				.setAttribute("id", "title")
				.setAttribute("style", "padding: 0px 0px 0px 5px;"));
		} else {
			tableElement1.addContent(new Element("div")
				.setText(uri)
				.setAttribute("id", "title"));
		}
		
		tableRow.addContent(tableElement1);
				// link to view resource with DEFAULT lens
		Element tableElement2 = new Element("td");
		
		Element tableIcon = new Element("table")
				.addContent(new Element("td")
				//.addContent(new Element("td")
				.addContent(new Element("a")
					.setAttribute("onclick", "setHrefs()")
					.setAttribute("href", "?resource="+uriEncoded+"&location="+locationString+"&meta="+metaString+ "&bookmark=" + bookmarkString)
					.setAttribute("class", "tooltip")
					.setAttribute("title","Lens: DEFAULT")
					.setAttribute("rel","Resource: "+uri)
						// RDF logo indicating DEFAULT lens
						.addContent(new Element("img")
							.setAttribute("src","public/images/rdf_icon.gif")
							.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
							.setAttribute("alt","Icon.")
						)));
		if (resource.getAttributeValue("lens")!=null) {
			try {
				tableIcon.addContent(new Element("td")
						.addContent(new Element("a")
						.setAttribute("onclick", "setHrefs()")
						.setAttribute("href", "?resource="+uriEncoded+"&lens="+URLEncoder.encode(resource.getAttributeValue("lens"),"UTF-8")+"&location="+locationString+"&meta="+metaString + "&bookmark=" + bookmarkString)
						.setAttribute("class", "tooltip")
						.setAttribute("title","Lens")
						.setAttribute("rel","Resource: "+resource.getAttributeValue("lens"))
//						// RDF logo indicating DEFAULT lens
							.addContent(new Element("img")
								.setAttribute("src","public/images/lens_icon.png")
								.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
								.setAttribute("alt","Icon.")
							)));
				
				
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
						
		}
		if (resource.getAttributeValue("bookmark")!=null){
			if (resource.getAttributeValue("bookmark").equals("true")){
			tableIcon.addContent(new Element("td")
			.addContent(new Element("a")
			.setAttribute("onclick", "alert('You have removed the bookmark for the resource"+uri+"')")//""setHrefs()")
			.setAttribute("href", "?resource="+uriEncoded+"&location="+locationString+"&meta="+metaString + "&bookmark=false")			
			.setAttribute("title","Remove Bookmark :: Resource: "+ uri)
				.addContent(new Element("img")
					.setAttribute("src","public/images/delete.png")
					.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
					.setAttribute("alt","Icon.")
				)));
			
			}else{
				tableIcon.addContent(new Element("td")
				.addContent(new Element("a")
				.setAttribute("onclick", "alert('You have added a bookmark for the resource"+uri+"')")//"setHrefs()")
				.setAttribute("href", "?resource="+uriEncoded+"&location="+locationString+"&meta="+metaString + "&bookmark=true")			
				.setAttribute("title","Add Bookmark :: Resource: "+ uri)
					.addContent(new Element("img")
						.setAttribute("src","public/images/add.png")
						.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
						.setAttribute("alt","Icon.")
					)));
			}
		}
		tableElement2.addContent(tableIcon);
		
		if(!uri.equalsIgnoreCase("unknown URI"))
			tableRow.addContent(tableElement2);	
		
		
		
		title.addContent(table);
	
//		title.addContent(
//				// link to view resource with DEFAULT lens
//				new Element("a")
//					.setAttribute("onclick", "setHrefs()")
//					.setAttribute("href", "?resource="+uriEncoded)
//					.setAttribute("class", "tooltip")
//					.setAttribute("title","Lens: DEFAULT)
//					.setAttribute("rel","Resource: "+uri)
//						// RDF logo indicating DEFAULT lens
//						.addContent(new Element("img")
//							.setAttribute("src","public/images/rdf_icon.gif")
//							.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
//							.setAttribute("alt","Icon.")
//						)						
//				);		
		return title;
	}
	
	private static Element handleProperty(Element propertyElement){
		String uriAttribute=propertyElement.getAttributeValue("uri");		 
		String classAttribute=propertyElement.getAttributeValue("class");
		String label=propertyElement.getChild("label", fresnelNS).getChildTextTrim("title", fresnelNS);
		URI pURI=new URIImpl(uriAttribute);
		
		Element tableRow=new Element("tr")
			.setAttribute("style","padding: 0px; margin: 0px; border: 0px;");
		
		Element property=new Element("div")
			.setAttribute("class", "property-div")
			.setAttribute("style", "overflow: hidden; padding: 5px; border-top: 1px dotted Gray;")
				.addContent(new Element("span")
					.setAttribute("style","vertical-align: top; margin: 0px; padding: 0px;")
						.addContent(new Element("table")
							.setAttribute("style","border: 0px; width: 100%; padding: 0px; margin: 0px; border-spacing: 0px;")
							.addContent(tableRow)));
		
		if (classAttribute!=null) {
			property.setAttribute("style", property.getAttributeValue("style")+";"+classAttribute);
		}
		
		// add label
		// div holding the label text
		Element labelDiv=new Element("div")
										.setAttribute("style","cursor: help;")
										.setAttribute("class","tooltip")
										.setAttribute("title","<b>Property: </b>"+pURI.stringValue())
										.setAttribute("rel","");

		if (label!=null && !label.trim().equals("")) {
			labelDiv.setText(label);
		} else {
			labelDiv.setText(pURI.getLocalName());
		}

		// td holding the labelDiv
		Element labelElement=new Element("td")
									.setAttribute("style","width: 35%; vertical-align: top; padding: 0px; margin: 0px; border: 0px;")
									.addContent(labelDiv);

		// add td holding label to row
		tableRow.addContent(labelElement);							
									
		// add values
		Element valuesElement=new Element("td")
			.setAttribute("style","width: *; vertical-align: top; padding: 0px; margin: 0px; border: 0px;");
		
		
		// get Content before/after declarations for value
		String beforeText="",afterText="";
		
		
		try {
			//Element before=(Element) XPath.selectSingleNode(propertyElement, "//content/before");
			Element before=propertyElement 
				.getChild("values", fresnelNS)
					.getChild("content", fresnelNS)
						.getChild("before", fresnelNS);
			if (before != null) {
				beforeText=before.getTextTrim();
			}
		} catch (NullPointerException e1) {//(JDOMException e1) {			
			//e1.printStackTrace();
		}
		try {
			//Element after=(Element) XPath.selectSingleNode(propertyElement, "//content/after");
			Element after=propertyElement 
			.getChild("values", fresnelNS)
				.getChild("content", fresnelNS)
					.getChild("after", fresnelNS);		
			if (after !=null) {
				afterText=after.getTextTrim();
			}
		} catch (NullPointerException e1) {//(JDOMException e1) {			
			//e1.printStackTrace();
		}
		
		
		Element values=new Element("div");
		Element valuesTable = new Element("table").setAttribute("style","border: 0px; width: 100%; padding: 0px; margin: 0px; border-spacing: 0px;");;		
		//Element valuesCol = new Element("td");			
		
		List<Element> valuesData=propertyElement.getChild("values",fresnelNS).getChildren("value",fresnelNS);	
		for (Element value:valuesData) {
			if(value.getChildTextTrim("title", fresnelNS)!=null){				
				Element valuesRow = new Element("tr");
				Element valuesCol = new Element("td");
				valuesCol.addContent(beforeText);
				String text=value.getChildTextTrim("title", fresnelNS);
				String outputType=value.getAttributeValue("output-type");
				if (outputType!=null) {
					if (outputType.equals("http://www.w3.org/2004/09/fresnel#externalLink")) {
						valuesCol.addContent(new Element("a")
							.setAttribute("href", text)
							.setText(text));
					} else if (outputType.equals("http://www.w3.org/2004/09/fresnel#image")) {
						valuesCol.addContent(new Element("img")
							.setAttribute("src", text));					
					} else {					
						valuesCol.addContent(text+"(unknown output-type: "+outputType+")");
					}
				} else {
					try {
						URI textURI=new URIImpl(text);
						String uriEncoded=URLEncoder.encode(textURI.stringValue(), "UTF-8");
						
						// If resource is an image -> render images (for default lens only)!
						if (text.endsWith(".jpg") || text.endsWith(".JPG") || text.endsWith(".png") || text.endsWith(".PNG") || text.endsWith(".gif") || text.endsWith(".GIF") || text.endsWith(".svg") || text.endsWith(".SVG")) {
						valuesCol.addContent(new Element("a")
							.setAttribute("href", text)
							.addContent(new Element("img")
								.setAttribute("src", text)
								.setAttribute("style", "width: 100px; border: 0px;")
								.setAttribute("class", "tooltip")
								.setAttribute("title", "Image: Click image to see the original size!")))
								.setAttribute("rel","Resource: "+textURI);
						} else {
						valuesCol.addContent(new Element("a")
							.setAttribute("href", text)
							.setText(text));
						valuesCol.addContent(new Element("a")
								.setAttribute("onclick", "setHrefs()")
								.setAttribute("href", "?resource="+uriEncoded+"&location="+locationString+"&meta="+metaString+"&bookmark="+bookmarkString+"&rank=true")
								.setAttribute("class", "tooltip")
								.setAttribute("title","Lens: DEFAULT")
								.setAttribute("rel","Resource: "+textURI)
								// RDF logo indicating DEFAULT lens
								.addContent(new Element("img")
									.setAttribute("src","public/images/rdf_icon.gif")
									.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
									.setAttribute("alt","Icon.")
							));
						}
						
						if (value.getAttributeValue("bookmark")!=null){
							if(value.getAttributeValue("bookmark").equals("true")){
								valuesCol.addContent(new Element("a")
								.setAttribute("onclick", "alert('You have removed the bookmark for the resource"+textURI+"')")//"setHrefs()")
								.setAttribute("href", "?resource="+uriEncoded+"&location="+locationString+"&meta="+metaString + "&bookmark=false")			
								.setAttribute("title","Remove Bookmark :: Resource: "+ textURI)
									.addContent(new Element("img")
										.setAttribute("src","public/images/delete.png")
										.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
										.setAttribute("alt","Icon.")
									));
							
							}else if(value.getAttributeValue("bookmark").equals("false")){
								valuesCol.addContent(new Element("a")
								.setAttribute("onclick", "alert('You have added a bookmark for the resource"+textURI+"')")//"setHrefs()")
								.setAttribute("href", "?resource="+uriEncoded+"&location="+locationString+"&meta="+metaString + "&bookmark=true")			
								.setAttribute("title","Add Bookmark :: Resource: "+ textURI)
									.addContent(new Element("img")
										.setAttribute("src","public/images/add.png")
										.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
										.setAttribute("alt","Icon.")
									));
							}	
						}
						
					} catch (Exception e) {
						valuesCol.addContent(new Element("i")
							.setAttribute("style", "color: #260")
							.setText(text)
						);
					}
				}			
				valuesCol.addContent(afterText);
				valuesRow.addContent(valuesCol);
				valuesTable.addContent(valuesRow);
			}else if (value.getChildTextTrim("resource",fresnelNS)!=null){
				Element valuesRow = new Element("tr");
				Element valuesCol = new Element("td");
				valuesCol.addContent(handleResourceElement(value.getChild("resource",fresnelNS)));
				valuesRow.addContent(valuesCol);
				valuesTable.addContent(valuesRow);
			}
		}
		//valuesTable.addContent(valuesCol);
		values.addContent(valuesTable);
		valuesElement.addContent(values);
		
		// add td holding values to row
		tableRow.addContent(valuesElement);
							
		Element metaknowledgeElement=new Element("div")
		.setAttribute("id", "metaknowledge")
		.setAttribute("style", "display:none")
		.addContent(new Element("div")
						.setAttribute("id", "mtitle")						
						.setText("Meta Knowledge")
						);
	
		List<Element> metaproperties=propertyElement.getChild("values",fresnelNS).getChild("value",fresnelNS).getChildren("metaproperty", fresnelNS);
		for (Element metapropertyElement:metaproperties) {
			metaknowledgeElement.addContent(handleMetaproperty(metapropertyElement));
		}
		if(metaknowledgeElement.getChildren().size() > 1){
			property.addContent(metaknowledgeElement);						
		}
		
		return property;
	
	}
	
	private static Element handleMetaproperty(Element propertyElement){
		String uriAttribute=propertyElement.getAttributeValue("uri");
		String classAttribute=propertyElement.getAttributeValue("class");
		String label=propertyElement.getChild("label", fresnelNS).getChildTextTrim("title", fresnelNS);
		URI pURI=new URIImpl(uriAttribute);		
						
		Element tableRow=new Element("tr")
			.setAttribute("style","padding: 0px; margin: 0px; border: 0px;");
		
		Element property=new Element("div")
			.setAttribute("class", "property-div")
			.setAttribute("style", "overflow: hidden;")
				.addContent(new Element("span")
					.setAttribute("style","vertical-align: top; margin: 0px; padding: 0px;")
						.addContent(new Element("table")
							.setAttribute("style","border: 0px; width: 100%; padding: 0px; margin: 0px; border-spacing: 0px;")
							.addContent(tableRow)));
		
		if (classAttribute!=null) {
			property.setAttribute("style", property.getAttributeValue("style")+";"+classAttribute);
		}
		
	// add label
		
		// div holding the label text
		Element labelDiv=new Element("div")
										.setAttribute("style","cursor: help;")
										.setAttribute("class","tooltip")
										.setAttribute("title","Property: "+pURI.stringValue())
										.setAttribute("rel","");

		if (label!=null && !label.trim().equals("")) {
			labelDiv.setText(label);
		} else {
			labelDiv.setText(pURI.getLocalName());
		}

		// td holding the labelDiv
		Element labelElement=new Element("td")
									.setAttribute("style","width: 35%; vertical-align: top; padding: 0px; margin: 0px; border: 0px;")
									.addContent(labelDiv);

		// add td holding label to row
		tableRow.addContent(labelElement);							
								
		// add values
		Element valuesElement=new Element("td")
			.setAttribute("style","width: *; vertical-align: top; padding: 0px; margin: 0px; border: 0px;");
		
		
		// get Content before/after declarations for value
		String beforeText="",afterText="";
		
		
		try {
			//Element before=(Element) XPath.selectSingleNode(propertyElement, "//content/before");
			Element before=propertyElement 
				.getChild("values", fresnelNS)
					.getChild("content", fresnelNS)
						.getChild("before", fresnelNS);
			if (before != null) {
				beforeText=before.getTextTrim();
			}
		} catch (NullPointerException e1) {//(JDOMException e1) {			
			//e1.printStackTrace();
		}
		try {
			//Element after=(Element) XPath.selectSingleNode(propertyElement, "//content/after");
			Element after=propertyElement 
			.getChild("values", fresnelNS)
				.getChild("content", fresnelNS)
					.getChild("after", fresnelNS);		
			if (after !=null) {
				afterText=after.getTextTrim();
			}
		} catch (NullPointerException e1) {//(JDOMException e1) {			
			//e1.printStackTrace();
		}
		
		
		Element values=new Element("div");		
		List<Element> valuesData=propertyElement.getChild("values",fresnelNS).getChildren("value",fresnelNS);	
		for (Element value:valuesData) {
			values.addContent(beforeText);
			String text=value.getChildTextTrim("title", fresnelNS);
			String outputType=value.getAttributeValue("output-type");
			if (outputType!=null) {
				if (outputType.equals("http://www.w3.org/2004/09/fresnel#externalLink")) {
					values.addContent(new Element("a")
						.setAttribute("href", text)
						.setText(text));
				} else if (outputType.equals("http://www.w3.org/2004/09/fresnel#image")) {
					values.addContent(new Element("img")
						.setAttribute("src", text));					
				} else {
					//NEW: take out unknown output-type
					values.addContent(text);//+"(unknown output-type: "+outputType+")");
				}
			} else {
				try {
					URI textURI=new URIImpl(text);
					values.addContent(new Element("a")
						.setAttribute("href", text)
						.setText(text));
					String uriEncoded=URLEncoder.encode(textURI.stringValue(), "UTF-8");
					values.addContent(new Element("a")
							.setAttribute("onclick", "setHrefs()")
							.setAttribute("href", "?resource="+uriEncoded)
							.setAttribute("class", "tooltip")
							.setAttribute("title","Lens: DEFAULT")
							.setAttribute("rel","Resource: "+textURI)
							// RDF logo indicating DEFAULT lens
							.addContent(new Element("img")
								.setAttribute("src","public/images/rdf_icon.gif")
								.setAttribute("style","width: 16px; border: 0px; margin: 0px 2px;")
								.setAttribute("alt","Icon.")
						));					
					
				} catch (Exception e) {
					values.addContent(text);
				}
				
			}
			values.addContent(afterText);
		}
		valuesElement.addContent(values);
		
		// add td holding values to row
		tableRow.addContent(valuesElement);	
		
		
							
		return property;
	}
}
