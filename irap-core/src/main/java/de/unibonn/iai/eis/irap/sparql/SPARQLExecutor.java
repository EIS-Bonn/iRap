/**
 * 
 */
package de.unibonn.iai.eis.irap.sparql;


import org.slf4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;



/**
 * @author keme686
 *
 */
public class SPARQLExecutor {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(SPARQLExecutor.class);
	/////////////////////////////////////////////////////////////////////////////
	/////////////////QUERY EXECUTION ///////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////
	/**
	 * execute a sparql select query on an RDF dataset 
	 * 
	 * @param dataset
	 * @param query
	 * @return
	 */
	public static ResultSet executeSelect(Object dataset, Query query){
		
		if(!query.isSelectType()){
			return null;
		}
		try{			
			QueryExecution exe;
			if(dataset == null){
				exe = QueryExecutionFactory.create(query);
			}else if(dataset instanceof String){
				String endpoint = (String)dataset;
				exe = QueryExecutionFactory.sparqlService(endpoint, query);
			}else if(dataset instanceof Dataset){
				Dataset ds = (Dataset)dataset;
				exe = QueryExecutionFactory.create(query, ds);
			}else if(dataset instanceof Model){
				Model model = (Model)dataset;
				exe = QueryExecutionFactory.create(query, model);
			}else{
				return null;
			}
			ResultSet result = exe.execSelect();
			return result;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * executes a sparql construct query on an RDF dataset
	 * 
	 * @param dataset
	 * @param query
	 * @return
	 */
	public static Model executeConstruct(Object dataset, Query query){
		if(!query.isConstructType()){
			return null;
		}
		try{			
			QueryExecution exe;
			if(dataset == null){
				exe = QueryExecutionFactory.create(query);
			}else if(dataset instanceof String){
				String endpoint = (String)dataset;
				exe = QueryExecutionFactory.sparqlService(endpoint, query);
			}else if(dataset instanceof Dataset){
				Dataset ds = (Dataset)dataset;
				exe = QueryExecutionFactory.create(query, ds);
			}else if(dataset instanceof Model){
				Model model = (Model)dataset;
				exe = QueryExecutionFactory.create(query, model);
			}else{
				return null;
			}
			Model result = exe.execConstruct();
			return result;
		}catch(Exception e){
			e.printStackTrace();
			logger.error("Make sure the endpoint is running!");
		}
		return null;
	}
	/**
	 * executes a SPARQL ASK query over an RDF dataset
	 * 
	 * @param dataset
	 * @param query
	 * @return
	 */
	public static boolean executeAsk(Object dataset, Query query){
		if(!query.isAskType()){
			return false;
		}
		try{			
			QueryExecution exe;
			if(dataset == null){
				exe = QueryExecutionFactory.create(query);
			}else if(dataset instanceof String){
				String endpoint = (String)dataset;
				exe = QueryExecutionFactory.sparqlService(endpoint, query);
			}else if(dataset instanceof Dataset){
				Dataset ds = (Dataset)dataset;
				exe = QueryExecutionFactory.create(query, ds);
			}else if(dataset instanceof Model){
				Model model = (Model)dataset;
				exe = QueryExecutionFactory.create(query, model);
			}else{
				return false;
			}
			return exe.execAsk();
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///////////////QUERY UPDATE //////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	public static boolean executeUpdate(Object dataset, String queryStr){
		try{			
			logger.info(queryStr);
			if(dataset == null){
				return false;
			}else if(dataset instanceof String){
				String endpoint = (String)dataset;
				UpdateRequest request1 = UpdateFactory.create(queryStr);
				UpdateProcessor proc = UpdateExecutionFactory.createRemote(request1, endpoint);
				proc.execute();
			}else if(dataset instanceof Dataset){
				Dataset ds = (Dataset)dataset;
				UpdateAction.parseExecute(queryStr, ds);
			}else if(dataset instanceof Model){
				Model model = (Model)dataset;
				UpdateAction.parseExecute(queryStr, model);
			}else{
				return false;
			}
			return  true;
		}catch(Exception e){
			e.printStackTrace();
			logger.error(e.getMessage());
			logger.error(queryStr);
		}
		return false;		
	}
	

	////////////////////////////////////////////////////////////////////
	/////////PREFIXES /////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////

	/**
	 * prepares mostly used prefixes such as:
	 * <ul>
	 * <li>rdf</li>
	 * <li>rdfs</li>
	 * <li>owl</li>
	 * <li>foaf</li>
	 * <li>skos</li>
	 * <li>dbpedia</li>
	 * <li>dbpedia-owl</li>
	 * <li>dbpedia-prop</li>
	 * <li>irap</li>
	 * <li>: (default - <http://eis.iai.uni-bonn.de/irap/>)</li>
	 * <li></li>
	 * </ul>
	 * @return a string of well known prefix definitions
	 */
	public static String prefixes(){
		return    " PREFIX rdf: <" + RDF.getURI() + "> "  
				+ " PREFIX rdfs: <" + RDFS.getURI() +"> " 
				+ " PREFIX owl: <" + OWL.getURI() +">  " 	
				+ " PREFIX foaf: <" + FOAF.getURI() +">  "	
			    + " PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " 	
				+ " PREFIX dbpedia: <http://dbpedia.org/resource/>  "
				+ " PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> " 
				+ " PREFIX dbpedia-prop: <http://dbpedia.org/property/> "
				+ " PREFIX irap:    <http://eis.iai.uni-bonn.de/irap/ontology/> "
				+ " PREFIX xsd:    <http://www.w3.org/2001/XMLSchema#>"
				+ " PREFIX :    <http://eis.iai.uni-bonn.de/irap/> ";
	}
	
	public static String dbpediaPrefixes(){
		return " PREFIX dbpedia: <http://dbpedia.org/resource/>  "
				+ "PREFIX dbpedia-owl: <http://dbpedia.org/ontology/> " 
				+ "PREFIX dbpedia-prop: <http://dbpedia.org/property/> ";
	}
	
	//////////////////////////////////////////////////////////////////////////////
	////////////////////  HASHing  ///////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 * @param triplePath
	 * @return
	 */
	public static String generateHashForCache(TriplePath triplePath) {
		String subject = toStringForHashForCache(triplePath.getSubject());
		String predicate =   toStringForHashForCache(triplePath.getPredicate());
		String object = toStringForHashForCache(triplePath.getObject());
		return subject+" "+predicate+" "+object;
	}
	
	/**
	 * 
	 * @param node
	 * @return
	 */
	public static String toStringForHashForCache(Node node) {
		
		if(node.isURI() ) {
			return "<"+node.toString()+">";
		} else if(node.isBlank()) {
			return "[]";
		} else if(node.toString().startsWith("??")) {
			return "[]";
		} else if(node.isVariable()) {
			return "?";
		} else if(node.isLiteral()) {
			return "v";
		}
		return "o";
	}	
	
}
