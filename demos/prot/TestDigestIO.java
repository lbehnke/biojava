/*
 * TestDigest2.java
 *
 * Created on July 10, 2001, 1:10 PM
 */

package prot;




import org.biojava.bio.proteomics.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.BioException;
import org.biojava.utils.*;
import java.io.*;
import java.util.*;
import java.text.NumberFormat;

/**
 *
 * @author  mjones
 * @version
 */
public class TestDigestIO extends Object {
    
    private MassCalc massCalc;
    
    
    NumberFormat nf = NumberFormat.getInstance();
    
    
    /** Creates new TestDigest2 */
    public TestDigestIO(String fileName, String mode) throws BioException,
    ChangeVetoException,
    IOException {
        nf.setMaximumFractionDigits(2);
        nf.setMinimumFractionDigits(2);
        
        //Initiate Digest
        Digest bioJavaDigest = new Digest();
        bioJavaDigest.setMaxMissedCleavages(1);
        
        massCalc = new MassCalc(SymbolPropertyTable.MONO_MASS, true);
        try{
            massCalc.setSymbolModification('W', 100000.00);
        }catch(Exception e){}
        
        // Protease protease = new Protease();
        // String[] list = Protease.getProteaseList();
        // for(int i=0; i<list.length ;i++)
        //         System.out.println(list[i]);
        
        //String proteaseName = Protease.ASP_N;
        bioJavaDigest.setProtese(Protease.getProteaseByName(Protease.TRYPSIN));
        
        
        SequenceIterator sourceI = getSeqIterator(fileName);
        
        
        // ArrayList list  = new ArrayList();
        long startTime = System.currentTimeMillis();
        while(sourceI.hasNext()) {
            Sequence sourceSeq = sourceI.nextSequence();
            bioJavaDigest.setSequence(sourceSeq);
            bioJavaDigest.addDigestFeatures();
            if(mode.equals("p")){
                printFeatures(sourceSeq.features(), sourceSeq.getName() + " " );
            }
            else if(mode.equals("m")){
                calcMasses(sourceSeq.features(), sourceSeq.getName() + " " );
            }
        }
        long endTime = System.currentTimeMillis();
        System.err.println("Total Time: " + (endTime - startTime));
        
        
        
        bioJavaDigest.setProtese(Protease.getProteaseByName(Protease.CNBr));
        
        //Get the Sequence Iterator
        sourceI = getSeqIterator(fileName);
        
        while(sourceI.hasNext()) {
            Sequence sourceSeq = sourceI.nextSequence();
            bioJavaDigest.setSequence(sourceSeq);
            bioJavaDigest.addDigestFeatures();
            if(mode.equals("p")){
                printFeatures(sourceSeq.features(), sourceSeq.getName() + " " );
            }
            else if(mode.equals("m")){
                calcMasses(sourceSeq.features(), sourceSeq.getName() + " " );
            }
        }
        
    }
    
    
    
    private  void calcMasses(Iterator i, String prefix) {
        
        for (; i.hasNext(); ) {
            Feature f = (Feature) i.next();
            //System.out.print(prefix);
            //System.out.print(f.getType());
            //System.out.print(f.getLocation().toString()+ " ");
            
            try{
                double mass = massCalc.getMass(f.getSymbols(),true);
                
                System.out.print(mass);
                
                System.out.println();
                
            }
            catch(Exception ise){
                System.out.println(ise.getMessage());
            }
        }
        
    }
    
    private  void printFeatures(Iterator i, String prefix) {
        for (; i.hasNext(); ) {
            Feature f = (Feature) i.next();
            System.out.print(prefix);
            System.out.print(f.getType());
            System.out.print(f.getLocation().toString()+ " ");
            
            //Use this for static
            try{
                double mass = MassCalc.getMass(
                f.getSymbols(),
                SymbolPropertyTable.MONO_MASS,
                true);
                System.out.print(nf.format(mass) + " ");
                
                //Use this for instances of MassCalc
                mass = massCalc.getMass(f.getSymbols(),true);
                
                System.out.print(" PTM " + nf.format(mass) + " ");
                
                
                System.out.print(" " + f.getSymbols().seqString() + "    " );
                
                
                
                System.out.println();
                //  printFeatures(f, pw, prefix + "    ");
                
            }
            catch(IllegalSymbolException ise){
                System.out.println(ise.getMessage());
            }
        }
    }
    
    private SequenceIterator getSeqIterator(String fileName) throws BioException{
        SequenceIterator it = null;
        try{
            //Get the Sequence Iterator
            Alphabet alpha = ProteinTools.getAlphabet();
            SymbolTokenization protParser = alpha.getTokenization("token");
            BufferedReader br = new BufferedReader(
            new FileReader(fileName));
            
            
            SequenceBuilderFactory sFact = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
            SequenceFormat sFormat = new FastaFormat();
            
            it = new StreamReader(
            br,
            sFormat,
            protParser,
            sFact
            );
        }catch(FileNotFoundException fnfe){
            System.err.println(fnfe.getMessage());
        }
        return it;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        if(args.length < 3){
            usage();
        }
        
        String fastaFile = args[0];
        String mode = "";
        if(!args[1].equals("-mode")){
            usage();
        }
        else{
            mode = args[2];
        }
        
        try{
            new TestDigestIO(fastaFile, mode);
        }
        catch (BioException bioe){
            bioe.printStackTrace();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
        }
        catch (ChangeVetoException cve){
            cve.printStackTrace();
        }
    }
    
    private static void usage(){
        System.out.println("Usage: java prot.TestDigest <fastaFile> -mode [p|m|n]");
        System.out.println("mode p = print, m = get masses, n=just calculate features");
        System.exit(-1);
    }
    
}
