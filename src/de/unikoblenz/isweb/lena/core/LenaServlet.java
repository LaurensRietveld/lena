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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Formatter;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.uni_hildesheim.ismll.integration.ConfiguringException;
import de.uni_hildesheim.ismll.integration.IntegrationSupport;

/**
 * Simple servlet for presenting the results of LENA.
 * 
 * @author Jörg Koch
 */

public final class LenaServlet extends HttpServlet {

	/**
	 * ID
	 */
	private static final long serialVersionUID = -8556704089196623197L;

	private Logger lenaLogger = null;

	private Logger getLenaLogger() {
		if (lenaLogger == null)
			try {
				// Create an appending file handler
				FileHandler handler = new FileHandler(getServletContext().getRealPath("../../logs/lena.log"));
				handler.setFormatter(new SimpleFormatter());

				// Add to the desired logger
				lenaLogger = Logger.getLogger("de.unikoblenz.isweb.lena");
				lenaLogger.addHandler(handler);
			} catch (IOException e) {
				e.printStackTrace();
			}
		return lenaLogger;
	}

	private LenaController jfe = null;

	private LenaController getLenaController() {
		return jfe;
	}

	String paramURI = "";
	String paramClass = "";
	String paramURL = "";
	String paramLens = "";
	String paramPage = "";
	String paramLocation = "";
	String paramReload = "";
	String paramMeta = "";
	String paramBookmark = "";

	String paramRank = "";
	String paramStatementLimit = "";
	String paramDepthLimit = "";
	String paramLinkLimit = "";
	String paramRetryLimit = "";
	String paramPause = "";
	String paramFactor = "";

	String paramAjaxClasses = "";
	String paramAjaxContent = "";
	String paramAjaxRank = "";
	String paramAjaxTime = "";
	String paramAjaxTime2 = "";
	String paramAjaxTime3 = "";

