package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ivan on 25/03/2017.
 */

public class Usuario {
    private int matricula;
    private String nombre;
    private String paterno;
    private String materno;
    private String celular;
    private String email;
    private String orden;
    private int temporal;
    private int tipoOrden;
    private int salud;

    public Usuario(ResultSet info){
        super();
        try {
            this.setMatricula(info.getInt("ID_matricula"));
            this.setNombre(info.getString("Nombres"));
            this.setPaterno(info.getString("Ap_paterno"));
            this.setMaterno(info.getString("Ap_materno"));
            this.setCelular(info.getString("No_celular"));
            this.setEmail(info.getString("Correo_electronico"));
            if(info.getString("orden").equals("")){
                this.setOrden("");
                this.setTipoOrden(0);
            }else{
                this.setOrden(info.getString("orden").substring(1));
                this.setTipoOrden(Integer.parseInt(info.getString("orden").substring(0,1)));
            }
            this.setTemporal(info.getInt("temporal"));
            this.setSalud(info.getInt("salud"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Usuario(JSONObject info) {
        super();

        try {
            this.setMatricula(info.getInt("matricula"));
            this.setNombre(info.getString("nombre"));
            this.setPaterno(info.getString("paterno"));
            this.setMaterno(info.getString("materno"));
            this.setCelular(info.getString("celular"));
            this.setEmail(info.getString("email"));
            this.setOrden(info.getString("orden"));
            this.setTipoOrden(info.getInt("tipoOrden"));
            this.setSalud(info.getInt("salud"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("matricula",this.getMatricula());
        obj.put("nombre",this.getNombre());
        obj.put("paterno",this.getPaterno());
        obj.put("materno",this.getMaterno());
        obj.put("celular",this.getCelular());
        obj.put("email",this.getEmail());
        obj.put("orden",this.getOrden());
        obj.put("tipoOrden",this.getTipoOrden());
        obj.put("salud",this.getSalud());
        return obj;
    }

    public int getMatricula() {
        return matricula;
    }

    public void setMatricula(int matricula) {
        this.matricula = matricula;
    }

    public String getNombre() {
        return nombre;
    }
    public String getNombreLargo() {
        return nombre+" "+paterno+" "+materno;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPaterno() {
        return paterno;
    }

    public void setPaterno(String paterno) {
        this.paterno = paterno;
    }

    public String getMaterno() {
        return materno;
    }

    public void setMaterno(String materno) {
        this.materno = materno;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrden() {
        return orden;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    public int getTemporal() {
        return temporal;
    }

    public void setTemporal(int temporal) {
        this.temporal = temporal;
    }

    public int getTipoOrden() {
        return tipoOrden;
    }

    public void setTipoOrden(int tipoOrden) {
        this.tipoOrden = tipoOrden;
    }

    public int getSalud() {
        return salud;
    }

    public void setSalud(int salud) {
        this.salud = salud;
    }
}
