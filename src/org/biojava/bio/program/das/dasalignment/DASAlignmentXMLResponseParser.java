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
 * Created on 13.5.2004
 * @author Andreas Prlic
 *
 */

package org.biojava.bio.program.das.dasalignment ;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import java.util.ArrayList ;
import java.util.HashMap ;

import org.biojava.bio.* ;
import org.biojava.bio.program.ssbind.* ;
/** A class to Parse the XML response of a DAS Alignment service. 
 * returns an Alignment object.
 *
 * @author Andreas Prlic
 * @since 1.4
 */
public class DASAlignmentXMLResponseParser  extends DefaultHandler{
    ArrayList alignments ;
    Alignment alignment      ;
    HashMap current_object   ;
    String  current_position ;
    HashMap current_block  ;
    HashMap  current_segment ;
    ArrayList segments ;
    AnnotationFactory annf ;



    public DASAlignmentXMLResponseParser() {
	super() ;
	//System.out.println("in init DASAlignmentXMLResponseParser");
	alignment = new Alignment() ;
	current_position = "start";
	alignments = new ArrayList() ;
	segments = new ArrayList() ;
    }

    /**
     * Returns the alignments.
     *
     * @return an array of Alignment objects 

     */
    public Alignment[] getAlignments() {

	return (Alignment[])alignments.toArray(new Alignment[alignments.size()]);
    }

    /**
     * Returns Alignment at position ...
     *
     * @param position  an int
     * @return an Alignment object
     */
    public Alignment getAlignment(int position) {
	Alignment ra = (Alignment) alignments.get(position) ; 
	return ra ;
    }

    public void startElement (String uri, String name, String qName, Attributes atts){
	//System.out.println("startElement " + qName) ;
	if (qName.equals("alignObject")     ) OBJECThandler     (atts);
	//if (qName.equals("DESCRIPTION")) DESCRIPTIONhandler(atts);
	if (qName.equals("sequence")   ) SEQUENCEhandler   (atts);
	if (qName.equals("score")      ) SCOREhandler      (atts);
	if (qName.equals("block")      ) BLOCKhandler      (atts);
	if (qName.equals("segment")    ) SEGMENThandler    (atts);
	if (qName.equals("cigar")      ) CIGARhandler      (atts);
	if (qName.equals("geo3D")      ) GEO3Dhandler      (atts);
	
	
	    
    }

    public void endElement (String uri, String name, String qName){
	//System.out.println("endElement >" + qName + "< >" + name + "<") ;
	
	if (qName.equals("alignObject")) {
	    try {
		Annotation oba = annf.makeAnnotation(current_object) ;
		alignment.addObject(oba);
	    } catch ( DASException  e) {
		e.printStackTrace() ;
	    }
	    current_object = new HashMap() ;
	}	
	if (qName.equals("segment")) {
	    Annotation sega = annf.makeAnnotation(current_segment);
	    //current_block.add(current_segment);
	    segments.add(sega) ;
	    current_segment = new HashMap() ;
	    
	}
	if (qName.equals("block")) {
	    try {
		current_block.put("segments",segments);
		Annotation bloa = annf.makeAnnotation(current_block);
		alignment.addBlock(bloa);
	    } catch ( DASException  e) {
		e.printStackTrace() ;
	    }
	    current_block = new HashMap() ;
	    segments = new ArrayList();
	}
	if (qName.equals("alignment")){
	    alignments.add(alignment) ;

	    alignment = new Alignment() ;

	    
	}
	
	    
    }

    private void SEGMENThandler(Attributes atts) {
	current_position = "segment";
	current_segment  = new HashMap() ;
	
	String id     = atts.getValue("intObjectId");
	String start  = atts.getValue("start");
	String end    = atts.getValue("end");
	// orientation, not implemented yet ...
	current_segment.put("intObjectId",id);
	if ( start != null ) {
	current_segment.put("start",start);
	}
	if ( end != null ) {
	    current_segment.put("end",end) ;
	}
	
	
    }
    
    private void CIGARhandler(Attributes atts) {
	current_position = "cigar" ;
    }
    private void BLOCKhandler(Attributes atts) {
	current_block = new HashMap();
	String blockOrder = atts.getValue("blockOrder");

	current_block.put("blockOrder",blockOrder);

	try {
	    String blockScore = atts.getValue("blockScore");
	    if ( blockScore != null ) {
		current_block.put("blockScore",blockScore);
	    }
	} catch (Exception e) {} ;

    }

    
    private void SEQUENCEhandler(Attributes atts) {
	//System.out.println("sequence");
	current_position = "sequence" ;
	String start    = atts.getValue("start");
	String end      = atts.getValue("end");
	if ( start != null ) {
	    current_object.put("seqStart",start);
	}
	if ( end != null ) {
	    current_object.put("seqEnd",end);
	}

    }


    private void SCOREhandler(Attributes atts) {
	System.out.println("SCOREhandler not implemented,yet...");

    }

    private void GEO3Dhandler(Attributes atts) {
	System.out.println("GEO3D not implemented,yet...");

    }

    private void OBJECThandler(Attributes atts) {
	// found a new object
	String dbAccessionId    = atts.getValue("dbAccessionId");
	String objectVersion    = atts.getValue("objectVersion");
	String intObjectId      = atts.getValue("intObjectId");
	String type             = "" ;

	try { type = atts.getValue("type");} catch (Exception e) {} 
	

	String dbSource         = atts.getValue("dbSource");
	String dbVersion        = atts.getValue("dbVersion");
	//System.out.println("here" + dbAccessionId + " | " + objectVersion + " | " + intObjectId + " | " + dbSource + " | " + dbVersion + " | " + type);
	String dbCoordSys       = atts.getValue("dbCoordSys");
	//System.out.println("there" + dbCoordSys);
	
	HashMap object = new HashMap() ;
	object.put("dbAccessionId" ,dbAccessionId);
	object.put("objectVersion" ,objectVersion);
	object.put("intObjectId"   ,intObjectId);

	object.put("dbSource"      ,dbSource) ;
	//System.out.println("daga");
	if ( dbCoordSys != null ) {
	    object.put("dbCoordSys"    ,dbCoordSys);
	} 
	//System.out.println("dong");
	object.put("dbVersion"     ,dbVersion) ;
	
	if ( type != null ){
	    object.put("type",type); 
	} 
	
	
	current_object = object ;
	//System.out.println("done");
       
    }
    

   public void startDocument() {
	//System.out.println("start document");
	
    }
	
    public void endDocument ()	{
	
    }

    public void characters (char ch[], int start, int length){
	String txt = "";
	for (int i = start; i < start + length; i++) {
	    txt += ch[i] ;
	}
	if ( current_position == "cigar"){
	    current_segment.put("cigar",txt);
	}
	if (current_position == "sequence"){
	    //System.out.println(txt);
	    current_object.put("sequence",txt);
	}

    }

}