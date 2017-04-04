package com.juanmartin.grabador;

/**
 * Created by juanmartin on 22/6/2016.
 */
public interface MainFragmentsInterface {

    public void recargarLista();

    public void iniciarServicioGrabacion(String nombreAudio, String urlAudio);

    public void pararServicioGrabacion();

    public void iniciarServicioReproduccion(String nombreAudio, String urlAudio);

    public void pararServicioReproduccion();


}
