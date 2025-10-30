package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Capacidad {
    private String Capacidad_gal;

    public Capacidad(ResultSet info){
        super();
        try {
            this.setCapacidad_gal(info.getString("Capacidad_gal"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Capacidad(JSONObject info) {
        super();
        try {
            this.setCapacidad_gal(info.getString("Capacidad_gal"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Capacidad_gal",this.getCapacidad_gal());
        return obj;
    }

    public String getCapacidad_gal() {
        return Capacidad_gal;
    }

    public void setCapacidad_gal(String Capacidad_gal) {
        this.Capacidad_gal = Capacidad_gal;
    }
}
