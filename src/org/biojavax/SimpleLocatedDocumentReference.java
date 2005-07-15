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

/*
 * SimpleLocatedDocumentReference.java
 *
 * Created on July 12, 2005, 8:10 AM
 */

package org.biojavax;

/**
 * Represents a documentary reference, the bioentryreference table in BioSQL.
 * @author Richard Holland
 */
public class SimpleLocatedDocumentReference implements LocatedDocumentReference {

    /** The docref */
    private DocumentReference docref;

    /** The start of the location */
    private int start;
    
    /** The end of the location */
    private int end;
    
    /** 
     * Constructs a new docref for a given location.
     * @param docref the document reference
     * @param start the start position of the location
     * @param end the end position of the location
     */
    public SimpleLocatedDocumentReference(DocumentReference docref, int start, int end) {
        if (docref==null) throw new IllegalArgumentException("Document reference cannot be null");
        this.docref = docref;
        this.start = start;
        this.end = end;
    }
    
        /**
     * Represents a reference to a document.
     * @return the document reference.
     */
    public DocumentReference getDocumentReference() {
        return this.docref;
    }
    
        /**
     * The start position in the sequence that this reference refers to.
     * @return the start position.
     */
    public int getStart() {
        return this.start;
    }
    
    /**
     * The end position in the sequence that this reference refers to.
     * @return the end position.
     */
    public int getEnd() {
        return this.end;
    }
    

    /**
     * Indicates whether some other object is "equal to" this one. Equality is
     * the combination of namespace, name, accession and version.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     * @see     #hashCode()
     * @see     java.util.Hashtable
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj==null || !(obj instanceof LocatedDocumentReference)) return false;
        else {
            LocatedDocumentReference them = (LocatedDocumentReference)obj;
            return (this.getDocumentReference().equals(them.getDocumentReference()) &&
                    this.getStart()==them.getStart() &&
                    this.getEnd()==them.getEnd());
        }
    }
    
    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     * @return a negative integer, zero, or a positive integer as this object
     * 		is less than, equal to, or greater than the specified object.
     * @param o the Object to be compared.
     */
    public int compareTo(Object o) {
        LocatedDocumentReference them = (LocatedDocumentReference)o;
        if (!this.getDocumentReference().equals(them.getDocumentReference())) return this.getDocumentReference().compareTo(them.getDocumentReference());
        if (this.getStart()!=them.getStart()) return this.getStart()-them.getStart();
        return this.getEnd()-them.getEnd();
    }
    
    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>Hashtable</code>.
     * @return  a hash code value for this object.
     * @see     java.lang.Object#equals(java.lang.Object)
     * @see     java.util.Hashtable
     */
    public int hashCode() {
        int code = 17;
        code = 37*code + this.getDocumentReference().hashCode();
        code = 37*code + this.getStart();
        code = 37*code + this.getEnd();
        return code;
    }
    
    /**
     * Returns a string representation of the object of the form
     * <code>this.getDocumentReference()+": "+this.getStart()+"-"+this.getEnd();</code>
     * @return  a string representation of the object.
     */
    public String toString() {
        return this.getDocumentReference()+": "+this.getStart()+"-"+this.getEnd();
    }
    
}
