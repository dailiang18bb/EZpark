package ameya.com.maptest;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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

public class GarageListAdapter extends ArrayAdapter<GarageParking> {
    private final Context context;
    private final ArrayList<GarageParking> values;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private String uid;
    private String garagename;



    public GarageListAdapter(Context context, ArrayList<GarageParking> values,String uid,String s) {
        super(context, -1, values);
        Log.d(TAG, "GarageListAdapter: inside adapter");
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
        View rowView = inflater.inflate(R.layout.garage_list_item, parent, false);
        TextView tv_title = rowView.findViewById(R.id.parking_title);
        TextView tv_dimen = rowView.findViewById(R.id.parking_dimen);
        TextView tv_type = rowView.findViewById(R.id.parking_type);
        Button book_btn=rowView.findViewById(R.id.book_btn);
        Button remove_btn=rowView.findViewById(R.id.remove_btn);
        if(values!=null){
        tv_title.setText(values.get(position).title);
        tv_dimen.setText(values.get(position).dimen);
        tv_type.setText(values.get(position).type);
        if(!(uid.equals(auth.getUid()))){
            remove_btn.setVisibility(View.INVISIBLE);
        }
        if(values.get(position).status==1){
            if(values.get(position).userid.equals(auth.getUid())){
                book_btn.setText("Unbook");
                book_btn.setBackgroundColor(Color.YELLOW);}
            else{
                book_btn.setBackgroundColor(Color.RED);
            book_btn.setEnabled(false);
            book_btn.setText("Booked");}

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
        mDatabase.child("parkings").child(key).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postsnapshot :dataSnapshot.getChildren()) {

                    String key = postsnapshot.getKey();
                    dataSnapshot.getRef().removeValue();}
                notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Remove Event cancelled");

            }
        });
    }
    private void bookparking(String key,int status, String s,String d) {
        if(status==1)
            mDatabase.child("parkings").child(key).child("status").setValue(1);
        else
            mDatabase.child("parkings").child(key).child("status").setValue(0);
        mDatabase.child("parkings").child(key).child("userid").setValue(auth.getUid());
        Toast.makeText(context,"Parking Booked",Toast.LENGTH_SHORT).show();
        Log.d(TAG, "bookparking: Parking booked");
        Map<String,Object> childupdates = new HashMap<>();
        childupdates.put(key,"Booked Garage name "+ s+" at Parking spot "+ d);
        mDatabase.child("Transactions").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates);
        Map<String,Object> childupdates1 = new HashMap<>();
        childupdates1.put(key,"$50- Garage name  "+s+" at Parking spot "+ d );
        mDatabase.child("Usage").child(Objects.requireNonNull(auth.getUid())).updateChildren(childupdates1);
        notifyDataSetChanged();
    }
}