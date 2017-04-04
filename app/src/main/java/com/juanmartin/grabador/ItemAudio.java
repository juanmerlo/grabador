package com.juanmartin.grabador;


/**
 * Created by juanmartin on 13/6/2016.
 */
public class ItemAudio{

    public int icon;
    public String title;
    public String duracion;
    public String fecha;
    public String pesoBytes;
    public ItemAudio(){
        super();
    }

    public ItemAudio(int icon,String title, String duracion, String fecha, String pesoBytes){

        super();
        this.icon = icon;
        this.title = title;
        this.duracion = duracion;
        this.fecha = fecha;
        this.pesoBytes = pesoBytes;

    }



}
