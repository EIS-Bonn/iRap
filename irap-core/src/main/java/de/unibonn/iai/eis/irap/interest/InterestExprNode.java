/**
 * 
 */
package de.unibonn.iai.eis.irap.interest;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.TriplePath;


/**
 * @author keme686
 *
 */
public class InterestExprNode {

	public static enum InterestExprNodeType { ROOT, SUBTREE_ROOT, LEAF, CONNECTING_LEAF, DISCONNECTED};
	
	public Node node;
	public InterestExprNodeType type;
	
	public final List<TriplePath> triplePath= new ArrayList<TriplePath>();
	
	public final List<Node> incomingNodes = new ArrayList<Node>();
	
	public final List<Node> outgoingNodes = new ArrayList<Node>();
	
	public InterestExprNode(Node node) {
		this.node = node;
	}
}
