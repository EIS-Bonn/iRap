/**
 * 
 */
package de.unibonn.iai.eis.irap.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.unibonn.iai.eis.irap.evaluation.InterestEvaluatorManager;
import de.unibonn.iai.eis.irap.helper.Utilities;
import de.unibonn.iai.eis.irap.interest.InterestManager;
import de.unibonn.iai.eis.irap.model.CMMethod;
import de.unibonn.iai.eis.irap.model.Changeset;

/**
 * @author Kemele M. Endris
 *
 */
public class RemoteChangesetManager implements ChangesetManager {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(RemoteChangesetManager.class);
	
	private static final String LAST_DOWNLOAD = "lastDownloadDate.dat";
	
    private static final String EXTENSION_ADDED =  ".added.nt.gz";
    private static final String EXTENSION_REMOVED =  ".removed.nt.gz";
   // private static final String EXTENSION_CLEAR = ".clear.nt.gz";
   // private static final String EXTENSION_REINSERT = ".reinserted.nt.gz ";
    
    private static final String EXTENSION_ADDED_NT =  ".added.nt";
    private static final String EXTENSION_REMOVED_NT =  ".removed.nt";
    
    /**
     * list of distinct addresses/folders where changesets are published from source dataset 
     */
    private List<String> changesetAddresses= new ArrayList<String>();
    /**
     * folder to save the extracted changeset files
     */
    private String changesetDownloadFolder = "./changesets/";
    
    private InterestManager interestManager;
    
    public RemoteChangesetManager(InterestManager interestManager) {
    	this.interestManager = interestManager;
	}
    public RemoteChangesetManager(String changesetDownload, InterestManager interestManager) {
		this.changesetDownloadFolder = changesetDownload;
		this.interestManager = interestManager;
	}
    
	@Override
	public void start(CMMethod cmMethod) {
		changesetAddresses = interestManager.getChangesetAddressURIs();
		for(String changesetAddress: changesetAddresses){
			
			String lastDownload = LastDownloadDateManager.getLastDownloadDate(LAST_DOWNLOAD);
			ChangesetCounter currentCounter = new HourlyChangesetCounter(lastDownload);
	        //currentCounter.incrementCount(); // move to next patch (this one is already applied)
	        
			
	        
	        int missing_urls=0;
	        // Download last published file from server
	        String lastPublishedFilename = interestManager.getLastPublishedFilename(changesetAddress);
	        String lastPublishFileRemote = changesetAddress + lastPublishedFilename;
	        Utilities.downloadFile(lastPublishFileRemote, changesetDownloadFolder);
	        String lastPublishFileLocal = changesetDownloadFolder + lastPublishedFilename;
	        ChangesetCounter remoteCounter = new HourlyChangesetCounter(Utilities.getFileAsString(lastPublishFileLocal));
	        
	        while(true){
	        	
	        	 // when we are about to go beyond the remote published file
	            if (currentCounter.compareTo(remoteCounter) > 0) {

	                /**
	                 * TODO between the app started (or last fetch of last published file)
	                 * the remote counter may be advanced but we don't take this into consideration here
	                 * probably should re-download in a temp counter, check if different and continue without sleep
	                 */
	            	
	                // in case of onetime run, exit
	                if (cmMethod.equals(CMMethod.ONETIME)) {
	                    logger.info("Finished the One-Time update, exiting...");
	                    break;
	                }

	                // sleep + download new file & continue
	                logger.info("Up-to-date with last published changeset, sleeping for a while ;)");
	                try {
	                    Thread.sleep(300000l);
	                } catch (InterruptedException e) {
	                    logger.warn("Could not sleep...", e);
	                }
	                
	                // code duplication
	                Utilities.downloadFile(lastPublishFileRemote, changesetDownloadFolder);
	                remoteCounter = new HourlyChangesetCounter(Utilities.getFileAsString(lastPublishFileLocal));
	                logger.info(remoteCounter.getSequenceNumber());
	                //now we have an updated remote counter so next time this block will not run (if the updates are running)
	                continue;
	            }
	            
	        	String addedTriplesURL =  changesetAddress + currentCounter.getFormattedFilePath() + EXTENSION_ADDED;
            	String deletedTriplesURL = changesetAddress +  currentCounter.getFormattedFilePath() + EXTENSION_REMOVED;
            	logger.info(" Downloading changeset number: "+ currentCounter.getFormattedFilePath());
            	//Download and decompress the file of deleted triples
                String addedCompressedDownloadedFile = Utilities.downloadFile(addedTriplesURL, changesetDownloadFolder);
                String deletedCompressedDownloadedFile = Utilities.downloadFile(deletedTriplesURL, changesetDownloadFolder);
               
                // Check for errors before proceeding
                if (addedCompressedDownloadedFile == null && deletedCompressedDownloadedFile == null) {
                    missing_urls++;
                    logger.info( currentCounter.getFormattedFilePath() + " Changeset not found!");
                    if (missing_urls >= ERRORS_TO_ADVANCE) {
                        // advance hour / day / month or year
                        currentCounter.advanceCounter();
                        logger.info("Moving to next hour: "+ currentCounter.getFormattedFilePath() );
                    }
                    continue;
                }
                // URL works, reset missing URLs
                missing_urls = 0;
                
                Model addedTriples = ModelFactory.createDefaultModel();
                Model removedTriples = ModelFactory.createDefaultModel();
                logger.info(" Reading triples from downloaded files");
                if (deletedCompressedDownloadedFile != null) {
                    String file = Utilities.decompressGZipFile(deletedCompressedDownloadedFile);
                    removedTriples = RDFDataMgr.loadModel(file);
                    logger.info(" Deleting downloaded file: "+ file);
                    Utilities.deleteFile(file);
                }

                if (addedCompressedDownloadedFile != null) {
                    String file = Utilities.decompressGZipFile(addedCompressedDownloadedFile);
                    addedTriples = RDFDataMgr.loadModel(file);
                    logger.info(" Deleting downloaded file: "+ file);
                    Utilities.deleteFile(file);
                }
                
                Changeset changeset = new Changeset(changesetAddress, removedTriples, addedTriples, currentCounter.getSequenceNumber());	        
                //Notify evaluator
                logger.info("Notifying interest evaluation manager by sending changeset triples .....");
                InterestEvaluatorManager eval= new InterestEvaluatorManager(interestManager, changeset);
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
	@Override
	public boolean end() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean setChangesetDownloadFolder(String folderurl) {
		File file = new File(folderurl);
		if(!file.isDirectory()){
			logger.error("Invalid changeset download folder provided!");
			return false;
		}
		else{
			this.changesetDownloadFolder = folderurl;
			return true;
		}
	}
	@Override
	public void refreshChangesetAddress() {
		changesetAddresses = interestManager.getChangesetAddressURIs();		
	}
	
	@Override
	public boolean deleteChangeset(String changesetId) {
		if(Utilities.deleteFile(changesetDownloadFolder+"/"+ changesetId+EXTENSION_ADDED_NT) && Utilities.deleteFile(changesetDownloadFolder+"/"+ changesetId+EXTENSION_REMOVED_NT)){
			return true;
		}
		return false;
	}
    	
	 
}
