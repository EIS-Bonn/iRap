/**
 * 
 */
package de.unibonn.iai.eis.irap.evaluation;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;

import de.unibonn.iai.eis.irap.interest.InterestExprGraph;
import de.unibonn.iai.eis.irap.model.Changeset;
import de.unibonn.iai.eis.irap.model.EvaluationResultModel;
import de.unibonn.iai.eis.irap.model.Interest;
import de.unibonn.iai.eis.irap.model.Subscriber;
import de.unibonn.iai.eis.irap.sparql.QueryDecomposer;
import de.unibonn.iai.eis.irap.sparql.QueryPatternExtractor;
import de.unibonn.iai.eis.irap.sparql.SPARQLExecutor;

/**
 * @author Kemele M. Endris
 *
 */
public class InterestEvaluator {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(InterestEvaluator.class);
	private Subscriber subscriber;
	private Changeset changeset;
	
	public InterestEvaluator(Subscriber subscriber, Changeset changeset) {
		this.subscriber = subscriber;
		this.changeset = changeset;
	}
	
	/**
	 * evaluate each interests of a subscriber sequentially on a changeset
	 */
	public void start(){
		List<Interest> interests = subscriber.getInterestExpressions();
		for (Interest i : interests) {
			evaluate(i);
		}
	}
	
	/**
	 * evaluate an interest over a changeset in two steps:
	 * <ol>
	 * <li>evaluate interest on removed triples</li>
	 * <li>evaluate interest on added triples</li>
	 * <ol>
	 * since SPARQL update operation works this way, it should be kept same step
	 * as above: delete then add (rename)
	 * 
	 * @param interest
	 *            the interest expression by a subscriber which contains source
	 *            Vs target datasets, changeset url, local PI graph name, etc
	 * @param changeset
	 *            an update that contains only removed and added triples since
	 *            the last update
	 */
	private void evaluate(final Interest interest) {
	
		logger.info("Evaluating removed triples ...");
		//evaluate deletion on target dataset
		evaluateRemoved(interest);
		logger.info("Evaluating added triples ...");
		//evaluate addition
		evaluateAdded(interest);
	}
	
	/**
	 *  Interest evaluation over removed triples 
	 *  
	 * @param interest
	 * @param changeset
	 * @return
	 */
	private void evaluateRemoved(final Interest interest) {
		
		Model removed = ModelFactory.createDefaultModel().add(changeset.getRemovedTriples());
		
		logger.info("Removing triples from potentially interesting dataset:"  + subscriber.getPiStoreBaseURI());
		//first evaluate the removed triples on Potentially interesting triples of an interest	
		if(!PIManager.removeFromPI(removed, subscriber, interest)) {
			logger.info("Cannot remove triples from potentially interesting dataset: " + subscriber.getPiStoreBaseURI());
		}
		
		// Evaluate interest expression directly on target endpoint of a subscriber process 
		EvaluationResultModel  removeResult = processRemove(interest);
		
		logger.info("Inserting triples to potentially interesting dataset:"  + subscriber.getPiStoreBaseURI());
		//store triples from target that become potentially interesting
		if(!removeResult.getPotentiallyInterestingTriples().isEmpty()){
			if(!PIManager.insertToPI(removeResult.getPotentiallyInterestingTriples(), subscriber)) {
				logger.info("Cannot insert triples to potentially interesting dataset: " + subscriber.getPiStoreBaseURI());
			}
		}
		logger.info("Removing intersting removed Triples from target: " + subscriber.getTargetUpdateURI());
		// propagate interesting removed triples to interest subscribers' target update uri (endpoint - in this case)
		if(!removeResult.getInterestingTriples().isEmpty()){
			if(!TargetManager.removeFromTarget(removeResult.getInterestingTriples(), subscriber)) {
				logger.info("Cannot remove triples from target: " + subscriber.getTargetUpdateURI());
			}
		}
		logger.info(changeset.getSequenceNum()+ ":" + "-----------------END of EVALUATION ON REMOVED TRIPLES -------------");
	}
	
