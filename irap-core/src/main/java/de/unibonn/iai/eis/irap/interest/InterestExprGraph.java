/**
 * 
 */
package de.unibonn.iai.eis.irap.interest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import java.util.Queue;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;

import de.unibonn.iai.eis.irap.interest.InterestExprNode.InterestExprNodeType;
import de.unibonn.iai.eis.irap.sparql.QueryPatternExtractor;

/**
 * @author keme686
 *
 */
public class InterestExprGraph {

	public int size;
	private Map<Node, InterestExprNode> nodesMap = new HashMap<Node, InterestExprNode>();
	
	/**
	 * add node m to outgoing edge of n from triple pattern path
	 * 
	 * @param n
	 * @param m
	 * @param path
	 */
	public void addEdge(Node n, Node m, TriplePath path){
	
		if(nodesMap.containsKey(n)){
			nodesMap.get(n).outgoingNodes.add(m);
			nodesMap.get(n).triplePath.add(path);
			nodesMap.get(n).type = computeType(n);			
		}else{
			InterestExprNode node = new InterestExprNode(n);
			node.outgoingNodes.add(m);
			node.triplePath.add(path);
			node.type = InterestExprNodeType.ROOT;
			nodesMap.put(n, node);
		}		
		if(nodesMap.containsKey(m)){
			nodesMap.get(m).incomingNodes.add(n);
			nodesMap.get(m).type = computeType(m);
		}else{
			InterestExprNode node = new InterestExprNode(m);
			node.incomingNodes.add(n);
			node.type = InterestExprNodeType.LEAF;
			nodesMap.put(m, node);
		}
	}
	/**
	 * check if there is a connection (outgoing Edge) from node n to node m
	 * 
	 * @param n subject node
	 * @param m object node
	 * @return
	 */
	public boolean hasEdge(Node n, Node m){
		if(nodesMap.containsKey(n)){
			return nodesMap.get(n).outgoingNodes.contains(m);
		}else return false;
	}
	/**
	 * extract root nodes of a graph pattern
	 * 
	 * @return
	 */
	public List<Node> getRootNodes(){
		List<Node> roots = new ArrayList<Node>();
		for(Node n: nodesMap.keySet()){
			if(getNodeType(n) == InterestExprNodeType.ROOT)
				roots.add(n);
		}
		
		return roots;
	}
	public List<Node> getSubtreeRoots(){
		List<Node> roots = new ArrayList<Node>();
		for(Node n: nodesMap.keySet()){
			if(getNodeType(n) == InterestExprNodeType.SUBTREE_ROOT)
				roots.add(n);
		}
		
		return roots;
	}
	public Map<Node, InterestExprNode> getTrees(){
		Map<Node, InterestExprNode> trees = new HashMap<Node, InterestExprNode>();
		for(Node n: nodesMap.keySet()){
			if(getNodeType(n) == InterestExprNodeType.ROOT || getNodeType(n) == InterestExprNodeType.SUBTREE_ROOT)
				trees.put(n, nodesMap.get(n));
		}
		return trees;
	}
	public List<Node> getConnectionLeafs(){
		List<Node> roots = new ArrayList<Node>();
		for(Node n: nodesMap.keySet()){
			if(getNodeType(n) == InterestExprNodeType.CONNECTING_LEAF)
				roots.add(n);
		}		
		return roots;
	}
	
	public List<Node> getNonConnectionLeafs(){
		List<Node> roots = new ArrayList<Node>();
		for(Node n: nodesMap.keySet()){
			if(getNodeType(n) == InterestExprNodeType.LEAF)
				roots.add(n);
		}
		
		return roots;
	}
	
	public List<Node> getAllLeafs(){
		List<Node> roots = new ArrayList<Node>();
		for(Node n: nodesMap.keySet()){
			if(getNodeType(n) == InterestExprNodeType.LEAF || getNodeType(n) == InterestExprNodeType.CONNECTING_LEAF  )
				roots.add(n);
		}
		
		return roots;
	}
	public List<Node> getOutgoingNodes(Node n){
		if(nodesMap.containsKey(n)){
			return nodesMap.get(n).outgoingNodes;
		}
		return new ArrayList<Node>();
	}
	
