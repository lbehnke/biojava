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

package org.biojava.bio.program.das;

import java.util.*;
import java.net.*;
import java.io.*;
import org.biojava.utils.*;
import org.biojava.utils.cache.*;

import org.biojava.bio.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.symbol.*;

import org.apache.xerces.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.*;

/**
 * Collection of sequences retrieved from the DAS network.
 *
 * <p>The DAS-specific parts of this API are still subject
 * to change.</p>
 *
 * @author Thomas Down
 * @since 1.1
 */

public class DASSequenceDB implements SequenceDB {
    static boolean USE_XFF;

    static {
	USE_XFF = Boolean.getBoolean("org.biojava.use_xff");
    }

    private final URL dataSourceURL;
    private Map sequences;
    private Cache symbolsCache;

    private SequenceDB allEntryPoints;

    {
	sequences = new HashMap();
	symbolsCache = new FixedSizeCache(20);
    }

    Cache getSymbolsCache() {
	return symbolsCache;
    }

    public DASSequenceDB(URL dataSourceURL) 
	throws BioException 
    {
	this.dataSourceURL = dataSourceURL;

	try {
	    URL epURL = new URL(dataSourceURL, "entry_points");
	    HttpURLConnection huc = (HttpURLConnection) epURL.openConnection();
	    huc.connect();
	    int status = huc.getHeaderFieldInt("X-DAS-Status", 0);
	    if (status == 0)
		throw new BioException("Not a DAS server");
	    else if (status != 200)
		throw new BioException("DAS error (status code = " + status +
                ") connecting to " + dataSourceURL + " with query " + epURL);


	    InputSource is = new InputSource(huc.getInputStream());
	    DOMParser parser = DASSequence.nonvalidatingParser();
	    parser.parse(is);
	    Element el = parser.getDocument().getDocumentElement();

	    NodeList segl = el.getElementsByTagName("SEGMENT");
	    Element segment = null;
	    for (int i = 0; i < segl.getLength(); ++i) {
		el = (Element) segl.item(i);
	        sequences.put(el.getAttribute("id"), null);
	    }
	} catch (SAXException ex) {
	    throw new BioException(ex, "Exception parsing DAS XML");
	} catch (IOException ex) {
	    throw new BioException(ex, "Error connecting to DAS server");
	} catch (NumberFormatException ex) {
	    throw new BioException(ex);
	}
    }

    /**
     * Return a SequenceDB exposing /all/ the entry points
     * in this DAS datasource.
     *
     * <p>FIXME: For better scalability, this should return some
     * kind of lazy implementation rather than preconstructing everything.
     * </p>
     */

    public SequenceDB allEntryPointsDB() {
	if (allEntryPoints == null) {
	    allEntryPoints = new HashSequenceDB("All entry points from " + getURL().toString());
	    try {
		for (SequenceIterator si = sequenceIterator(); si.hasNext(); ) {
		    Sequence seq = si.nextSequence();
		    allEntryPoints.addSequence(seq);
		    FeatureHolder allComponents = seq.filter(
		            new FeatureFilter.ByClass(ComponentFeature.class),
			    true);
		    for (Iterator cfi = allComponents.features(); cfi.hasNext(); ) {
			ComponentFeature cf = (ComponentFeature) cfi.next();
			allEntryPoints.addSequence(cf.getComponentSequence());
		    }
		}
	    } catch (BioException ex) {
		throw new BioError(ex);
	    } catch (ChangeVetoException ex) {
		throw new BioError(ex, "Assertion failed: Couldn't modify our SequenceDB");
	    }
	}

	return allEntryPoints;
    }

    /**
     * Return the URL of the reference server for this database.
     */

    public URL getURL() {
	return dataSourceURL;
    }

    public String getName() {
	return dataSourceURL.toString();
    }

    public Sequence getSequence(String id) {
	if (! sequences.containsKey(id))
	    throw new NoSuchElementException("Couldn't find sequence " + id);
	Sequence seq = (Sequence) sequences.get(id);
	if (seq == null) {
	    try {
		seq = new DASSequence(this, dataSourceURL, id);
	    } catch (Exception ex) {
		throw new BioError(ex);
	    }
	    sequences.put(id, seq);
	}
	return seq;
    }

    public Set ids() {
	return sequences.keySet();
    }

    public void addSequence(Sequence seq)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("No way we're adding sequences to DAS");
    }

    public void removeSequence(String id)
        throws ChangeVetoException
    {
	throw new ChangeVetoException("No way we're removing sequences from DAS");
    }

    public SequenceIterator sequenceIterator() {
	return new SequenceIterator() {
	    private Iterator i = ids().iterator();

	    public boolean hasNext() {
		return i.hasNext();
	    }

	    public Sequence nextSequence() {
		return getSequence((String) i.next());
	    }
	} ;
    }

    // 
    // Changeable stuff (which we're not, fortunately)
    //

    public void addChangeListener(ChangeListener cl) {}
    public void addChangeListener(ChangeListener cl, ChangeType ct) {}
    public void removeChangeListener(ChangeListener cl) {}
    public void removeChangeListener(ChangeListener cl, ChangeType ct) {}
}

