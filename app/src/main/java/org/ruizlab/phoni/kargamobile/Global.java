// -----------------------------------------------------------------------
// <copyright file="GlobalVariables.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;

import android.annotation.SuppressLint;
import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.util.ArrayList;
import java.util.HashMap;

public class Global extends Application {

    //Default value of k is set to 17
    private int kValue = 17;
    private float coverageValue = 80;
    private ArrayList<String> finalGeneList;
    private Uri mappedGenesUri;
    private Boolean mapperIsRunning = false;
    private Boolean analyticsStatus = true;
    private ArrayList<String> analyticsData;

    public int getKValue() {
        return kValue;
    }
    public void setKValue(int kValue) {
        this.kValue = kValue;
    }

    public float getCoverageValue() {
        return coverageValue;
    }
    public void setCoverageValue(float coverageValue) {
        this.coverageValue = coverageValue;
    }

    public ArrayList<String> getFinalGeneList() {
        return finalGeneList;
    }
    public void setFinalGeneList(ArrayList<String> finalGeneList) { this.finalGeneList = finalGeneList; }

    public Uri getMappedGenesUri() {
        return mappedGenesUri;
    }
    public void setMappedGenesUri(Uri mappedGenesUri) {
        this.mappedGenesUri = mappedGenesUri;
    }

    public Boolean mapperIsRunning() {
        return mapperIsRunning;
    }
    public void startMapper() {
        this.mapperIsRunning = Boolean.TRUE;
    }
    public void stopMapper() {
        this.mapperIsRunning = Boolean.FALSE;
    }

    public Boolean analyticsAreEnabled() {return analyticsStatus;}
    public void enableAnalytics() {
        this.analyticsStatus = Boolean.TRUE;
    }
    public void disableAnalytics() {
        this.analyticsStatus = Boolean.FALSE;
    }

    public ArrayList<String> getAnalyticsData() {
        return analyticsData;
    }
    public void setAnalyticsData(ArrayList<String> analyticsData) { this.analyticsData = analyticsData; }


}