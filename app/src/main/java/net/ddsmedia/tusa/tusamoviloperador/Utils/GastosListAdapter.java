package net.ddsmedia.tusa.tusamoviloperador.Utils;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.ddsmedia.tusa.tusamoviloperador.R;

import java.util.ArrayList;
import java.util.HashMap;

import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_EFECTIVO;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_GASTO;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_NOTA;
import static net.ddsmedia.tusa.tusamoviloperador.Utils.Globals.COLUMN_TRANSFER;

/**
 * Created by Ivan on 21/07/2017.
 */

public class GastosListAdapter extends BaseAdapter {

    public ArrayList<HashMap<String, String>> list;
    Activity activity;
    TextView txtGasto;
    TextView txtNota;
    TextView txtTrans;
    TextView txtEfec;

    public GastosListAdapter(Activity activity,ArrayList<HashMap<String, String>> list){
        super();
        this.activity=activity;
        this.list=list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=activity.getLayoutInflater();

        if(convertView == null){

            convertView=inflater.inflate(R.layout.gasto_item, null);

            txtGasto =(TextView) convertView.findViewById(R.id.gasto);
            txtNota =(TextView) convertView.findViewById(R.id.nota);
            txtTrans =(TextView) convertView.findViewById(R.id.transfer);
            txtEfec =(TextView) convertView.findViewById(R.id.efectivo);

        }

        HashMap<String, String> map=list.get(position);
        txtGasto.setText(map.get(COLUMN_GASTO));
        txtNota.setText(map.get(COLUMN_NOTA));
        txtTrans.setText(map.get(COLUMN_TRANSFER));
        txtEfec.setText(map.get(COLUMN_EFECTIVO));

        if(position == 7){
            txtNota.setTypeface(null, Typeface.BOLD);
            txtTrans.setTypeface(null, Typeface.BOLD);
            txtEfec.setTypeface(null, Typeface.BOLD);
        }

        return convertView;
    }

}