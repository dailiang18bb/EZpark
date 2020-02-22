package ameya.com.maptest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AskQuest extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private DatabaseReference mDatabase;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(AskQuest.this, Login.class));
                    finish();
                }
            }
        };

        setContentView(R.layout.ask_layout);
        Button button =findViewById(R.id.bkbtn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AskQuest.this,Help_home.class));
            }
        });
        Button btn1= findViewById(R.id.sub_quest_btn);
        final EditText editText =findViewById(R.id.et_quest);
        final EditText editText2 =findViewById(R.id.ot_quest);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String et=editText.getText().toString();
                String et2=editText2.getText().toString();
                if(et.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please type your question",Toast.LENGTH_SHORT).show();
                else if(et2.isEmpty())
                    Toast.makeText(getApplicationContext(),"Please type your details",Toast.LENGTH_SHORT).show();
                else
                {
                    String key = mDatabase.child("questions").push().getKey();
                    Question post = new Question(auth.getUid(),et,et2);
                    Log.d("AskQuest", "writeNewQuest:"+et);
                    Map<String, Object> postValues = post.toMap();
                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/questions/"+key, postValues);
                    mDatabase.updateChildren(childUpdates);
                    editText2.getText().clear();
                    editText.getText().clear();
                    Toast.makeText(getApplicationContext(),"Your Question has been submitted",Toast.LENGTH_LONG).show();}
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AskQuest.this,Help_home.class));
    }
}
