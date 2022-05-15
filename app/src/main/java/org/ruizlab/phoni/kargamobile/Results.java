// -----------------------------------------------------------------------
// <copyright file="Results.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.google.android.material.tabs.TabLayout;


public class Results extends AppCompatActivity {

    FrameLayout flFrame;
    TabLayout tlTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        getSupportActionBar().hide();

        //Set up the interface objects
        flFrame = findViewById(R.id.flFrame);
        tlTabs = findViewById(R.id.tlTabs);

        //Tab selection logic
        tlTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    Fragment fragment = null;
                    switch (tab.getPosition()) {
                        case 0:
                            fragment = new GenesFragment();
                            break;
                        case 1:
                            fragment = new ClassesFragment();
                            break;
                        case 2:
                            fragment = new ExportFragment();
                            break;
                    }
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.flFrame, fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
        });

        //Create multiple tabs
        TabLayout.Tab firstTab = tlTabs.newTab();
        firstTab.setText("Genes");
        tlTabs.addTab(firstTab);
        TabLayout.Tab secondTab = tlTabs.newTab();
        secondTab.setText("Classes");
        tlTabs.addTab(secondTab);
        TabLayout.Tab thirdTab = tlTabs.newTab();
        thirdTab.setText("Export");
        tlTabs.addTab(thirdTab);

    }
}