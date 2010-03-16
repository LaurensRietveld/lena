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

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.swing.filechooser.FileFilter;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.xmedia.accessknow.sesame.model.SesameOntology;
import org.xmedia.kernel.handlers.XMSessionHandler;
import org.xmedia.kernel.handlers.XMSourceHandler;
import org.xmedia.kernel.handlers.XMUserHandler;
import org.xmedia.kernel.scratchpad.ApplicationHandler;
import org.xmedia.kernel.scratchpad.bookmarks.BookmarksHandler;

public class LenaConfig {
	
	String path = System.getProperty("user.dir");

	//private static LenaConfig lc =new LenaConfig();
	File configurationFile =new File(path + "/src/java/resources/lena.properties");
	File lensFile=new File(path + "/src/java/resources/configuration/fresnel.n3");
	File defaultLensFile=new File(path + "/src/java/resources/configuration/default-lens.n3");
	File dataDir=new File(path + "/src/java/resources/data");	
	File remoteRepositoryDir=new File(path + "/src/java/resources/repositories/remote");
	//File remoteRepositoryDir=new File("src/main/webapp/public/resources");
	//ServletContext servletContext;
	Properties props;
	int numberOfResourcesPage = 15;
	
	private Repository localRepository;
	private Repository remoteDataRepository;
	protected Repository fresnelDefaultLensRepository;
	protected Repository fresnelLensRepository;
	
