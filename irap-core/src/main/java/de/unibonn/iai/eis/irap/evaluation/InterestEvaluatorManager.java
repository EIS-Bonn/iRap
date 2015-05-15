/**
 * 
 */
package de.unibonn.iai.eis.irap.evaluation;

import java.util.List;

import org.slf4j.Logger;

import de.unibonn.iai.eis.irap.interest.InterestManager;
import de.unibonn.iai.eis.irap.model.Changeset;
import de.unibonn.iai.eis.irap.model.Subscriber;

/**
 * @author Kemele M. Endris
 *
 */
public class InterestEvaluatorManager {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(InterestEvaluatorManager.class);
	
	InterestManager interestManager;
	Changeset changeset;
	
	 public InterestEvaluatorManager(InterestManager interestManager, Changeset changeset) {
		this.interestManager = interestManager;
		this.changeset = changeset;
	}
	
	public void begin(){
		logger.info("Reading interest expressions subscribed for changeset from: " + changeset.getChangesetUri());
		List<Subscriber> subscribers = interestManager.getSubscribers(changeset.getChangesetUri());
		for(Subscriber s: subscribers){
			logger.info("Stating evalaution for Subscriber: " + s.getId());
			InterestEvaluator eval = new InterestEvaluator(s, changeset);
			eval.start();
		}
	}
}
