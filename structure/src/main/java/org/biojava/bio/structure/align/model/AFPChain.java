/*
 *                    PDB web development code
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
 *
 * Created on Jun 17, 2009
 * Created by ap3
 *
 */

package org.biojava.bio.structure.align.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.align.ce.CeMain;
import org.biojava.bio.structure.align.ce.CeSideChainMain;
import org.biojava.bio.structure.align.seq.SmithWaterman3Daligner;
import org.biojava.bio.structure.align.util.AFPAlignmentDisplay;
import org.biojava.bio.structure.jama.Matrix;


/** a bean to contain the core of an alignment
 * 
 * The FatCat aligner class is working on the AFPChain class.
 * 
 * @author Andreas Prlic
 * 
 *
 */

public class AFPChain implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = -4474029015606617947L;

   public static final String newline = System.getProperty("line.separator");

   String algorithmName;

   String version;

   String name1;
   String name2;
   long ioTime;
   long calculationTime;
   long id;
   // results:
   double alignScore;
   double alignScoreUpdate;
   int afpChainTwiNum;
   // end of results

   // utility
   int minLen ; // the length of the shorter 2 proteins.


   List<AFP> afpSet;
   int[][] afpIndex;
   int[][] afpAftIndex;
   int[][] afpBefIndex;

   Matrix disTable1;
   Matrix disTable2;

   int[] twi = null ; //the number of twists making the best score ending at each AFP

   int afpChainLen;
   int[] afpChainList;
   double[] afpChainTwiBin;
   double[] afpChainTwiList;
   double chainRmsd;

   int chainLen,misLen,gapLen;
   int     blockNum;       //the final block number
   int     blockNumIni;    //block number before block clustering and split
   int     blockNumClu;    //block number after clustering blocks
   int     blockNumSpt;    //block number after spliting blocks
   double[]  blockRmsd;     //the RMSD of each block
   int[]     block2Afp;     //the index of afp for each block
   int[]     blockSize;     //the number of AFPs involved in a block
   double[]  blockScore;    //the score associated with each block
   int[]     blockGap;      //the gaps in each block
   int[]     blockResSize;  //the number of residues involved in a block
   int[][][]     blockResList;//the list of AFP for each block
   Matrix[] blockRotationMatrix;
   Atom[]   blockShiftVector;

   int     focusResn;      //the size of the set
   int[]     focusRes1;     //the residues from protein 1
   int[]     focusRes2;     //the residues from protein 2
   int     focusAfpn;      //the AFP number
   int[]     focusAfpList;  //the AFP list

   boolean shortAlign;

   String [][][] pdbAln; // only needed temp. during XML serialization, since we don;t have coordinates loaded at that time and can map from PDB res numbers to atom positions.
   int[][][] optAln;
   int[] optLen ;
   double[] optRmsd ;
   int optLength;

   char[] alnsymb;
   char[] alnseq1;
   char[] alnseq2;
   int alnLength;
   int alnbeg1;
   int alnbeg2;

   int totalLenIni;
   int totalLenOpt = 0;

   double totalRmsdIni;
   double totalRmsdOpt;

   int ca1Length;
   int ca2Length;

   // this one is special. it comes from the FatCatParameters...
   // default is flexible alignment...
   int maxTra = 5;

   Double conn;
   Double dvar;

   double probability;
   double identity;
   double similarity;
   double normAlignScore;

   int myResultsEQR;
   int myResultsSimilarity1;
   int myResultsSimilarity2;

   public AFPChain(){
      init();
      similarity = -1;
      identity   = -1;
      myResultsEQR = -1;
      myResultsSimilarity1 = -1;
      myResultsSimilarity2 = -1;
      algorithmName = CeMain.algorithmName;
      version = CeMain.version+"";

   }

   public long getId()
   {
      return id;
   }
   public void setId(long id)
   {
      this.id = id;
   }

   public String toCE(Atom[] ca1, Atom[]ca2) {

      if (similarity == -1 || identity == -1)
         calcSimilarity();


      StringBuffer txt = new StringBuffer();

      txt.append("Chain 1: ");
      txt.append(name1);
      txt.append(" (Size=");
      txt.append(ca1.length);
      txt.append(")");
      txt.append(newline);
      txt.append("Chain 2: ");
      txt.append(name2);
      txt.append(" (Size=");
      txt.append(ca2.length);
      txt.append(")");
      txt.append(newline);
      txt.append(newline);
      txt.append(String.format("Alignment length = %d Rmsd = %.2fA Z-Score = %.1f",optLength,totalRmsdOpt,probability));
      txt.append(String.format(" Gaps = %d(%.1f%%) CPU = %d ms. Sequence identities = %.1f%%",gapLen,( gapLen*100.0/optLength),calculationTime,identity*100));

      int     linelen = 70;
      String a;
      String b;



      int     t = 0;
      int     ap = alnbeg1;
      int     bp = alnbeg2;
      int     k, len;

      while((alnLength - t) > 0)      {
         if(alnLength - t > linelen)     len = linelen;
         else    len = alnLength - t;


         //System.err.println("t,len:"+t+":"+len);
         a = new String(alnseq1).substring(t,t+len);
         b = new String(alnseq2).substring(t,t+len);

         //System.err.println("B:" + b);

         /*
			txt.append(newline);
			txt.append(String.format("%14s", " "));

			for(k = 10; k <= len; k += 10)
				txt.append("    .    :");
			if(k <= len + 5) txt.append("    .");
          */

         //String pdb1 = ca1[ap].getParent().getPDBCode();
         //String pdb2 = ca2[bp].getParent().getPDBCode();
         txt.append(newline);
         txt.append(String.format("Chain 1:%5s %s"+newline+"Chain 2:%5s %s",
               (ap+1), a, (bp+1), b));
         txt.append(newline);
         for(k = 0; k < len; k ++)       {
            if(a.charAt(k) != '-') ap ++;
            if(b.charAt(k) != '-') bp ++;
         }
         t += len;

      }
      txt.append(newline);

      txt.append(toRotMat());

      return txt.toString();

   }

   public String toRotMat(){
      StringBuffer txt = new StringBuffer();

      if ( blockRotationMatrix == null || blockRotationMatrix.length < 1)
         return "";

      int blockNum = getBlockNum();
      for ( int blockNr = 0 ; blockNr < blockNum  ; blockNr++){
         Matrix m = blockRotationMatrix[blockNr];
         Atom shift   = blockShiftVector[blockNr];
         if ( blockNum > 1) {
            txt.append("Operations for block " );
            txt.append(blockNr);
            txt.append(newline);
         }
         
         String origString = "orig";
         if ( blockNr > 0)
            origString = (blockNr)+""; 
            
         
         txt.append(String.format("     X"+(blockNr+1)+" = (%9.6f)*X"+ origString +" + (%9.6f)*Y"+ origString +" + (%9.6f)*Z"+ origString +" + (%12.6f)",m.get(0,0),m.get(1,0), m.get(2,0), shift.getX()));
         txt.append( newline); 
         txt.append(String.format("     Y"+(blockNr+1)+" = (%9.6f)*X"+ origString +" + (%9.6f)*Y"+ origString +" + (%9.6f)*Z"+ origString +" + (%12.6f)",m.get(0,1),m.get(1,1), m.get(2,1), shift.getY()));
         txt.append( newline);
         txt.append(String.format("     Z"+(blockNr+1)+" = (%9.6f)*X"+ origString +" + (%9.6f)*Y"+ origString +" + (%9.6f)*Z"+ origString +" + (%12.6f)",m.get(0,2),m.get(1,2), m.get(2,2), shift.getZ()));
         txt.append(newline);
      }
      return txt.toString();
   }

   public String toFatcat(Atom[] ca1, Atom[]ca2){
      StringBuffer txt = new StringBuffer();

      txt.append(String.format("Align %s.pdb %d with %s.pdb %d", name1, ca1Length, name2, ca2Length));
      txt.append(newline);
      if ( isShortAlign()){
         txt.append("Short match");
         return txt.toString();
      }
      //txt.append(String.format("raw-score: %.2f norm.-score: %.2f ", alignScore, normAlignScore));
      txt.append(String.format( "Twists %d ini-len %d ini-rmsd %.2f opt-equ %d opt-rmsd %.2f chain-rmsd %.2f Score %.2f align-len %d gaps %d (%.2f%%)",
            blockNum - 1, totalLenIni, totalRmsdIni, optLength, totalRmsdOpt, chainRmsd, alignScore, 
            alnLength, gapLen, (100.0 * (double)gapLen/(double)alnLength)) );
      txt.append(newline);


      int afpNum = afpSet.size();

      if (similarity == -1 || identity == -1){
         calcSimilarity();
      }
      
      //txt.append(String.format("P-value %.2e Afp-num %d Identity %.2f%% Similarity %.2f%% norm.-score: %.2f"+newline, probability, afpNum, identity * 100, similarity * 100, normAlignScore));

      
      
      if ( algorithmName.equalsIgnoreCase(CeMain.algorithmName) || algorithmName.equalsIgnoreCase(CeSideChainMain.algorithmName) ){
         txt.append(String.format("Z-score %.2f ", probability));
      } else if ( algorithmName.equalsIgnoreCase(SmithWaterman3Daligner.algorithmName)) {

      } else {
         txt.append(String.format("P-value %.2e ",probability));
      }
    
      txt.append(String.format("Afp-num %d Identity %.2f%% Similarity %.2f%%", afpNum, identity * 100, similarity * 100));
      txt.append(newline);

      int i;
      double gap;

      int fragLen = 8 ; // FatCatParameters.DEFAULT_FRAGLEN;
      for(i = 0; i < blockNum; i ++)  {
         gap = (double)blockGap[i] /( (double)blockGap[i] + fragLen * blockSize[i]);
         txt.append(String.format( "Block %2d afp %2d score %5.2f rmsd %5.2f gap %d (%.2f%%)",
               i, blockSize[i], blockScore[i], blockRmsd[i], blockGap[i], gap));
         txt.append(newline);
      }

      int     linelen = 70;
      String a;
      String b;
      String c;


      int     t = 0;
      int     ap = alnbeg1;
      int     bp = alnbeg2;
      int     k, len;
      while((alnLength - t) > 0)      {
         if(alnLength - t > linelen)     len = linelen;
         else    len = alnLength - t;


         //System.err.println("t,len:"+t+":"+len);
         a = new String(alnseq1).substring(t,t+len);
         b = new String(alnseq2).substring(t,t+len);
         c = new String(alnsymb).substring(t,t+len);
         //System.err.println("B:" + b);

         txt.append(newline);
         txt.append(String.format("%14s", " "));


         for(k = 10; k <= len; k += 10)
            txt.append("    .    :");
         if(k <= len + 5) txt.append("    .");

         String pdb1 = ca1[ap].getParent().getPDBCode();
         String pdb2 = ca2[bp].getParent().getPDBCode();
         txt.append(newline);
         txt.append(String.format("Chain 1:%5s %s"+newline +"%14s%s"+newline+"Chain 2:%5s %s",
               pdb1, a, " ", c, pdb2, b));
         txt.append(newline);
         for(k = 0; k < len; k ++)       {
            if(a.charAt(k) != '-') ap ++;
            if(b.charAt(k) != '-') bp ++;
         }
         t += len;

      }
      txt.append(newline);
      if ( algorithmName.equalsIgnoreCase(CeMain.algorithmName) || 
            algorithmName.equalsIgnoreCase(SmithWaterman3Daligner.algorithmName)){
         txt.append("Note: positions are from PDB; | means alignment of identical amino acids, : of similar amino acids ");

      } else {
         txt.append("Note: positions are from PDB; the numbers between alignments are block index");
      }
      txt.append(newline);

      return txt.toString();
   }

   private void calcSimilarity() {
      Map<String,Double> idMap = AFPAlignmentDisplay.calcIdSimilarity(alnseq1,alnseq2,alnLength);

      //probability = idMap.get("probability");
      similarity = idMap.get("similarity");
      identity   = idMap.get("identity");


   }


   public String toDBSearchResult(){
      StringBuffer str = new StringBuffer();

      str.append(getName1());
      str.append("\t");
      str.append(getName2());
      str.append("\t");
      str.append(String.format("%.2f",this.getAlignScore()));
      str.append("\t");		
      if ( algorithmName.equalsIgnoreCase(CeMain.algorithmName)){
         str.append(String.format("%.2f",this.getProbability()));
      } else {
         str.append(String.format("%.2e",this.getProbability()));
      }
      str.append("\t");
      str.append(String.format("%.2f",this.getTotalRmsdOpt()));
      str.append("\t");
      str.append(this.getCa1Length());
      str.append("\t");
      str.append(this.getCa2Length());		
      str.append("\t");
      str.append(this.getSimilarity1());
      str.append("\t");
      str.append(this.getSimilarity2());
      str.append("\t");
      str.append(newline);

      return str.toString();
   }

   /** get the number of structurally equivalent residues
    * 
    * @return
    */
   public int getNrEQR(){

      if (myResultsEQR < 0){
         if ( optLen == null) {
            myResultsEQR = 0;
            return 0;
         }

         int nrEqr = 0;
         for(int bk = 0; bk < blockNum; bk ++)       {        

            for ( int i=0;i< optLen[bk];i++){
               nrEqr++;
            }
         }
         myResultsEQR = nrEqr;
      }
      return myResultsEQR;
   }

   public int getSimilarity1(){
      if ( myResultsSimilarity1 < 0 ) {
         int distance = ca1Length + ca2Length - 2 * getNrEQR();

         int similarity = (ca1Length + ca2Length - distance ) / 2;

         myResultsSimilarity1 = Math.round(similarity /(float) ca1Length * 100);
      } 
      return myResultsSimilarity1;
   }

   public int getSimilarity2(){
      if ( myResultsSimilarity2 < 0 ) {
         int distance = ca1Length + ca2Length - 2 * getNrEQR();

         int similarity = (ca1Length + ca2Length - distance ) / 2;
         myResultsSimilarity2 = Math.round(similarity /(float) ca2Length * 100);
      }
      return myResultsSimilarity2;
   }

   public String toString(){

      //int lA = ca1Length;
      //int lB = ca2Length;
      //int distance = lA + lB - 2 * getNrEQR();

      StringBuffer str = new StringBuffer("");
      str.append("EQR:");
      str.append(getNrEQR());

      str.append("\tLen1:");
      str.append(this.getCa1Length());
      str.append("\tLen2:");
      str.append(this.getCa2Length());
      str.append(String.format("\tscore: %.2f",this.getAlignScore()));
      str.append("\t");		
      if ( algorithmName.equalsIgnoreCase(CeMain.algorithmName) || algorithmName.equalsIgnoreCase(CeSideChainMain.algorithmName)){
         str.append("Z-score:");
         str.append(String.format("%.2f",this.getProbability()));
      } else {
         str.append("Probability:");
         str.append(String.format("%.2e",this.getProbability()));
      }
      str.append("\tRMSD:");
      str.append(String.format("%.2f",this.getTotalRmsdOpt()));

      str.append("\tSim1:");
      str.append(this.getSimilarity1());
      str.append("%\tSim2:");
      str.append(this.getSimilarity2());
      str.append("%");
      str.append(newline);


      return str.toString();
   }

   public boolean isSignificantResult(){
      if ( algorithmName.equalsIgnoreCase(CeMain.algorithmName) || algorithmName.equalsIgnoreCase(CeSideChainMain.algorithmName)){
         if (probability >= 3.5)
            return true;			
      } else {
         if (probability < 0.01)
            return true;
      }
      return false;
   }




   private void init(){
      shortAlign = false;
      afpChainLen = 0;

      afpChainList      = null;
      afpChainTwiBin  = null;
      afpChainTwiList = null;
      chainRmsd=0;
      chainLen = misLen = gapLen = 0;

      blockResSize = null;
      blockScore = null;
      blockGap = null;
      optAln = null;
      pdbAln = null;
      optLen = null;

      optRmsd = null;

      block2Afp = new int[maxTra+1];
      blockSize = new int[maxTra+1];      
      blockRmsd = new double[maxTra+1];
      blockScore = new double[maxTra+1];
      blockGap = new int[maxTra+1];

      blockResSize = new int[maxTra+1];

      afpSet = new ArrayList<AFP>();
      totalLenIni = totalLenOpt = 0;
      totalRmsdIni = totalRmsdOpt = 0.0;

      afpChainTwiNum = 0;
      alignScore = 0;
      alignScoreUpdate = 0;
      conn = new Double(0);
      dvar = new Double(0);
      calculationTime = 0;
   }


   /** used temporarily during XML serialization to track the PDB positions of the alignmnet
    * 
    * @return
    */
   public String[][][] getPdbAln() {
      return pdbAln;
   }


   public void setPdbAln(String[][][] pdbAln) {
      this.pdbAln = pdbAln;
   }


   public Double getConn()
   {
      return conn;
   }


   public void setConn(Double conn)
   {
      this.conn = conn;
   }


   public Double getDVar()
   {
      return dvar;
   }


   public void setDVar(Double dvar)
   {
      this.dvar = dvar;
   }


   /** get the maximum nr of Twists that are allowed...
    * 
    * @return
    */
   public int getMaxTra()
   {
      return maxTra;
   }

   /**
    * Set the maximum number of Twists that are allowed...
    * @param maxTra
    */
   public void setMaxTra(int maxTra)
   {
      this.maxTra = maxTra;
   }


   public double getAlignScore()
   {
      return alignScore;
   }

   public void setAlignScore(double alignScore)
   {
      this.alignScore = alignScore;
   }

   public double getAlignScoreUpdate()
   {
      return alignScoreUpdate;
   }

   public void setAlignScoreUpdate(double alignScoreUpdate)
   {
      this.alignScoreUpdate = alignScoreUpdate;
   }

   public int getAfpChainTwiNum()
   {
      return afpChainTwiNum;
   }

   public void setAfpChainTwiNum(int afpChainTwiNum)
   {
      this.afpChainTwiNum = afpChainTwiNum;
   }

   public int getMinLen()
   {
      return minLen;
   }

   public void setMinLen(int minLen)
   {
      this.minLen = minLen;
   }




   public List<AFP> getAfpSet()
   {
      return afpSet;
   }

   public void setAfpSet(List<AFP> afpSet)
   {
      this.afpSet = afpSet;
   }

   public int[][] getAfpIndex()
   {
      return afpIndex;
   }

   public void setAfpIndex(int[][] afpIndex)
   {
      this.afpIndex = afpIndex;
   }


   public int[][] getAfpAftIndex()
   {
      return afpAftIndex;
   }

   public void setAfpAftIndex(int[][] afpAftIndex)
   {
      this.afpAftIndex = afpAftIndex;
   }


   public int[][] getAfpBefIndex()
   {
      return afpBefIndex;
   }

   public void setAfpBefIndex(int[][] afpBefIndex)
   {
      this.afpBefIndex = afpBefIndex;
   }


   public Matrix getDisTable1()
   {
      return disTable1;
   }

   public void setDisTable1(Matrix disTable1)
   {
      this.disTable1 = disTable1;
   }


   public Matrix getDisTable2()
   {
      return disTable2;
   }

   public void setDisTable2(Matrix disTable2)
   {
      this.disTable2 = disTable2;
   }


   public int[] getTwi()
   {
      return twi;
   }

   public void setTwi(int[] twi)
   {
      this.twi = twi;
   }

   public int getAfpChainLen()
   {
      return afpChainLen;
   }

   public void setAfpChainLen(int afpChainLen)
   {
      this.afpChainLen = afpChainLen;
   }

   public int[] getAfpChainList()
   {
      return afpChainList;
   }

   public void setAfpChainList(int[] afpChainList)
   {
      this.afpChainList = afpChainList;
   }

   public double[] getAfpChainTwiBin()
   {
      return afpChainTwiBin;
   }

   public void setAfpChainTwiBin(double[] afpChainTwiBin)
   {
      this.afpChainTwiBin = afpChainTwiBin;
   }

   public double[] getAfpChainTwiList()
   {
      return afpChainTwiList;
   }

   public void setAfpChainTwiList(double[] afpChainTwiList)
   {
      this.afpChainTwiList = afpChainTwiList;
   }

   public double getChainRmsd()
   {
      return chainRmsd;
   }

   /** The RMSD of the chain of AFPs. Set during AFPCHainer.traceBack();
    * 
    * @param chainRmsd
    */
   public void setChainRmsd(double chainRmsd)
   {
      this.chainRmsd = chainRmsd;
   }

   public int getChainLen()
   {
      return chainLen;
   }

   public void setChainLen(int chainLen)
   {
      this.chainLen = chainLen;
   }

   public int getMisLen()
   {
      return misLen;
   }

   public void setMisLen(int misLen)
   {
      this.misLen = misLen;
   }

   public int getGapLen()
   {
      return gapLen;
   }

   public void setGapLen(int gapLen)
   {
      this.gapLen = gapLen;
   }

   /** The number of blocks in the alignment
    * 
    * @return
    */
   public int getBlockNum()
   {
      return blockNum;
   }

   public void setBlockNum(int blockNum)
   {         
      this.blockNum = blockNum;
   }

   public int getBlockNumIni()
   {
      return blockNumIni;
   }

   public void setBlockNumIni(int blockNumIni)
   {
      this.blockNumIni = blockNumIni;
   }

   public int getBlockNumClu()
   {
      return blockNumClu;
   }

   public void setBlockNumClu(int blockNumClu)
   {
      this.blockNumClu = blockNumClu;
   }

   public int getBlockNumSpt()
   {
      return blockNumSpt;
   }

   public void setBlockNumSpt(int blockNumSpt)
   {
      this.blockNumSpt = blockNumSpt;
   }

   public double[] getBlockRmsd()
   {
      return blockRmsd;
   }

   public void setBlockRmsd(double[] blockRmsd)
   {
      this.blockRmsd = blockRmsd;
   }

   public int[] getBlock2Afp()
   {
      return block2Afp;
   }

   public void setBlock2Afp(int[] block2Afp)
   {
      this.block2Afp = block2Afp;
   }

   public int[] getBlockSize()
   {
      return blockSize;
   }

   public void setBlockSize(int[] blockSize)
   {
      this.blockSize = blockSize;
   }

   public double[] getBlockScore()
   {
      return blockScore;
   }

   public void setBlockScore(double[] blockScore)
   {
      this.blockScore = blockScore;
   }

   public int[] getBlockGap()
   {
      return blockGap;
   }

   public void setBlockGap(int[] blockGap)
   {
      this.blockGap = blockGap;
   }

   public int[] getBlockResSize()
   {
      return blockResSize;
   }

   public void setBlockResSize(int[] blockResSize)
   {
      this.blockResSize = blockResSize;
   }


   /** tracks the residues of the initial blocks (before optimization)
    * 
    * 
    * @return
    */
   public int[][][] getBlockResList()
   {
      return blockResList;
   }

   public void setBlockResList(int[][][] blockResList)
   {
      this.blockResList = blockResList;
   }

   public int getFocusResn()
   {
      return focusResn;
   }

   public void setFocusResn(int focusResn)
   {
      this.focusResn = focusResn;
   }


   public int[] getFocusRes1()
   {
      return focusRes1;
   }

   public void setFocusRes1(int[] focusRes1)
   {
      this.focusRes1 = focusRes1;
   }


   public int[] getFocusRes2()
   {
      return focusRes2;
   }

   public void setFocusRes2(int[] focusRes2)
   {
      this.focusRes2 = focusRes2;
   }

   public int getFocusAfpn()
   {
      return focusAfpn;
   }

   public void setFocusAfpn(int focusAfpn)
   {
      this.focusAfpn = focusAfpn;
   }

   public int[] getFocusAfpList()
   {
      return focusAfpList;
   }

   public void setFocusAfpList(int[] focusAfpList)
   {
      this.focusAfpList = focusAfpList;
   }

   public boolean isShortAlign()
   {
      return shortAlign;
   }

   public void setShortAlign(boolean shortAlign)
   {
      this.shortAlign = shortAlign;
   }

   /** Tracks the Atom positions in the optimal alignment. Note: only considers the equivalent positions, gaps are ignored...
    * first dimension is the block nr
    * second dimension is 0 or 1 (the alignment chain index)
    * third is the position
    * @return
    */
   public int[][][] getOptAln()
   {
      return optAln;
   }

   public void setOptAln(int[][][] optAln)
   {
      this.optAln = optAln;
   }

   public int[] getOptLen()
   {
      return optLen;
   }

   public void setOptLen(int[] optLen)
   {
      this.optLen = optLen;
   }

   public double[] getOptRmsd()
   {
      return optRmsd;
   }

   public void setOptRmsd(double[] optRmsd)
   {
      this.optRmsd = optRmsd;
   }

   public int getOptLength()
   {
      return optLength;
   }

   /** The length of the optimal alignment. Set by AFPOptimizer.optimizeAln().
    * 
    * @param optLength
    */
   public void setOptLength(int optLength)
   {
      this.optLength = optLength;
   }


   public char[] getAlnsymb()
   {
      return alnsymb;
   }

   public void setAlnsymb(char[] alnsymb)
   {
      this.alnsymb = alnsymb;
   }


   public char[] getAlnseq1()
   {
      return alnseq1;
   }

   public void setAlnseq1(char[] alnseq1)
   {
      this.alnseq1 = alnseq1;
   }


   public char[] getAlnseq2()
   {
      return alnseq2;
   }

   public void setAlnseq2(char[] alnseq2)
   {
      this.alnseq2 = alnseq2;
   }


   public int getAlnLength()
   {
      return alnLength;
   }

   public void setAlnLength(int alnLength)
   {
      this.alnLength = alnLength;
   }

   public int getAlnbeg1()
   {
      return alnbeg1;
   }

   public void setAlnbeg1(int alnbeg1)
   {
      this.alnbeg1 = alnbeg1;
   }

   public int getAlnbeg2()
   {
      return alnbeg2;
   }

   public void setAlnbeg2(int alnbeg2)
   {
      this.alnbeg2 = alnbeg2;
   }

   public int getTotalLenIni()
   {
      return totalLenIni;
   }

   public void setTotalLenIni(int totalLenIni)
   {
      this.totalLenIni = totalLenIni;
   }

   public int getTotalLenOpt()
   {
      return totalLenOpt;
   }

   public void setTotalLenOpt(int totalLenOpt)
   {
      this.totalLenOpt = totalLenOpt;
   }

   public double getTotalRmsdIni()
   {
      return totalRmsdIni;
   }

   /** this is the ini-RMSD
    * 
    * @param totalRmsdIni
    */
   public void setTotalRmsdIni(double totalRmsdIni)
   {
      this.totalRmsdIni = totalRmsdIni;
   }


   public double getTotalRmsdOpt()
   {
      return totalRmsdOpt;
   }

   public void setTotalRmsdOpt(double totalRmsdOpt)
   {
      this.totalRmsdOpt = totalRmsdOpt;
   }


   public String getName1()
   {
      return name1;
   }


   public void setName1(String name1)
   {
      this.name1 = name1;
   }



   public String getName2()
   {
      return name2;
   }

   public void setName2(String name2)
   {
      this.name2 = name2;
   }


   public long getCalculationTime()
   {
      return calculationTime;
   }

   public void setCalculationTime(long calculationTime)
   {
      this.calculationTime = calculationTime;
   }

   public int getCa1Length()
   {
      return ca1Length;
   }

   public void setCa1Length(int ca1Length)
   {
      this.ca1Length = ca1Length;
   }

   public int getCa2Length()
   {
      return ca2Length;
   }

   public void setCa2Length(int ca2Length)
   {
      this.ca2Length = ca2Length;
   }

   public long getIoTime()
   {
      return ioTime;
   }

   public void setIoTime(long ioTime)
   {
      this.ioTime = ioTime;
   }

   public double getProbability()
   {
      return probability;
   }

   public void setProbability(double probability)
   {
      this.probability = probability;
   }

   public double getIdentity() {
      if ( identity < 0)
         calcSimilarity();
      return identity;
   }


   public void setIdentity(double identity) {
      this.identity = identity;
   }


   public double getSimilarity() {
      if ( similarity < 0)
         calcSimilarity();
      return similarity;
   }


   public void setSimilarity(double similarity) {
      this.similarity = similarity;
   }


   public double getNormAlignScore()
   {
      return normAlignScore;
   }

   public void setNormAlignScore(double normAlignScore)
   {
      this.normAlignScore = normAlignScore;
   }

   public Matrix[] getBlockRotationMatrix()
   {
      return blockRotationMatrix;
   }

   public void setBlockRotationMatrix(Matrix[] blockRotationMatrix)
   {
      this.blockRotationMatrix = blockRotationMatrix;
   }

   public Atom[] getBlockShiftVector()
   {
      return blockShiftVector;
   }

   public void setBlockShiftVector(Atom[] blockShiftVector)
   {
      this.blockShiftVector = blockShiftVector;
   }

   public String getAlgorithmName() {
      return algorithmName;
   }

   public void setAlgorithmName(String algorithmName) {
      this.algorithmName = algorithmName;
   }

   public String getVersion() {
      return version;
   }

   public void setVersion(String version) {
      this.version = version;
   }





}