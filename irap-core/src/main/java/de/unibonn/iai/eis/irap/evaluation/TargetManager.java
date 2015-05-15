/**
 * 
 */
package de.unibonn.iai.eis.irap.evaluation;

import java.util.List;

import org.apache.jena.atlas.lib.NotImplemented;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.tdb.TDBFactory;

import de.unibonn.iai.eis.irap.model.DataStoreType;
import de.unibonn.iai.eis.irap.model.Subscriber;
import de.unibonn.iai.eis.irap.sparql.QueryDecomposer;
import de.unibonn.iai.eis.irap.sparql.SPARQLExecutor;

/**
 * @author Kemele M. Endris
 *
 */
public class TargetManager {

	/**
	 * 
	 * @param removedTriples
	 * @param subscriber
	 * @return
	 */
	public static boolean removeFromTarget(Model removedTriples, Subscriber subscriber){
		return applyChange(removedTriples, subscriber, false);
	}
	
	/**
	 * 
	 * @param removedTriples
	 * @param subscriber
	 * @return
	 */
	public static boolean insertToTarget(Model removedTriples, Subscriber subscriber){
		return applyChange(removedTriples, subscriber, true);
	}
	
	/**
	 * 
	 * @param addedTriples
	 * @param subscriber
	 * @param isAdd
	 * @return
	 */
	private static boolean applyChange(Model triples, Subscriber subscriber, boolean isAdd){
				
		if(subscriber.getTargetType() == DataStoreType.TDB || subscriber.getTargetType() == DataStoreType.SPARQL_ENDPOINT){
			StringBuffer updateQuery = QueryDecomposer.toUpdate(triples,  isAdd);
			if(subscriber.getTargetType() == DataStoreType.TDB ){
				Dataset targetTdb = TDBFactory.createDataset(subscriber.getTargetEndpoint());
				return SPARQLExecutor.executeUpdate(targetTdb, updateQuery.toString());
			}else  {
				return SPARQLExecutor.executeUpdate(subscriber.getTargetUpdateURI(), updateQuery.toString());
			}
		}else if(subscriber.getTargetType() == DataStoreType.VIRTUOSO_JDBC){
			throw new NotImplemented("Vertuoso jdbc not yet supported!");
		}
		return false;
	}
	/**
	 * 
	 * @param subscriber
	 * @param paths
	 * @param candidatePaths
	 * @param candidateModel
	 * @return
	 */
	public static Model getMissingFromTarget(Subscriber subscriber, List<TriplePath> paths,List<TriplePath> optpaths, List<TriplePath> candidatePaths, Model candidateModel){
		Model rmisss = ModelFactory.createDefaultModel();
		Query aq = QueryDecomposer.bindValues(paths, optpaths, candidatePaths, candidateModel);
		if(subscriber.getTargetType() == DataStoreType.TDB){
			Dataset target = TDBFactory.createDataset(subscriber.getTargetEndpoint());
			rmisss = SPARQLExecutor.executeConstruct(target, aq);
		}else if(subscriber.getTargetType() == DataStoreType.SPARQL_ENDPOINT){
			rmisss = SPARQLExecutor.executeConstruct(subscriber.getTargetEndpoint(), aq);
		}else{
			throw new NotImplemented();
		}
		
		return rmisss;
	}
}
