// -----------------------------------------------------------------------
// <copyright file="Mapper.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.*;
import androidx.core.content.FileProvider;
import androidx.work.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

//Class that includes all Anti Microbial Resistant Gene information from the selected reference file
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
public class Mapper extends Worker{

    // Defines the parameter key:
    public static final String KEY_SOURCE = "SOURCE";
    public static final String KEY_DATA = "DATA";

    public Mapper(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        Uri sourceFile = Uri.parse(getInputData().getString(KEY_SOURCE));
        Uri dataFile = Uri.parse(getInputData().getString(KEY_DATA));
        try {
            ((Global)this.getApplicationContext()).mapperStarts();
            runKarga(sourceFile, dataFile);
            ((Global)this.getApplicationContext()).mapperStops();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }

    @Override
    public void onStopped() {

        // TBD: Something must be added here so that work actually stops when the button is hit
        super.onStopped();

    }

    /*All following methods come from KARGAM. They have been adapted so that they
        work correctly with Android requirements, with some efficiency adjustments. */
    public void runKarga(Uri sourceFile, Uri dataFile) throws Exception
    {
        long time0 = System.currentTimeMillis();
        long startTime, endTime, elapsedTime;
        float totalRam, usedRam;
        ArrayList<String> finalGeneList = new ArrayList<>();
        DecimalFormat dfZero = new DecimalFormat("0.00");

        Random randomNumber = new Random();
        int numT = 25000;

        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(dataFile);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));
        String fileLocation;

        int k = ((Global)this.getApplicationContext()).getKValue();
        if (k%2==0) k=k+1; if (k<11) {System.out.println("Minimum value of k must be 11"); k=11;}

        System.out.println("Reading AMR gene database, creating k-mer mapping (k="+k+")");

        startTime = System.currentTimeMillis();

        HashMap<String, ArrayList<String>> kmerGeneMapping = new HashMap<>();
        HashMap<String,AMRGene> geneKmerMapping = new HashMap<>();

        String header = bufferedReader.readLine();
        long i=0;

