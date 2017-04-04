package com.juanmartin.grabador;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by juanmartin on 1/7/2016.
 */
public class RecService extends Service {

    MediaRecorder mediaRecorder;
    TimerTask timerTask;
    Timer timer;
    String url, nombreAudio;
    Chronometer chronometer;
    Boolean grabando = true;
    private NotificationManager mNotifyManager;
    int id = 1;
    NotificationCompat.Builder builder;
    @Override
    public void onCreate() {

        inicializarFiltros();
        inicializarMediaRecorder();

        timer = new Timer();

        timerTask = new TimerTask() {
            @Override
            public void run() {


                //builder.setProgress(30000, mediaRecorder.getMaxAmplitude(), false);
                //mNotifyManager.notify(id, builder.build());
                Intent localIntent = new Intent(Constants.SERVICIO_REC_CORRIENDO)
                        .putExtra(Constants.NOMBREAUDIOGRAB, nombreAudio)
                        .putExtra(Constants.ESTADOGRABADOR, grabando)
                        .putExtra(Constants.TIEMPOGRABACION,chronometer.getBase())
                        .putExtra(Constants.AMPLITUD, mediaRecorder.getMaxAmplitude());
                // Emitir el intent a la actividad
                LocalBroadcastManager.getInstance(RecService.this).sendBroadcast(localIntent);

            }

        };

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        nombreAudio = intent.getStringExtra(Constants.NOMBREAUDIOGRAB);
       mediaRecorder.setOutputFile(intent.getStringExtra(Constants.URLAUDIOGRAB));

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();

        } catch (IOException e) {

        }

        chronometer = new Chronometer(getApplicationContext());
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();


        timer.scheduleAtFixedRate(timerTask, 0, 50);



        // Se construye la notificación

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, i, 0);


        builder = new NotificationCompat.Builder(this);
        builder.setAutoCancel(true);
        builder.setSmallIcon(android.R.drawable.ic_btn_speak_now);
        builder.setContentTitle(getResources().getString(R.string.grabando));
        builder.setContentText(nombreAudio);
        //builder.setContent(remoteViews);
        builder.setContentIntent(pendingIntent);
        builder.setTicker(getResources().getString(R.string.grabando) + " " + nombreAudio);
       //.addAction(android.R.drawable.sym_action_call, getResources().getString(R.string.grabando), pendingIntent);
        startForeground(1,builder.build());
        //mNotifyManager.notify(1,builder.build());
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        grabando = false;
        mediaRecorder.stop();
        mediaRecorder.release();
        timer.cancel();
        timer.purge();
        mNotifyManager.cancel(1);
        super.onDestroy();

    }

    @Nullable
    @Override



    public IBinder onBind(Intent intent) {
        return null;
    }



    public void inicializarMediaRecorder(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setAudioEncodingBitRate(96000);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    }

    public void inicializarFiltros(){
        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter(Constants.DETENERGRABACION);
        // Crear un nuevo ResponseReceiver
        ResponseReceiver receiver =new ResponseReceiver();
        // Registrar el receiver y su filtro
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);

    }

    private class ResponseReceiver extends BroadcastReceiver {

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {


                case Constants.DETENERGRABACION:

                    break;

            }
        }
    }
}
