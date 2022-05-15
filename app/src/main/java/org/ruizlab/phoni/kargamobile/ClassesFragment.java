package org.ruizlab.phoni.kargamobile;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ClassesFragment extends Fragment implements OnChartValueSelectedListener {

    ArrayList<String> finalGeneList,xValues;
    BarChart chartClasses;
    ArrayList<BarEntry> barEntriesArrayList;
    BarData barData;
    BarDataSet barDataSet;

    public ClassesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_classes, container, false);

        //Populate class list in graphs
        this.setClassMap();
        barDataSet = new BarDataSet(barEntriesArrayList, "Gene Classes");
        barDataSet.setColor(Color.parseColor("#0021A5"));
        barDataSet.setHighLightAlpha(255);
        barDataSet.setHighLightColor(Color.parseColor("#FA4616"));
        barData = new BarData(barDataSet);

        // Inflate the layout for this fragment
        chartClasses = rootView.findViewById(R.id.chartClasses);
        chartClasses.setOnChartValueSelectedListener(this);

        XAxis xaxis = chartClasses.getXAxis();
        xaxis.setDrawGridLines(false);
        xaxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xaxis.setGranularity(1f);
        xaxis.setDrawLabels(false);
        xaxis.setDrawAxisLine(false);
        xaxis.setValueFormatter(new IndexAxisValueFormatter(xValues));

        chartClasses.setDrawBarShadow(false);
        chartClasses.setDrawValueAboveBar(true);
        chartClasses.getDescription().setEnabled(false);
        chartClasses.setDrawGridBackground(false);
        chartClasses.setData(barData);

        ClassMarkerView mv = new ClassMarkerView(rootView.getContext(), new IndexAxisValueFormatter(xValues) );
        mv.setChartView(chartClasses); // For bounds control
        chartClasses.setMarker(mv);

        return rootView;
    }

    public void setClassMap() {

        xValues = new ArrayList<>();
        HashMap<String,Integer> classMap = new HashMap<>();
        barEntriesArrayList = new ArrayList<>();

        finalGeneList = ((Global) requireActivity().getApplicationContext()).getFinalGeneList();

        finalGeneList.forEach((n)-> {
            String[] geneData, geneId;
            String geneKey;

            geneData = n.split(",",3);
            geneId = geneData[0].split("\\|",5);
            geneKey = geneId[2];

            classMap.merge(geneKey, 1, Integer::sum);
        });

        HashMap<String,Integer> sortedClassMap = sortByValues(classMap);
        xValues.add("");
        AtomicInteger counter = new AtomicInteger();
        sortedClassMap.forEach((x,y)->{
            barEntriesArrayList.add(new BarEntry(counter.incrementAndGet(),y));
            xValues.add(x);
        });


    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private static HashMap sortByValues(HashMap<String,Integer> map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });
        Collections.reverse(list);

        HashMap sortedHashMap = new LinkedHashMap();

        //Only show the top 5 elements
        int counter = 5;

        for (Object o : list) {
           if (counter > 0) {
               Map.Entry entry = (Map.Entry) o;
               sortedHashMap.put(entry.getKey(), entry.getValue());
               counter --;
           }
        }
        return sortedHashMap;
    }
}