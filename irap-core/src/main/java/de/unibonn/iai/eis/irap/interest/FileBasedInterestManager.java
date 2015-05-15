/**
 * 
 */
package de.unibonn.iai.eis.irap.interest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.irap.model.Interest;
import de.unibonn.iai.eis.irap.model.Subscriber;
import de.unibonn.iai.eis.irap.sparql.SPARQLExecutor;

/**
 * @author Kemele M. Endris
 *
 */
public class FileBasedInterestManager implements InterestManager {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(FileBasedInterestManager.class);
	private String filename;
	List<Subscriber> subscribers = new ArrayList<Subscriber>();
	private Model interestExpr = ModelFactory.createDefaultModel();
	
	public FileBasedInterestManager(String filename) {
		this.filename = filename;
		readInterest();
	}
	
	@Override
	public List<Interest> getInterestEpressions(String subscriberId) {
		for(Subscriber s: subscribers){
			if(s.getId().equals(subscriberId)){
				return  s.getInterestExpressions();
			}
		}
		return null;
	}
	
	@Override
	public List<Subscriber> getSubscribers() {
		return subscribers;
	}
	
	@Override
	public List<String> getChangesetAddressURIs() {
		List<String> uris = new ArrayList<String>();
		for(Subscriber s: subscribers){
			for(Interest i: s.getInterestExpressions()){
				if(!uris.contains(i.getChangesetBaseURI()))
					uris.add(i.getChangesetBaseURI());
			}
		}
		return uris;
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
	
	@Override
	public String getLastPublishedFilename(String changesetAddress) {
		String name="";
		
		for(Subscriber s: subscribers){
			for(Interest i: s.getInterestExpressions()){
				if(i.getChangesetBaseURI().equals(changesetAddress)){
					return i.getLastPublishedFilename();
				}
			}
		}
		return name;
	}
	
	private void readInterest(){
		try{
			
			interestExpr = RDFDataMgr.loadModel(filename);
			//interestExpr.write(System.out, "N-TRIPLE");
			Query query = getSubscribersQuery();
			
			ResultSet rs = SPARQLExecutor.executeSelect(null, query);
			Map<String, Subscriber> results = new HashMap<String, Subscriber>();
			while(rs.hasNext()){
				QuerySolution s = rs.nextSolution();
				RDFNode subscriberRes = s.get("subscriber");	
				
				Subscriber subscriber;
				if(!results.containsKey(subscriberRes.toString())){
					subscriber = new Subscriber(subscriberRes.toString());
					
					Literal piStoreBaseURI = s.getLiteral("piStoreBaseURI");						
					Literal piStorageType = s.getLiteral("piStorageType");
					Literal piTrackingMethod = s.getLiteral("piTrackingMethod");	
					
					Literal targetType = s.getLiteral("targetType");						
					Literal targetEndpoint = s.getLiteral("targetEndpoint");
					Literal targetUpdateURI = s.getLiteral("targetUpdateURI");
					
					subscriber.setPiMethod(piTrackingMethod.toString());
					subscriber.setPiType(piStorageType.toString());
					subscriber.setPiStoreBaseURI(piStoreBaseURI.toString());

					subscriber.setTargetType(targetType.toString());
					subscriber.setTargetEndpoint(targetEndpoint.toString());
					subscriber.setTargetUpdateURI(targetUpdateURI.toString());

					results.put(subscriberRes.toString(), subscriber);
				}else{
					subscriber = results.get(subscriberRes.toString());
				}
				
				RDFNode interestId = s.get("interest");
				Literal changesetPublicationType = s.getLiteral("changesetPublicationType");
				Literal sourceEndpoint = s.getLiteral("sourceEndpoint");						
				Literal changesetBaseURI = s.getLiteral("changesetBaseURI");
				
				Literal lastPublishedFilename = s.getLiteral("lastPublishedFilename");						
				
				Literal bgp = s.getLiteral("bgp");						
				Literal ogp = s.getLiteral("ogp");
				
				Interest interest = new Interest(interestId.toString());
				interest.setBgp(bgp.toString());
				interest.setOgp(ogp.toString());
				interest.setChangesetBaseURI(changesetBaseURI.toString());
				interest.setSourceEndpoint(sourceEndpoint.toString());
				interest.setChangesetPublicationType(changesetPublicationType.toString());
				interest.setLastPublishedFilename(lastPublishedFilename.toString());
				
				subscriber.addInterest(interest);
			}
			
			for(String s: results.keySet()){
				subscribers.add(results.get(s));
			}
			logger.info(subscribers.size() + " Subscriber(s) found!");
		}catch(Exception e){
			e.printStackTrace();
			logger.info("INVALID interest expression file!");
			System.exit(1);
		}
	}

	private Query getSubscribersQuery(){
		String qstr = SPARQLExecutor.prefixes() 
				+ "  PREFIX irap: <http://eis.iai.uni-bonn.de/irap/ontology/> "
				+ " SELECT * "
				+ "FROM <"+ filename +"> "
				+ "  WHERE { "
				+ " ?subscriber  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://eis.iai.uni-bonn.de/irap/ontology/Subscriber>; "
				+ "              <http://eis.iai.uni-bonn.de/irap/ontology/id> ?subscriberId;   "
				+ "              <http://eis.iai.uni-bonn.de/irap/ontology/piStoreBaseURI> ?piStoreBaseURI; "
				+ "              <http://eis.iai.uni-bonn.de/irap/ontology/piStorageType>  ?piStorageType; "
				+ "              <http://eis.iai.uni-bonn.de/irap/ontology/targetType>     ?targetType;  "
				+ "              <http://eis.iai.uni-bonn.de/irap/ontology/targetEndpoint> ?targetEndpoint; "
				+ "              <http://eis.iai.uni-bonn.de/irap/ontology/piTrackingMethod> ?piTrackingMethod;"
				+ "              <http://eis.iai.uni-bonn.de/irap/ontology/targetUpdateURI> ?targetUpdateURI. "				
				+ "  ?interest  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://eis.iai.uni-bonn.de/irap/ontology/Interest>; "
				+ "             <http://eis.iai.uni-bonn.de/irap/ontology/id>  ?interestId; "
				+ "             <http://eis.iai.uni-bonn.de/irap/resource/sourceEndpoint> ?sourceEndpoint; "
				+ "             <http://eis.iai.uni-bonn.de/irap/resource/subscriber> ?subscriber; "
				+ "             <http://eis.iai.uni-bonn.de/irap/resource/changesetPublicationType> ?changesetPublicationType; "
				+ "             <http://eis.iai.uni-bonn.de/irap/ontology/changesetBaseURI> ?changesetBaseURI;   "
				+ "             <http://eis.iai.uni-bonn.de/irap/resource/lastPublishedFilename>  ?lastPublishedFilename; "
				+ "             <http://eis.iai.uni-bonn.de/irap/ontology/bgp> ?bgp; "
				+ "             <http://eis.iai.uni-bonn.de/irap/ontology/ogp> ?ogp."				
				+ " } ";
		Query query = QueryFactory.create(qstr);
		return query;
	}

	@Override
	public List<Subscriber> getSubscribers(String changesetAddress) {
		List<Subscriber> subs = new ArrayList<Subscriber>();
		for(Subscriber s: subscribers){
			Subscriber s1 = new Subscriber(s.getId());
			for(Interest i: s.getInterestExpressions()){
				if(i.getChangesetBaseURI().equals(changesetAddress)){
					s1.getInterestExpressions().add(i);
				}
			}
			if(!s1.getInterestExpressions().isEmpty()){
				s1.setPiMethod(s.getPiMethod());
				s1.setPiStoreBaseURI(s.getPiStoreBaseURI());
				s1.setPiType(s.getPiType());
				s1.setTargetEndpoint(s.getTargetEndpoint());
				s1.setTargetType(s.getTargetType());
				s1.setTargetUpdateURI(s.getTargetUpdateURI());
				subs.add(s1);
			}
		}
		return subs;
	}
	
}
