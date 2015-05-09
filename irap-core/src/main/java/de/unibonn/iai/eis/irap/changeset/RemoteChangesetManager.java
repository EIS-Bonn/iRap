/**
 * 
 */
package de.unibonn.iai.eis.irap.changeset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import de.unibonn.iai.eis.irap.helper.Utilities;
import de.unibonn.iai.eis.irap.interest.InterestManager;
import de.unibonn.iai.eis.irap.model.CMMethod;

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
    private String changesetDownloadFolder = "./changesets";
    
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
