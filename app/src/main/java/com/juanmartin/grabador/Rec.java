package com.juanmartin.grabador;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class Rec extends Fragment{
    View rootView;
    MediaRecorder grabador;
    ImageView micIcono;
    Button grabar, parar;
    Chronometer cronometroGrabar;
    TextView nombreAudioNuevo;
    String carpetaAudios = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Grabaciones";
    String rutaAudioNuevo;
    ProgressBar amplitudVoz;
    MainFragmentsInterface mainFragmentsInterface;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_rec, container, false);

        inicializar();
        inicializarFiltros();
        return rootView;

    }

    public void setCarpetaAudios(String directorio){

        carpetaAudios = directorio;
    }

    public void inicializar(){

        micIcono = (ImageView)rootView.findViewById(R.id.micIcono);
        grabar = (Button)rootView.findViewById(R.id.grabar);
        parar = (Button)rootView.findViewById(R.id.parar);
        cronometroGrabar = (Chronometer) rootView.findViewById(R.id.cronometroGrabar);
        nombreAudioNuevo = (TextView) rootView.findViewById(R.id.nombreAudioNuevo);
        amplitudVoz = (ProgressBar) rootView.findViewById(R.id.amplitudVoz);
        amplitudVoz.setMax(30000);
        grabar.setOnClickListener(new GrabarBoton());
        micIcono.setOnClickListener(new GrabarBoton());
        parar.setOnClickListener(new PararBoton());

        parar.setEnabled(false);

        crearNombreAudio();

    }

    class GrabarBoton implements View.OnClickListener{

        @Override
        public void onClick(View v) {
           String nombreAudio = crearNombreAudio();

            mainFragmentsInterface.iniciarServicioGrabacion(nombreAudio,carpetaAudios + "/" + nombreAudio);
            grabar.setEnabled(false);
            micIcono.setEnabled(false);
            parar.setEnabled(true);
           /* String nombreAudio = crearNombreAudio();
            recServiceInterface.iniciarServicioGrabacion(nombreAudio, carpetaAudios + "/" + nombreAudio);
            cronometroGrabar.setBase(SystemClock.elapsedRealtime());
            grabar.setEnabled(false);
            micIcono.setEnabled(false);
            parar.setEnabled(true);
            //MuestraAmplitud muestraAmplitud = new MuestraAmplitud();
            estaGrabando = true;
            //muestraAmplitud.run();*/

        }


    }

    class PararBoton implements View.OnClickListener{

        @Override
        public void onClick(View v) {


            mainFragmentsInterface.pararServicioGrabacion();
            mainFragmentsInterface.recargarLista();
            grabar.setEnabled(true);
            micIcono.setEnabled(true);
            parar.setEnabled(false);
            cronometroGrabar.setBase(SystemClock.elapsedRealtime());
            cronometroGrabar.stop();
            amplitudVoz.setProgress(0);


        }
    }

    public void onAttach(Activity activity){
        super.onAttach(activity);
        mainFragmentsInterface = (MainFragmentsInterface) activity;

    }


    public String crearNombreAudio(){

        int i = 0;
        String nombre = "Audio.mp3";
        File archivo = new File(carpetaAudios +"/"+ nombre);
        while(archivo.exists()){
            nombre = "Audio"+i+".mp3";
            archivo = new File(carpetaAudios +"/"+ nombre);
            i++;
        }

        rutaAudioNuevo = carpetaAudios +"/"+ nombre;
        nombreAudioNuevo.setText(nombre);
        return nombre;
    }

    public String dameTiempo(int tiempo){

        int minutos = (int) (tiempo / 1000 / 60);
        int segundos = (int) (tiempo / 1000) - (minutos * 60);

        String minutosString;
        String segundosString;

        if(minutos < 10){
            minutosString = "0" + minutos;
        }else{
            minutosString = minutos + "";
        }

        if(segundos < 10){
            segundosString = "0" + segundos;
        }else{
            segundosString = segundos + "";
        }

        return minutosString + ":" + segundosString;
    }


    //Inicializar Filtros
    public void inicializarFiltros(){
        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter(Constants.SERVICIO_REC_CORRIENDO);
        // Crear un nuevo ResponseReceiver
        ResponseReceiver receiver = new ResponseReceiver();
        // Registrar el receiver y su filtro
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiver,filter);
    }
    // Broadcast receiver que recibe las emisiones desde los servicios
    private class ResponseReceiver extends BroadcastReceiver {

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent){
            switch (intent.getAction()){
                case Constants.SERVICIO_REC_CORRIENDO:
                    Log.v("grabador","servicio_rec_corriendo");
                    nombreAudioNuevo.setText(intent.getStringExtra(Constants.NOMBREAUDIOGRAB));
                    Boolean grabando = intent.getBooleanExtra(Constants.ESTADOGRABADOR,false);

                    if(grabando){
                        grabar.setEnabled(false);
                        micIcono.setEnabled(false);
                        parar.setEnabled(true);
                        cronometroGrabar.setBase(intent.getLongExtra(Constants.TIEMPOGRABACION,0));
                        amplitudVoz.setProgress(intent.getIntExtra(Constants.AMPLITUD, 0));

                    }else{
                        grabar.setEnabled(true);
                        micIcono.setEnabled(true);
                        parar.setEnabled(false);
                        cronometroGrabar.setBase(SystemClock.elapsedRealtime());
                        cronometroGrabar.stop();
                        amplitudVoz.setProgress(0);
                    }


                    break;

            }
        }


    }
}
