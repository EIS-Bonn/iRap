/**
 * 
 */
package de.unibonn.iai.eis.irap.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;

import de.unibonn.iai.eis.irap.sparql.QueryDecomposer;
import de.unibonn.iai.eis.irap.sparql.QueryPatternExtractor;

/**
 * @author Kemele M. Endris
 *
 */
public class Interest {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Interest.class);
	
	/**
	 * unique interest identifier
	 */
	private String id;
	/**
	 * update server address from which changesets will be  published
	 */
	private String changesetBaseURI;
	/**
	 * location of changeset publication type as LOCAL folder or REMOTE
	 */
	private String changesetPublicationType;
	/**
	 * endpoint of a source dataset
	 * This will be used as LIVE_ON_SOURCE PI(read-only == potentially interesting dataset)
	 */
	private String sourceEndpoint;
	
	private String lastPublishedFilename;
	
	/**
	 * interest expression basic graph pattern (BGP)
	 */
	private List<TriplePath> bgp = new ArrayList<TriplePath>();
	/**
	 * interest expression optional graph pattern (OGP)
	 */
	private List<TriplePath> ogp= new ArrayList<TriplePath>();
	/**
	 * filter expressions for BGP of interest expression
	 */
	private List<ElementFilter> filter = new ArrayList<ElementFilter>();
	
	public Interest(String id){
		this.id = id;
	}	
	
	public Interest(String id, String changesetUri, String query){
		this.id = id;
		this.changesetBaseURI = changesetUri;
		Query q = QueryFactory.create(query);
		this.bgp = QueryPatternExtractor.getBGPTriplePaths(q);
		this.ogp = QueryPatternExtractor.getOptionalTriplePaths(q);
		this.filter = QueryPatternExtractor.getFilters(q);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getChangesetBaseURI() {
		return changesetBaseURI;
	}
	public void setChangesetBaseURI(String changesetBaseURI) {
		this.changesetBaseURI = changesetBaseURI;
	}
	public String getSourceEndpoint() {
		return sourceEndpoint;
	}
	public void setSourceEndpoint(String sourceEndpoint) {
		this.sourceEndpoint = sourceEndpoint;
	}
	
	public List<TriplePath> getBgp() {
		return bgp;
	}
	public void setBgp(String bgpStr){
		String q = "SELECT * WHERE{ "+ bgpStr+"}";
		try{
			Query query = QueryFactory.create(q);
			this.bgp = QueryPatternExtractor.getBGPTriplePaths(query);
			this.filter = QueryPatternExtractor.getFilters(query);
		}catch(Exception e){
			logger.error("Invalid BGP expression:\" " + bgpStr +"\"", e);
		}
	}	
	
	public List<TriplePath> getOgp() {
		return ogp;
	}
	
	public void setOgp(String ogpStr){
		String q = "SELECT * WHERE{ "+ ogpStr+"}";
		try{
			this.ogp = QueryPatternExtractor.getBGPTriplePaths(QueryFactory.create(q));
		}catch(Exception e){
			logger.error("Invalid OGP expression:\" " + ogpStr +"\"", e);
		}
	}
	
	public List<ElementFilter> getFilter() {
		return filter;
	}
	
	public Query getInterestQuery() {		
		return QueryDecomposer.toConstructQuery(bgp, ogp, filter);
	}

	
	/**
	 * @return the changesetLocaion
	 */
	public String getChangesetPublicationType() {
		return changesetPublicationType;
	}

	/**
	 * @param changesetLocaion the changesetLocaion to set
	 */
	public void setChangesetPublicationType(String changesetPublicationType) {
		this.changesetPublicationType = changesetPublicationType;
	}

	/**
	 * @return the lastPublishedFilename
	 */
	public String getLastPublishedFilename() {
		return lastPublishedFilename;
	}

	/**
	 * @param lastPublishedFilename the lastPublishedFilename to set
	 */
	public void setLastPublishedFilename(String lastPublishedFilename) {
		this.lastPublishedFilename = lastPublishedFilename;
	}
	
	
	
}
