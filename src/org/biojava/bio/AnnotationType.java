package org.biojava.bio;

import java.util.*;

import org.biojava.utils.*;

/**
 * <p>
 * A type to constrain annotation bundles.
 * </p>
 *
 * <p>
 * <code>AnnotatoinType</code> instances can be used to validate an <code>Annotation</code> to
 * check that it has the apropreate properties and that they are of the right type. 
 * </p>
 *
 * @since 1.3
 * @author Matthew Pocock
 */
public interface AnnotationType {
  /**
   * Validate an Annotation against this AnnotationType.
   *
   * @param ann  the Annotation to validate
   * @return true if ann conforms to this type and false if it doesn't
   */
  public boolean instanceOf(Annotation ann);
  
  /**
   * <p>
   * See if an AnnotationType is a specialisation of this type.
   * </p>
   *
   * <p>
   * An AnnotationType is a sub-type if it restricts each of the properties
   * of the super-type to a type that can be cast to the type in the super-type.
   * </p>
   *
   * @param subType  an AnnotationType to check
   * @return true if subType is a sub-type of this type
   */
  public boolean subTypeOf(AnnotationType subType);
  
  /**
   * <p>
   * Retrieve the constraint that will be applied to all properties with a given key.
   * </p>
   *
   * <p>
   * For an <code>Annotation</code> to be accepted, each key in getProperties() must
   * be present in the annotation, and each of the values associated with those properties
   * match the constraint.
   * </p>
   *
   * @param key  the property that would be validated
   * @return PropertyConstraint  the constraint that values must be acepted by
   */
  public PropertyConstraint getPropertyConstraint(Object key);
  
  /**
   * Retrieve the complete set of properties that must be present for an <code>Annotation</code>
   * to be accepted by this <code>AnnotationType</code>.
   *
   * @return the Set of properties to validate
   */
  public Set getProperties();
  
  public void setProperty(Annotation ann, Object property, Object value)
  throws ChangeVetoException;
  
  public static final AnnotationType ANY = new AnnotationType() {
    public boolean instanceOf(Annotation ann) {
      return true;
    }
    public boolean subTypeOf(AnnotationType subType) {
      return true;
    }
    public PropertyConstraint getPropertyConstraint(Object key) {
      return PropertyConstraint.ANY;
    }
    public Set getProperties() {
      return Collections.EMPTY_SET;
    }
    public void setProperty(Annotation ann, Object property, Object value)
    throws ChangeVetoException {
      getPropertyConstraint(property).setProperty(ann, property, value);
    }
  };
  
  /**
   * <p>
   * An implementation of <code>AnnotationType</code>.
   * </p>
   *
   * <p>
   * To build an instance of <code>AnnotationType.Impl</code>, first invoke the
   * no-args constructor, and then use the setPropertyConstraint method to build
   * the property->constraint mapping.
   * </p>
   * 
   * @author Matthew Pocock
   */
  public class Impl implements AnnotationType {
    private Map cons;
    
    /**
     * Create a new Impl with no constraints.
     */
    public Impl() {
      cons = new SmallMap();
    }
    
    public PropertyConstraint getPropertyConstraint(Object key) {
      PropertyConstraint pc = (PropertyConstraint) cons.get(key);
      if(pc == null) {
        pc = PropertyConstraint.ANY;
      }
      return pc;
    }
    
    /**
     * Set a constraint for a property.
     *
     * @param key  the property to constrain
     * @param con  the PropertyConstraint to constrain the property
     */
    public void setPropertyConstraint(Object key, PropertyConstraint con) {
      cons.put(key, con);
    }
    
    public Set getProperties() {
      return cons.keySet();
    }
    
    public boolean instanceOf(Annotation ann) {
      for(Iterator i = cons.entrySet().iterator(); i.hasNext(); ) {
        Map.Entry pair = (Map.Entry) i.next();
        Object key = pair.getKey();
        PropertyConstraint con = (PropertyConstraint) pair.getValue();
        if(!con.accept(ann.getProperty(key))) {
          return false;
        }
      }
        
      return true;
    }
    
    public boolean subTypeOf(AnnotationType subType) {
      for(Iterator i = cons.keySet().iterator(); i.hasNext(); ) {
        Object key = i.next();
        PropertyConstraint thisPropertyConstraint = getPropertyConstraint(key);
        PropertyConstraint subPropertyConstraint = subType.getPropertyConstraint(key);
        if(!thisPropertyConstraint.subConstraintOf(subPropertyConstraint)) {
          return false;
        }
      }
      
      return true;
    }

    public void setProperty(Annotation ann, Object property, Object value)
    throws ChangeVetoException {
      getPropertyConstraint(property).setProperty(ann, property, value);
    }
  }
}
