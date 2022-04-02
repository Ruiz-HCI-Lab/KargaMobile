// -----------------------------------------------------------------------
// <copyright file="Mapper.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import androidx.annotation.*;
import androidx.work.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

//Class that includes all Anti Microbial Resistant Gene information from the selected database
class AMRGene
{
    public String sequence;
    public float [] mappedK;
    public AMRGene(@NonNull String s)
    {
        sequence=s;
        mappedK=new float[s.length()];
    }
}

// Class that extends the Worker functionality to create the background working thread
public class Mapper extends Worker {

    // Defines the parameter key:
    public static final String KEY_URI = "URI";

    public Mapper(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        Uri uri = Uri.parse(getInputData().getString(KEY_URI));
        try {
            runKarga(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    @Override
    public void onStopped() {

        // Something must be added here so that work actually stops when the button is hit
        super.onStopped();

    }

    /*All following methods come from KARGAM. They have been adapted so that they
        would work correctly with Android requirements, with some efficiency adjustments. */
    public void runKarga(Uri uri) throws Exception
    {

        BufferedReader r = returnBufferedReaderFromMEGARES();

        long time0 = System.currentTimeMillis();
        long startTime;
        long endTime;
        long elapsedTime;

        float allram;
        float usedram;
        Random rando = new Random();
        int k = 17;
        int numT = 25000;

        boolean classifyReads = true;
        boolean reportMultipleHits = false;

        /*KARGA
        // Currently no implementation provided for cmd-invoked parameters, default values used
        for (int t=0; t<args.length; t++)
        {
            //if (args[t].startsWith("d:")) dbfile=args[t].split(":")[1];
            if (args[t].endsWith(".fastq") || args[t].endsWith(".gz")) readfile=args[t];
            if (args[t].startsWith("f:")) readfile=args[t].split(":")[1];
            if (args[t].startsWith("k:")) k=Integer.parseInt(args[t].split(":")[1]);
            if (args[t].startsWith("i:")) numT=Integer.parseInt(args[t].split(":")[1]);
            if (args[t].startsWith("s:")) rando.setSeed(Integer.parseInt(args[t].split(":")[1]));
            if (args[t].equals("r:n") || args[t].equals("r:no")) classifyReads = false;
            if (args[t].equals("r:y") || args[t].equals("r:yes")) classifyReads = true;
            if (args[t].equals("m:n") || args[t].equals("m:no")) reportMultipleHits = false;
            if (args[t].equals("m:y") || args[t].equals("m:yes")) reportMultipleHits = true;
        }
        if (k%2==0) k=k+1; if (k<11) {System.out.println("Minimum value of k must be 11"); k=11;}
        if (readfile.equals("")) {System.out.println("Please specify a read file"); System.exit(0);}
        */

        System.out.println("Reading AMR gene database, creating k-mer mapping (k="+k+")");
        startTime = System.currentTimeMillis();
        HashMap<String, ArrayList<String>> kmerGeneMapping = new HashMap<String,ArrayList<String>>();
        HashMap<String,AMRGene> geneKmerMapping = new HashMap<String,AMRGene>();

        String header = r.readLine();
        long i=0;

        while(true)
        {
            if (header==null) break;
            if (!header.startsWith(">")) {System.out.println("Wrong fasta format"); System.exit(0);}
            String sequence = r.readLine();
            if (sequence==null) break;
            String nextl = r.readLine();
            if (nextl==null) break;
            while(nextl!=null && !nextl.startsWith(">")) {sequence=sequence+nextl; nextl=r.readLine();}

            //This filters only sequences that DO NOT HAVE the RequiresSNPConfirmation in the header
            if (sequence.length()>=k && header.indexOf("RequiresSNPConfirmation")==-1)
            {
                sequence = checkAndAmendRead(sequence);
                AMRGene amrgene = new AMRGene(sequence);

                //This looks for 17 character long strings in the sequence
                for (int g=0; g<sequence.length()-k+1; g++)
                {
                    String fk = sequence.substring(g,g+k);

                    //If the kmer is not in the kmer map, it creates a new temporal arrayList with the current header.
                    //Otherwise, it copies the kmer and header it is already mapped with, and adds the new header into the temporal arrayList object.
                    //Now, that specific kmer maps to 2+ headers.
                    //In both previous cases, it inputs the created mapping (kmer -> header(s)) and adds it to the gene map.
                    //It will do nothing only if the specific mapping of kmer->header already exists in the map.
                    ArrayList<String> al = kmerGeneMapping.get(fk);
                    if (al==null)
                    {
                        al = new ArrayList<String>();
                        al.add(header);
                    }
                    else if (!al.contains(header))
                    {
                        al.add(header);
                    }
                    //Factored this out of the if/else structure
                    kmerGeneMapping.put(fk,al);

                }

                //It maps the AMRGene object to the header, and adds it to the gene to kmer map.
                geneKmerMapping.put(header,amrgene);
            }

            header=nextl;
            if (nextl==null) break;
            i++;
            if (i%100==0) {
                System.out.println("I made it to: "+i);
            }
            if (i%500==0)
            {
                System.gc();
                allram = (float)(Runtime.getRuntime().maxMemory());
                usedram = (float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                System.out.println("\t"+i+" genes processed; used RAM = "+usedram/(1024*1024)+"MB ("+100*usedram/allram+"%)");
            }
        }

        r.close();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println(i+" genes read and k-mers mapped in "+elapsedTime/1000+" seconds");

        System.out.print("Estimating background/random k-mer match distribution");
        startTime = System.currentTimeMillis();


        /*KARGA
        //Currently, it only parses .txt documents.
        if(readfile.endsWith(".gz"))
        {
            r=new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(readfile),DEFAULT_BUFFER_SIZE)),DEFAULT_BUFFER_SIZE);
        }
        else
        {
            r=new BufferedReader(new FileReader(readfile),DEFAULT_BUFFER_SIZE);
        }*/

        //Creates a BufferedReader from the selected testable file
        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(uri);

        System.out.println("Reading file for the 1st time and calculating");
        r = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));

        i=0;
        double avg=0f;
        String line;
        while((line=r.readLine())!=null || i<numT)
        {
            line=r.readLine();
            String fwd = line;
            if (fwd==null) break;
            avg=avg+(double)(fwd.length());
            r.readLine();
            r.readLine();
            i++;
        }
        avg=avg/(double)(i);
        System.out.println(" (average read length is "+Math.round(avg)+" bases)");
        if ( avg<k ) {System.out.println("Average read length too short for the chosen k"); System.exit(0);}
        int [] matchDist = new int [numT];
        System.out.print("\t");

        for (int y=0; y<numT; y++)
        {
            int hf=0;
            int hr=0;
            String fwd = randomString((int)(avg),rando);
            for (int g=0; g<fwd.length()-k+1; g++)
            {
                String fk = fwd.substring(g,g+k);
                if (kmerGeneMapping.get(fk)!=null) {hf=hf+1;}
            }
            String rwd = reverseComplement(fwd);
            for (int g=0; g<rwd.length()-k+1; g++)
            {
                String rk = rwd.substring(g,g+k);
                if (kmerGeneMapping.get(rk)!=null) {hr=hf+1;}
            }
            matchDist[y]=hf;
            if (hr>hf) {matchDist[y]=hr;}
            if (y%(numT/5)==0) System.out.print(y+"..");
        }
        System.out.println();
        Arrays.sort(matchDist);
        int pvalthres=matchDist[99*numT/100];
        System.out.println("99th percentile of random k-mers match distribution is "+pvalthres+" (max is "+matchDist[numT-1]+")");
        r.close();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Empirical distribution for "+numT+" random reads estimated in "+elapsedTime/1000+" seconds");


        startTime = System.currentTimeMillis();
        String fileLocation = "";

        /*NOT MAPPING READS
        System.out.println("Creating file for mapping reads");
        fileLocation = getApplicationContext().getExternalFilesDir(null)+"/"+"_KARGAM_mappedReads.csv";
        FileWriter rfilewriter = new FileWriter(fileLocation);
        BufferedWriter rwriter = new BufferedWriter(rfilewriter);
        rwriter.write("Idx,");
        rwriter.write("GeneProbability/KmersHitsOnGene/KmersHitsOnAllGenes/KmersTotal,");
        rwriter.write("GeneAnnotation");


        if (reportMultipleHits) rwriter.write("s,...");
        rwriter.write("\r\n");
        */

        inputStream = getApplicationContext().getContentResolver().openInputStream(uri);
        r = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));

        i=0;
        while((line=r.readLine())!=null)
        {
            header = line;
            line=r.readLine();
            String fwd = line;
            i++;
            if (line==null) break;
            r.readLine();
            r.readLine();
            fwd = checkAndAmendRead(fwd);
            if (fwd.length()>k)
            {
                int hf=0;
                int hr=0;
                String rwd = reverseComplement(fwd);
                for (int g=0; g<fwd.length()-k+1; g++)
                {
                    String pfk = fwd.substring(g,g+k);
                    String prk = rwd.substring(g,g+k);
                    if (kmerGeneMapping.get(pfk)!=null) {hf++;}
                    if (kmerGeneMapping.get(prk)!=null) {hr++;}
                }
                if (hr>hf) {fwd=rwd;}
                ArrayList<String> kmerhits = new ArrayList<String>();
                HashMap<String,Float> geneHitsWeighted = new HashMap<String,Float>();
                HashMap<String,Integer> geneHitsUnweighted = new HashMap<String,Integer>();
                for (int g=0; g<fwd.length()-k+1; g++)
                {
                    String fk = fwd.substring(g,g+k);
                    ArrayList<String> kmerGenes = kmerGeneMapping.get(fk);
                    if (kmerGenes!=null)
                    {
                        kmerhits.add(fk);
                        for (int y=0; y<kmerGenes.size(); y++)
                        {
                            String key = kmerGenes.get(y);
                            float frac = 1f/(float)(kmerGenes.size());
                            if (geneHitsWeighted.get(key)==null) {geneHitsWeighted.put(key,frac);} else {geneHitsWeighted.put(key,geneHitsWeighted.get(key)+frac);}
                            if (geneHitsUnweighted.get(key)==null) {geneHitsUnweighted.put(key,1);} else {geneHitsUnweighted.put(key,geneHitsUnweighted.get(key)+1);}
                        }
                    }
                }
                if (kmerhits.size()>pvalthres)
                {
                    if (!reportMultipleHits)
                    {
                        List<String> keys = new ArrayList<>(geneHitsWeighted.keySet());
                        Collections.shuffle(keys,rando);
                        float maxGeneFreq = 0;
                        String maxGene="";
                        for (String key : keys)
                        {
                            float curr = geneHitsWeighted.get(key);
                            if (curr>maxGeneFreq) {maxGeneFreq=curr;maxGene=key;}
                        }
                        /*NOT MAPPING READS
                        if (classifyReads)
                        {
                            rwriter.write(header+",");

                            float fr = (float)Math.round(maxGeneFreq*100)/100;
                            fr = fr/kmerhits.size();
                            fr = (float)Math.round(fr*100)/100;
                            rwriter.write(fr+"/"+geneHitsUnweighted.get(maxGene)+"/"+kmerhits.size()+"/"+(fwd.length()-k+1)+",");
                            rwriter.write(maxGene);
                            rwriter.write("\r\n");
                        }
                        */
                        AMRGene genehit = geneKmerMapping.get(maxGene);
                        for (int y=0; y<kmerhits.size(); y++)
                        {
                            String kh = kmerhits.get(y);
                            ArrayList<Integer> khl = new ArrayList();
                            int h1 = genehit.sequence.indexOf(kh);
                            while (h1>=0)
                            {
                                khl.add(h1);
                                h1=genehit.sequence.indexOf(kh,h1+1);
                            }
                            for (int yyy=0; yyy<khl.size(); yyy++)
                            {
                                genehit.mappedK[khl.get(yyy)]+=1f/(float)(khl.size());
                            }
                        }
                    }
                    if (reportMultipleHits)
                    {
                        ArrayList<HashMap.Entry<String,Float>> genehitsarr = new ArrayList<HashMap.Entry<String,Float>>();
                        for (HashMap.Entry<String,Float> e: geneHitsWeighted.entrySet()) {genehitsarr.add(e);}
                        Collections.sort(genehitsarr,sortHashMapByValueFloat);
                        /*NOT MAPPING READS
                        if (classifyReads)
                        {

                            rwriter.write(header+",");
                            float cumul = 0f;
                            for (int y=0; y<genehitsarr.size(); y++)
                            {
                                float fr = genehitsarr.get(y).getValue();
                                fr = ((fr)/(float)(kmerhits.size()));
                                float fp = (float)Math.round(fr*100)/100;
                                rwriter.write(fp+"/"+geneHitsUnweighted.get(genehitsarr.get(y).getKey())+"/"+kmerhits.size()+"/"+(fwd.length()-k+1)+",");
                                rwriter.write(genehitsarr.get(y).getKey());
                                cumul = cumul+fr;
                                if (y>19 || cumul>0.95f) break;
                                rwriter.write(",");
                            }
                            rwriter.write("\r\n");
                        }
                        */
                        float cumul = 0f;
                        for (int y=0; y<genehitsarr.size(); y++)
                        {
                            if (y<=19 && cumul<=0.95f)
                            {
                                AMRGene genehit = geneKmerMapping.get(genehitsarr.get(y).getKey());
                                float fr = genehitsarr.get(y).getValue();
                                fr = (fr)/(float)(kmerhits.size());
                                cumul = cumul+fr;
                                fr = 1;
                                for (int c=0; c<kmerhits.size(); c++)
                                {
                                    String kh = kmerhits.get(c);
                                    ArrayList<Integer> khl = new ArrayList();
                                    int h1 = genehit.sequence.indexOf(kh);
                                    while (h1>=0)
                                    {
                                        khl.add(h1);
                                        h1=genehit.sequence.indexOf(kh,h1+1);
                                    }
                                    for (int yyy=0; yyy<khl.size(); yyy++)
                                    {
                                        genehit.mappedK[khl.get(yyy)]+=fr/(float)(khl.size());
                                    }
                                }
                            }
                        }
                    }
                }
                /*NOT MAPPING READS
                else if (classifyReads)
                    {
                        rwriter.write(header+",");
                        rwriter.write("?/?/?/?,");
                        rwriter.write("?");
                        rwriter.write("\r\n");
                    }

                */
            }
            if (i%100==0) {
                System.out.println("I made it to: "+i);
            }
            if (i%50000==0)
            {
                System.gc();
                allram = (float)(Runtime.getRuntime().maxMemory());
                usedram = (float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                System.out.print(i+" reads processed; used RAM = "+usedram/(1024*1024)+"MB ("+100*usedram/allram+"%); time = "+elapsedTime/1000+" s \r\n");
            }
        }

        r.close();
        /*NOT MAPPING READS
        rwriter.close();
        if (!classifyReads) {File f = new File(fileLocation); f.delete();}
        */

        fileLocation = getApplicationContext().getExternalFilesDir(null)+"/"+"_KARGAM_mappedGenes.csv";

        FileWriter filewriter = new FileWriter(fileLocation);
        BufferedWriter writer = new BufferedWriter(filewriter);
        writer.write("GeneIdx,PercentGeneCovered,AverageKMerDepth,longestCoveredSegment\r\n");
        Collection<String> keysc = geneKmerMapping.keySet();
        ArrayList<String> keys = new ArrayList<String>(keysc);
        Collections.sort(keys);

        for (String key : keys)
        {
            AMRGene ag = geneKmerMapping.get(key);
            double percCovered = 0;
            double kmerDepth = 0;
            int bestStart=0;
            int bestStop=0;
            int sstart = 0;
            int sstop = 0;
            for (int ww=0; ww<ag.mappedK.length; ww++)
            {
                if (ag.mappedK[ww]>=0.999f)
                {
                    sstop=ww;
                    percCovered++;
                }
                else
                {
                    if ( (bestStop-bestStart) < (sstop-sstart) )
                    {
                        bestStart=sstart;
                        bestStop=sstop;
                    }
                    sstart=ww;
                    sstop=ww;
                }
                kmerDepth+=ag.mappedK[ww];
            }
            kmerDepth = kmerDepth/percCovered;
            percCovered = percCovered/(double)(ag.mappedK.length);
            bestStart++; bestStop++;
            if (percCovered>0.01f)
            {
                writer.write(key+",");
                writer.write(100*percCovered+"%,");
                writer.write(kmerDepth+",");
                writer.write(bestStart+"to"+bestStop+",");
                writer.write("\r\n");
            }
        }
        writer.close();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.print("Reads and genes mapped in = "+elapsedTime/1000+" s\r\n");

        endTime = System.currentTimeMillis();
        elapsedTime = endTime - time0;
        System.out.print("Total time employed  = "+elapsedTime/1000+" s\r\n");

    }

    @NonNull
    public static String checkAndAmendRead(@NonNull String s)
    {
        StringBuffer k = new StringBuffer();
        for (int i=0; i<s.length(); i++)
        {
            char c=s.charAt(i);
            if (c=='A' || c=='a') {k.append('A');}
            else
            if (c=='C' || c=='c') {k.append('C');}
            else
            if (c=='G' || c=='g') {k.append('G');}
            else
            if (c=='T' || c=='t' || c=='U' || c=='u') {k.append('T');}
            else
            {k.append('N');}
        }
        return k.toString();
    }

    @NonNull
    public static String reverseComplement(@NonNull String s)
    {
        char[] reverse = new char[s.length()];
        for (int i=0; i<s.length(); i++)
        {
            char c = s.charAt(i);
            if (c=='A') {reverse[(reverse.length-1)-i]='T';}
            else
            if (c=='C') {reverse[(reverse.length-1)-i]='G';}
            else
            if (c=='G') {reverse[(reverse.length-1)-i]='C';}
            else
            if (c=='T') {reverse[(reverse.length-1)-i]='A';}
            else
            if (c=='N') {reverse[(reverse.length-1)-i]='N';}
        }
        return String.valueOf(reverse);
    }


    @NonNull
    public static String randomString(int n, Random r)
    {
        StringBuffer k = new StringBuffer();
        for (int i=0; i<n; i++)
        {
            double d = r.nextDouble();
            if (d<0.000001d) k.append('N');
            else
            {
                d = r.nextDouble();
                if (d<0.25d) k.append('A');
                else if (d<0.5d) k.append('C');
                else if (d<0.75d) k.append('G');
                else k.append('T');
            }
        }
        return k.toString();
    }

    /**
     * Method that compares two hashmap objects by their stored float value
     */
    public static Comparator<HashMap.Entry<String,Float>> sortHashMapByValueFloat = new Comparator<HashMap.Entry<String,Float>>()
    {
        @Override
        public int compare(Map.Entry<String,Float> e1, Map.Entry<String,Float> e2)
        {
            Float f1 = e1.getValue();
            Float f2 = e2.getValue();
            return f2.compareTo(f1);
        }
    };

    /**
     * Temporary method that extracts the string from the MEGARES file in the assets folder of our app
     * @return BufferedReader of the MEGARES file
     */
    public BufferedReader returnBufferedReaderFromMEGARES() throws IOException{
        AssetManager am = getApplicationContext().getAssets();
        InputStream is = am.open("Megares.fasta");
        //Move to global
        final int DEFAULT_BUFFER_SIZE=16384;

        return new BufferedReader(new InputStreamReader(is), DEFAULT_BUFFER_SIZE);
    }



}
