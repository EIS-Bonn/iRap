package de.unibonn.iai.eis.irap;

import org.slf4j.Logger;

import de.unibonn.iai.eis.irap.changeset.ChangesetManager;
import de.unibonn.iai.eis.irap.changeset.RemoteChangesetManager;
import de.unibonn.iai.eis.irap.interest.FileBasedInterestManager;
import de.unibonn.iai.eis.irap.interest.InterestManager;
import de.unibonn.iai.eis.irap.model.CMMethod;

/**
 * main
 *
 */
public class Main 
{
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);
	
    public static void main( String[] args )
    {
    	
    	if(args == null || args.length == 0){
    		logger.info("Usage: $java -jar irap.jar <InterestExpression.nt[.ttl/.rdf]>");
    		return;
    	}
    	
    	String filename = args[0];
    	
        logger.info("reding interest expression started");
        
        InterestManager imgr  = new FileBasedInterestManager(filename); //"interest.ttl"
        
        ChangesetManager cmgr = new RemoteChangesetManager(imgr);
        
        cmgr.start(CMMethod.ENDLESS);        
    }
}
