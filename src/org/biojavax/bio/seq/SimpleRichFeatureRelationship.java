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

package org.biojavax.bio.seq;

import org.biojava.utils.AbstractChangeable;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.bio.db.RichObjectFactory;
import org.biojavax.ontology.ComparableTerm;

/**
 * Represents a relationship between two features that is described by a term.
 * @author Richard Holland
 * @author Mark Schreiber
 *
 */
public class SimpleRichFeatureRelationship extends AbstractChangeable implements RichFeatureRelationship {

    private RichFeature subject;
    private ComparableTerm term;
    private int rank;
    
    private static ComparableTerm CONTAINS_TERM = null;    
    
    /**
     * Gets the default CONTAINS term used for defining the relationship between features.
     * @return the default CONTAINS term.
     */
    public static ComparableTerm getContainsTerm() {
        if (CONTAINS_TERM==null) CONTAINS_TERM = RichObjectFactory.getDefaultOntology().getOrCreateTerm("contains");
        return CONTAINS_TERM;
    }
    
    /**
     * Creates a new instance of SimpleRichFeatureRelationship.
     * @param subject The subject RichFeature.
     * @param term The relationship term.
     * @param rank the rank of the relationship.
     */    
    public SimpleRichFeatureRelationship(RichFeature subject, ComparableTerm term, int rank) {
        if (subject==null) throw new IllegalArgumentException("Subject cannot be null");
        if (term==null) throw new IllegalArgumentException("Term cannot be null");
        this.subject = subject;
        this.term = term;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    private SimpleRichFeatureRelationship() {}
    
    /**
     * {@inheritDoc}
     */
    public void setRank(int rank) throws ChangeVetoException {
        if(!this.hasListeners(RichFeatureRelationship.RANK)) {
            this.rank = rank;
        } else {
            ChangeEvent ce = new ChangeEvent(
                    this,
                    RichFeatureRelationship.RANK,
                    new Integer(rank),
                    new Integer(this.rank)
                    );
            ChangeSupport cs = this.getChangeSupport(RichFeatureRelationship.RANK);
            synchronized(cs) {
                cs.firePreChangeEvent(ce);
                this.rank = rank;
                cs.firePostChangeEvent(ce);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public int getRank() { return this.rank; }
        
    /**
     * Getter for property subject.
     * @return Value of property subject.
     */
    public RichFeature getSubject() { return this.subject; }
    
    // Hibernate requirement - not for public use.
    private void setSubject(RichFeature subject) { this.subject = subject; }
    
    /**
     * Getter for property term.
     * @return Value of property term.
     */
    public ComparableTerm getTerm() { return this.term; }
    
    // Hibernate requirement - not for public use.
    private void setTerm(ComparableTerm term) { this.term = term; }
    
    /**
     * {@inheritDoc}
     * Relations are compared first by rank, then subject, then finally term.
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.subject==null) return -1;
        // Normal comparison
        RichFeatureRelationship them = (RichFeatureRelationship)o;
        if (this.rank!=them.getRank()) return this.rank-them.getRank();
        if (!this.subject.equals(them.getSubject())) return this.subject.compareTo(them.getSubject());
        else return this.getTerm().compareTo(them.getTerm());
    }
    
    /**
     * {@inheritDoc}
     * Relations are equal if their subjects and terms are equal.
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof RichFeatureRelationship)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.subject==null) return false;
        // Normal comparison
        RichFeatureRelationship them = (RichFeatureRelationship)obj;
        return (this.subject.equals(them.getSubject()) &&
                this.term.equals(them.getTerm()));
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.subject==null) return code;
        // Normal comparison
        code = code*37 + this.subject.hashCode();
        code = code*37 + this.term.hashCode();
        return code;
    }
    
    /**
     * {@inheritDoc}
     * Form: "(#rank) term(subject)"
     */
    public String toString() {
        return "(#"+this.rank+") "+this.getTerm()+"("+this.getSubject()+")";
    }
    
    // Hibernate requirement - not for public use.
    private Integer id;
    
    // Hibernate requirement - not for public use.
    private Integer getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Integer id) { this.id = id; }
}

