
package com.parkpal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HostApplicationFragment extends Fragment {

    private DatabaseReference myRef;
    Button tor;
    CheckBox chktor;
    private FirebaseDatabase mFirebaseDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_hostapplication, null);


    }

    @Override
    public void onViewCreated(@NonNull View myView, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        myView.findViewById(R.id.torcheck).setVisibility(View.GONE);
        myView.findViewById(R.id.accpTor).setVisibility(View.GONE);
        myView.findViewById(R.id.textLayout).setVisibility(View.GONE);
        myView.findViewById(R.id.accepted).setVisibility(View.GONE);

        tor = (Button) myView.findViewById(R.id.accpTor);
        chktor = (CheckBox)myView.findViewById(R.id.torcheck);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentFirebaseUser.getUid();

        myRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.child("isHost").getValue(boolean.class)){
                    myView.findViewById(R.id.accepted).setVisibility(View.VISIBLE);

                }else{
                    myView.findViewById(R.id.torcheck).setVisibility(View.VISIBLE);
                    myView.findViewById(R.id.accpTor).setVisibility(View.VISIBLE);
                    myView.findViewById(R.id.textLayout).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chktor.isChecked())
                {
                    myRef.child("users").child(uid).child("isHost").setValue(true);
                    Toast.makeText(getActivity(), "Request sent", Toast.LENGTH_LONG).show();
                    myView.findViewById(R.id.torcheck).setVisibility(View.GONE);
                    myView.findViewById(R.id.accpTor).setVisibility(View.GONE);
                    myView.findViewById(R.id.textLayout).setVisibility(View.GONE);
                    myView.findViewById(R.id.accepted).setVisibility(View.VISIBLE);

                    NavigationView nav_view = (NavigationView)getActivity().findViewById(R.id.nav_view);
                    Menu nav_Menu = nav_view.getMenu();
                    nav_Menu.findItem(R.id.myReservations).setVisible(true);
                    nav_Menu.findItem(R.id.manageParking).setVisible(true);
                    nav_Menu.findItem(R.id.hostReservations).setVisible(true);




                }
                else{
                    Toast.makeText(getActivity(), "You have to accept TOR to continue", Toast.LENGTH_SHORT).show();
                }
            }
        });

        super.onViewCreated(myView, savedInstanceState);
    }
}
