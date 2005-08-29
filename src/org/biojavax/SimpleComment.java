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

/**
 * An implementaion of Comment.
 * @author Richard Holland
 */
public class SimpleComment implements Comment {
    
    private String comment;
    private int rank;
    
    /**
     * Constructs a new, immutable comment, given some text and a rank.
     * @param comment the text of the comment. Cannot be null.
     * @param rank the rank of the comment.
     */
    public SimpleComment(String comment, int rank) {
        if (comment==null) throw new IllegalArgumentException("Comment cannot be null");
        this.comment = comment;
        this.rank = rank;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleComment() {}
    
    // Hibernate requirement - not for public use.
    private void setComment(String comment) { this.comment = comment; }
    
    /**
     * {@inheritDoc}
     */
    public String getComment() { return this.comment; }
    
    // Hibernate requirement - not for public use.
    private void setRank(int rank) { this.rank = rank; }
    
    /**
     * {@inheritDoc}
     */
    public int getRank() { return this.rank; }
    
    /**
     * {@inheritDoc}
     * Two comments are defined as equal if their text values and
     * rankings are identical.
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof Comment)) return false;
        // Hibernate comparison - we haven't been populated yet
        if (this.comment==null) return false;
        // Normal comparison
        Comment them = (Comment)obj;
        return (this.rank==them.getRank() &&
                this.comment.equals(them.getComment()));
    }
    
    /**
     * {@inheritDoc}
     * Comments are ordered first by their rank, then by a string
     * comparison of their text values.
     */
    public int compareTo(Object o) {
        // Hibernate comparison - we haven't been populated yet
        if (this.comment==null) return -1;
        // Normal comparison
        Comment them = (Comment)o;
        if (this.rank!=them.getRank()) return this.rank-them.getRank();
        return this.comment.compareTo(them.getComment());
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int code = 17;
        // Hibernate comparison - we haven't been populated yet
        if (this.comment==null) return code;
        // Normal comparison
        code = 37*code + this.comment.hashCode();
        code = 37*code + this.rank;
        return code;
    }
    
    /**
     * {@inheritDoc}
     * Form: "(#rank) comment"
     */
    public String toString() { 
        return "(#"+this.rank+") "+this.comment; 
    }
    
    // Hibernate requirement - not for public use.
    private Integer id;
    
    // Hibernate requirement - not for public use.
    private Integer getId() { return this.id; }
    
    // Hibernate requirement - not for public use.
    private void setId(Integer id) { this.id = id;}
}
