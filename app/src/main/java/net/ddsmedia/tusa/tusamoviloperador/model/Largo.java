package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Largo {
    private String Largo_in;

    public Largo(ResultSet info){
        super();
        try {
            this.setLargo_in(info.getString("Largo_in"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Largo(JSONObject info) {
        super();
        try {
            this.setLargo_in(info.getString("Largo_in"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Largo_in",this.getLargo_in());
        return obj;
    }

    public String getLargo_in() {
        return Largo_in;
    }

    public void setLargo_in(String Largo_in) {
        this.Largo_in = Largo_in;
    }
}
