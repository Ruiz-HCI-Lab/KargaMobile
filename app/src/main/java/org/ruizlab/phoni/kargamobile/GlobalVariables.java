// -----------------------------------------------------------------------
// <copyright file="GlobalVariables.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;

import android.app.Application;

public class GlobalVariables extends Application {

    //Default value of k is set to 17
    private int kValue = 17;

    public int getKValue() {
        return kValue;
    }

    public void setKValue(int kValue) {
        this.kValue = kValue;
    }
}