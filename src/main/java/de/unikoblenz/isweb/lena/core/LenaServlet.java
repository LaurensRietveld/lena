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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * Simple servlet for the results of the RDF browser.
 * 
 * @author Jörg Koch
 */

public final class LenaServlet extends HttpServlet {
	
	
	private LenaController jfe;

	private LenaController getLenaController(){
		if(jfe==null)
			jfe = new LenaController(new LenaConfig(getServletContext()));
		return jfe;
	}
	
	
	/** - read lena config files
	 *  - initiate JFresnel engine accordingly with the correct data, lens definition file and so on	
	 */
	/*
	public void init() throws ServletException {
		super.init();		
	}
	*/

	/**
	 * Respond to a GET request for the content produced by this servlet.
	 * Constructs a stackwriter for exceptions and creates the browser object,
	 * which is the initialiszed.
	 * 
	 * @param request
	 *          The servlet request we are processing
	 * @param response
	 *          The servlet response we are producing
	 * @throws IOException
	 * @exception IOException
	 *              if an input/output error occurs
	 * @exception ServletException
	 *              if a servlet error occurs
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		System.out.println("----------------------------------------------------------");
		System.out.println("----------------------------------------------------------");
		System.out.println("Query String: " + request.getQueryString());
		System.out.println("Context Path: " + request.getContextPath());
		System.out.println("Servlet Path: " + request.getServletPath());
		System.out.println("Content Type: " + request.getContentType());
		System.out.println("----------------------------------------------------------");
		 
		String paramURI = "";
		String paramClass = "";
		String paramURL = "";
		String paramLens = "";
		String paramPage = "";
		String paramLocation = "";
		String paramReload = "";
		String paramMeta = "";
		boolean sidebar = true;
		boolean sidebarChanged;		
		String menuLenses = "";
		String menuRemote = "";
		String menuLocal = "";
		

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");

		HttpSession session = request.getSession(true);
		System.out.println("Session ID: " + session.getId());
		System.out.println("Session creation time: " + session.getCreationTime());
		System.out.println("----------------------------------------------------------");
		PrintWriter writer = response.getWriter();
		
		try {
		
			boolean sidebarStatus = sidebar;
			
			// Get resource parameter, if null set an empty string.
			if (request.getParameter("resource") != null) {
				paramURI = request.getParameter("resource");
			} else {
				paramURI = "";
			}
			
			// Get class parameter, if null set an empty string.
			if (request.getParameter("class") != null) {
				paramClass = request.getParameter("class");
			} else {
				paramClass = "";
			}
	
			// Get fresnel lens parameter, if null set an empty string.
			if (request.getParameter("lens") != null) {
				paramLens = request.getParameter("lens");
			} else {
				paramLens = "";
			}
	
			// Get page parameter, if null set to one.
			if (request.getParameter("page") != null) {
				paramPage = request.getParameter("page");
			} else {
				paramPage = "1";
			}
	
			// Get page parameter, if null set to one.
			if (request.getParameter("location") != null) {
				paramLocation = request.getParameter("location");
			} else {
				paramLocation = "local";
			}
			
			if (request.getParameter("meta") != null) {
				paramMeta = request.getParameter("meta");
			} else {
				paramMeta = "false";
			}
	
			// Get url parameter, if null set an empty string.
			if (request.getParameter("uri") != null) {
				paramURL = request.getParameter("uri");
				paramURI = paramURL;
				paramLocation = "remote";
				try {
					//rdfBrowser.addURLDataToRepository(paramURL);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//rdfBrowser.getErrorMessage().append(e.toString() + "<br/>");
					e.printStackTrace();
				}
				menuRemote = getLenaController().getClasses("remote",paramMeta);
				session.setAttribute("menuRemote", menuRemote);
			} else {
				paramURL = "";
			}			
	
			// Get sidebar status parameter
			if (request.getParameter("sidebar") != null) {
				String sidebarParam = request.getParameter("sidebar");
				if (sidebarParam.equals("false")) {
					sidebar = false;
				} else {
					sidebar = true;
				}
			}
	
			
			if (session.isNew()) {
				/*rdfBrowser = new RDFBrowser();
				rdfBrowser.initRepositories(getServletContext());
				rdfBrowser.initConfiguration();*/
				//jfe=new LenaController(new LenaConfig(getServletContext()));
				menuLenses = getLenaController().getLenses(paramMeta);
				menuRemote = getLenaController().getClasses("remote",paramMeta);
				menuLocal = getLenaController().getClasses("local",paramMeta);
				session.setAttribute("rdfBrowser", getLenaController());
				session.setAttribute("menuLenses", menuLenses);
				session.setAttribute("menuRemote", menuRemote);
				session.setAttribute("menuLocal", menuLocal);				
				
			} else {
				
				//rdfBrowser = ((RDFBrowser) session.getAttribute("rdfBrowser"));
				//jfe = ((LenaController) session.getAttribute("rdfBrowser"));
				menuLenses = ((String) session.getAttribute("menuLenses"));
				menuRemote = ((String) session.getAttribute("menuRemote"));
				menuLocal = ((String) session.getAttribute("menuLocal"));
				
				/*
				if (rdfBrowser == null) {
					rdfBrowser = new RDFBrowser();
					session.setAttribute("rdfBrowser", rdfBrowser);
					rdfBrowser.initRepositories(getServletContext());
					rdfBrowser.initConfiguration();
				}*/
				/*if (jfe == null) {
					jfe=new LenaController(new LenaConfig(getServletContext()));
				}*/
				if (menuLenses == "") {
					menuLenses = getLenaController().getLenses(paramMeta);
					session.setAttribute("menuLenses", menuLenses);
				}
				
				if (menuRemote == "") {
					menuRemote = getLenaController().getClasses("remote",paramMeta);
					session.setAttribute("menuRemote", menuRemote);
				}
				if (menuLocal == "") {
					menuLocal = getLenaController().getClasses("local",paramMeta);
					session.setAttribute("menuLocal", menuLocal);
				}
				
			}
	
			//rdfBrowser.initConfiguration();
			//rdfBrowser.getErrorMessage().delete(0, rdfBrowser.getErrorMessage().length());
	
			
			
			// Reload repositories.
			
			if (request.getParameter("reload") != null) {
				paramReload = request.getParameter("reload");
			} else {
				paramReload = "false";
			}
	
			if (paramReload.equals("true")) {
				System.out.println("RELOAD!!!");
				//jfe.shutDown();
				//jfe.initRepositories(getServletContext());
				//jfe.initConfiguration();
				//jfe=new LenaController(new LenaConfig(getServletContext()));
				menuLenses = getLenaController().getLenses(paramMeta);
				menuRemote = getLenaController().getClasses("remote",paramMeta);
				menuLocal = getLenaController().getClasses("local",paramMeta);
				session.setAttribute("menuLenses", menuLenses);
				session.setAttribute("menuRemote", menuRemote);
				session.setAttribute("menuLocal", menuLocal);
			}
			
	
			
			// Check if sidebar status changed.
			if (sidebarStatus == sidebar) {
				sidebarChanged = false;
			} else {
				sidebarChanged = true;
			}
	
			System.out.println("Sidebar visible: " + sidebar);
			System.out.println("URI: " + paramURL);
			System.out.println("Resource: " + paramURI);
			System.out.println("Class: " + paramClass);
			System.out.println("Lens: " + paramLens);
			System.out.println("Page: " + paramPage);
			System.out.println("Location: " + paramLocation);
			System.out.println("----------------------------------------------------------");
	
			try {
				// Include XHTML head.
				getServletContext().getRequestDispatcher("/public/head.html").include(request, response);
	
				writer.println("<body onload='checkboxChanged()'>");
	
				writer.println("<script type='text/javascript'>");
				writer.println("setSidebar(" + sidebar + ")");
				writer.println("sidebarChanged(" + sidebarChanged + ")");
				//writer.println("setChecked("+paramMeta+")");
				writer.println("</script>");
	
				if (sidebar == true) {
					writer.println("<a href='#' onClick='toggleSidebar()' onFocus='if (this.blur) this.blur();'><img id='icon' src='public/images/control_rewind.png' alt='icon' title='Hide sidebar' style='border: 0px; padding: 2px 2px 0px 2px; position: absolute; left: 0px; top: 0px; z-index: 1;'/></a>");
				} else {
					writer.println("<a href='#' onClick='toggleSidebar()' onFocus='if (this.blur) this.blur();'><img id='icon' src='public/images/control_fastforward.png' alt='icon' title='Show sidebar' style='border: 0px; padding: 2px 2px 0px 2px; position: absolute; left: 0px; top: 0px; z-index: 1;'/></a>");
				}
	
				writer.println("<div id='sidebar'>");
	
				writer.println("<div id='lensesAndClasses'>");
				writer.println("<a class='tooltip' title='::Home' href='.'><h2 style='font-variant: small-caps;'>LENA <span style='font-size: 8pt'>2.0.1a</span></h2></a><hr>");
	
				writer.println("<menu>");
				writer.println("<li><a id='home' onClick='setHrefs()' class='tooltip' title='::Home' href='.'>HOME</a></li>");
				writer.println("<li id='reloadLI'><a id='reload' onClick='setHrefs();' class='tooltip' title='::The repositories are shut down and re-initialized, added remote data will get lost...' href='?reload=true"); // javascript:setHrefs();reload();'>");
	
				if (!paramLocation.equalsIgnoreCase("remote")) {
					System.out.println("Location CHECK: " + paramLocation);
					if (paramURI != "") {
						writer.println("&resource=" + URLEncoder.encode(paramURI, "UTF-8"));
					}
					if (paramLens != "") {
						writer.println("&lens=" + URLEncoder.encode(paramLens, "UTF-8"));
					}
				}
	
				writer.println("'>Reload</a></li>");
				writer.println("</menu>");
				writer.println("</div>");
	
				writer.println(menuLenses);
				writer.println(menuRemote);
				writer.println(menuLocal);
	
				getServletContext().getRequestDispatcher("/public/footer.html").include(request, response);
	
				writer.println("</div>");
	
				if (sidebar == true) {
					writer.println("<div id='selection' style='position: absolute; top: 10px; right: 10px; left: 220px; z-index: 0;'>");
				} else {
					writer.println("<div id='selection' style='position: absolute; top: 10px; right: 10px; left: 10px; z-index: 0;'>");
				}
	
				if (paramReload == "true") {
					writer.println("<script type='text/javascript'>");
					writer.println("var bcChange = new Fx.Style('reload', 'background-color',{duration:1000})");
					writer.println("bcChange.start('#fff', '#0c0').chain(function(){bcChange.start('#eee')})");
					writer.println("$('reload').setText('Reload successful!')");
					writer.println("$('reload').setStyle('color', '#0c0')");
					// writer.println("$('reloadLI').setStyle('color', '#0c0')");
					writer.println("var bcChange2 = new Fx.Style('reload', 'background-color',{duration:1000})");
					writer.println("bcChange2.start('#fff', '#0c0').chain(function(){bcChange2.start('#eee')})");
					writer.println("$('reload').setText('Reload')");
					writer.println("$('reload').setStyle('color', '#000')");
					// writer.println("$('reloadLI').setStyle('color', '#eee')");
					writer.println("</script>");
					paramReload = "false";
				}
				/*
				if (rdfBrowser.getErrorMessage().length() != 0) {
					writer.println("<div id='errors'>");
					writer.println("<b>Exception: </b><br/>");
					writer.println(rdfBrowser.getErrorMessage().toString());
					writer.println("</div>");
				}
				*/ 
				if (paramURI != "" || paramLens != "" || paramClass != "") {
					//String selection = rdfBrowser.selectAsXHTML(paramURI, paramLens, paramPage, paramLocation);
					String selection = getLenaController().selectAsXHTML(paramURI, paramClass, paramLens, paramPage, paramLocation, paramMeta);
					
					writer.println("<div id='pages'>");
					writer.println("<table>");
					writer.println("<tr>");
					writer.println("<td style=width:85%; vertical-align: top>");
					writer.println(getLenaController().getPages());
					writer.println("</td>");					
					if (paramMeta.equalsIgnoreCase("true"))
						writer.println("<td class='smallfont' nowrap='nowrap'><input type='checkbox' name='meta' id='show_metaknowledge' onClick='checkboxChanged()' checked/><acronym style='border-bottom: 1px dotted #000000; cursor: help;' title='Show meta knowledge.'>Show meta knowledge values?</acronym></td>");					
					else if (paramMeta.equalsIgnoreCase("false"))
						writer.println("<td class='smallfont' nowrap='nowrap'><input type='checkbox' name='meta' id='show_metaknowledge' onClick='checkboxChanged()'/><acronym style='border-bottom: 1px dotted #000000; cursor: help;' title='Show meta knowledge.'>Show meta knowledge values?</acronym></td>");					
					writer.println("</td>");
					writer.println("</tr>");
					writer.println("</table>");
					writer.println("</div>");
					writer.println(selection);
					writer.println("<div id='pages'>");
					writer.println(getLenaController().getPages());
					writer.println("</div>");
	
				} else if (paramURL != "") {
					writer.println("<div id='hometext'>");
					writer.println("<h2 class='infoHeading'>Data loaded...</h2>");
					writer.println("<p>...start browsing with help of the 'Remote Classes' sidebar!</p>");
					writer.println("</div>");
				} else {
					getServletContext().getRequestDispatcher("/public/home.html").include(request, response);
				}
	
				writer.println("<div style='height: 10px;'></div>");
				writer.println("</div>");
				writer.println("<script type='text/javascript'>var gaJsHost = (('https:' == document.location.protocol) ? 'https://ssl.' : 'http://www.');document.write(unescape('%3Cscript src=\"' + gaJsHost + 'google-analytics.com/ga.js\" type=\"text/javascript\"%3E%3C/script%3E'));</script><script type='text/javascript'>var pageTracker = _gat._getTracker('UA-5623686-3');pageTracker._trackPageview();</script>");
				writer.println("</body>");
				writer.println("</html>");
		
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				//rdfBrowser.getErrorMessage().append(e.toString() + "<br/>");
				e.printStackTrace();
			}
		} finally {
			writer.flush();
			writer.close();
		}		
	}
}