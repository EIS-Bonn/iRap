/**
 * 
 */
package de.unibonn.iai.eis.irap.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.lib.NotImplemented;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.unibonn.iai.eis.irap.model.DataStoreType;
import de.unibonn.iai.eis.irap.model.Interest;
import de.unibonn.iai.eis.irap.model.PITrakingMethod;
import de.unibonn.iai.eis.irap.model.Subscriber;
import de.unibonn.iai.eis.irap.sparql.QueryDecomposer;
import de.unibonn.iai.eis.irap.sparql.SPARQLExecutor;

/**
 * @author Kemele M. Endris
 *
 */
public class PIManager {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PIManager.class);
	/**
	 * 
	 * @param removedTriples
	 * @param subscriber
	 * @return
	 */
	public static boolean removeFromPI(Model removedTriples, Subscriber subscriber, Interest interest){
		List<TriplePath> paths = new ArrayList<TriplePath>();
		paths.addAll(interest.getBgp());
		paths.addAll(interest.getOgp());
		Query cu = QueryDecomposer.toConstructOfUnions(paths);
		logger.info(cu+"");
		Model removed = SPARQLExecutor.executeConstruct(removedTriples, cu);
		if(removed.isEmpty()){
			System.out.println("Nothing matches");
			return true;
		}
		return applyChange(removed, subscriber, false);
	}
	
	/**
	 * 
	 * @param addedTriples
	 * @param subscriber
	 * @return
	 */
	public static boolean insertToPI(Model addedTriples, Subscriber subscriber){
		return applyChange(addedTriples, subscriber, true);
	}
	
	/**
	 * 
	 * @param addedTriples
	 * @param subscriber
	 * @param isAdd
	 * @return
	 */
	private static boolean applyChange(Model triples, Subscriber subscriber, boolean isAdd){
		if(subscriber.getPiMethod() == PITrakingMethod.LIVE_ON_SOURCE){
			return true;
		}
		
		if(subscriber.getPiType() == DataStoreType.TDB || subscriber.getPiType() == DataStoreType.SPARQL_ENDPOINT){
			StringBuffer removePIQuery = QueryDecomposer.toUpdate(triples,  isAdd);
			if(subscriber.getPiType() == DataStoreType.TDB ){
				Dataset pitdb = TDBFactory.createDataset(subscriber.getPiStoreBaseURI());
				return SPARQLExecutor.executeUpdate(pitdb, removePIQuery.toString());
			}else  {
				return SPARQLExecutor.executeUpdate(subscriber.getPiStoreBaseURI(), removePIQuery.toString());
			}
		}else if(subscriber.getPiType() == DataStoreType.VIRTUOSO_JDBC){
			throw new NotImplemented("Vertuoso jdbc not yet supported!");
		}
		return false;
	}
	
	public static Model getMissingFromPI(Subscriber subscriber, String sourceEndpoint, List<TriplePath> paths,List<TriplePath> optionals, List<TriplePath> candidatePaths, Model candidateModel){
		Model rmisss = ModelFactory.createDefaultModel();
		Query aq = QueryDecomposer.bindValues(paths, optionals, candidatePaths, candidateModel);
		if(subscriber.getPiMethod() == PITrakingMethod.LIVE_ON_SOURCE){
			//TODO: define source access type - TDB, VIRTUOSO_JDBC or SPARQL_ENDPOINT
			
			//assuming source dataset access type as SPARQL_ENDPOINT 
			rmisss = SPARQLExecutor.executeConstruct(sourceEndpoint, aq);
		}else{
			if (subscriber.getPiType() == DataStoreType.TDB) {
				Dataset target = TDBFactory.createDataset(subscriber.getPiStoreBaseURI());
				rmisss = SPARQLExecutor.executeConstruct(target, aq);
			} else if (subscriber.getPiType() == DataStoreType.SPARQL_ENDPOINT) {
				rmisss = SPARQLExecutor.executeConstruct(subscriber.getPiStoreBaseURI(), aq);
			} else {
				throw new NotImplemented();
			}
		}
		
		return rmisss;
	}
}
