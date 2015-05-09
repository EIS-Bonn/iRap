/**
 * 
 */
package de.unibonn.iai.eis.irap.interest;

import java.util.List;

import org.slf4j.Logger;

import de.unibonn.iai.eis.irap.model.Interest;
import de.unibonn.iai.eis.irap.model.Subscriber;

/**
 * @author Kemele M. Endris
 *
 */
public class FileBasedInterestManager implements InterestManager {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(FileBasedInterestManager.class);
	private String filename;
	
	public FileBasedInterestManager(String filename) {
		this.filename = filename;
	}
	
	@Override
	public List<Interest> getInterestEpressions(String subscriberId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Subscriber> getSubscribers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getChangesetAddressURIs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addInterest(String subscriberId, Interest interestExpression) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean subscribe(Subscriber subscriber) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getChangesetDownloadFolder(String changesetAddress) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
