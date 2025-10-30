package net.ddsmedia.tusa.tusamoviloperador.model;

import org.json.JSONException;
import org.json.JSONObject;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Check {

    private int login_app;

    public Check(ResultSet info){
        super();
        try {
            this.setLoginapp(info.getInt("Login_app"));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Check(JSONObject info) {
        super();

        try {
            this.setLoginapp(info.getInt("Login_app"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("Login_app",this.getLoginapp());

        return obj;
    }

    public int getLoginapp() {
        return login_app;
    }
    public void setLoginapp(int loginapp) {
        this.login_app = loginapp;
    }

}

