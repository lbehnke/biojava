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

package org.biojava.bio.seq;

import java.util.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.genomic.*;
import org.biojava.bio.seq.impl.*;
import junit.framework.TestCase;

/**
 * Tests for FeatureFilters.
 *
 * @author Thomas Down
 * @since 1.3
 */
 
public class FeatureFilterTest extends TestCase
{
    protected Sequence seq;
    protected Feature fooFeature;
    
    public FeatureFilterTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
        seq = new SimpleSequence(
                DNATools.createDNA("gattaca"),
                "foo",
                "foo",
                Annotation.EMPTY_ANNOTATION
        );
        StrandedFeature.Template template = new StrandedFeature.Template();
        template.type = "foo";
        template.source = "bar";
        template.location = new RangeLocation(2, 5);
        template.strand = StrandedFeature.POSITIVE;
        template.annotation = new SmallAnnotation();
        template.annotation.setProperty("baz", Boolean.TRUE);
        fooFeature = seq.createFeature(template);
    }
    
    public void testHasAnnotation() {
        assertTrue(new FeatureFilter.HasAnnotation("baz").accept(fooFeature));
        assertTrue(!new FeatureFilter.HasAnnotation("evil").accept(fooFeature));
    }
    
    public void testByAnnotation() {
        assertTrue(new FeatureFilter.ByAnnotation("baz", Boolean.TRUE).accept(fooFeature));
        assertTrue(!new FeatureFilter.ByAnnotation("baz", Boolean.FALSE).accept(fooFeature));
        assertTrue(!new FeatureFilter.ByAnnotation("evil", Boolean.TRUE).accept(fooFeature));
    }
    
    public void testByType() {
        assertTrue(new FeatureFilter.ByType("foo").accept(fooFeature));
        assertTrue(!new FeatureFilter.ByType("evil").accept(fooFeature));
    }
    
    public void testBySource() {
        assertTrue(new FeatureFilter.BySource("bar").accept(fooFeature));
        assertTrue(!new FeatureFilter.BySource("evil").accept(fooFeature));
    }
    
    public void testOverlapsLocation() {
        assertTrue(new FeatureFilter.OverlapsLocation(new RangeLocation(1, 6)).accept(fooFeature));
        assertTrue(new FeatureFilter.OverlapsLocation(new RangeLocation(3, 6)).accept(fooFeature));
        assertTrue(!new FeatureFilter.OverlapsLocation(new RangeLocation(1, 1)).accept(fooFeature));
    }
    
    public void testContainedByLocation() {
        assertTrue(new FeatureFilter.ContainedByLocation(new RangeLocation(1, 6)).accept(fooFeature));
        assertTrue(!new FeatureFilter.ContainedByLocation(new RangeLocation(3, 6)).accept(fooFeature));
        assertTrue(!new FeatureFilter.ContainedByLocation(new RangeLocation(1, 1)).accept(fooFeature));
    }
}
