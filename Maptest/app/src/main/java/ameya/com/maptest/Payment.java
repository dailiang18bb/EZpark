package ameya.com.maptest;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Payment extends AppCompatActivity {
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    ListView listView;
    PaymentListAdapter paymentListAdapter;
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference mstorage = storage.getReference();
    private String TAG="Payment";
    private ArrayList<Card> cardlist=new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_det_layot);
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        Button addcbtn=findViewById(R.id.addcbtn);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(Payment.this, Login.class));
                    finish();
                }
            }
        };
        listView=findViewById(R.id.paylist);
        paymentListAdapter=new PaymentListAdapter(getApplicationContext(),cardlist,auth.getUid());
        listView.setAdapter(paymentListAdapter);

        mDatabase.child("/user-data/").child(auth.getUid()).child("/cards/").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                cardlist.clear();
                cardlist.add(dataSnapshot.getValue(Card.class));
                paymentListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                cardlist.add(dataSnapshot.getValue(Card.class));
                    paymentListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                cardlist.remove(dataSnapshot.getValue(Card.class));
                paymentListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mDatabase.child("/user-data/").child(auth.getUid()).child("/cards/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                paymentListAdapter.notifyDataSetChanged();
                cardlist.clear();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Log.d(TAG, "onChildChanged: ");
                    cardlist.add(ds.getValue(Card.class));}
                    paymentListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        addcbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addcard();
            }
        });
    }
    void addcard()
    {
        final Dialog dg =new Dialog(Payment.this);
        dg.setContentView(R.layout.pay_info_item);
        // Set up the input
        final EditText et_ctitle = dg.findViewById(R.id.et_ctitle);
        et_ctitle.setEnabled(true);
        final EditText et_cno = dg.findViewById(R.id.et_cno);
        et_cno.setEnabled(true);
        final EditText et_expm = dg.findViewById(R.id.et_expm);
        et_expm.setEnabled(true);
        final EditText et_expd = dg.findViewById(R.id.et_expd);
        et_expd.setEnabled(true);
        final EditText et_cvv = dg.findViewById(R.id.et_cvv);
        et_cvv.setEnabled(true);
        final Button edit_btn =dg.findViewById(R.id.edit_card_btn);
        edit_btn.setVisibility(View.GONE);
        Button pos = dg.findViewById(R.id.save_card_button);
        pos.setEnabled(true);
        Button ng =dg.findViewById(R.id.rmv_card_btn);
        ng.setText("Cancel");
        //final EditText input3 = new EditText(getApplicationContext());
        //input3.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        //input3.setHint("Enter Type Title");

        pos.setOnClickListener(new View.OnClickListener() {
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
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy", Locale.US);
                        dateFormat.setLenient(false);
                        Date date =dateFormat.parse(expm+"/"+expd);
                        String key =  mDatabase.child("/user-data/" + "/" + auth.getUid() + "/cards/").push().getKey();
                        Card post = new Card(cnam,cno,cvv,expd ,expm,auth.getUid(),key);
                        //Log.d(TAG, "writeNewGaragePark: "+gkey);
                        Map<String, Object> postValues = post.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("/user-data/" + "/" + auth.getUid() + "/cards/"+key, postValues);
                        mDatabase.updateChildren(childUpdates);
                        dg.cancel();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(),"Enter Correct Year and  Month",Toast.LENGTH_LONG).show();

                    }

            }
                else
                    Toast.makeText(getApplicationContext(),"All Fields are mandatory",Toast.LENGTH_LONG).show();

            }
        });
        ng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dg.cancel();
            }
        });
        //AlertDialog alertDialog = builder.create();
        dg.show();
    }
}
