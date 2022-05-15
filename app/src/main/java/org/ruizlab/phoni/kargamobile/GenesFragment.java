package org.ruizlab.phoni.kargamobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;

public class GenesFragment extends Fragment implements GeneListAdapter.ItemClickListener{
    GeneListAdapter adapter;
    Slider coverageSlider;
    Float coverageValue;
    ArrayList<String> finalGeneList;
    Integer geneCount;

    public GenesFragment() {
    // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_genes, container, false);

        RecyclerView recyclerView = rootView.findViewById(R.id.rvGeneList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Populate gene list in recyclerview
        finalGeneList = ((Global) requireActivity().getApplicationContext()).getFinalGeneList();

        TextView geneCountValue = rootView.findViewById(R.id.tvGeneCountValue);
        geneCount = finalGeneList.size();
        geneCountValue.setText(String.valueOf(geneCount));

        adapter = new GeneListAdapter(requireActivity().getApplicationContext(), finalGeneList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);


        /* Work in process:
        Add a slider geneList so we can update what genes to be shown in the list
        needs a coverageMap

        coverageSlider = rootView.findViewById(R.id.sSlider);
        coverageSlider.setVisibility(View.INVISIBLE);
        coverageSlider.setValue(((Global)this.getApplicationContext()).getCoverageValue());

        String coverageValue[] = geneData[1].split("%",2);
        this.mCoverage.put(position,Float.parseFloat(coverageValue[0]));


        coverageSlider.addOnChangeListener((coverageSlider, value, fromUser) -> {

            adapter.notifyDataSetChanged();
        });
        */

        return rootView;
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(requireActivity().getApplicationContext(), "You clicked \n" + adapter.getItem(position) + "\n" + "on row number " + position, Toast.LENGTH_SHORT).show();
    }

}
