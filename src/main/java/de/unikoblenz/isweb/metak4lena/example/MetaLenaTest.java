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
package de.unikoblenz.isweb.metak4lena.example;

import java.io.*;
import java.util.*;

import junit.framework.*;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.rio.RDFFormat;

import fr.inria.jfresnel.sesame.FresnelSesameParser;
import fr.inria.jfresnel.sesame.SesameRenderer;
import fr.inria.jfresnel.Constants;
import fr.inria.jfresnel.FresnelDocument;
import fr.inria.jfresnel.RendererUtils;
import javax.xml.transform.stream.StreamResult;

public class MetaLenaTest extends TestCase {
	
    protected static String FOAF_PLANET_TEST_MODEL_FILE = "src/main/webapp/public/resources/data/meta/mfoafPlanet.rdf";    
    protected static String TEST_FRESNEL_PROGRAM_FILE_NAME = "src/main/webapp/public/resources/configuration/fresnel.n3";
    protected static String FRESNEL_FDL_OUTPUT_FILE_NAME = "src/main/webapp/public/resources/data/target.xml";
        
    protected Repository fresnelRepository;
	protected Repository dataRepository;
	FresnelDocument fd;
	
	public MetaLenaTest(String name){
		super(name);
    }
	
	public void doMetaLena(String outputFDLFile) {
		//jfresnel renderer
		//SesameRenderer renderer = new SesameRenderer();
		//metak renderer
		de.unikoblenz.isweb.metak4lena.SesameRenderer renderer = new de.unikoblenz.isweb.metak4lena.SesameRenderer();
		org.w3c.dom.Document doc;
		try {
			/* Set up the repository that contains Fresnel lenses and formats */
			fresnelRepository = new SailRepository(new MemoryStore());
		    fresnelRepository.initialize();
		    RepositoryConnection fresnelConnection = fresnelRepository.getConnection();
		    
		    
		    FresnelSesameParser fp = new FresnelSesameParser();
		    File lensFile= new File(TEST_FRESNEL_PROGRAM_FILE_NAME);
			fd = fp.parse(lensFile, Constants.N3_READER);
			
		    File fresnelFile = new File(TEST_FRESNEL_PROGRAM_FILE_NAME);
		    fresnelConnection.add(fresnelFile, fresnelFile.toURL().toString(), RDFFormat.N3);
			/* Set up the repository that contains RDF data */
			dataRepository = new SailRepository(new MemoryStore());
			dataRepository.initialize();
			RepositoryConnection dataConnection = dataRepository.getConnection();
			File dataFile = new File(FOAF_PLANET_TEST_MODEL_FILE);
		    dataConnection.add(dataFile, dataFile.toURL().toString(), RDFFormat.TRIX);
			
		    //doc = renderer.render(fd, dataRepository, "http://isweb/RSS_planetRDF");
		    doc = renderer.render(fd, dataRepository, "http://isweb/FOAF_person");
			
			
			if (outputFDLFile != null) {
				StreamResult res = new StreamResult(new File(outputFDLFile));
	        	RendererUtils.printDoc(doc, res);
	        }
	        else
	        	RendererUtils.printDoc(doc, new StreamResult(System.out));
		} catch (Exception e) {
			assertTrue(false);
			e.printStackTrace();
		}
		
	}
	
	public void testMetaLena() {
		try {
			doMetaLena(FRESNEL_FDL_OUTPUT_FILE_NAME);
			assertTrue(true);
		} catch (Exception e) {
			assertTrue(false);
		}
    }
    
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(MetaLenaTest.class);
		return suite;
    }
}