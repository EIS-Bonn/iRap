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
public class Changeset {
	
	/**
	 * Changeset URI
	 */
	private String changesetUri;
	
	/**
	 * Sequence number of a changeset
	 */
	private String sequenceNum;
	/**
	 * removed triples from source dataset
	 */
	private Model removedTriples = ModelFactory.createDefaultModel();
	/**
	 * added triples to the source dataset
	 */
	private Model addedTriples = ModelFactory.createDefaultModel();
	
	
	public Changeset() {		
	}
	
	public Changeset(String uri, String sequenceNum) {
		this.changesetUri = uri;
		this.sequenceNum = sequenceNum;
	}
	
	public Changeset(String uri, Model removed, Model added, String sequenceNum){
		this.changesetUri = uri;
		this.removedTriples.add(removed);
		this.addedTriples.add(added);
		this.sequenceNum = sequenceNum;
	}

	
	public String getChangesetUri() {
		return changesetUri;
	}

	public void setChangesetUri(String uri) {
		this.changesetUri = uri;
	}

	public String getSequenceNum() {
		return sequenceNum;
	}

	public void setSequenceNum(String sequenceNum) {
		this.sequenceNum = sequenceNum;
	}

	public Model getRemovedTriples() {
		return removedTriples;
	}

	public void setRemovedTriples(Model removedTriples) {
		this.removedTriples.removeAll();
		this.removedTriples.add(removedTriples);
	}

	public Model getAddedTriples() {
		return addedTriples;
	}

	public void setAddedTriples(Model addedTriples) {
		this.addedTriples.removeAll();
		this.addedTriples.add(addedTriples);
	}
	
}