	/**
	 * 
	 * @param interest
	 * @return 
	 */
	private EvaluationResultModel processRemove(final Interest interest){
		
		Model removed = ModelFactory.createDefaultModel().add(changeset.getRemovedTriples());
		
		/*//first evaluate the removed triples on Potentially interesting triples of an interest	
		if(PIManager.removeFromPI(removed, subscriber, interest)){
			logger.info("Triples from potentially interesting graph has been removed!");
		}
		else {
			logger.info("Cannot remove triples from potentially interesting graph!");
		}
		*/
		//TODO: include filters with BGP
		
		//Interesting removed triples
		Model bgpMatching = getMatching(interest.getBgp(), removed);
		removed.remove(bgpMatching);
		
		//Interesting optional removed triples
		Model ogpMatching = getMatching(interest.getOgp(), removed);
		removed.remove(ogpMatching);
		
		//Interesting removed triples and potentially interesting triples (because of partially removed pattern)
		EvaluationResultModel result = getBGPCombinationsForRemove(interest, removed);
		
		//add bgp and ogp matchings to interesting removed triples
		result.addInterestingTriples(bgpMatching);
		result.addPotentiallyInterestingTriples(ogpMatching);
		return result;
	}
	
	/**
	 * 
	 * @param paths
	 * @param model
	 * @return
	 */
	private Model getMatching(List<TriplePath> paths, Model model){		
		Query query = QueryDecomposer.toConstructQuery(paths);
		return SPARQLExecutor.executeConstruct(model, query);
	}
	/**
	 * combination of BGP of interest expression
	 * @param interest
	 * @return
	 */
	private EvaluationResultModel getBGPCombinationsForRemove(final Interest interest, Model removed){
		EvaluationResultModel result = new EvaluationResultModel();
		List<TriplePath> paths = interest.getBgp();
		//Combinations - BGP
		for(int i = paths.size()-1; i>0; i--){
			//2^b ask queries
			List<Query> askQueries = QueryDecomposer.composeAskQueries(paths, i);
			InterestExprGraph g = new InterestExprGraph();
			for (Query q : askQueries) {	
				List<TriplePath> askPaths = QueryPatternExtractor.getBGPTriplePaths(q);
				//if q contains disjoint pattern				
				if(askPaths.size() > 1 && !g.isNonDisjoint(q)){
					logger.info("SKIPPING: Disjoint Query: \n" + q);
					continue;
				}
				if (SPARQLExecutor.executeAsk(removed, q)) {
					Query cq = QueryDecomposer.toConstructQuery(askPaths);
					// extract c_i
					Model r = SPARQLExecutor.executeConstruct(removed, cq);
					EvaluationResultModel partials = assertRemoved(interest, askPaths, r);
					
					result.addInterestingTriples(partials.getInterestingTriples());
					result.addPotentiallyInterestingTriples(partials.getPotentiallyInterestingTriples());
					removed.remove(r);
				}
			}
		}
		
		/*List<TriplePath> ogps = interest.getOgp();
		for(int i= ogps.size()-1; i>0; i++){
			
		}*/
		return result;
	}
	/**
	 * 
	 * @param interest
	 * @param candidatePaths
	 * @param candidateModel
	 * @return
	 */
	private EvaluationResultModel assertRemoved(final Interest interest, List<TriplePath> candidatePaths, Model candidateModel){
		List<TriplePath> paths = interest.getBgp();
		Model interestingTriples = ModelFactory.createDefaultModel();
		Model potentiallyInterestingTriples = ModelFactory.createDefaultModel();
		
		//InterestExprGraph g = new InterestExprGraph();
		
		Model missing= TargetManager.getMissingFromTarget(subscriber, paths, new ArrayList<TriplePath>(), candidatePaths, candidateModel);
		if(!missing.isEmpty()){
			// TODO: check if the prime is related to candidateModel only. 			
		   // If there are other triples connected a triple in prime from target, then leave this triple (remove from missing)			
			interestingTriples.add(missing);
			Model prime = missing.remove(candidateModel);
			potentiallyInterestingTriples.add(prime);
		}
		
		EvaluationResultModel result = new EvaluationResultModel();
		result.getInterestingTriples().add(interestingTriples);
		result.getPotentiallyInterestingTriples().add(potentiallyInterestingTriples);
		return result;
	}

