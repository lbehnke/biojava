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

package org.biojava.ontology;

import org.biojava.bio.Annotatable;
import org.biojava.bio.Annotation;
import org.biojava.bio.SmallAnnotation;
import org.biojava.utils.ChangeType;

/**
 * A term in an ontology.  This has an {@link org.biojava.bio.Annotation Annotation}
 * which can be used for storing additional human-displayable information.  It
 * is strongly recommended that the Annotation is not used for any machine-readable
 * data -- this should be represented by relations in the ontology instead.
 *
 * <p>
 * Terms are things that represent things. They are the same sort of thing as a
 * Java object or a prolog atom. A sub-set of terms are themselves relations.
 * This means that they are used to describe associations between pairs of terms.
 * Since all terms can be described, it is possible (and indeed encouraged) to
 * describe relations. As a minimum, you should consider saying if they are
 * identity or partial order relations, or if they are transitive, reflexive,
 * symmetrical, anti-symmetrical or anything else you know about them. This gives
 * the inference engine some chance of working out what is going on.
 * </p>
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.4
 */

public interface Term extends Annotatable {
    /**
     * ChangeType which indicates that this term's ontology has been
     * altered
     */

    public static final ChangeType ONTOLOGY = new ChangeType(
      "This term's ontology has been changed",
      "org.biojava.ontology.Term",
      "ONTOLOGY"
    );

    /**
     * Return the name of this term.
     */

    public String getName();

    /**
     * Return a human-readable description of this term, or the empty string if
     * none is available.
     */

    public String getDescription();

    /**
     * Return the ontology in which this term exists.
     */

    public Ontology getOntology();

    /**
     * Simple in-memory implementation of an ontology term.
     *
     * @for.developer This can be used to implement Ontology.createTerm
     */

    public static class Impl
    extends AbstractTerm
    implements Term, java.io.Serializable {
        private final String name;
        private final String description;
        private final Ontology ontology;
        private Annotation annotation;

        public Impl(Ontology ontology, String name, String description) {
            if (name == null) {
                throw new NullPointerException("Name must not be null");
            }
            if (description == null) {
                throw new NullPointerException("Description must not be null");
            }
            if (ontology == null) {
                throw new NullPointerException("Ontology must not be null");
            }

            this.name = name;
            this.description = description;
            this.ontology = ontology;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Ontology getOntology() {
            return ontology;
        }

        public String toString() {
            return name;
        }

        public Annotation getAnnotation() {
            if (annotation == null) {
                annotation = new SmallAnnotation();
            }
            return annotation;
        }
    }
}
