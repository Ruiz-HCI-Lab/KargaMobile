package org.ruizlab.phoni.kargamobile;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class ExportFragment extends Fragment {
    Button bExport;

    public ExportFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_export, container, false);

        bExport = rootView.findViewById(R.id.bExport);

        //Bugged. Error is in URI definition.
        bExport.setOnClickListener(
                v -> {
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, ((Global) requireActivity().getApplicationContext()).getMappedGenesUri());
                    shareIntent.setType("text/csv");
                    startActivity(Intent.createChooser(shareIntent, null));
                    startActivity(shareIntent);
                }
        );

        return rootView;
    }

}