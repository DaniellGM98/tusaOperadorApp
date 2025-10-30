package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Ticket {
    private int id_geo;
    private String fecha;

    public Ticket(ResultSet info){
        super();
        try {
            this.setId_geo(info.getInt("id_geo"));
            this.setFecha(info.getString("fecha"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Ticket(JSONObject info) {
        super();

        try {
            this.setId_geo(info.getInt("id_geo"));
            this.setFecha(info.getString("fecha"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("id_geo",this.getId_geo());
        obj.put("fecha",this.getFecha());
        return obj;
    }

    public int getId_geo() {
        return id_geo;
    }

    public void setId_geo(int Id_geo) {
        this.id_geo = Id_geo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
