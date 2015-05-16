/**
 * 
 */
package de.unibonn.iai.eis.irap.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.unibonn.iai.eis.irap.evaluation.InterestEvaluationManager;
import de.unibonn.iai.eis.irap.helper.Utilities;
import de.unibonn.iai.eis.irap.interest.InterestManager;
import de.unibonn.iai.eis.irap.model.CMMethod;
import de.unibonn.iai.eis.irap.model.Changeset;

/**
 * @author Kemele M. Endris
 *
 */
public class LocalChangesetManager implements ChangesetManager {

	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LocalChangesetManager.class);
	private boolean stop = false;
	private String changesetFolder;
	 /**
     * list of distinct addresses/folders where changesets are published from source dataset 
     */
    private List<String> changesetAddresses= new ArrayList<String>();
    private InterestManager interestManager;
    
	
	public LocalChangesetManager(InterestManager interestManager) {
		this.interestManager = interestManager;
	}
	
	@Override
	public void start(CMMethod cmMethod) {
		changesetAddresses = interestManager.getChangesetAddressURIs();
		for(String folder: changesetAddresses){
			File changes = new File(folder);
			if(changes.isDirectory()){
				List<String> changesets = Arrays.asList(changes.list());
				Collections.sort(changesets);
				
				String lastDownload = LastDownloadDateManager.getLastDownloadDate(LAST_DOWNLOAD);
				ChangesetCounter currentCounter = new HourlyChangesetCounter(lastDownload);
		        currentCounter.incrementCount(); // move to next patch (this one is already applied)
		        
		        int missing_urls = 0;
		        int processedFiles = 0;
		        //TODO: run this in a different thread. Otherwise other changesets folders will not be visited
		        while(true){
		        	if(stop){
		        		break;
		        	}
		        	if(processedFiles >= changesets.size() && CMMethod.ONETIME == cmMethod){
		        		break;
		        	}
		        	String addedTriplesURL =  folder+'/'+currentCounter.getFormattedFilePath() + EXTENSION_ADDED;
	            	String deletedTriplesURL =  folder+'/'+currentCounter.getFormattedFilePath() + EXTENSION_REMOVED;
	            	
	            	File addedchanges = new File(addedTriplesURL);
	            	File deletedchanges = new File(deletedTriplesURL);
	    			if(!addedchanges.exists() && !deletedchanges.exists()){
	    				 missing_urls++;
	            		 if (missing_urls >= ERRORS_TO_ADVANCE) {
							// advance hour / day / month or year
							currentCounter.advanceCounter();							
						}
	            		 if(processedFiles >= changesets.size() && CMMethod.ONETIME == cmMethod){
	 		        		break;
	 		        	}
	            		 continue;
	    			}	            	
	            	// changesets exists, reset missing URLs
	                 missing_urls = 0;
	                 
	                 Model addedTriples = ModelFactory.createDefaultModel();
	                 Model removedTriples = ModelFactory.createDefaultModel();
	                 
	                 if(addedchanges.exists()){
	                	 String deletedTriples = Utilities.decompressGZipFile(deletedTriplesURL);
	                	 
	                	 removedTriples = RDFDataMgr.loadModel(deletedTriples);
	                	 processedFiles++;
	                	 Utilities.deleteFile(deletedTriplesURL);
	                 }
	                 
	                 if(deletedchanges.exists()){
	                	 String insertedTriples = Utilities.decompressGZipFile(addedTriplesURL);
	                	 
	                	 addedTriples = RDFDataMgr.loadModel(insertedTriples);
	                	 processedFiles++;
	                	 Utilities.deleteFile(addedTriplesURL);
	                 }
	                 Changeset changeset = new Changeset(folder, removedTriples, addedTriples, currentCounter.getSequenceNumber());	        
	                 //Notify evaluator
	                 logger.info("Notifying interest evaluation manager by sending changeset triples .....");
	                 InterestEvaluationManager eval= new InterestEvaluationManager(interestManager, changeset);
	                 eval.begin();
	                 logger.info("Updating last processed changeset data ...");
	                 // save last processed date
	                 LastDownloadDateManager.writeLastDownloadDate(LAST_DOWNLOAD, currentCounter.toString());
	                 logger.info("Incrementing changeseet counter .. .");
	                 //increment to next counter
	                 currentCounter.incrementCount();
		        }
			}
		}
	}

	@Override
	public boolean end() {
		 this.stop = true;
			return true;
	}

	@Override
	public boolean setChangesetDownloadFolder(String folderurl) {
		if(folderurl != null && new File(folderurl).isDirectory()){
			this.changesetFolder = folderurl;
			return true;
		}		
		return false;
	}

	@Override
	public void refreshChangesetAddress() {
		
	}

	@Override
	public boolean deleteChangeset(String changesetId) {
		if(Utilities.deleteFile(changesetFolder+"/"+ changesetId+EXTENSION_ADDED_NT) && Utilities.deleteFile(changesetFolder+"/"+ changesetId+EXTENSION_REMOVED_NT)){
			return true;
		}
		return false;
	}

}
