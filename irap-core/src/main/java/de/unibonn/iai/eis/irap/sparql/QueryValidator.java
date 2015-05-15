/**
 * 
 */
package de.unibonn.iai.eis.irap.sparql;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.core.TriplePath;

import de.unibonn.iai.eis.irap.interest.InterestExprGraph;

/**
 * @author Kemele M. Endris
 *
 */
public class QueryValidator {

	public static boolean isValidCombination(List<TriplePath> paths, List<TriplePath> optpaths){
		InterestExprGraph g = new InterestExprGraph();
		if (!optpaths.isEmpty()) {
			List<TriplePath> comb = new ArrayList<TriplePath>();
			comb.addAll(paths);
			comb.addAll(optpaths);
			Query combq = QueryDecomposer.toAskQuery(comb);			
			if (!g.isNonDisjoint(combq)) {
				return false;
			}
		}	
		return true;
	}
	
	public static boolean isAllVars(TriplePath tp){
		if(!tp.getSubject().isVariable())
			return false;
		if(!tp.getPredicate().isVariable())
			return false;
		if(!tp.getObject().isVariable())
			return false;
		return true;
	}
}
