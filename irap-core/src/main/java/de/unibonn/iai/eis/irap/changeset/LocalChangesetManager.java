/**
 * 
 */
package de.unibonn.iai.eis.irap.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
	private static final String LAST_DOWNLOAD = "lastDownloadDate.dat";
	private String changesetFolder;
	 /**
     * list of distinct addresses/folders where changesets are published from source dataset 
     */
    private List<String> changesetAddresses= new ArrayList<String>();
    private InterestManager interestManager;
    
	private CMMethod cmMethod= CMMethod.ONETIME;
	
	public LocalChangesetManager(InterestManager interestManager) {
		this.interestManager = interestManager;
	}
	
	@Override
	public void start(CMMethod cmMethod) {
		this.cmMethod = cmMethod;	
		changesetAddresses = interestManager.getChangesetAddressURIs();
		for(String folder: changesetAddresses){
			File changes = new File(folder);
			if(changes.isDirectory()){
				List<String> changesets = Arrays.asList(changes.list());
				Collections.sort(changesets);
				
				String lastDownload = LastDownloadDateManager.getLastDownloadDate(folder+"/"+LAST_DOWNLOAD);
				ChangesetCounter currentCounter = new HourlyChangesetCounter(lastDownload);
		        currentCounter.incrementCount(); // move to next patch (this one is already applied)
		        int missing_urls=0;
		        while(true){
		        	String addedTriplesURL =  currentCounter.getFormattedFilePath() + EXTENSION_ADDED;
	            	String deletedTriplesURL =  currentCounter.getFormattedFilePath() + EXTENSION_REMOVED;
	            	 
	            	 if(!changesets.contains(addedTriplesURL) && !changesets.contains(deletedTriplesURL)){
	            		 missing_urls++;
	            		 if (missing_urls >= ERRORS_TO_ADVANCE) {
							// advance hour / day / month or year
							currentCounter.advanceCounter();
						}
	            		 continue;
	            	 }
	            	// URL works, reset missing URLs
	                 missing_urls = 0;
	                 
	                 Model addedTriples = ModelFactory.createDefaultModel();
	                 Model removedTriples = ModelFactory.createDefaultModel();
	                 
	                 if(changesets.contains(deletedTriplesURL)){
	                	 String deletedTriples = Utilities.decompressGZipFile(deletedTriplesURL);
	                	 
	                	// removedTriples = RDFDataMgr.loadModel(deletedTriples);
	                	 
	                	 Utilities.deleteFile(deletedTriplesURL);
	                 }
	                 
	                 if(changesets.contains(addedTriplesURL)){
	                	 String insertedTriples = Utilities.decompressGZipFile(addedTriplesURL);
	                	 
	                	// addedTriples = RDFDataMgr.loadModel(insertedTriples);
	                	 
	                	 Utilities.deleteFile(addedTriplesURL);
	                 }
	                 Changeset changeset = new Changeset(folder, removedTriples, addedTriples, currentCounter.getSequenceNumber());	        
	                 //Notify evaluator
		        }
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
		// TODO Auto-generated method stub
		return false;
	}

}
