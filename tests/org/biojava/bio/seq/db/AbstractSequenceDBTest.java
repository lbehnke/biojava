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

package org.biojava.bio.seq.db;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.biojava.bio.SimpleAnnotation;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.symbol.LocationTools;

/**
 * Abstract test class for <code>SequenceDB</code> implementations. To
 * use this class, you should only have to create a subclass that
 * implements the <code>getSequenceDB</code> method.
 * 
 * @author Len Trigg
 */
public abstract class AbstractSequenceDBTest extends TestCase {
    
    protected SequenceDB mSequenceDB = null;

    public AbstractSequenceDBTest(String name) {
        super(name);
    }

    /**
     * Subclasses override this to supply the particular SequenceDB
     * implementation to test. 
     */
    protected abstract SequenceDB getSequenceDB() throws Exception;

    
    public void setUp() throws Exception {
        mSequenceDB = getSequenceDB();
    }


    public void tearDown() throws Exception {
        mSequenceDB = null;
    }
    

    // This is a simple test that BioSQLSequenceDB failed.
    public void testAddFeature() throws Exception {
        String name = "dna_1";
        assertTrue(!mSequenceDB.ids().contains(name));
        mSequenceDB.addSequence(DNATools.createDNASequence("atgctgatgatgatg", name));
        assertTrue(mSequenceDB.ids().contains(name));
        Sequence seq = mSequenceDB.getSequence(name);

        // Uncommenting this line makes BioSQLSequenceDB pass the test
        // assertTrue(seq.countFeatures() == 0);

        Feature.Template template = new Feature.Template();
        template.annotation = new SimpleAnnotation();
        template.location = LocationTools.makeLocation(10, 15);
        template.type = "noTS";
        template.source = "asource";
        Feature noTS = seq.createFeature(template);
        assertTrue(seq.countFeatures() == 1);
    }
    

    // A test that does a bit of feature editing
    public void testEditFeatures() throws Exception {
        String name = "dna_1";
        Sequence seq = DNATools.createDNASequence("atgctgatgatgatg", name);

        assertTrue(!mSequenceDB.ids().contains(name));
        mSequenceDB.addSequence(seq);
        assertTrue(mSequenceDB.ids().contains(name));
        seq = mSequenceDB.getSequence(name);

        String sourceType = "asource";
        String annoTag = "anno";
        String annoVal = "123456";

        // Create some features
        Feature.Template template = new Feature.Template();
        template.annotation = new SimpleAnnotation();
        template.location = LocationTools.makeLocation(10, 15);
        template.type = "noANNO";
        template.source = sourceType;
        Feature noANNO = seq.createFeature(template);
        assertTrue(!noANNO.getAnnotation().containsProperty(annoTag));
        //new org.biojava.bio.seq.io.GenbankFormat().writeSequence(seq, System.err);
        assertTrue(seq.countFeatures() == 1);

        template = new Feature.Template();
        template.annotation = new SimpleAnnotation();
        template.location = LocationTools.makeLocation(10, 15);
        template.type = "yesANNO";
        template.source = sourceType;
        template.annotation.setProperty(annoTag, annoVal);
        Feature yesANNO = seq.createFeature(template);
        assertTrue(!noANNO.getAnnotation().containsProperty(annoTag));
        assertTrue(yesANNO.getAnnotation().containsProperty(annoTag));
        //new org.biojava.bio.seq.io.GenbankFormat().writeSequence(seq, System.err);
        assertTrue(seq.countFeatures() == 2);

        // Check the features are both there
        Iterator i = seq.features();
        assertTrue(i.hasNext());
        Feature feature = (Feature) i.next();
        boolean yesFound = feature.getAnnotation().containsProperty(annoTag);
        if (yesFound) {
            assertTrue(feature.getType().equals("yesANNO"));
        } else {
            assertTrue(feature.getType().equals("noANNO"));
        }

        assertTrue(i.hasNext());
        feature = (Feature) i.next();
        if (yesFound) {
            assertTrue(feature.getType().equals("noANNO"));
            assertTrue(!feature.getAnnotation().containsProperty(annoTag));
        } else {
            assertTrue(feature.getType().equals("yesANNO"));
            assertTrue(feature.getAnnotation().containsProperty(annoTag));
        }
        assertTrue(!i.hasNext());


        // Delete the noANNO feature
        FeatureHolder fh = seq.filter(new FeatureFilter.And(new FeatureFilter.BySource(sourceType),
                                                            new FeatureFilter.Not(new FeatureFilter.ByAnnotation(annoTag, annoVal))));
        i = fh.features();
        assertTrue(i.hasNext());
        feature = (Feature) i.next();
        assertTrue(!feature.getAnnotation().containsProperty(annoTag));
        seq.removeFeature(feature);
        System.err.println("Removed old feature: " + feature);

        //new org.biojava.bio.seq.io.GenbankFormat().writeSequence(seq, System.err);

        // Check the yesANNO feature is still there.
        i = seq.features();
        assertTrue(i.hasNext());
        feature = (Feature) i.next();
        assertTrue(feature.getAnnotation().containsProperty(annoTag));
        feature.getAnnotation().removeProperty(annoTag);
        assertTrue(!feature.getAnnotation().containsProperty(annoTag));

        //new org.biojava.bio.seq.io.GenbankFormat().writeSequence(seq, System.err);
    }
}