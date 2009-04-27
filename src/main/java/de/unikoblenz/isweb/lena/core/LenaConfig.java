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
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.sail.nativerdf.NativeStore;



import de.unikoblenz.isweb.metak4lena.SesameRenderer;

public class LenaConfig {

	//private static LenaConfig lc =new LenaConfig();
	File configurationFile=new File("src/main/resources/lena.properties");
	File lensFile=new File("src/main/webapp/public/resources/configuration/fresnel.n3");
	File defaultLensFile=new File("src/main/webapp/public/resources/configuration/default-lens.n3");
	File dataDir=new File("src/main/webapp/public/resources/data");	
	File remoteRepositoryDir=new File("src/main/webapp/public/resources");
	//ServletContext servletContext;
	Properties props;
	
	private Repository localRepository;
	private Repository remoteRepository;
		
	
	private boolean metaknowledge = true;	
	
	//Changes: 02/04/2009
	//new parameter: metaknowledge (serve as flag to load the dataset with metaknoweldge)	
	public LenaConfig(ServletContext servletContext) {
		// Properties loading.
				
		props = new Properties();
		try {
			if (!configurationFile.exists()) {
				configurationFile=new File(this.getClass().getResource("lena.properties").toURI());
			}
			InputStream is=new FileInputStream(configurationFile);
			props.load(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//errorMessage.append(e.toString() + "<br/>");
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
					System.out.println("found defaultLensFile at "+lensFile.toString());
			} catch (Exception e){
				//configurationFile=new File(this.getClass().getResource("fresnel.n3").toString());
				e.printStackTrace();
			}
		}			
		
		
		
		
		/** init repositories holding data **/
		initRepositories(servletContext);
	}
	
	/**
	 * Initialize the repositories.
	 * 
	 * @throws UnresolvableException
	 * @throws ParsingException
	 */
	public void initRepositories(ServletContext servletContext) {	
		try {
			String repositoryType;
			if (props.get("repository.type") != null) {
				repositoryType = (String) props.get("repository.type");
			} else {
				repositoryType = "memory";
			}
			if (repositoryType.equals("http")) {
				String dataServer = (String) props.get("server.url");
				String dataRepositoryID = (String) props.get("data.repository.id");
				localRepository = new HTTPRepository(dataServer, dataRepositoryID);
				localRepository.shutDown();
				localRepository.initialize();
			} else {
				if (!dataDir.exists()) {
					dataDir=new File(servletContext.getRealPath("/public/resources/data/"));
				}
				
				//dataRepository = new SailRepository(new NativeStore(dataDir));
				localRepository = new SailRepository(new MemoryStore());
				localRepository.initialize();
				//File dataPath = new File(servletContext.getRealPath("/public/resources/data"));				
				File[] dataFiles = dataDir.listFiles();
				addFilesToRepository(dataFiles, localRepository);
			}
			if (!remoteRepositoryDir.exists()) {
				remoteRepositoryDir=new File(servletContext.getRealPath("/public/resources/"));
			}
			remoteRepository = new SailRepository(new NativeStore(remoteRepositoryDir));
			remoteRepository.initialize();
			
		} catch (RepositoryException e) {
			//errorMessage.append(e.toString() + "<br/>");
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
					try {
						con.add(files[i].toURI().toURL(), baseURI, RDFFormat.TURTLE);
						con.commit();
						} catch (Exception e) {							
							try{
								con.add(files[i].toURI().toURL(), baseURI, RDFFormat.RDFXML);
								con.commit();
							}catch (Exception e1) {
								try{
									con.add(files[i].toURI().toURL(), baseURI, RDFFormat.TRIX);
									con.commit();
								}catch (Exception e2) {
									try{
										con.add(files[i].toURI().toURL(), baseURI, RDFFormat.N3);
										con.commit();
									}catch (Exception e3) {
										try{
											con.add(files[i].toURI().toURL(), baseURI, RDFFormat.NTRIPLES);
											con.commit();
										}catch (Exception e4) {
											try{
												con.add(files[i].toURI().toURL(), baseURI, RDFFormat.TRIG);
												con.commit();
											}catch (Exception e5) {
												e5.printStackTrace();
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
	
	public Properties getProperties() {
		return props;
	}
	
	public File getLensFile() {
		return lensFile;
	}
	
	public File getLenaConfigFile() {
		return configurationFile;
	}
	
	public File getDefaultLensFile() {
		return defaultLensFile;
	}
	
	public Repository getLocalRepository() {
		return localRepository;
	}
	
	public Repository getRemoteRepository() {
		return remoteRepository;
	}
	
	public boolean getMetaknowledge(){
		return metaknowledge;
	}	
	
	public File[] getDataFiles() {
		return dataDir.listFiles();		
	}
}