	/**
	 * Respond to a GET request for the content produced by this servlet.
	 * Constructs a stackwriter for exceptions and creates the browser object,
	 * which is the initialiszed.
	 * 
	 * @param request
	 *            The servlet request we are processing
	 * @param response
	 *            The servlet response we are producing
	 * @throws IOException
	 * @exception IOException
	 *                if an input/output error occurs
	 * @exception ServletException
	 *                if a servlet error occurs
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			handleRequest(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			handleRequest(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void handleRequest(HttpServletRequest request, HttpServletResponse response) 
		throws Exception {
		String menuLenses = "";
		String menuLocal = "";
		String menuRemote = "";
		String menuApplication = "";
		String userName = "";
		String userToken = "";
		String userURI = "";

		boolean sidebar = true;
		boolean sidebarChanged;

		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/html; charset=utf-8");

		HttpSession session = request.getSession(true);
		PrintWriter writer = response.getWriter();
		
		getLenaLogger().log(Level.INFO, "LENA TR: Query String: " + request.getQueryString());
		getLenaLogger().log(Level.INFO, "LENA TR: Session ID: " + session.getId());

		try {

			boolean sidebarStatus = sidebar;

			getParameters(request);

			// Create NEW or INIT session:
			if (session.isNew() || jfe == null) {
				if (jfe == null)
					jfe = new LenaController(new LenaConfig(getServletContext()), getLenaLogger(), getServletContext().getRealPath(""));

				System.out.println("SESSION: NEW ##############################################");
				System.out.println("SESSION: LenaController Hash: " + jfe.hashCode());

				// Disabled with TripleRank
				 menuLenses = getLenaController().getLenses(paramMeta);
				 menuLocal = getLenaController().getClasses("local",paramMeta);
				 menuRemote = getLenaController().getClasses("remote",paramMeta);

				if ((getLenaController().getXMUser() != null) && (paramURI != null))
					menuApplication = getLenaController().getIntegrationMenu(new java.net.URI(paramURI), paramMeta);

				session.setAttribute("lenaController", jfe);
				session.setAttribute("menuApplication", menuApplication);				
				
				// Disabled with TripleRank
				session.setAttribute("menuLenses", menuLenses);
				session.setAttribute("menuLocal", menuLocal);
				session.setAttribute("menuRemote", menuRemote);

			} else {
				jfe = ((LenaController) session.getAttribute("lenaController"));
				System.out.println("SESSION: OLD ##############################################");
				System.out.println("SESSION: LenaController Hash: " + jfe.hashCode());

				if (paramRank.equals("true")) {
					getLenaController().resetStoptimes();
					getLenaController().resetStoptimeBools();
				}

				// Disabled for TripleRank
				menuLenses = ((String) session.getAttribute("menuLenses"));
				menuLocal = ((String) session.getAttribute("menuLocal"));
				
				if ((getLenaController().getXMUser() != null) && (paramURI != null))
					menuApplication = getLenaController().getIntegrationMenu(new java.net.URI(paramURI), paramMeta);

				// Disabled for TripleRank
				if (menuLenses == "") {
				 menuLenses = getLenaController().getLenses(paramMeta);
				 session.setAttribute("menuLenses", menuLenses);
				}
				if (menuLocal == "") {
					menuLocal = getLenaController().getClasses("local",paramMeta);
					session.setAttribute("menuLocal", menuLocal);
				}
				if (menuRemote == "") {
					menuRemote =getLenaController().getClasses("remote",paramMeta);
					session.setAttribute("menuRemote", menuRemote);
				}

				if (menuApplication == "") {
					if ((getLenaController().getXMUser() != null) && (paramURI != null))
						menuApplication = getLenaController().getIntegrationMenu(new java.net.URI(paramURI), paramMeta);
					session.setAttribute("menuApplication", menuApplication);
				}
			}
				
			/*
			// Process AJAX calls:			
			if (paramAjaxTime.equals("true")) {
				System.out.println("AJAX I: AJAX Time call initiated!!!");
				String time = "<table cellspacing='0' cellpadding='0'><tr><td style='width:120px;'>Crawl:</td><td><i>"
						+ new Formatter().format("%3.3f", getLenaController().getStoptimeCrawl()).toString() + " s</i></td></tr></table>";
				writer.println(time);
				writer.flush();
				writer.close();
				return;
			} else if (paramAjaxTime2.equals("true")) {
				System.out.println("AJAX II: AJAX Time2 call initiated!!!");
				String time = "<table cellspacing='0' cellpadding='0'><tr><td style='width:120px;'>Matrices Creation:</td><td><i>"
						+ new Formatter().format("%3.3f", getLenaController().getStoptimeRDF2Matlab()).toString()
						+ " s</i></td></tr></table>";
				writer.println(time);
				writer.flush();
				writer.close();
				return;
			} else if (paramAjaxTime3.equals("true")) {
				System.out.println("AJAX III: AJAX Time3 call initiated!!!");
				String time = "<table cellspacing='0' cellpadding='0'><tr><td style='width:120px;'>Ranking:</td><td><i>"
						+ new Formatter().format("%3.3f", getLenaController().getStoptimeGetURIsFromMatlab()).toString()
						+ " s</i></td></tr></table>";
				// time +=
				// "<table cellspacing='0' cellpadding='0'><tr><td style='width:120px;'><b>Total:</b></td><td><b>"+new
				// Formatter().format("%3.3f",getLenaController().getStoptimeCrawl()+getLenaController().getStoptimeRDF2Matlab()+getLenaController().getStoptimeGetURIsFromMatlab()).toString()+" s</b></td></tr></table>";
				writer.println(time);
				writer.flush();
				writer.close();
				return;
			} else 
				*/
				
