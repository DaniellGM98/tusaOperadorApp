package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Diametro {
    private String Diametro_in;

    public Diametro(ResultSet info){
        super();
        try {
            this.setDiametro_in(info.getString("Diametro_in"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Diametro(JSONObject info) {
        super();
        try {
            this.setDiametro_in(info.getString("Diametro_in"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Diametro_in",this.getDiametro_in());
        return obj;
    }

    public String getDiametro_in() {
        return Diametro_in;
    }

    public void setDiametro_in(String Diametro_in) {
        this.Diametro_in = Diametro_in;
    }
}
