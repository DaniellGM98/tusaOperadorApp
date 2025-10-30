package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Forma {
    private String Forma_tanque;

    public Forma(ResultSet info){
        super();
        try {
            this.setForma_tanque(info.getString("Forma_tanque"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Forma(JSONObject info) {
        super();
        try {
            this.setForma_tanque(info.getString("Forma_tanque"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Forma_tanque",this.getForma_tanque());
        return obj;
    }

    public String getForma_tanque() {
        return Forma_tanque;
    }

    public void setForma_tanque(String Forma_tanque) {
        this.Forma_tanque = Forma_tanque;
    }
}
