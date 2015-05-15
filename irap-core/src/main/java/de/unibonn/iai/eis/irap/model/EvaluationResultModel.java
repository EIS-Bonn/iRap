/**
 * 
 */
package de.unibonn.iai.eis.irap.model;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Kemele M. Endris
 *
 */
public class EvaluationResultModel {

	private Model interestingTriples = ModelFactory.createDefaultModel();
	private Model potentiallyInterestingTriples = ModelFactory.createDefaultModel();
	
	public EvaluationResultModel(Model interestingTriples, Model potentiallyInterestingTriples) {
		this.interestingTriples = interestingTriples;
		this.potentiallyInterestingTriples = potentiallyInterestingTriples;
	}
	public EvaluationResultModel() {
	}
	/**
	 * @return the interestingTriples
	 */
	public Model getInterestingTriples() {
		return interestingTriples;
	}
	/**
	 * @return the potentiallyInterestingTriples
	 */
	public Model getPotentiallyInterestingTriples() {
		return potentiallyInterestingTriples;
	}
	
	public void addInterestingTriples(Model interestingTriples){
		this.interestingTriples.add(interestingTriples);
	}
	public void addPotentiallyInterestingTriples(Model potentiallyInterestingTriples){
		this.potentiallyInterestingTriples.add(potentiallyInterestingTriples);
	}
}
