package com.parkpal;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;



public class GetInfoFragment extends DialogFragment {
    private TextView parkName, parkCirclingTime;
    private Button navigateBtn;
    private String parkNameLocal, parkDetailLocal;
    private TextView txtClose;
    private GoogleMap mMap;

    public GetInfoFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_get_info, container, false);
        getDialog().setTitle("ParkPal");

        parkName = rootView.findViewById(R.id.parkNameText);
        parkCirclingTime = rootView.findViewById(R.id.parkCirclingTime);
        txtClose = rootView.findViewById(R.id.txtClose);
        navigateBtn = rootView.findViewById(R.id.navigateBtn);
        parkName.setText(getArguments().getString("parkName"));
        parkCirclingTime.setText("Circling Time: "+getArguments().getString("parkDetail") +" seconds");


        txtClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("name", getArguments().getString("parkName"));
                bundle.putDouble("parkLat",getArguments().getDouble("parkLat"));
                bundle.putDouble("parkLong",getArguments().getDouble("parkLong"));
                Intent intent = new Intent().putExtras(bundle);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, intent);

                dismiss();
            }
        });

        return rootView;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }
    @Override
    public void onDetach() {
        super.onDetach();

    }
    public interface OnFragmentInteractionListener {

    }
    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
    }
}
