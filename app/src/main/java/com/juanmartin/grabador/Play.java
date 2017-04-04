package com.juanmartin.grabador;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Play extends Fragment{
    View rootView;
    ListView listaAudios;
    String[] arrayAudios;
    Button atras,reproducir,pararReproduccion,adelante;
    String carpetaAudios = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Grabaciones";
    MediaPlayer mediaplayer;
    int posicionAudioReproduciendo;
    ProgressBar barraProgreso;
    TextView nombreAudioReproduciendo, tiempoTranscurrido, tiempoTotal;
    MainFragmentsInterface mainFragmentsInterface;
    int tiempoTotalEnMilis, tiempoTranscurridoEnMilis = 0;
    Boolean estaPausado = false;
    Boolean estaReproduciendo = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_play, container, false);

        inicializar();
        inicializarFiltros();
        registerForContextMenu(listaAudios);
        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        if(v.getId() == R.id.listaAudios){

            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(arrayAudios[info.position]);
            String[] menuItems = getResources().getStringArray(R.array.opcionesMenuContextual);
            for(int i = 0; i< menuItems.length;i++){

                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }

    }

    public void setCarpetaAudios(String directorio){

        carpetaAudios = directorio;
    }

    public void inicializar(){


        listaAudios = (ListView)rootView.findViewById(R.id.listaAudios);
        atras = (Button) rootView.findViewById(R.id.atras);
        reproducir = (Button) rootView.findViewById(R.id.reproducir);
        pararReproduccion = (Button) rootView.findViewById(R.id.pararReproduccion);
        adelante = (Button) rootView.findViewById(R.id.adelante);
        barraProgreso = (ProgressBar) rootView.findViewById(R.id.barraProgreso);
        tiempoTotal = (TextView) rootView.findViewById(R.id.tiempoTotal);
        tiempoTranscurrido = (TextView) rootView.findViewById(R.id.tiempoTranscurrido);
        nombreAudioReproduciendo = (TextView) rootView.findViewById(R.id.nombreAudioReproduciendo);


        mediaplayer = new MediaPlayer();
        atras.setOnClickListener(new AtrasBoton());
        reproducir.setOnClickListener(new ReproducirBoton());
        pararReproduccion.setOnClickListener(new PararReproduccionBoton());
        adelante.setOnClickListener(new AdelanteBoton());
        barraProgreso.setOnTouchListener(new ArrastrarBarraListener());
        cargarLista();
    }


    class ArrastrarBarraListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /*int tiempoTotalCancion = mediaplayer.getDuration();

            barraProgreso.setProgress(touchBarra);
            mediaplayer.seekTo(tiempoTotalCancion / widthBarra * touchBarra);*/
            int widthBarra = v.getWidth();
            int touchBarra = (int) event.getX();
            Intent localIntent = new Intent(Constants.ARRASTRARBARRA);
            localIntent.putExtra(Constants.TIEMPOARRASTRE,tiempoTotalEnMilis / widthBarra * touchBarra);
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localIntent);
            return false;

        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = getResources().getStringArray(R.array.opcionesMenuContextual);
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = arrayAudios[info.position];
        final File archivo = new File(carpetaAudios + "/" +arrayAudios[info.position] );
        String eliminar = getResources().getString(R.string.eliminar).toString();

        //switch (item.getTitle().toString()){
        switch (item.getItemId()){

            case 0:
                archivo.delete();
                Toast.makeText(getContext(),arrayAudios[info.position] + " "+ getResources().getString(R.string.eliminadoListo),Toast.LENGTH_SHORT).show();
                break;
            case 1:
                //Alert Dialog setting up
                // get prompts.xml view
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setView(promptView);

                final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
                final TextView textView = (TextView) promptView.findViewById(R.id.textView);
                editText.setHint(arrayAudios[info.position]);
                textView.setText(getResources().getString(R.string.ingresoNombre));
                // setup a dialog window
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String nombrenuevototal = editText.getText() + ".mp3";

                                archivo.renameTo(new File(carpetaAudios + "/" + nombrenuevototal));
                                Toast.makeText(getContext(),getResources().getString(R.string.cambioNombreListo) + " " + nombrenuevototal,Toast.LENGTH_SHORT).show();
                                cargarLista();
                            }
                        })
                        .setNegativeButton(getResources().getString(R.string.cancelar),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create an alert dialog
                AlertDialog alert = alertDialogBuilder.create();
                alert.show();
                break;
            case 2:
                pararReproduccion();
                nombreAudioReproduciendo.setText("");
                eliminarPorExtension(carpetaAudios,"mp3");
                Toast.makeText(getContext(),getResources().getString(R.string.eliminoTodoListo),Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        nombreAudioReproduciendo.setText("");
        cargarLista();
        return true;


    }

    public static void eliminarPorExtension(String path, final String extension){
        File[] archivos = new File(path).listFiles(new FileFilter() {
            public boolean accept(File archivo) {
                if (archivo.isFile())
                return archivo.getName().endsWith('.' + extension);
                return false;
            }
        });
        for (File archivo : archivos) {
            archivo.delete();
        }
    }

    class AtrasBoton implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent localIntent = new Intent(Constants.BOTONATRASAR);
            // Emitir el intent a la actividad
            LocalBroadcastManager.
                    getInstance(getContext()).sendBroadcast(localIntent);
        }
    }


    class ReproducirBoton implements View.OnClickListener{

        @Override
        public void onClick(View v) {


            if(!estaReproduciendo){
                reproducir.setBackgroundResource(R.drawable.boton_pausa_plano);
                String nombreAudio = nombreAudioReproduciendo.getText().toString();
                if(nombreAudio.equals("")){
                    String nuevoAudio = arrayAudios[arrayAudios.length - 1];
                    mainFragmentsInterface.iniciarServicioReproduccion(nuevoAudio, carpetaAudios + "/" + nuevoAudio );
                }else{
                    mainFragmentsInterface.iniciarServicioReproduccion(nombreAudio, carpetaAudios + "/" + nombreAudio);

                }
            }else{

                Intent localIntent = new Intent(Constants.BOTONPLAY);
                // Emitir el intent a la actividad
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(localIntent);
                if (estaPausado){
                    reproducir.setBackgroundResource(R.drawable.boton_pausa_plano);
                }else {
                    reproducir.setBackgroundResource(R.drawable.boton_reproducir_plano);
                }

            }



        }
    }

    class PararReproduccionBoton implements View.OnClickListener{

        @Override
        public void onClick(View v) {

           pararReproduccion();

        }
    }

    public void pararReproduccion(){

        /*Intent localIntent = new Intent(Constants.BOTONSTOP);
        // Emitir el intent a la actividad
        LocalBroadcastManager.
                getInstance(getContext()).sendBroadcast(localIntent);*/
        mainFragmentsInterface.pararServicioReproduccion();
        barraProgreso.setProgress(0);
        tiempoTranscurrido.setText(dameTiempo(0));
        estaPausado = false;
        estaReproduciendo = false;
        reproducir.setBackgroundResource(R.drawable.boton_reproducir_plano);
    }

    class AdelanteBoton implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent localIntent = new Intent(Constants.BOTONADELANTAR);
            // Emitir el intent a la actividad
            LocalBroadcastManager.
                    getInstance(getContext()).sendBroadcast(localIntent);


        }
    }

    public void cargarLista(){
        File grabaciones = new File(carpetaAudios);
        arrayAudios = grabaciones.list();
        ItemAudio itemAudio_datos[] = new ItemAudio[arrayAudios.length];

        if(arrayAudios.length==0){
            String[] ningunElemento = {getResources().getString(R.string.noHayGrabaciones)};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_expandable_list_item_1,ningunElemento);
            listaAudios.setAdapter(adapter);
            listaAudios.setEnabled(false);
            reproducir.setEnabled(false);
            pararReproduccion.setEnabled(false);
            atras.setEnabled(false);
            adelante.setEnabled(false);
            barraProgreso.setEnabled(false);
            barraProgreso.setProgress(0);

        }else {

            for (int i = 0; i < arrayAudios.length ; i++) {

                File file = new File(carpetaAudios+"/"+ arrayAudios[i]);
                Date lastModDate = new Date(file.lastModified());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                //MediaPlayer mp;
                //Uri uri = Uri.parse(Uri.encode(carpetaAudios +"/"+ arrayAudios[i]));

                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
               mediaMetadataRetriever.setDataSource(carpetaAudios + "/" + arrayAudios[i]);
                int duracion = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                //mp = MediaPlayer.create(getContext(), uri);
                //itemAudio_datos[i] = new ItemAudio(R.drawable.itemicono, arrayAudios[i],dameTiempo(mp.getDuration()), simpleDateFormat.format(lastModDate), damePeso(file.length()));
               itemAudio_datos[i] = new ItemAudio(R.drawable.itemicono, arrayAudios[i],dameTiempo(duracion), simpleDateFormat.format(lastModDate), damePeso(file.length()));

            }
            ItemAudioAdapter adapter = new ItemAudioAdapter(getContext(), R.layout.listview_item_row, itemAudio_datos);
            listaAudios.setAdapter(adapter);
            listaAudios.setEnabled(true);
            listaAudios.setOnItemClickListener(new SeleccionItem());
            reproducir.setEnabled(true);
            pararReproduccion.setEnabled(true);
            atras.setEnabled(true);
            adelante.setEnabled(true);
            barraProgreso.setEnabled(true);
        }
    }

    class SeleccionItem implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {

            String nombreAudio = arrayAudios[position];
            String urlAudio = carpetaAudios + "/" + nombreAudio;
            reproducir.setBackgroundResource(R.drawable.boton_pausa_plano);
            mainFragmentsInterface.iniciarServicioReproduccion(nombreAudio, urlAudio);

        }
    }




    public void setPosicionAudioReproduciendo(){
        posicionAudioReproduciendo = listaAudios.getCount() - 1;

    }

    public void setPosicionAudioReproduciendo(int position){
        posicionAudioReproduciendo = position;
    }

    public String damePeso(long bytes){
        long kb = bytes / 1024;
        String bytesTotales ;

        if (kb <= 1){
            bytesTotales = "1 KByte";
        }else if(kb <1024){
            bytesTotales = kb + " KBytes";
        }else if(kb/1024 < 2){
            bytesTotales = "1 MByte";
        }else{
            bytesTotales = (kb / 1024 ) + " Mbytes";
        }

        return bytesTotales;
    }

    //Me da el tiempo en formato 00:00, recibe int en milisegundos
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
        IntentFilter filter = new IntentFilter(Constants.SERVICIO_CORRIENDO);

        //filter.addAction(Constants.TIEMPO_AUDIO);

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
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.SERVICIO_CORRIENDO:

                    nombreAudioReproduciendo.setText(intent.getStringExtra(Constants.NOMBRE_AUDIO));
                    tiempoTranscurrido.setText(dameTiempo(intent.getIntExtra(Constants.TIEMPO_AUDIO, -1)));
                    tiempoTotalEnMilis = intent.getIntExtra(Constants.TIEMPO_TOTAL_AUDIO, -1);
                    tiempoTranscurridoEnMilis = intent.getIntExtra(Constants.TIEMPO_AUDIO, -1);
                    tiempoTotal.setText(dameTiempo(intent.getIntExtra(Constants.TIEMPO_TOTAL_AUDIO, -1)));
                    barraProgreso.setMax(intent.getIntExtra(Constants.TIEMPO_TOTAL_AUDIO, -1));
                    barraProgreso.setProgress(intent.getIntExtra(Constants.TIEMPO_AUDIO, -1));
                    estaPausado = intent.getBooleanExtra(Constants.ESTAPAUSADO, false);
                    estaReproduciendo = intent.getBooleanExtra(Constants.ESTAREPRODUCIENDO, false);
                    if(estaReproduciendo){
                        if(estaPausado){
                            reproducir.setBackgroundResource(R.drawable.boton_reproducir_plano);

                        }else{
                            reproducir.setBackgroundResource(R.drawable.boton_pausa_plano);

                        }
                    }else{
                        reproducir.setBackgroundResource(R.drawable.boton_reproducir_plano);

                    }

                    break;

            }
        }
    }

    public void onAttach(Activity activity){
        super.onAttach(activity);
        mainFragmentsInterface = (MainFragmentsInterface) activity;

    }

}
