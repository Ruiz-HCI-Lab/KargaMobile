// -----------------------------------------------------------------------
// <copyright file="MainActivity.java" company="Ruiz HCI Lab">
// Copyright (c) Ruiz HCI Lab. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the repository root for full license information.
// </copyright>
// -----------------------------------------------------------------------
package org.ruizlab.phoni.kargamobile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

//WorkManager
import androidx.lifecycle.LifecycleOwner;
import androidx.work.*;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity{

    private static final boolean TEST = true;

    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int SOURCE_SEARCH = 1;
    private static final int DATABASE_SEARCH = 2;

    Button bSelectSource, bSelectDataBase, bScanMatch, bShowResults;
    TextView tvOutput, tvSelectedSource, tvSelectedDatabase;
    Uri sourceFileUri, dataBaseFileUri;
    ProgressBar pbMatchProgress;
    WorkRequest mapperWorkRequest, analyticsWorkRequest;
    Boolean boolSourceSelected, boolDataBaseSelected, boolScanButton;

    //Method that attaches actions to screen objects
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        Log.v("onCreate", "maxMemory:" + maxMemory);

        bSelectSource = findViewById(R.id.bSelectSourceData);
        bSelectDataBase = findViewById(R.id.bSelectDatabaseData);
        bScanMatch = findViewById(R.id.bScanMatch);
        bShowResults = findViewById(R.id.bShowResults);

        tvOutput = findViewById(R.id.tvOutput);
        tvSelectedSource = findViewById(R.id.tvSourceFile);
        tvSelectedDatabase = findViewById(R.id.tvDatabaseFile);
        pbMatchProgress = findViewById(R.id.pbMatchProgress);

        bSelectSource.setOnClickListener(v -> performSearch(SOURCE_SEARCH));
        bSelectDataBase.setOnClickListener(v -> performSearch(DATABASE_SEARCH));
        bScanMatch.setOnClickListener(v -> performScanMatch());

        //On page load, Scan Button is showing, no Source or Database picked
        boolScanButton = true;
        boolSourceSelected =  false;
        boolDataBaseSelected = false;

        bShowResults.setOnClickListener(
                v -> {
                    Intent i = new Intent(MainActivity.this,Results.class);
                    startActivity(i);
                }
        );

        if (TEST) {
            Button bTest;
            bTest = findViewById(R.id.bTest);
            bTest.setVisibility(View.VISIBLE);
            bTest.setOnClickListener(
                    v -> {
                        Intent i = new Intent(MainActivity.this,TestResults.class);
                        startActivity(i);
                    }
            );
        }
    }

    //Invokes the file search method.
    private void performSearch(Integer activityType)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");

        //Type 1 = Sequence/Source, Type 2 = Reference/Database
        if (activityType == SOURCE_SEARCH) {
            openActivityResultLauncherSource.launch(intent);
        }
        else if (activityType == DATABASE_SEARCH) {
            openActivityResultLauncherDatabase.launch(intent);
        }
    }

    private void checkIfButtonCanBeActivated()
    {
        if (boolSourceSelected && boolDataBaseSelected)
        {
            bScanMatch.setEnabled(true);
        }
    }

    //Calls the method that reads the text from the given URI, which also includes all KARGA functionality
    private void performScanMatch()
    {
        WorkManager workManager = WorkManager.getInstance(getApplicationContext());

        if (boolScanButton){
            Data myParameters = new Data.Builder()
                    .putString(Mapper.KEY_SOURCE,sourceFileUri.toString())
                    .putString(Mapper.KEY_DATA,dataBaseFileUri.toString())
                    .build();

            try {
                mapperWorkRequest = new OneTimeWorkRequest.Builder(Mapper.class)
                                        .setInputData(myParameters)
                                        .build();
                workManager.enqueue(mapperWorkRequest);
                System.out.println("MAPPER STARTING");
                pbMatchProgress.setVisibility(View.VISIBLE);
                bScanMatch.setText(R.string.stop);
                boolScanButton = false;
                if(((Global)getApplicationContext()).analyticsAreEnabled())
                {
                    analyticsWorkRequest = new OneTimeWorkRequest.Builder(Analytics.class)
                            .build();
                    workManager.enqueue(analyticsWorkRequest);
                    System.out.println("ANALYTICS STARTING");
                }
                workManager.getWorkInfoByIdLiveData(mapperWorkRequest.getId())
                        .observe((LifecycleOwner) this, workInfo -> {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                Toast.makeText(getApplicationContext(),"READ FINALIZED!", Toast.LENGTH_SHORT).show();
                                pbMatchProgress.setVisibility(View.GONE);
                                bScanMatch.setText(R.string.scan_match);
                                boolScanButton = true;
                                bShowResults.setVisibility(View.VISIBLE);
                            }
                        });
                workManager.getWorkInfoByIdLiveData(analyticsWorkRequest.getId())
                        .observe((LifecycleOwner) this, workInfo -> {
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                Toast.makeText(getApplicationContext(), "ANALYTICS DONE!", Toast.LENGTH_SHORT).show();
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            workManager.cancelAllWork();
            System.gc();
            pbMatchProgress.setVisibility(View.GONE);
            bScanMatch.setText(R.string.scan_match);
            boolScanButton = true;
        }
    }

    //After the SEQUENCE/SOURCE file has been picked, this activity executes.
    //It takes the file URI and saves the global variable for later use.
    ActivityResultLauncher<Intent> openActivityResultLauncherSource = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;

                    sourceFileUri = data.getData();
                    String sequenceFileName = getFileName(sourceFileUri);
                    tvSelectedSource.setText(sequenceFileName);
                    ((Global)this.getApplicationContext()).setSequenceFilename(sequenceFileName);
                    boolSourceSelected = true;
                    checkIfButtonCanBeActivated();
                }
            });

    //After the REFERENCE/DATABASE has been picked, this activity executes.
    //It takes the file URI and saves the global variable for later use.
    ActivityResultLauncher<Intent> openActivityResultLauncherDatabase = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;

                    dataBaseFileUri = data.getData();
                    String referenceFileName = getFileName(dataBaseFileUri);
                    tvSelectedDatabase.setText(referenceFileName);
                    ((Global)this.getApplicationContext()).setReferenceFilename(referenceFileName);
                    boolDataBaseSelected = true;
                    checkIfButtonCanBeActivated();
                }
            });

    //This method informs if permission to access files has been granted, or stops the process if not.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission not granted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
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
        return result;
    }
}