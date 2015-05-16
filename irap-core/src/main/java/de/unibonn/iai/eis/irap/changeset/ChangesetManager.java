/**
 * 
 */
package de.unibonn.iai.eis.irap.changeset;

import de.unibonn.iai.eis.irap.model.CMMethod;

/**
 * @author Kemele M. Endris
 *
 */
public interface ChangesetManager {
	
	public static final String LAST_DOWNLOAD = "lastDownloadDate.dat";

	public static final int ERRORS_TO_ADVANCE = 5;

	public static final String EXTENSION_ADDED =  ".added.nt.gz";
	public static final String EXTENSION_REMOVED =  ".removed.nt.gz";
	public static final String EXTENSION_CLEAR = ".clear.nt.gz";
	public static final String EXTENSION_REINSERT = ".reinserted.nt.gz ";
    
	public static final String EXTENSION_ADDED_NT =  ".added.nt";
	public static final String EXTENSION_REMOVED_NT =  ".removed.nt";
    
	public void start(CMMethod cmMethod);
	
	public boolean end();
	
	public boolean setChangesetDownloadFolder(String folderurl);
	
	public void refreshChangesetAddress();
	
	public boolean deleteChangeset(String changesetId);
	
}
