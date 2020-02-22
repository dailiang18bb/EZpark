package ameya.com.maptest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.support.constraint.Constraints.TAG;

public class ExpandableListDataPump {



    public static HashMap<String, List<String>> getData() {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();
        final List<String> his = new ArrayList<String>();
        final List<String> hist_t = new ArrayList<String>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference db;
        db = FirebaseDatabase.getInstance().getReference();
        db.child("Transactions").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildChanged: inside pump"+dataSnapshot.hasChildren());
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String gparking = postSnapshot.getValue(String.class);
                    his.add(gparking);
                    Log.d(TAG, "onDataChange:mi "+his.isEmpty());
                }
                if(his.isEmpty())
                    his.add("No Transactions");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        db.child("Usage").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String gparking = postSnapshot.getValue(String.class);
                    hist_t.add(gparking);
                    Log.d(TAG, "onDataChange:hist "+hist_t.isEmpty());
                }
                if(hist_t.isEmpty())
                    hist_t.add("No Usage");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        expandableListDetail.put("History of transactions", hist_t);
        expandableListDetail.put("Usage history", his);
        return expandableListDetail;
    }
}