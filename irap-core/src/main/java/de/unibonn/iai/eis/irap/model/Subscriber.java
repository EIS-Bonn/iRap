/**
 * 
 */
package de.unibonn.iai.eis.irap.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kemele M. Endris
 *
 */
public class Subscriber {

	/**
	 * unique interest identifier
	 */
	private String id;
	
	/**
	 * target dataset type -either
	 * <ul>
	 *  <li>DataStoreType.TDB , or</li>
	 *  <li>DataStoreType.SPARQL_ENDPOINT</li>
	 * </ul>
	 */
	private DataStoreType targetType;
	
	/**
	 * if target dataset type is SPARQL_ENDPOINT: target dataset endpoint or URI to TDB folder
	 * 
	 */
	private String targetEndpoint; 
	
	/**
	 * if target dataset type is SPARQL_ENDPOINT: target dataset update endpoint or URI to TDB folder 
	 */
	private String targetUpdateURI;
	
	/**
	 * potentially interesting triples tracking method
	 * either: LOCAL or ON_THE_FLY
	 */
	private PITrakingMethod piMethod;
	/**
	 * potentilly interesting datastore type
	 * either: TDB, SPARQL_ENDPOINT, VIRTUOSO_JDBC
	 */
	private DataStoreType piType;
	/**
	 * potentially interesting dataset endpoint/tdb uri
	 */
	private String piStoreBaseURI;
	//private String piGraphURI;
	
	private List<Interest> interestExpressions= new ArrayList<Interest>();
	
	
	public Subscriber(String id) {
		this.id = id;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * @return the targetType
	 */
	public DataStoreType getTargetType() {
		return targetType;
	}


	/**
	 * @param targetType the targetType to set
	 */
	public void setTargetType(DataStoreType targetType) {
		this.targetType = targetType;
	}

	/**
	 * @param targetType the targetType to set
	 */
	public void setTargetType(String targetType) {
		if("VIRTUOSO_JDBC".equals(targetType)){
			this.targetType = DataStoreType.VIRTUOSO_JDBC;
		}else if("SPARQL_ENDPOINT".equals(targetType)){
			this.targetType = DataStoreType.SPARQL_ENDPOINT;
		}else {
			this.targetType = DataStoreType.TDB;
		}
	}

	/**
	 * @return the targetEndpoint
	 */
	public String getTargetEndpoint() {
		return targetEndpoint;
	}


	/**
	 * @param targetEndpoint the targetEndpoint to set
	 */
	public void setTargetEndpoint(String targetEndpoint) {
		this.targetEndpoint = targetEndpoint;
	}


	/**
	 * @return the targetUpdateURI
	 */
	public String getTargetUpdateURI() {
		return targetUpdateURI;
	}


	/**
	 * @param targetUpdateURI the targetUpdateURI to set
	 */
	public void setTargetUpdateURI(String targetUpdateURI) {
		this.targetUpdateURI = targetUpdateURI;
	}


	/**
	 * @return the piMethod
	 */
	public PITrakingMethod getPiMethod() {
		return piMethod;
	}


	/**
	 * @param piMethod the piMethod to set
	 */
	public void setPiMethod(PITrakingMethod piMethod) {
		this.piMethod = piMethod;
	}

	/**
	 * @param piMethod the piMethod to set
	 */
	public void setPiMethod(String piMethod) {
		if("LIVE_ON_SOURCE".equals(piMethod)){
			this.piMethod = PITrakingMethod.LIVE_ON_SOURCE;
		}else{
			this.piMethod = PITrakingMethod.LOCAL;
		}
	}

	/**
	 * @return the piType
	 */
	public DataStoreType getPiType() {
		return piType;
	}


	/**
	 * @param piType the piType to set
	 */
	public void setPiType(DataStoreType piType) {
		this.piType = piType;
	}


	/**
	 * @param piType the piType to set
	 */
	public void setPiType(String piType) {
		if("VIRTUOSO_JDBC".equals(piType)){
			this.piType = DataStoreType.VIRTUOSO_JDBC;
		}else if("SPARQL_ENDPOINT".equals(piType)){
			this.piType = DataStoreType.SPARQL_ENDPOINT;
		}else {
			this.piType = DataStoreType.TDB;
		}
	}
	
	/**
	 * @return the piStoreBaseURI
	 */
	public String getPiStoreBaseURI() {
		return piStoreBaseURI;
	}


	/**
	 * @param piStoreBaseURI the piStoreBaseURI to set
	 */
	public void setPiStoreBaseURI(String piStoreBaseURI) {
		this.piStoreBaseURI = piStoreBaseURI;
	}


	/**
	 * @return the interestExpressions
	 */
	public List<Interest> getInterestExpressions() {
		return interestExpressions;
	}


	/**
	 * @param interestExpressions the interestExpressions to set
	 */
	public void setInterestExpressions(List<Interest> interestExpressions) {
		this.interestExpressions = interestExpressions;
	}
	
	public void addInterest(Interest interest){
		//TODO: check if interest exists or not. In addition, interests might have common triple pattern with other interests. (might be important if this afects other interests)
		this.interestExpressions.add(interest);
	}
	
}
