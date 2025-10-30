package net.ddsmedia.tusa.tusamoviloperador.model;


import org.json.JSONException;
import org.json.JSONObject;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Operacion {

    private int Id;
    private String nombre;

    public Operacion(ResultSet info){
        super();
        try {
            this.setId(info.getInt("Id"));
            this.setNombre(info.getString("Nombre"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Operacion(JSONObject info) {
        super();
        try {
            this.setId(info.getInt("Id"));
            this.setNombre(info.getString("indicaciones"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Id",this.getId());
        obj.put("Nombre",this.getNombre());
        return obj;
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
