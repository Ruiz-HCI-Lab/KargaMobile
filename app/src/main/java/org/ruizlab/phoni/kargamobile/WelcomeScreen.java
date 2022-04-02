// -----------------------------------------------------------------------
// <copyright file="WelcomeScreen.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.ruizlab.phoni.kargamobile.R;

public class WelcomeScreen extends AppCompatActivity {
    Button bNewScan, bConfigurations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

        getSupportActionBar().hide();
        bNewScan = findViewById(R.id.bNewScanMatch);
        bConfigurations = findViewById(R.id.bConfigurations);


        bNewScan.setOnClickListener(
                v -> {
                    Intent i = new Intent(WelcomeScreen.this,MainActivity.class);
                    startActivity(i);
                }
        );
        bConfigurations.setOnClickListener(
                v -> {
                    Intent i = new Intent(WelcomeScreen.this,Configurations.class);
                    startActivity(i);
                }
        );
    }


}