	private ApplicationHandler applicationHandler;	
	private XMSourceHandler xmSourceHandler;
	private XMUserHandler xmUserHandler;	
	private BookmarksHandler bmHandler; 
	private XMSessionHandler sessionHandler;
	private boolean metaknowledge = true;
	
	
	//Changes: 02/04/2009
	//new parameter: metaknowledge (serve as flag to load the dataset with metaknoweldge)	
	public LenaConfig(ServletContext servletContext) {
		System.out.println("PATH: " + path);
		RepositoryConnection con=null;
		// Properties loading.			
		props = new Properties();
		try {
			if (!configurationFile.exists()) {
				configurationFile = new File(servletContext.getRealPath("/public/resources/lena.properties"));				
			}
			InputStream is=new FileInputStream(configurationFile);
			props.load(is);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/** init lens file information ... */
		if (!lensFile.exists()) {
			System.out.println(lensFile+" does not exist, trying somehting else ...");
			try {
				lensFile = new File(servletContext.getRealPath("/public/resources/configuration/fresnel.n3"));
				if (lensFile.exists())
					System.out.println("found lensFile at "+lensFile.toString());
			} catch (Exception e){
				//configurationFile=new File(this.getClass().getResource("fresnel.n3").toString());
				e.printStackTrace();
			}
		}	

		/** init lens file information ... */
		if (!defaultLensFile.exists()) {
			System.out.println(defaultLensFile+" does not exist, trying somehting else ...");
			try {
				defaultLensFile = new File(servletContext.getRealPath("/public/resources/configuration/default-lens.n3"));
				if (defaultLensFile.exists())
					System.out.println("found defaultLensFile at "+defaultLensFile.toString());
			} catch (Exception e){
				//configurationFile=new File(this.getClass().getResource("fresnel.n3").toString());
				e.printStackTrace();
			}
		}			
		fresnelDefaultLensRepository = new SailRepository(new MemoryStore());
		fresnelLensRepository = new SailRepository(new MemoryStore());
	    try {
			fresnelLensRepository.initialize();
			fresnelLensRepository.getConnection().add(lensFile, lensFile.toURL().toString(), RDFFormat.N3);
		   	
		    fresnelDefaultLensRepository.initialize();
		    con=fresnelDefaultLensRepository.getConnection();		    
		    con.add(defaultLensFile, defaultLensFile.toURL().toString(), RDFFormat.N3);
		   	
		} catch (RDFParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (con!=null)
				try {
					con.close();
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
		}
		
		/** init repositories holding data **/
		initRepositories(servletContext);
	}
	
	/**
	 * Initialize the repositories.
	 *
	 *
	 */
	public void initRepositories(ServletContext servletContext) {
		try {
			String repositoryType;
			if (props.get("repository.type") != null) {
				repositoryType = (String) props.get("repository.type");
			} else {
				repositoryType = "xmedia";
			}
			if (repositoryType.equals("http")) {
				System.out.println("HTTP Connection");
				String dataServer = (String) props.get("server.url");
				String dataRepositoryID = (String) props.get("data.repository.id");
				System.out.println("Server: "+dataServer);
				System.out.println("Repository: "+dataRepositoryID);
				localRepository = new HTTPRepository(dataServer, dataRepositoryID);
				localRepository.shutDown();
				localRepository.initialize();
			} 
			else if (repositoryType.equals("xmedia")) {
				System.out.println("Initializing x-media repository ...");
				WebApplicationContext wac=WebApplicationContextUtils.getWebApplicationContext(servletContext);
				
				/*SesameSession  session = (SesameSession) getSessionFactory().openSession(
						getKbConnection(), getKb());
				
				RepositoryConnection repositoryConnection = session
				.getRepositoryConnection();*/
				
				System.out.println("Exists 'sharedKB': "+wac.containsBean("sharedKB"));			
				SesameOntology o=(SesameOntology) wac.getBean("sharedKB");
				System.out.println("Sesame Ontology: "+o);
				System.out.println("Sesame Ontology Name: "+o.getName());			
				localRepository=o.getRepository();
				setXmSourceHandler((XMSourceHandler) wac.getBean("sourceHandler"));
				setXmUserHandler((XMUserHandler) wac.getBean("userHandler"));
				setApplicationHandler((ApplicationHandler) wac.getBean("applicationHandler"));
				setBookmarksHandler((BookmarksHandler)wac.getBean("bookmarksHandler"));
				setSessionHandler((XMSessionHandler)wac.getBean("sessionHandler"));
											
				//XMediaIntegration.configureForRedirect(application, sessionHandler, user, uri);				
			
			}else if (repositoryType.equals("memory")){
				System.out.println("Initializing memory repository...");
				if (!dataDir.exists()) {
					dataDir=new File(servletContext.getRealPath("/public/resources/data/rdf/"));					
				}
				
				//dataRepository = new SailRepository(new NativeStore(dataDir));
				localRepository = new SailRepository(new MemoryStore());
				localRepository.initialize();
				//File dataPath = new File(servletContext.getRealPath("/public/resources/data"));				
				File[] dataFiles = dataDir.listFiles(new FilenameFilter(){					
					public boolean accept(File f, String arg1) {
						if (f.isHidden()) return false;
						return true;
					}					
				});
				addFilesToRepository(dataFiles, localRepository);
			}
			
			System.out.println("LINKEDDATA: Initializing remote repository ...");
			if (!remoteRepositoryDir.exists()) {
				System.out.println("LINKEDDATA: Remote repository DIR does not exist ... try something else...");
				remoteRepositoryDir=new File(servletContext.getRealPath("/public/resources/repositories/remote"));
			}
			// Native store is only maintaining one repository per session -> all data can be seen by all users.
			remoteDataRepository = new SailRepository(new NativeStore(remoteRepositoryDir));
			//remoteDataRepository = new SailRepository(new MemoryStore());
			remoteDataRepository.initialize();
			
		} catch (RepositoryException e) {
			//errorMessage.append(e.toString() + "<br/>");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Shutdown remote repository to remove triples
	 *
	 */
	public void shutdownRepositories() {
		try {
			System.out.println("PATH: " + path);
			remoteDataRepository.getConnection().clear();
			remoteDataRepository.shutDown();
			remoteDataRepository.initialize();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//NEW: add RDFFormat parameter! (Metaknowledge Format = RDFFormat.TRIX)
	/**
	 * Method for adding all files from an array into a repository.
	 * 
	 * @param files
	 * @param repository
	 */
	private void addFilesToRepository(File[] files, Repository repository) {			
		try {
			RepositoryConnection con = repository.getConnection();
			
				String baseURI = "http://example.org/example/local";
				for (int i = 0; i < files.length; i++) {
					System.out.println("File Name: "+ files[i].getPath() + " File Index:"+i);
					try {
						con.add(files[i].toURI().toURL(), baseURI, RDFFormat.TRIX);
						System.out.println("Read File: "+ files[i].getPath());
						con.commit();
						} catch (Exception e) {							
							try{
								con.add(files[i].toURI().toURL(), baseURI, RDFFormat.RDFXML);
								System.out.println("Read File: "+ files[i].getPath());
								con.commit();
							}catch (Exception e1) {
								try{
									con.add(files[i].toURI().toURL(), baseURI, RDFFormat.TURTLE);
									System.out.println("Read File: "+ files[i].getPath());
									con.commit();
								}catch (Exception e2) {
									try{
										con.add(files[i].toURI().toURL(), baseURI, RDFFormat.N3);
										System.out.println("Read File: "+ files[i].getPath());
										con.commit();
									}catch (Exception e3) {
										try{
											con.add(files[i].toURI().toURL(), baseURI, RDFFormat.NTRIPLES);
											System.out.println("Read File: "+ files[i].getPath());
											con.commit();
										}catch (Exception e4) {
											try{
												con.add(files[i].toURI().toURL(), baseURI, RDFFormat.TRIG);
												System.out.println("Read File: "+ files[i].getPath());
												con.commit();
											}catch (Exception e5) {
												System.out.println("ignoring file "+files[i]+" as data input");
											}
										}
									}
								}
							}
						}
				}
				con.close();
			 
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
			
	}
	
	/**
	 * Method for adding remote data from an URI into a repository.
	 * 
	 * @param paramURL
	 */
	public void addURLDataToRepository(String paramURL) {
		try {
			RepositoryConnection conRemote = remoteDataRepository.getConnection();
			
			String baseURI = "http://example.org/example/local";
			try {
				String fileExtension = paramURL.substring(paramURL.lastIndexOf(".") + 1);
				URL url = new URL(paramURL);

				URLConnection urlc = url.openConnection();
				urlc.setRequestProperty("accept", "application/rdf+xml");
				urlc.setConnectTimeout(10000);
				urlc.setUseCaches(true);
				urlc.connect();

				InputStream in = urlc.getInputStream();

				if (fileExtension.equals("n3")) {
					//conRemote.add(url, url.toURI().toString(), RDFFormat.TURTLE);
					conRemote.add(in, url.toURI().toString(), RDFFormat.TURTLE);
					System.out.println("AJAX IV: Added TURTLE!");
				} else {
					//conRemote.add(url, url.toURI().toString(), RDFFormat.RDFXML);
					conRemote.add(in, url.toURI().toString(), RDFFormat.RDFXML);
					System.out.println("AJAX IV: Added RDFXML!");
				}

				conRemote.commit();
			} catch (Exception e) {
				//errorMessage.append(e.toString() + "<br/>");
				//errorMessage.append("No RDF data available. Showing resource locally.<br/>");
				e.printStackTrace();
			}finally {
				conRemote.close();
			}
		} catch (RepositoryException e) {
			//errorMessage.append(e.toString() + "<br/>");
			e.printStackTrace();
		}
	}
	
	public Properties getProperties() {
		return props;
	}
	
	public Repository getLensRepository() {
		return this.fresnelLensRepository;
	}
	
	public Repository getDefaultLensRepository() {
		return this.fresnelDefaultLensRepository;
	}
	public File getLenaConfigFile() {
		return configurationFile;
	}
	
	/*public File getDefaultLensFile() {
		return defaultLensFile;
	}*/
	
	public Repository getLocalRepository() {
		return localRepository;
	}
	
	public Repository getRemoteRepository() {
		return remoteDataRepository;
	}
	
	public boolean getMetaknowledge(){
		return metaknowledge;
	}	
	
	public File[] getDataFiles() {
		return dataDir.listFiles();		
	}
	public int getMaxResourcesPage(){
		return this.numberOfResourcesPage;
	}
	
	public XMSourceHandler getXmSourceHandler() {
		return xmSourceHandler;
	}
	public void setXmSourceHandler(XMSourceHandler xmSourceHandler) {
		this.xmSourceHandler = xmSourceHandler;
	}
	public XMUserHandler getXmUserHandler() {
		return xmUserHandler;	
	}
	public void setXmUserHandler(XMUserHandler xmUserHandler) {
		this.xmUserHandler = xmUserHandler;
	}
	public void setApplicationHandler(ApplicationHandler applicationHandler) {
		this.applicationHandler = applicationHandler;	}

	public ApplicationHandler getApplicationHandler() {
		return applicationHandler;
	}
	public void setBookmarksHandler(BookmarksHandler bmHandler) {
		this.bmHandler = bmHandler;		
	}
	public BookmarksHandler getBookmarksHandler() {
		return bmHandler;		
	}
	public void setSessionHandler(XMSessionHandler sessionHandler) {
		this.sessionHandler = sessionHandler;		
	}
	public XMSessionHandler getSessionHandler() {
		return sessionHandler;		
	}
}
