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

import java.util.ArrayList;


public class Results extends AppCompatActivity implements GeneListAdapter.ItemClickListener{

    GeneListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        getSupportActionBar().hide();

        // data to populate the RecyclerView with
        ArrayList<String> geneList = new ArrayList<>();
        geneList.add(">MEG_1000|Drugs|Aminoglycosides|Aminoglycoside_O-nucleotidyltransferases|ANT6");
        geneList.add(">MEG_1002|Drugs|Aminoglycosides|Aminoglycoside_O-nucleotidyltransferases|ANT6");
        geneList.add(">MEG_1003|Drugs|Aminoglycosides|Aminoglycoside_O-nucleotidyltransferases|ANT9");
        geneList.add(">MEG_2419|Drugs|betalactams|Class_A_betalactamases|CTX");
        geneList.add(">MEG_6447|Drugs|betalactams|Class_A_betalactamases|SHV");

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvGeneList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GeneListAdapter(this, geneList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }
}