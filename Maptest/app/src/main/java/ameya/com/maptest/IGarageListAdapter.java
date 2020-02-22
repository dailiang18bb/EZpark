package ameya.com.maptest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static android.content.ContentValues.TAG;


public class IGarageListAdapter extends ArrayAdapter<IGarageParking> {
    private final Context context;
    private final ArrayList<IGarageParking> values;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private String uid;
    private String garagename;



    public IGarageListAdapter(Context context, ArrayList<IGarageParking> values,String uid,String s) {
        super(context, -1, values);
        Log.d(TAG, "IGarageListAdapter: inside adapter");
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.context = context;
        this.values = values;
        this.uid=uid;
        this.garagename=s;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.igarage_list_item, parent, false);
        TextView tv_title = rowView.findViewById(R.id.parking_title);
        TextView tv_dimen = rowView.findViewById(R.id.parking_dimen);
        TextView tv_type = rowView.findViewById(R.id.parking_type);
        TextView tv_time = rowView.findViewById(R.id.parking_time);
        Button book_btn=rowView.findViewById(R.id.book_btn);
        Button remove_btn=rowView.findViewById(R.id.remove_btn);
        if(values!=null){
            if(values.get(position).status==1){
                if(values.get(position).userid.equals(auth.getUid())){
                    book_btn.setText("Unbook");
                    book_btn.setBackgroundColor(Color.YELLOW);}
                else{
                    book_btn.setBackgroundColor(Color.RED);
                    book_btn.setEnabled(false);
                    book_btn.setText("Booked");}
            }
            tv_title.setText(values.get(position).title);
            tv_dimen.setText(values.get(position).dimen);
            tv_type.setText(values.get(position).type);
            tv_time.setText("Available for:"+values.get(position).time+"hrs");
            if(!(uid.equals(auth.getUid()))){
                remove_btn.setVisibility(View.INVISIBLE);
            }
            book_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bookparking(values.get(position).key,values.get(position).status,garagename,values.get(position).title);
                }
            });
            remove_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeparking(values.get(position).key);
                }
            });}
        // change the icon for Windows and iPhone
        //String s = values.get(position).title;
        // if (s.startsWith("iPhone")) {
        //      imageView.setImageResource(R.drawable.no);
        // } else {
        //      imageView.setImageResource(R.drawable.ok);
        // }

        return rowView;
    }

    private void removeparking(String key) {
        mDatabase.child("iparkings").child(key).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postsnapshot :dataSnapshot.getChildren()) {

                    String key = postsnapshot.getKey();
                    dataSnapshot.getRef().removeValue();}
                notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Remove Event cancelled");

            }
        });
    }
    private void bookparking(final String key,int status,String s,String d) {
        if(status==0)
            mDatabase.child("iparkings").child(key).child("status").setValue(1);
            else
            mDatabase.child("iparkings").child(key).child("status").setValue(0);


        mDatabase.child("iparkings").child(key).child("userid").setValue(auth.getUid());
                        Log.d(TAG, "ibookparking: Parking booked");
        Toast.makeText(context,"Parking rented successfully!",Toast.LENGTH_SHORT).show();
        Map<String,Object> childupdates = new HashMap<>();
        childupdates.put(key,"Booked at Individual Garage name "+ s+" at Parking spot "+ d);
        mDatabase.child("Transactions").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates);
        Map<String,Object> childupdates1 = new HashMap<>();
        childupdates1.put(key,"$25- Individual Garage name  "+s+" at Parking spot "+ d );
        mDatabase.child("Usage").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates1);
                        notifyDataSetChanged();



    }
}