	public List<Node> getIncomingNodes(Node n){
		if(nodesMap.containsKey(n)){
			return nodesMap.get(n).incomingNodes;
		}
		return new ArrayList<Node>();
	}
	/**
	 * return interest expression node type
	 * 
	 * @param n
	 * @return
	 */
	public InterestExprNodeType getNodeType(Node n){
		if(!nodesMap.containsKey(n))
			return InterestExprNodeType.DISCONNECTED;
		return nodesMap.get(n).type;
	}
	
	/**
	 * computes node type of an interest expression
	 * @param n
	 * @return
	 */
	private InterestExprNodeType computeType(Node n){
		if(!nodesMap.containsKey(n))
			return InterestExprNodeType.DISCONNECTED;
		InterestExprNode node = nodesMap.get(n);
		if(node.incomingNodes.isEmpty() && !node.outgoingNodes.isEmpty())
			return InterestExprNodeType.ROOT;
		else if(!node.incomingNodes.isEmpty() && !node.outgoingNodes.isEmpty())
			return InterestExprNodeType.SUBTREE_ROOT;
		else if(!node.incomingNodes.isEmpty() && node.outgoingNodes.isEmpty()){
			if(node.incomingNodes.size()>1)
				return InterestExprNodeType.CONNECTING_LEAF;
			return InterestExprNodeType.LEAF;
		}else
			return InterestExprNodeType.DISCONNECTED;
	}
	
	public  boolean isNonDisjoint(){
		List<Node> roots = getRootNodes();
		if(!roots.isEmpty())
			return isValid(roots.get(0));
		else{
			roots = getSubtreeRoots();
			if(!roots.isEmpty())
				return isValid(roots.get(0));
		}
		return false;
	}
	
	public boolean isNonDisjoint(Query query){
		createGraph(query);
		return isNonDisjoint();
	}
	
	private boolean isValid(Node root){
		Map<Node, Boolean> seen = new HashMap<Node, Boolean>();
		Queue<Node> q = new SLList<Node>();
		q.add(root);
		while(!q.isEmpty()){
			Node n= q.remove();
			for(Node m: this.getOutgoingNodes(n)){
				if(!seen.containsKey(m) || !seen.get(m)){
					q.add(m);
					seen.put(m, true);
				}
			}
			for(Node m: this.getIncomingNodes(n)){
				if(!seen.containsKey(m) || !seen.get(m)){
					q.add(m);
					seen.put(m, true);
				}
			}
		}
		for(Node n: nodesMap.keySet()){
			if(!seen.containsKey(n) || !seen.get(n).booleanValue()){
				return false;
			}
		}
		return true;
	}

	
	public void createGraph(Query query){
		nodesMap.clear();
		List<TriplePath> paths = QueryPatternExtractor.getBGPTriplePaths(query);
		for(TriplePath tp: paths){
			Node subject = tp.getSubject();
			Node object = tp.getObject();
			if(this.hasEdge(subject, object))
				continue;
			this.addEdge(subject, object, tp);
		}
	}
	/*public static void main(String a[]){
		String que = "SELECT *  WHERE { "
				+" ?person <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person>. "
				+" ?person <http://xmlns.com/foaf/0.1/name> ?name. "
				+" ?person <http://xmlns.com/foaf/0.1/givenName> ?givenName. "
				+" ?person <http://xmlns.com/foaf/0.1/surname> ?surname.  "
				+" ?person <http://dbpedia.org/property/almaMater>  ?univ."
				+" ?univ   <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  ?unitype."
				+" ?person <http://purl.org/dc/elements/1.1/description> ?description. "
				+" ?person <http://dbpedia.org/ontology/abstract> ?abstract. "
				+" ?person <http://purl.org/dc/terms/subject> ?subject. "
				+" ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label. "
				+" ?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. "
				+" ?type  <http://www.w3.org/2000/01/rdf-schema#label> ?typelabel.}";
		String query = "SELECT * WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o. ?d ?s ?m. }";
		Query q = QueryFactory.create(query);
		InterestExprGraph g = new InterestExprGraph();
		g.createGraph(q);
		System.out.println(g.isNonDisjoint());
	}*/
}
