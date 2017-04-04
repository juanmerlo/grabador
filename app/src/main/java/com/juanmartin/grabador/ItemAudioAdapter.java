package com.juanmartin.grabador;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by juanmartin on 13/6/2016.
 */
public class ItemAudioAdapter extends ArrayAdapter<ItemAudio> {

    Context myContext;
    int mylayoutResourceID;
    ItemAudio mydata[] = null;

    public ItemAudioAdapter(Context context, int layoutResourceID, ItemAudio[] data){

        super(context,layoutResourceID,data);
        this.myContext = context;
        this.mylayoutResourceID = layoutResourceID;
        this.mydata = data;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;
        ItemAudioHolder holder = null;

        if(row == null){
            LayoutInflater inflater = ((Activity)myContext).getLayoutInflater();
            row = inflater.inflate(mylayoutResourceID,parent,false);
            holder = new ItemAudioHolder();
            holder.image = (ImageView) row.findViewById(R.id.image);
            holder.texto = (TextView) row.findViewById(R.id.texto);
            holder.duracion = (TextView) row.findViewById(R.id.textoTiempo);
            holder.fecha = (TextView) row.findViewById(R.id.textoFecha);
            holder.pesoBytes = (TextView) row.findViewById(R.id.pesoBytes);
            row.setTag(holder);
        }else{

            holder = (ItemAudioHolder)row.getTag();
        }

        ItemAudio itemAudio = mydata[position];
        holder.texto.setText(itemAudio.title);
        holder.image.setImageResource(itemAudio.icon);
        holder.duracion.setText(itemAudio.duracion);
        holder.fecha.setText(itemAudio.fecha);
        holder.pesoBytes.setText(itemAudio.pesoBytes);

        return row;
    }

    static class ItemAudioHolder{

        ImageView image;
        TextView texto;
        TextView duracion;
        TextView fecha;
        TextView pesoBytes;
    }
}
