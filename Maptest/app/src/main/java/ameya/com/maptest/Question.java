package ameya.com.maptest;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Question {
    String uid;
    String question;
    String userdetails;

    public Question() {
        // Default constructor required for calls to DataSnapshot.getValue(Garage.class)
    }
    public Question(String uid, String question, String userdetails) {
        this.uid = uid;
        this.question = question;
        this.userdetails = userdetails;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUserdetails() {
        return userdetails;
    }

    public void setUserdetails(String userdetails) {
        this.userdetails = userdetails;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("userdetails", userdetails);
        result.put("uid",uid);
        return result;
    }
}
