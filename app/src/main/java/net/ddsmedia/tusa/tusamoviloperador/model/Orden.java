package net.ddsmedia.tusa.tusamoviloperador.model;

import android.location.Location;
import android.util.Log;

import net.sourceforge.jtds.jdbc.DateTime;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Ivan on 27/03/2017.
 */

public class Orden {

    private String ID;
    private String VIN;
    private String indicaciones;
    private int estado;
    private int tipo;
    private int extra;
    private String celAlerta;
    private String origen;
    private String dirOrigen;
    private String contOrigen;
    private String pobOrigen;
    private String destino;
    private String dirDestino;
    private String contDestino;
    private String pobDestino;

    private int origenID;
    private int destinoID;
    private String iniciada;

    private String geoDestinoStr;
    private String geoOrigenStr;

    private int fotos;

    private String semanaDePago;

    public Orden(ResultSet info){
        super();
        try {
            this.setID(info.getString("ID_orden"));
            this.setVIN(info.getString("No_chasis"));
            this.setIndicaciones(info.getString("Instrucciones_operador"));
            this.setEstado(info.getInt("estado"));
            this.setTipo(info.getInt("tipo"));
            this.setExtra(info.getInt("extra"));
            this.setCelAlerta(info.getString("celAlerta"));
            this.setOrigen(info.getString("nomOrigen"));
            this.setDirOrigen(info.getString("dirOrigen"));
            this.setContOrigen(info.getString("contOrigen"));
            this.setPobOrigen(info.getString("pobOrigen"));
            this.setDestino(info.getString("nomDestino"));
            this.setDirDestino(info.getString("dirDestino"));
            this.setContDestino(info.getString("contDestino"));
            this.setPobDestino(info.getString("pobDestino"));

            this.setOrigenID(info.getInt("origen"));
            this.setDestinoID(info.getInt("destino"));
            this.setIniciada(info.getString("iniciada"));
            this.setGeoDestinoStr(info.getString("geoDestinoStr"));
            this.setGeoOrigenStr(info.getString("geoOrigenStr"));

            this.setFotos(info.getInt("fotos"));

            this.setSemanaDePago(info.getString("Semana_de_pago"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Orden(JSONObject info) {
        super();

        try {
            this.setID(info.getString("ID"));
            this.setVIN(info.getString("VIN"));
            this.setIndicaciones(info.getString("indicaciones"));
            this.setEstado(info.getInt("estado"));
            this.setTipo(info.getInt("tipo"));
            this.setCelAlerta(info.getString("celAlerta"));
            this.setOrigen(info.getString("origen"));
            this.setDirOrigen(info.getString("dirOrigen"));
            this.setContOrigen(info.getString("contOrigen"));
            this.setPobOrigen(info.getString("pobOrigen"));
            this.setDestino(info.getString("destino"));
            this.setDirDestino(info.getString("dirDestino"));
            this.setContDestino(info.getString("contDestino"));
            this.setPobDestino(info.getString("pobDestino"));

            this.setOrigenID(info.getInt("origenid"));
            this.setDestinoID(info.getInt("destinoid"));
            this.setIniciada(info.getString("iniciada"));
            this.setGeoDestinoStr(info.getString("geoDestinoStr"));
            this.setGeoOrigenStr(info.getString("geoOrigenStr"));

            this.setFotos(info.getInt("fotos"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("ID",this.getID());
        obj.put("VIN",this.getVIN());
        obj.put("indicaciones",this.getIndicaciones());
        obj.put("estado",this.getEstado());
        obj.put("tipo",this.getTipo());
        obj.put("celAlerta",this.getCelAlerta());
        obj.put("origen",this.getOrigen());
        obj.put("dirOrigen",this.getDirOrigen());
        obj.put("contOrigen",this.getContOrigen());
        obj.put("pobOrigen",this.getPobOrigen());
        obj.put("destino",this.getDestino());
        obj.put("dirDestino",this.getDirDestino());
        obj.put("contDestino",this.getContDestino());
        obj.put("pobDestino",this.getPobDestino());

        obj.put("origenid",this.getOrigenID());
        obj.put("destinoid",this.getDestinoID());
        obj.put("iniciada",this.getIniciada());
        obj.put("geoDestinoStr",this.getGeoDestinoStr());
        obj.put("geoOrigenStr",this.getGeoOrigenStr());

        obj.put("fotos",this.getFotos());

        obj.put("semanaDePago", this.getSemanaDePago());

        return obj;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getVIN() {
        return VIN;
    }

    public void setVIN(String VIN) {
        this.VIN = VIN;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDirOrigen() {
        return dirOrigen;
    }

    public void setDirOrigen(String dirOrigen) {
        this.dirOrigen = dirOrigen;
    }

    public String getContOrigen() {
        return contOrigen;
    }

    public void setContOrigen(String contOrigen) {
        this.contOrigen = contOrigen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getDirDestino() {
        return dirDestino;
    }

    public void setDirDestino(String dirDestino) {
        this.dirDestino = dirDestino;
    }

    public String getContDestino() {
        return contDestino;
    }

    public void setContDestino(String contDestino) {
        this.contDestino = contDestino;
    }

    public String getPobOrigen() {
        return pobOrigen;
    }

    public void setPobOrigen(String pobOrigen) {
        this.pobOrigen = pobOrigen;
    }

    public String getPobDestino() {
        return pobDestino;
    }

    public void setPobDestino(String pobDestino) {
        this.pobDestino = pobDestino;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getExtra() {
        return extra;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public String getCelAlerta() {
        return celAlerta;
    }

    public void setCelAlerta(String celAlerta) {
        this.celAlerta = celAlerta;
    }

    public int getOrigenID() {
        return origenID;
    }

    public void setOrigenID(int origenID) {
        this.origenID = origenID;
    }

    public int getDestinoID() {
        return destinoID;
    }

    public void setDestinoID(int destinoID) {
        this.destinoID = destinoID;
    }

    public String getIniciada() {
        return iniciada;
    }

    public void setIniciada(String iniciada) {
        this.iniciada = iniciada;
    }

    public String getGeoDestinoStr() {
        return geoDestinoStr;
    }

    public void setGeoDestinoStr(String geoDestinoStr) {
        this.geoDestinoStr = geoDestinoStr;
    }

    public Location getGeoDestino(){
        Location geo = new Location("");
        geo.setLatitude(0);
        geo.setLongitude(0);
        if(!getGeoDestinoStr().equals("0,0") && !getGeoDestinoStr().equals(",")){
            String[] geoStr = getGeoDestinoStr().split(",");

            // Eliminar caracteres no válidos
            String latitudSinCaracteresNoValidos = "";
            for (char caracter : geoStr[0].toCharArray()) {
                if (Character.isDigit(caracter) || caracter == '-' || caracter == '.') {
                    latitudSinCaracteresNoValidos += caracter;
                }
            }
            //Log.i("LatitudCorrecta",latitudSinCaracteresNoValidos);
            geo.setLatitude(Double.parseDouble(latitudSinCaracteresNoValidos));
            //geo.setLatitude(Double.parseDouble(geoStr[0]));

            // Eliminar caracteres no válidos
            String longitudSinCaracteresNoValidos = "";
            for (char caracter2 : geoStr[1].toCharArray()) {
                if (Character.isDigit(caracter2) || caracter2 == '-' || caracter2 == '.') {
                    longitudSinCaracteresNoValidos += caracter2;
                }
            }
            //Log.i("LongitudCorrecta",longitudSinCaracteresNoValidos);
            geo.setLongitude(Double.parseDouble(longitudSinCaracteresNoValidos));
            //geo.setLongitude(Double.parseDouble(geoStr[1]));
        }
        return geo;
    }

    public String getGeoOrigenStr() {
        return geoOrigenStr;
    }

    public void setGeoOrigenStr(String geoOrigenStr) {
        this.geoOrigenStr = geoOrigenStr;
    }

    public Location getGeoOrigen(){
        Location geo = new Location("");
        geo.setLatitude(0);
        geo.setLongitude(0);
        String[] geoStr = getGeoOrigenStr().split(",");
        if(!geoStr[0].equals("0") && !geoStr[1].equals("0")){

            // Eliminar caracteres no válidos
            String latitudSinCaracteresNoValidos = "";
            for (char caracter : geoStr[0].toCharArray()) {
                if (Character.isDigit(caracter) || caracter == '-' || caracter == '.') {
                    latitudSinCaracteresNoValidos += caracter;
                }
            }
            //Log.i("LatitudCorrecta",latitudSinCaracteresNoValidos);
            geo.setLatitude(Double.parseDouble(latitudSinCaracteresNoValidos));
            //geo.setLatitude(Double.parseDouble(geoStr[0]));

            // Eliminar caracteres no válidos
            String longitudSinCaracteresNoValidos = "";
            for (char caracter2 : geoStr[1].toCharArray()) {
                if (Character.isDigit(caracter2) || caracter2 == '-' || caracter2 == '.') {
                    longitudSinCaracteresNoValidos += caracter2;
                }
            }
            //Log.i("LongitudCorrecta",longitudSinCaracteresNoValidos);
            geo.setLongitude(Double.parseDouble(longitudSinCaracteresNoValidos));
            //geo.setLongitude(Double.parseDouble(geoStr[1]));
        }
        return geo;
    }

    public int getFotos() {
        return fotos;
    }

    public void setFotos(int fotos) {
        this.fotos = fotos;
    }

    public String getSemanaDePago() {
        return semanaDePago;
    }

    public void setSemanaDePago(String semanaDePago) {
        this.semanaDePago = semanaDePago;
    }


}
