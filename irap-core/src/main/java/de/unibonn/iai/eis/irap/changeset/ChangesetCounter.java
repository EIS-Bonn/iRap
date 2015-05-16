/**
 * 
 */
package de.unibonn.iai.eis.irap.changeset;

/**
 * @author Kemele M. Endris
 *
 */
public interface ChangesetCounter extends Comparable<ChangesetCounter>{
	
	public String getFormattedFilePath();
	
	public int incrementCount();
	
	public boolean equalsTo(ChangesetCounter counter);
	
	public void advanceCounter();
	
	public String getSequenceNumber();
}