	/**
	 * Interest evaluation over Added triples of a changeset,
	 * 
	 * @param interest
	 * @param changeset
	 * @return
	 */
	private void evaluateAdded(final Interest interest) {
		
		EvaluationResultModel evaluationResult = processAdditions(interest);
		
		if(!evaluationResult.getPotentiallyInterestingTriples().isEmpty()){
			logger.info("Inserting triples to potentially interesting dataset:"  + subscriber.getPiStoreBaseURI());
			// Insert potentially interesting triples to local triple store
			if(!PIManager.insertToPI(evaluationResult.getPotentiallyInterestingTriples(), subscriber)){
				logger.info("Cannot insert triples to potentially interesting dataset: " + subscriber.getPiStoreBaseURI());
			}
		}else{
			logger.info("NO potentially intersting added triples found");
		}
		if(!evaluationResult.getInterestingTriples().isEmpty()){		
			logger.info("Inserting interesting added triples to target dataset:"  + subscriber.getTargetUpdateURI());
			if(!TargetManager.insertToTarget(evaluationResult.getInterestingTriples(), subscriber)){
				logger.info("Cannot propagate interesting added triples to target dataset: " + subscriber.getTargetUpdateURI());
			}
		}else{
			logger.info(" NO Interesting added triples Found!");
		}
		logger.info(changeset.getSequenceNum()+ ":" + "~~~~~~~~~~~~~~~~END of EVALUATING ADDED TRIPLES ~~~~~~~~~~~~~~~~~~");
	}
	