			if (paramAjaxClasses.equals("true")) {
				System.out.println("AJAX IV: AJAX Classes call initiated!!!");
				try {
					System.out.println("AJAX IV: Add remote data!");
					System.out.println("AJAX IV: URI: " + paramURI);
					getLenaController().lenaConfig.addURLDataToRepository(paramURI);
				} catch (Exception e) {
					e.printStackTrace();
				}
				menuRemote = getLenaController().getClasses("remote", paramMeta);
				session.setAttribute("menuRemote", menuRemote);
				writer.println(menuRemote);
				writer.flush();
				writer.close();
			} else if (paramAjaxContent.equals("true")) {
				System.out.println("AJAX V: AJAX Content call initiated!!!");
				String selection = getLenaController().selectAsXHTML(paramURI, paramClass, paramLens, paramPage, paramLocation, paramMeta,
						paramBookmark);
				writer.println("<div id='pages'>");
				writer.println("<table>");
				writer.println("<tr>");
				writer.println("<td style=width:85%; vertical-align: top>");
				writer.println(getLenaController().getPages());
				writer.println("</td>");
				// System.out.println("Meta= "+paramMeta);
				if (paramMeta.equalsIgnoreCase("true"))
					writer
							.println("<td class='smallfont' nowrap='nowrap'><input type='checkbox' name='meta' id='show_metaknowledge' onClick='checkboxChanged()' checked/><acronym style='cursor: help;' title='Show meta knowledge.'>Show meta knowledge values?</acronym></td>");
				else if (paramMeta.equalsIgnoreCase("false"))
					writer
							.println("<td class='smallfont' nowrap='nowrap'><input type='checkbox' name='meta' id='show_metaknowledge' onClick='checkboxChanged()'/><acronym style='cursor: help;' title='Show meta knowledge.'>Show meta knowledge values?</acronym></td>");

				writer.println("</td>");
				writer.println("</tr>");
				writer.println("</table>");
				writer.println("</div>");
				writer.println(selection);
				writer.println("<div id='pages'>");
				writer.println(getLenaController().getPages());
				writer.println("</div>");
				writer.flush();
				writer.close();
			} else {
				System.out.println("----------------------------------------------------------");
				System.out.println("----------------------------------------------------------");
				System.out.println("Query String: " + request.getQueryString());
				System.out.println("Context Path: " + request.getContextPath());
				System.out.println("Servlet Path: " + request.getServletPath());
				System.out.println("Content Type: " + request.getContentType());
				System.out.println("----------------------------------------------------------");
				System.out.println("Session ID: " + session.getId());
				System.out.println("Session creation time: " + session.getCreationTime());
				System.out.println("----------------------------------------------------------");

				// Get sidebar status parameter
				if (request.getParameter("sidebar") != null) {
					String sidebarParam = request.getParameter("sidebar");
					if (sidebarParam.equals("false")) {
						sidebar = false;
					} else {
						sidebar = true;
					}
				}

				if (request.getParameter(IntegrationSupport.JSP_PARAMETER_USER_NAME) != null) {
					userName = URLDecoder.decode(request.getParameter(IntegrationSupport.JSP_PARAMETER_USER_NAME), "UTF-8");
				}
				if (request.getParameter(IntegrationSupport.JSP_PARAMETER_USER_TOKEN) != null) {
					userToken = URLDecoder.decode(request.getParameter(IntegrationSupport.JSP_PARAMETER_USER_TOKEN), "UTF-8");
				}
				if (request.getParameter(IntegrationSupport.JSP_PARAMETER_USER_URI) != null) {
					userURI = URLDecoder.decode(request.getParameter(IntegrationSupport.JSP_PARAMETER_USER_URI), "UTF-8");
				}
				if ((!userName.equals("")) && (!userToken.equals("")) && (!userURI.equals("")))
					getLenaController().setXMUser(userName, userToken, userURI);

				System.out.println("UserName: " + userName);
				System.out.println("UserURI: " + userURI);
				System.out.println("UserToken: " + userToken);

				// Reload repositories.
				if (request.getParameter("reload") != null) {
					paramReload = request.getParameter("reload");
				} else {
					paramReload = "false";
				}

				if (paramReload.equals("true")) {
					System.out.println("RELOAD!!!");

					getLenaController().lenaConfig.shutdownRepositories();
					System.out.println("RELOAD!!!");

					// jfe.shutDown();
					// jfe.initRepositories(getServletContext());
					// jfe.initConfiguration();
					// jfe=new LenaController(new
					// LenaConfig(getServletContext()));

					// menuLenses = getLenaController().getLenses(paramMeta);
					// if (paramRank.equals("true")) {
					// menuFacets = getLenaController().getFacets(paramMeta,
					// path, paramURI);
					// } else {
					// menuFacets = "";
					// }
					// menuRemote =
					// getLenaController().getClasses("remote",paramMeta);
					// menuLocal =
					// getLenaController().getClasses("local",paramMeta);
					if ((getLenaController().getXMUser() != null) && (paramURI != null))
						menuApplication = getLenaController().getIntegrationMenu(new java.net.URI(paramURI), paramMeta);

					// session.setAttribute("menuLenses", menuLenses);
					// session.setAttribute("menuRemote", menuRemote);
					// session.setAttribute("menuLocal", menuLocal);
					session.setAttribute("menuApplication", menuApplication);

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
				System.out.println("Rank: " + paramRank);
				System.out.println("Ajax Rank: " + paramAjaxRank);
				System.out.println("----------------------------------------------------------");
							
				writePage(writer,sidebar,sidebarChanged,
							menuApplication,menuLenses,menuLocal,menuRemote,
							request,response);				
			}
		} finally {
			writer.flush();
			writer.close();
		}
	}
	
	private void writePage(PrintWriter writer, boolean sidebar, 
			boolean sidebarChanged, String menuApplication, 
			String menuLenses, String menuLocal,String menuRemote,
			HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		// Include XHTML head.
		getServletContext().getRequestDispatcher("/public/head.html").include(request, response);

		writer.println("<body onload='checkboxChanged()'>");

		writer.println("<script type='text/javascript'>");
		writer.println("setSidebar(" + sidebar + ")");
		writer.println("sidebarChanged(" + sidebarChanged + ")");
		// writer.println("setChecked("+paramMeta+")");
		writer.println("</script>");

		// Sidebar right:
		writer.println("<div id='sidebar_right'>");
		writer.println("<div id='lensesAndClasses'><h3>Facets:</h3><hr>");
		writer.println("<div style='padding: 2px 0px 0px 5px;' id='times'></div>");
		writer.println("<div style='padding: 0px 0px 0px 5px;' id='times2'></div>");
		writer.println("<div style='padding: 0px 0px 0px 5px;' id='times3'></div>");
		writer.println("</div>");
		writer.println("<div id='sidebar_right_content'>");		
		writer.println("</div>");
		writer.println("</div>");

		// if (paramLocation.equals("remote")) {
		// try {
		// System.out.println("LINKEDDATA: Add remote data!");
		// getLenaController().lenaConfig.addURLDataToRepository(paramURI);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// menuRemote =
		// getLenaController().getClasses("remote",paramMeta);
		// session.setAttribute("menuRemote", menuRemote);
		// } else {
		// System.out.println("LINKEDDATA: Local - Remote Param. NOT set!!!");
		// menuRemote = ((String)
		// session.getAttribute("menuRemote"));
		// }
		
		// Sidebar left:
		if (sidebar) {
			writer.println("<a href='#' onClick='toggleSidebar()' onFocus='if (this.blur) this.blur();'><img id='icon' src='public/images/control_rewind.png' alt='icon' title='Hide sidebar' style='border: 0px; padding: 2px 2px 0px 2px; position: absolute; left: 0px; top: 0px; z-index: 1;'/></a>");
		} else {
			writer.println("<a href='#' onClick='toggleSidebar()' onFocus='if (this.blur) this.blur();'><img id='icon' src='public/images/control_fastforward.png' alt='icon' title='Show sidebar' style='border: 0px; padding: 2px 2px 0px 2px; position: absolute; left: 0px; top: 0px; z-index: 1;'/></a>");
		}

		writer.println("<div id='sidebar'>");
		writer.println("<div id='lensesAndClasses'>");
		writer.println("<a class='tooltip' title='Home' rel='' href='.'><h2 style='font-variant: small-caps;'>LENA <span style='font-size: 8pt'>2.0.1b2</span></h2></a><hr>");
		writer.println("<menu>");
		writer.println("<li><a id='home' onClick='setHrefs()' class='tooltip' title='Home' rel='' href='.'>HOME</a></li>");
		writer.println("<li id='reloadLI'><a id='reload' onClick='setHrefs();' class='tooltip' title='The repositories are shut down and re-initialized, added remote data will get lost...' rel='' href='?reload=true"); // javascript:setHrefs();reload();'>");
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
		writer.println("<div id='menuRemote'>");
		writer.println(menuRemote);
		writer.println("</div>");
		writer.println(menuLocal);
		writer.println(menuApplication);
		getServletContext().getRequestDispatcher("/public/footer.html").include(request, response);
		writer.println("</div>");

		if (sidebar == true) {
			writer.println("<div id='selection' style='position: absolute; top: 10px; right: 220px; left: 220px; z-index: 0;'>");
		} else {
			writer.println("<div id='selection' style='position: absolute; top: 10px; right: 220px; left: 10px; z-index: 0;'>");
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

		if (paramURI != "" || paramLens != "" || paramClass != "") {
			try {
				if ((paramURI != "") && (paramBookmark.equals("true"))) {
					java.net.URI buri = new java.net.URI(paramURI);
					URI[] uris = { buri };
					if (!getLenaController().isBookmarkInScratchpad(buri)) {
						getLenaController().insertBookmarks(uris);
					}
				} else if ((paramURI != "") && (paramBookmark.equals("false"))) {
					java.net.URI buri = new java.net.URI(paramURI);
					if (getLenaController().isBookmarkInScratchpad(buri))
						getLenaController().deleteBookmarks(buri);
				}
			} catch (Exception e) {
			}

			writer.println("<div id='selection'>");
			writer.println("</div>");
			
			// Make appropriate AJAX calls.
			System.out.println("LINKEDDATA: Ajax Calling...");
			writer.println("<script type='text/javascript'>");
			
			writer.println("requestAjaxCalls(" +
					"'"+request.getContextPath()+"','" + paramURI + "','"+paramClass+"','" + paramLocation + "','" + paramMeta + "','" + paramBookmark
					+ "','" + paramRank + "','" + paramStatementLimit + "','" + paramDepthLimit + "','" + paramLinkLimit + "','"
					+ paramFactor + "');");
			writer.println("</script>");
			System.out.println("LINKEDDATA: Ajax Calling... DONE");

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

		writer.println("</body>");
		writer.println("</html>");
	}
	

	/**
	 * Get parameters of current HTTP request.
	 * 
	 * @param request The current HTTP request.
	 * @throws UnsupportedEncodingException
	 */
	private void getParameters(HttpServletRequest request) throws UnsupportedEncodingException {
		if (request.getParameter("resource") != null) {
			paramURI = URLDecoder.decode(request.getParameter("resource"), "UTF-8");
		} else {
			paramURI = "";
		}
		if (request.getParameter("class") != null) {
			paramClass = request.getParameter("class");
		} else {
			paramClass = "";
		}
		if (request.getParameter("lens") != null) {
			paramLens = request.getParameter("lens");
		} else {
			paramLens = "";
		}
		if (request.getParameter("page") != null) {
			paramPage = request.getParameter("page");
		} else {
			paramPage = "1";
		}
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
		if (request.getParameter("bookmark") != null) {
			paramBookmark = request.getParameter("bookmark");
		} else {
			paramBookmark = "false";
		}
		if (request.getParameter("rank") != null) {
			paramRank = request.getParameter("rank");
		} else {
			paramRank = "false";
		}
		if (request.getParameter("statementLimit") != null) {
			paramStatementLimit = request.getParameter("statementLimit");
		} else {
			paramStatementLimit = "";
		}
		if (request.getParameter("depthLimit") != null) {
			paramDepthLimit = request.getParameter("depthLimit");
		} else {
			paramDepthLimit = "";
		}
		if (request.getParameter("linkLimit") != null) {
			paramLinkLimit = request.getParameter("linkLimit");
		} else {
			paramLinkLimit = "";
		}
		if (request.getParameter("retryLimit") != null) {
			paramRetryLimit = request.getParameter("retryLimit");
		} else {
			paramRetryLimit = "";
		}
		if (request.getParameter("pause") != null) {
			paramPause = request.getParameter("pause");
		} else {
			paramPause = "";
		}
		if (request.getParameter("factor") != null) {
			paramFactor = request.getParameter("factor");
		} else {
			paramFactor = "";
		}
		if (request.getParameter("ajaxClasses") != null) {
			paramAjaxClasses = request.getParameter("ajaxClasses");
		} else {
			paramAjaxClasses = "false";
		}
		if (request.getParameter("ajaxContent") != null) {
			paramAjaxContent = request.getParameter("ajaxContent");
		} else {
			paramAjaxContent = "false";
		}
		if (request.getParameter("ajaxRank") != null) {
			paramAjaxRank = request.getParameter("ajaxRank");
		} else {
			paramAjaxRank = "false";
		}
		if (request.getParameter("ajaxTime") != null) {
			paramAjaxTime = request.getParameter("ajaxTime");
		} else {
			paramAjaxTime = "false";
		}
		if (request.getParameter("ajaxTime2") != null) {
			paramAjaxTime2 = request.getParameter("ajaxTime2");
		} else {
			paramAjaxTime2 = "false";
		}
		if (request.getParameter("ajaxTime3") != null) {
			paramAjaxTime3 = request.getParameter("ajaxTime3");
		} else {
			paramAjaxTime3 = "false";
		}
	}
}