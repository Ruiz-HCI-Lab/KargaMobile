// -----------------------------------------------------------------------
// <copyright file="Configurations.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.NumberPicker;

import java.util.Objects;

public class Configurations extends AppCompatActivity {

    private NumberPicker npKValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurations);

        int kValue = ((Global)this.getApplication()).getKValue();

        npKValue = findViewById(R.id.npKValue);
        Button bBack = findViewById(R.id.bBack);

        npKValue.setMinValue(11);
        npKValue.setMaxValue(41);

        npKValue.setValue(kValue);

        npKValue.setOnValueChangedListener((numberPicker, i, i1) -> ((Global)this.getApplication()).setKValue(npKValue.getValue()));

        bBack.setOnClickListener(
                v -> {
                    Intent i = new Intent(Configurations.this,WelcomeScreen.class);
                    startActivity(i);
                }
        );

        Objects.requireNonNull(getSupportActionBar()).hide();
    }
}