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
 * created at Oct 18, 2008
 */
package org.biojava.bio.structure.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.io.StructureIOFile;
import org.biojava.bio.structure.io.mmcif.MMcifParser;
import org.biojava.bio.structure.io.mmcif.SimpleMMcifConsumer;
import org.biojava.bio.structure.io.mmcif.SimpleMMcifParser;
import org.biojava.utils.io.InputStreamProvider;


public class MMCIFFileReader implements StructureIOFile {

	String path;
	List<String> extensions;
	boolean autoFetch;

	public static void main(String[] args){
		String filename =  "/Users/andreas/WORK/PDB/mmcif_files/a9/2a9w.cif.gz" ;

		StructureIOFile reader = new MMCIFFileReader();
		reader.setAutoFetch(true);
		try{
			Structure struc = reader.getStructure(filename);
			System.out.println(struc);



		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public MMCIFFileReader(){
		extensions    = new ArrayList<String>();
		path = "" ;
		extensions.add(".cif");
		extensions.add(".mmcif");
		extensions.add(".cif.gz");
		extensions.add(".mmcif.gz");

		autoFetch     = false;

	}

	public void addExtension(String ext) {
		extensions.add(ext);

	}

	public void clearExtensions(){
		extensions.clear();
	}

	/** opens filename, parses it and returns
	 * a Structure object .
	 * @param filename  a String
	 * @return the Structure object
	 * @throws IOException ...
	 */
	public Structure getStructure(String filename) 
	throws IOException
	{
		File f = new File(filename);
		return getStructure(f);

	}

	/** opens filename, parses it and returns a Structure object
	 * 
	 * @param filename a File object
	 * @return the Structure object
	 * @throws IOException ...
	 */
	public Structure getStructure(File filename) throws IOException {

		InputStreamProvider isp = new InputStreamProvider();

		InputStream inStream = isp.getInputStream(filename);
	
		return parseFromInputStream(inStream);
	}
	
	

	private Structure parseFromInputStream(InputStream inStream) throws IOException{

		MMcifParser parser = new SimpleMMcifParser();

		SimpleMMcifConsumer consumer = new SimpleMMcifConsumer();

		// The Consumer builds up the BioJava - structure object.
		// you could also hook in your own and build up you own data model.          
		parser.addMMcifConsumer(consumer);


		parser.parse(new BufferedReader(new InputStreamReader(inStream)));

		// now get the protein structure.
		Structure cifStructure = consumer.getStructure();

		return cifStructure;
	}

	public void setPath(String path) {
		this.path = path;

	}


	public String getPath() {
		return path;
	}

	public Structure getStructureById(String pdbId) throws IOException {
		InputStream inStream = getInputStream(pdbId);

		return parseFromInputStream(inStream);
	}

	private InputStream getInputStream(String pdbId) throws IOException{
		InputStream inputStream =null;

		String pdbFile = null ;
		File f = null ;

		// this are the possible PDB file names...
		String fpath = path+"/"+pdbId;
		//String ppath = path +"/pdb"+pdbId;

		String[] paths = new String[]{fpath,};

		for ( int p=0;p<paths.length;p++ ){
			String testpath = paths[p];
			//System.out.println(testpath);
			for (int i=0 ; i<extensions.size();i++){
				String ex = (String)extensions.get(i) ;
				//System.out.println("PDBFileReader testing: "+testpath+ex);
				f = new File(testpath+ex) ;

				if ( f.exists()) {
					//System.out.println("found!");
					pdbFile = testpath+ex ;

					InputStreamProvider isp = new InputStreamProvider();

					inputStream = isp.getInputStream(pdbFile);
					break;
				}

				if ( pdbFile != null) break;        
			}
		}

		if ( pdbFile == null ) {
			if ( autoFetch) 
				return downloadAndGetInputStream(pdbId);

			String message = "no structure with PDB code " + pdbId + " found!" ;
			throw new IOException (message);
		}

		return inputStream ;
	}

	
	private InputStream downloadAndGetInputStream(String pdbId)
		throws IOException{
		//PDBURLReader reader = new PDBURLReader();
		//Structure s = reader.getStructureById(pdbId);
		File tmp = downloadPDB(pdbId);
		if ( tmp != null ) {
			InputStreamProvider prov = new InputStreamProvider();
			return prov.getInputStream(tmp);
			

		} else {
			throw new IOException("could not find PDB " + pdbId + " in file system and also could not download");
		}

	}
	
	private File downloadPDB(String pdbId){
		
		File tempFile = new File(path+"/"+pdbId+".cif.gz");		
		File pdbHome = new File(path);
		
		if ( ! pdbHome.canWrite() ){
			System.err.println("can not write to " + pdbHome);
			return null;
		}
		
		String ftp = String.format("ftp://ftp.wwpdb.org/pub/pdb/data/structures/all/mmCIF/%s.cif.gz", pdbId.toLowerCase()); 

		System.out.println("Fetching " + ftp); 
		try {
			URL url = new URL(ftp);
			InputStream conn = url.openStream();			

			// prepare destination
			System.out.println("writing to " + tempFile);
					
			FileOutputStream outPut = new FileOutputStream(tempFile);
			GZIPOutputStream gzOutPut = new GZIPOutputStream(outPut);
			PrintWriter pw = new PrintWriter(gzOutPut);
			
			BufferedReader fileBuffer = new BufferedReader(new InputStreamReader(new GZIPInputStream(conn)));
			String line;
			while ((line = fileBuffer.readLine()) != null) {				
				pw.println(line);
			}
			pw.flush();
			pw.close();
			outPut.close();
			conn.close();
		} catch (Exception e){
			e.printStackTrace();
			return null; 
		}
		return tempFile;
	}
	
	public boolean isAutoFetch() {		
		return autoFetch;
	}


	public void setAutoFetch(boolean autoFetch) {
		this.autoFetch = autoFetch;

	}



}
