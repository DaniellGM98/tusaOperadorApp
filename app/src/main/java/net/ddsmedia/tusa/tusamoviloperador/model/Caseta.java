package net.ddsmedia.tusa.tusamoviloperador.model;

public class Caseta {

    private String nombre;
    private String geo;

    public Caseta() { }

    public Caseta(String nombre, String geo) {
        this.nombre = nombre;
        this.geo = geo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGeo() {
        return geo;
    }

    public void setGeo(String geo) {
        this.geo = geo;
    }
}
