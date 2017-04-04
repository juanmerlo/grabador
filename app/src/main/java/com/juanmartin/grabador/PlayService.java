package com.juanmartin.grabador;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by juanmartin on 1/7/2016.
 */
public class PlayService extends Service {

    MediaPlayer mediaPlayer;
    TimerTask timerTask;
    Timer timer;
    Boolean reproduciendo = false;
    String url, nombreAudio;
    Boolean estaPausado = false;
    private NotificationManager mNotifyManager;
    int id = 2;
    NotificationCompat.Builder builder;


    @Override
    public void onCreate() {



        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter(
                Constants.SERVICIO_CORRIENDO);
        filter.addAction(Constants.BOTONPLAY);
        filter.addAction(Constants.BOTONSTOP);
        filter.addAction(Constants.BOTONATRASAR);
        filter.addAction(Constants.BOTONADELANTAR);
        filter.addAction(Constants.ARRASTRARBARRA);

        // Crear un nuevo ResponseReceiver
        ResponseReceiver receiver =
                new ResponseReceiver();
        // Registrar el receiver y su filtro
        LocalBroadcastManager.getInstance(this).registerReceiver(
                receiver,
                filter);
        //super.onCreate();

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {

                if(reproduciendo) {
                    int tiempoTranscurrido = mediaPlayer.getCurrentPosition();
                    Intent localIntent = new Intent(Constants.SERVICIO_CORRIENDO)
                            .putExtra(Constants.TIEMPO_AUDIO, tiempoTranscurrido)
                            .putExtra(Constants.NOMBRE_AUDIO, nombreAudio)
                            .putExtra(Constants.TIEMPO_TOTAL_AUDIO, mediaPlayer.getDuration())
                            .putExtra(Constants.ESTAPAUSADO, estaPausado)
                            .putExtra(Constants.ESTAREPRODUCIENDO, reproduciendo);

                    // Emitir el intent a la actividad
                    LocalBroadcastManager.getInstance(PlayService.this).sendBroadcast(localIntent);
                }
            }

    };
    timer.scheduleAtFixedRate(timerTask, 0, 50);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(reproduciendo){
            mediaPlayer.stop();
            mediaPlayer.reset();
            reproduciendo = false;
        }

            url = intent.getStringExtra("urlAudio");
            nombreAudio = intent.getStringExtra("nombreAudio");
            Uri uri = Uri.parse(Uri.encode(url));
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        try {
            mediaPlayer.start();
            reproduciendo = true;
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    estaPausado = false;
                    reproduciendo = false;
                    /*int tiempoTranscurrido = mediaPlayer.getCurrentPosition();
                    Intent localIntent = new Intent(Constants.SERVICIO_CORRIENDO)
                            .putExtra(Constants.TIEMPO_AUDIO, tiempoTranscurrido)
                            .putExtra(Constants.NOMBRE_AUDIO, nombreAudio)
                            .putExtra(Constants.TIEMPO_TOTAL_AUDIO, mediaPlayer.getDuration())
                            .putExtra(Constants.ESTAPAUSADO, false)
                            .putExtra(Constants.ESTAREPRODUCIENDO, false);
                    LocalBroadcastManager.getInstance(PlayService.this).sendBroadcast(localIntent);*/
                    stopSelf();

                }
            });
        }catch (RuntimeException e){
            Toast.makeText(getApplicationContext(),"Archivo borrado",Toast.LENGTH_SHORT).show();
        }


        // Se construye la notificación

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);


        builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setSmallIcon(android.R.drawable.ic_media_play);
        builder.setContentTitle(getResources().getString(R.string.reproduciendo));
        builder.setContentText(nombreAudio);
        builder.setContentIntent(pendingIntent);
        builder.setTicker(getResources().getString(R.string.reproduciendo) + " " + nombreAudio);
        startForeground(2,builder.build());
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        return START_NOT_STICKY;
    }

        @Override
    public void onDestroy() {
        mediaPlayer.stop();
        reproduciendo = false;
        estaPausado = false;

        int tiempoTranscurrido = mediaPlayer.getCurrentPosition();
        Intent localIntent = new Intent(Constants.SERVICIO_CORRIENDO)
                .putExtra(Constants.TIEMPO_AUDIO, tiempoTranscurrido)
                .putExtra(Constants.NOMBRE_AUDIO, nombreAudio)
                .putExtra(Constants.TIEMPO_TOTAL_AUDIO, mediaPlayer.getDuration())
                .putExtra(Constants.ESTAPAUSADO, false)
                .putExtra(Constants.ESTAREPRODUCIENDO, false);
        LocalBroadcastManager.getInstance(PlayService.this).sendBroadcast(localIntent);
        mNotifyManager.cancel(2);


        timer.cancel();
        timer.purge();
        super.onDestroy();
    }

    @Nullable
    @Override



    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ResponseReceiver extends BroadcastReceiver {

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {


                /*case Constants.BOTONSTOP:
                    reproduciendo = false;
                    estaPausado = false;
                    stopSelf();
                    break;*/
                case Constants.BOTONATRASAR:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()- 2000);
                    break;
                case Constants.BOTONADELANTAR:
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+ 2000);
                    break;
                case Constants.ARRASTRARBARRA:
                    mediaPlayer.seekTo(intent.getIntExtra(Constants.TIEMPOARRASTRE, -1));
                    break;
                case Constants.BOTONPLAY:
                    if(estaPausado){
                        mediaPlayer.start();
                        estaPausado = false;
                    }else{
                        mediaPlayer.pause();
                        estaPausado = true;
                    }
                    break;

            }
        }
    }
}
