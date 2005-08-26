/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojavax;
import org.biojava.utils.Changeable;

/**
 * Allows cross-references to other databases to be ranked.
 * @author Richard Holland
 * @see RankedCrossRefable
 * @see CrossRef
 */
public interface RankedCrossRef extends Comparable,Changeable {
    
    /**
     * Return the cross reference associated with this object.
     * @return a crossref object.
     */
    public CrossRef getCrossRef();
    
    /**
     * Return the rank associated with the cross reference.
     * @return the rank.
     */
    public int getRank();
    
}
