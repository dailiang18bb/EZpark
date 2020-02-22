package ameya.com.maptest;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Card {
    public String title;
    public String cno;
    public String cvv;
    public String expd;
    public String expm;
    public String userid;
    public String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return cno;
    }

    public void setType(String type) {
        this.cno = type;
    }

    public Card(String title, String cno, String cvv, String expd, String expm, String userid,String key) {
        this.title = title;
        this.cno = cno;
        this.cvv = cvv;
        this.key=key;
        this.expd = expd;
        this.expm = expm;
        this.userid = userid;
    }

    public String getCno() {
        return cno;
    }

    public void setCno(String cno) {
        this.cno = cno;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getExpd() {
        return expd;
    }

    public void setExpd(String expd) {
        this.expd = expd;
    }

    public String getExpm() {
        return expm;
    }

    public void setExpm(String expm) {
        this.expm = expm;
    }

    public Card() {
        // Default constructor required for calls to DataSnapshot.getValue(Garage.class)
    }
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("title", title);
        result.put("cno", cno);
        result.put("key",key);
        result.put("expm",expm);
        result.put("expd",expd);
        result.put("cvv",cvv);
        result.put("userid",userid);
        return result;
    }
}
