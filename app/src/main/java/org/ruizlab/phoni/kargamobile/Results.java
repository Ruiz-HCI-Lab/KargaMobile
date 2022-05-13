// -----------------------------------------------------------------------
// <copyright file="Results.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.HashMap;


public class Results extends AppCompatActivity implements GeneListAdapter.ItemClickListener{

    GeneListAdapter adapter;
    Slider coverageSlider;
    Float coverageValue;
    HashMap<String,Integer> classMap;
    ArrayList<String> finalGeneList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        getSupportActionBar().hide();

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvGeneList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        finalGeneList = ((Global)this.getApplicationContext()).getFinalGeneList();

        adapter = new GeneListAdapter(this, finalGeneList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        classMap = new HashMap<>();
        this.setClassMap();

        coverageSlider = findViewById(R.id.sSlider);
        coverageSlider.setVisibility(View.INVISIBLE);

        /* Work in process:
        Add a coverage map to the geneList so we can update what genes to be shown in the list
        coverageSlider.setValue(((Global)this.getApplicationContext()).getCoverageValue());

        String coverageValue[] = geneData[1].split("%",2);
        this.mCoverage.put(position,Float.parseFloat(coverageValue[0]));


        coverageSlider.addOnChangeListener((coverageSlider, value, fromUser) -> {

            adapter.notifyDataSetChanged();
        });
        */

    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    public void setClassMap() {
        finalGeneList.forEach((n)-> {
            String[] geneData, geneId;
            String geneKey;

            geneData = n.split(",",3);
            geneId = geneData[0].split("\\|",5);
            geneKey = geneId[2];

            classMap.merge(geneKey, 1, Integer::sum);
        });
    }

}