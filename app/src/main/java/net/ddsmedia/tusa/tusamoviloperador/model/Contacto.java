package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Contacto {

    private int matricula;
    private String empleado;
    private String linea_asignada;

    public Contacto(ResultSet info){
        super();
        try {
            this.setMatricula(info.getInt("Matricula"));
            this.setEmpleado(info.getString("Empleado"));
            this.setLinea_Asignada(info.getString("Linea_Asignada"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Contacto(JSONObject info) {
        super();

        try {
            this.setMatricula(info.getInt("Matricula"));
            this.setEmpleado(info.getString("Empleado"));
            this.setLinea_Asignada(info.getString("Linea_Asignada"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Matricula",this.getMatricula());
        obj.put("Empleado",this.getEmpleado());
        obj.put("Linea_Asignada",this.getLinea_Asignada());

        return obj;
    }

    public int getMatricula() {
        return matricula;
    }
    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

    public String getEmpleado() {
        return empleado;
    }
    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public String getLinea_Asignada() {
        return linea_asignada;
    }
    public void setLinea_Asignada(String linea_asignada) {
        this.linea_asignada = linea_asignada;
    }

}
