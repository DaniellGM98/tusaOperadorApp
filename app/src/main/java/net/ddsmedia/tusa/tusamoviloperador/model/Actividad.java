package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Actividad {
    private int desde, hasta;
    private String actividad;

    public Actividad(){
    }

    public Actividad(JSONObject info) {
        super();
        try {
            this.setDesde(info.getInt("desde"));
            this.setHasta(info.getInt("hasta"));
            this.setActividad(info.getString("actividad"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("desde",this.getDesde());
        obj.put("hasta",this.getHasta());
        obj.put("actividad",this.getActividad());
        return obj;
    }

    public int getDesde() {
        return desde;
    }

    public void setDesde(int Desde) {
        this.desde = Desde;
    }

    public int getHasta() {
        return hasta;
    }

    public void setHasta(int Hasta) { this.hasta = Hasta; }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }
}
