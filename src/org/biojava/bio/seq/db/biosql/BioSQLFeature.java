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

package org.biojava.bio.seq.db.biosql;

import org.biojava.bio.seq.*;
import org.biojava.bio.seq.impl.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.*;

class BioSQLFeature extends SimpleFeature implements BioSQLFeatureI {
    private Annotation _annotation;

    BioSQLFeature(Sequence seq,
		  FeatureHolder parent,
		  Feature.Template templ)
	throws IllegalArgumentException, IllegalAlphabetException
    {
	super(seq, parent, mungeTemplate(templ));
	_annotation = templ.annotation;
    }

    public void _setAnnotation(Annotation a) {
	_annotation = a;
    }

    private static Template mungeTemplate(Template templ) {
	Feature.Template sft = new Feature.Template();
	sft.location = templ.location;
	sft.type = templ.type;
	sft.source = templ.source;
	sft.annotation = Annotation.EMPTY_ANNOTATION;
	return sft;
    }

    public Annotation getAnnotation() {
	return _annotation;
    }
} 
