package ameya.com.maptest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class PaymentListAdapter extends ArrayAdapter<Card> {
    private final Context context;
    private final ArrayList<Card> values;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private String uid;
    FirebaseUser user;



    public PaymentListAdapter(Context context, ArrayList<Card> values,String uid) {
        super(context, -1, values);
        Log.d(TAG, "CardListAdapter: inside adapter");
        auth = FirebaseAuth.getInstance();
         user = auth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        this.context = context;
        this.values = values;
        this.uid=uid;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        if(convertView!=null)
            return convertView;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.pay_info_item, parent, false);
        final EditText et_ctitle = rowView.findViewById(R.id.et_ctitle);
        final EditText et_cno = rowView.findViewById(R.id.et_cno);
        final EditText et_expm = rowView.findViewById(R.id.et_expm);
        final EditText et_expd = rowView.findViewById(R.id.et_expd);
        final EditText et_cvv = rowView.findViewById(R.id.et_cvv);
        final Button save_btn=rowView.findViewById(R.id.save_card_button);
        Button rmc_btn=rowView.findViewById(R.id.rmv_card_btn);
        final Button edit_btn =rowView.findViewById(R.id.edit_card_btn);
        if(values!=null){
            et_ctitle.setText(values.get(position).title);
            et_cno.setText(values.get(position).cno);
            et_expm.setText(values.get(position).expm);
            et_expd.setText(values.get(position).expd);
            et_cvv.setText(values.get(position).cvv);
            edit_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    et_cno.setEnabled(true);
                    et_ctitle.setEnabled(true);
                    et_cvv.setEnabled(true);
                    et_expd.setEnabled(true);
                    et_expm.setEnabled(true);
                    save_btn.setEnabled(true);
                }
            });

            save_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cno=et_cno.getText().toString().trim();
                    String cnam=et_ctitle.getText().toString().trim();
                    String cvv=et_cvv.getText().toString().trim();
                    String expd=et_expd.getText().toString().trim();
                    String expm=et_expm.getText().toString().trim();
                    if(!(cno.isEmpty() || cnam.isEmpty() || cvv.isEmpty() || expd.isEmpty() || expm.isEmpty()))
                    {
                        try {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd", Locale.US);
                            dateFormat.setLenient(false);
                            Date date =dateFormat.parse(expm+"/"+expd);
                            Log.d(TAG, "onClick: Date is :"+date);
                            if(date!=null)
                            {edit_btn.setVisibility(View.VISIBLE);
                            et_cno.setEnabled(false);
                            et_ctitle.setEnabled(false);
                            et_cvv.setEnabled(false);
                            et_expd.setEnabled(false);
                            et_expm.setEnabled(false);
                            save_btn.setEnabled(false);
                            savechange(cno,cnam,cvv,expd,expm,values.get(position).key);}
                        } catch (ParseException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"Enter Correct Day and  Month",Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                        Toast.makeText(context,"All Fields are mandatory",Toast.LENGTH_LONG).show();
                }
            });
            rmc_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removecard(values.get(position).key);
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

    private void removecard(String key) {
        mDatabase.child("/user-data/" + "/" + user.getUid() + "/cards/"+ key).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    dataSnapshot.getRef().removeValue();
                notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Remove Event cancelled");

            }
        });
        notifyDataSetChanged();
    }
    private void savechange(String cno,String cnam,String cvv,String expm,String expd,String key) {
        if(key!=null)
        {
            mDatabase = mDatabase.child("/user-data/" + "/" + user.getUid() + "/cards/"+ key);
        mDatabase.child("cno").setValue(cno);
        mDatabase.child("title").setValue(cnam);
        mDatabase.child("expd").setValue(expd);
        mDatabase.child("expm").setValue(expm);
        mDatabase.child("cvv").setValue(cvv);}

        notifyDataSetChanged();
    }


}
