/**
 * 
 */
package de.unibonn.iai.eis.irap.interest;

import java.util.List;

import de.unibonn.iai.eis.irap.model.Interest;
import de.unibonn.iai.eis.irap.model.Subscriber;

/**
 * @author Kemele M. Endris
 *
 */
public interface InterestManager {

	public final String BASE_URI = "http://eis.iai.uni-bonn.de/irap/";
	
	public List<Interest> getInterestEpressions(String subscriberId);
	
	public List<Subscriber> getSubscribers();
	
	public List<Subscriber> getSubscribers(String changesetAddress);
	
	public List<String> getChangesetAddressURIs();
	
	public String getChangesetDownloadFolder(String changesetAddress);
	
	public String getLastPublishedFilename(String changesetAddress);
	
	
	public boolean addInterest(String subscriberId, Interest interestExpression);
	
	public boolean subscribe(Subscriber subscriber);
	
}