        while(true)
        {
            if (header==null) break;
            if (!header.startsWith(">")) {System.out.println("Wrong fasta format"); System.exit(0);}
            StringBuilder sequence = new StringBuilder(bufferedReader.readLine());
            String nextl = bufferedReader.readLine();
            if (nextl==null) break;
            while(nextl!=null && !nextl.startsWith(">"))
            {
                sequence.append(nextl); nextl=bufferedReader.readLine();
            }

            //This filters only sequences that DO NOT HAVE the RequiresSNPConfirmation in the header
            if (sequence.length()>=k && !header.contains("RequiresSNPConfirmation"))
            {
                sequence = new StringBuilder(checkAndAmendRead(sequence.toString()));
                AMRGene amrgene = new AMRGene(sequence.toString());

                //For each kmer (k character long string in the sequence)
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
                        al = new ArrayList<>();
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
                totalRam = (float)(Runtime.getRuntime().maxMemory());
                usedRam = (float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                System.out.println("\t"+i+" genes processed; used RAM = "+usedRam/(1024*1024)+"MB ("+100*usedRam/totalRam+"%)");
            }
        }

        bufferedReader.close();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println(i+" genes read and k-mers mapped in "+elapsedTime/1000+" seconds");

        System.out.print("Estimating background/random k-mer match distribution");
        startTime = System.currentTimeMillis();

        //Creates a BufferedReader from the selected testable file
        inputStream = getApplicationContext().getContentResolver().openInputStream(sourceFile);

        System.out.println("Reading file for the 1st time and calculating");
        bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));

        i=0;
        double avg=0f;
        String line;


        while(bufferedReader.readLine() !=null || i<numT)
        {
            line=bufferedReader.readLine();
            String fwd = line;
            if (fwd==null) break;
            avg=avg+(double)(fwd.length());
            bufferedReader.readLine();
            bufferedReader.readLine();
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
            String fwd = randomString((int)(avg),randomNumber);
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
            matchDist[y] = Math.max(hr, hf);
            if (y%(numT/5)==0) System.out.print(y+"..");
        }
        System.out.println();
        Arrays.sort(matchDist);
        int pvalthres=matchDist[99*numT/100];
        System.out.println("99th percentile of random k-mers match distribution is "+pvalthres+" (max is "+matchDist[numT-1]+")");

        bufferedReader.close();
        endTime = System.currentTimeMillis();
        elapsedTime = endTime - startTime;
        System.out.println("Empirical distribution for "+numT+" random reads estimated in "+elapsedTime/1000+" seconds");

        startTime = System.currentTimeMillis();

        inputStream = getApplicationContext().getContentResolver().openInputStream(sourceFile);
        bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream)));

        i=0;
        while(bufferedReader.readLine() !=null)
        {
            line=bufferedReader.readLine();
            String fwd = line;
            i++;
            if (line==null) break;
            bufferedReader.readLine();
            bufferedReader.readLine();
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
                ArrayList<String> kmerhits = new ArrayList<>();
                HashMap<String,Float> geneHitsWeighted = new HashMap<>();
                HashMap<String,Integer> geneHitsUnweighted = new HashMap<>();
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
                            geneHitsWeighted.merge(key, frac, Float::sum);
                            geneHitsUnweighted.merge(key, 1, Integer::sum);
                        }
                    }
                }
                if (kmerhits.size()>pvalthres)
                {
                    List<String> keys = new ArrayList<>(geneHitsWeighted.keySet());
                    Collections.shuffle(keys,randomNumber);
                    float maxGeneFreq = 0;
                    String maxGene="";
                    for (String key : keys)
                    {
                        float curr = geneHitsWeighted.get(key);
                        if (curr>maxGeneFreq) {maxGeneFreq=curr;maxGene=key;}
                    }

                    AMRGene genehit = geneKmerMapping.get(maxGene);
                    for (int y=0; y<kmerhits.size(); y++)
                    {
                        String kh = kmerhits.get(y);
                        ArrayList khl = new ArrayList();
                        assert genehit != null;
                        int h1 = genehit.sequence.indexOf(kh);
                        while (h1>=0)
                        {
                            khl.add(h1);
                            h1=genehit.sequence.indexOf(kh,h1+1);
                        }
                        for (int yyy=0; yyy<khl.size(); yyy++)
                        {
                            genehit.mappedK[(int) khl.get(yyy)]+=1f/(float)(khl.size());
                        }
                    }
                }
            }
            if (i%100==0) {
                System.out.println("I made it to: "+i);
            }
            if (i%50000==0)
            {
                System.gc();
                totalRam = (float)(Runtime.getRuntime().maxMemory());
                usedRam = (float)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                System.out.print(i+" reads processed; used RAM = "+usedRam/(1024*1024)+"MB ("+100*usedRam/totalRam+"%); time = "+elapsedTime/1000+" s \r\n");
            }
        }

        bufferedReader.close();

        fileLocation = getApplicationContext().getExternalFilesDir(null)+"/"+getFileName(sourceFile)+"_KARGAM_mappedGenes.csv";
        System.out.print("File location is: "+fileLocation+"w \r\n");

        FileWriter filewriter = new FileWriter(fileLocation);
        BufferedWriter writer = new BufferedWriter(filewriter);

        writer.write("GeneIdx,PercentGeneCovered,AverageKMerDepth,longestCoveredSegment\r\n");

        Collection<String> keysc = geneKmerMapping.keySet();
        ArrayList<String> keys = new ArrayList<>(keysc);
        Collections.sort(keys);

        for (String key : keys)
        {
            AMRGene aGene = geneKmerMapping.get(key);
            double percCovered = 0;
            double kmerDepth = 0;
            int bestStart=0;
            int bestStop=0;
            int sstart = 0;
            int sstop = 0;
            for (int ww = 0; ww< Objects.requireNonNull(aGene).mappedK.length; ww++)
            {
                if (aGene.mappedK[ww]>=0.999f)
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
                kmerDepth+=aGene.mappedK[ww];
            }
            kmerDepth = kmerDepth/percCovered;
            percCovered = percCovered/(double)(aGene.mappedK.length);
            bestStart++; bestStop++;
            if (percCovered>0.01f)
            {

                finalGeneList.add(key+","+dfZero.format(100*percCovered)+"%,"+kmerDepth);

                writer.write(key+",");
                writer.write(100*percCovered+"%,");
                writer.write(kmerDepth+",");
                writer.write(bestStart+"to"+bestStop+",");
                writer.write("\r\n");
            }
        }
        writer.close();
        ((Global)this.getApplicationContext()).setFinalGeneList(finalGeneList);
        ((Global)this.getApplicationContext()).setMappedGenesUri(FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", new File(fileLocation)));

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
        StringBuilder k = new StringBuilder();
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
        StringBuilder k = new StringBuilder();
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
    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        String[] finalResult;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        System.out.print("Filename is: " +result+" \r\n");
        return result;
    }
}

