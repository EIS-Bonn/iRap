package de.unibonn.iai.eis.irap;

import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;

/**
 * Hello world!
 *
 */
public class Main 
{
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(Main.class);
	
    public static void main( String[] args )
    {
    	BasicConfigurator.configure();
        logger.info("Logging started");
    }
}