	/**
	 * 
	 * @param interest
	 * @return
	 */
	private EvaluationResultModel processAdditions(final Interest interest){
		Model added = ModelFactory.createDefaultModel().add(changeset.getAddedTriples());
		//EvaluationResultModel result = new EvaluationResultModel();
		
		
		// Interesting removed triples
		Model bgpMatching = getMatching(interest.getBgp(), added);
		Model matching = PIManager.getMissingFromPI(subscriber, interest.getSourceEndpoint(), interest.getBgp(), interest.getOgp(), interest.getBgp(), bgpMatching);
		bgpMatching.add(matching);
		
		//remove matchings found from PI
		Model cleaning = matching.remove(bgpMatching);
		PIManager.removeFromPI(cleaning, subscriber, interest);
		//result.getInterestingTriples().add(bgpMatching);
		
		added.remove(bgpMatching);
		
		EvaluationResultModel result = getBGPCombinationsForAdded(interest, added);
		result.addInterestingTriples(bgpMatching);
		
		
		// Interesting optional removed triples
		Model ogpMatching = getMatching(interest.getOgp(), added);
		
		Model missing= TargetManager.getMissingFromTarget(subscriber, interest.getBgp(), interest.getOgp(), interest.getOgp(), ogpMatching);
		if(!missing.isEmpty()){
			result.addInterestingTriples(missing);
			result.addPotentiallyInterestingTriples(ogpMatching.remove(missing));
		}else
			result.addPotentiallyInterestingTriples(ogpMatching);
		return result;
	}
	/**
	 * 
	 * @param interest
	 * @param added
	 * @return
	 */
	private EvaluationResultModel getBGPCombinationsForAdded(final Interest interest, Model added){
		EvaluationResultModel result = new EvaluationResultModel();
		List<TriplePath> paths = interest.getBgp();
		
		//Combinations - BGP
		for (int i = paths.size() - 1; i > 0; i--) {
			// 2^b ask queries
			List<Query> askQueries = QueryDecomposer.composeAskQueries(paths, i);
			InterestExprGraph g = new InterestExprGraph();
			for (Query q : askQueries) {
				List<TriplePath> askPaths = QueryPatternExtractor.getBGPTriplePaths(q);
				// if q contains disjoint pattern
				if (askPaths.size() > 1 && !g.isNonDisjoint(q)) {
					logger.info("SKIPPING: Disjoint Query: \n" + q);
					continue;
				}

				if (SPARQLExecutor.executeAsk(added, q)) {
					Query cq = QueryDecomposer.toConstructQuery(askPaths);
					// extract c_i
					Model r = SPARQLExecutor.executeConstruct(added, cq);
					
					// Interesting added triples (combined with triples in PI)
					Model m = PIManager.getMissingFromPI(subscriber, interest.getSourceEndpoint(), paths, interest.getOgp(), askPaths, r);
					if (!m.isEmpty()) {
						result.addInterestingTriples(m);
						Model dif = ModelFactory.createDefaultModel().add(m).remove(r);
						if(!PIManager.removeFromPI(dif, subscriber, interest)){
							logger.info("Cannot remove triples that become interesting from PI");
						}
						r.remove(m);
						added.remove(m);
						if (r.isEmpty()) {
							continue;
						}
					}
					
					Model partials = assertAdded(interest, askPaths, r);
					
					if(partials.isEmpty()){
						result.addPotentiallyInterestingTriples(r);
					}
					else{
						result.addPotentiallyInterestingTriples(r.remove(partials));
						result.addInterestingTriples(partials);
					}
					added.remove(r);
				}
			}
		}
		
		return result;
	}
	/**
	 * 
	 * @param interest
	 * @param askPaths
	 * @param res
	 * @return
	 */
	private Model assertAdded(final Interest interest, List<TriplePath> askPaths, Model res){
		InterestExprGraph g = new InterestExprGraph();
		Model interestingTriples = ModelFactory.createDefaultModel();
		Model potentiallyInterestingTriples = ModelFactory.createDefaultModel();
		
		Model partial = ModelFactory.createDefaultModel().add(res);
		// find partially matching from PI and check the rest from target
		List<TriplePath> askDiff = new ArrayList<TriplePath>();
		askDiff.addAll(interest.getBgp());
		askDiff.removeAll(askPaths);
		
		for (int j = askDiff.size()-1 ; j > 0; j--) {
			List<Query> consQueries = QueryDecomposer.composeConstructQueries(askDiff, j);

			for (Query qc : consQueries) {
				List<TriplePath> diffAndAsk = new ArrayList<TriplePath>();
				diffAndAsk.addAll(askPaths);
				diffAndAsk.addAll(QueryPatternExtractor.getBGPTriplePaths(qc));
				
				if(!g.isNonDisjoint(QueryDecomposer.toAskQuery(diffAndAsk))){
					//logger.info("DISJOINT delta: \n" + QueryDecomposer.toAskQuery(diffAndAsk));					
					continue;
				}
				//candidate C_i from Delta = A and pi
				Model piR = PIManager.getMissingFromPI(subscriber, interest.getSourceEndpoint(), diffAndAsk, interest.getOgp(), askPaths, partial);
				if (piR.isEmpty()) {
					diffAndAsk = askPaths;
				}
				piR.add(partial);
				Model m = getPartialsFromTarget(interest, diffAndAsk, piR);
				if(!m.isEmpty()){
					interestingTriples.add(m);
					partial.remove(m);
				}
			}
		}
		if(interestingTriples.isEmpty()){
			potentiallyInterestingTriples.add(partial);
		}else{
			Model pire = ModelFactory.createDefaultModel().add(interestingTriples).remove(partial);
			if(!PIManager.removeFromPI(pire, subscriber, interest)){
				logger.info("Cannot remove triples from PIs that becomes interesting!");
			}
		}
		
		return interestingTriples;
	}
	/**
	 * 
	 * @param interest
	 * @param askPaths
	 * @param candidateModel
	 * @return
	 */
	private Model getPartialsFromTarget(final Interest interest, List<TriplePath> askPaths, Model candidateModel){
		
		Model result = ModelFactory.createDefaultModel();
		Model missing= TargetManager.getMissingFromTarget(subscriber, interest.getBgp(), interest.getOgp(), askPaths, candidateModel);
		if(!missing.isEmpty()){
			result.add(missing);
			Model prime = missing.remove(candidateModel);
			return result.remove(prime);
		}
		
		return result;
	}
}
