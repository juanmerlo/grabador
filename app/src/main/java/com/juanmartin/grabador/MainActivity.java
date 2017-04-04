package com.juanmartin.grabador;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

public class MainActivity extends AppCompatActivity implements MainFragmentsInterface{
    Rec fragmentRec;
    Play fragmentPlay;
    String carpetaAudios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cargarVariables();
        inicializar();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        viewPager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);

        fragmentRec = new Rec();
        fragmentPlay = new Play();

    }

    public void cargarVariables(){

        SharedPreferences prefe=getSharedPreferences("datos", Context.MODE_PRIVATE);
        String directorio = prefe.getString("directorio","");
        if(!directorio.equals("")){
            carpetaAudios = directorio;
            fragmentPlay.setCarpetaAudios(directorio);
            fragmentRec.setCarpetaAudios(directorio);
        }else {
            carpetaAudios = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Grabaciones";
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.action_settings:
                Intent i = new Intent(this, Settings.class );
                i.putExtra("directorio",carpetaAudios);
                startActivity(i);
                break;*/

            case R.id.privacypolicy:
                String url = "https://sites.google.com/view/privacypolicy-mp3-br";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
        }
        return true;
    }


    public void inicializar(){
        File carpetaRoot = new File(carpetaAudios);
        if(carpetaRoot.mkdir()){
            Log.i("Creación de carpeta: ","Completa");
        }else{
            Log.i("Creación de carpeta: ","Ya existe");
        }
    }

    @Override
    public void recargarLista() {
        fragmentPlay.cargarLista();
        fragmentPlay.setPosicionAudioReproduciendo();
    }

    @Override
    public void iniciarServicioGrabacion(String nombreAudio, String urlAudio) {
        Intent intent = new Intent(getApplicationContext(), RecService.class);
        intent.putExtra(Constants.NOMBREAUDIOGRAB, nombreAudio).putExtra(Constants.URLAUDIOGRAB, urlAudio);
        startService(intent); //Iniciar servicio
    }

    public void pararServicioGrabacion(){

        Intent intent = new Intent(getApplicationContext(), RecService.class);
        stopService(intent); //Iniciar servicio
    }

    @Override
    public void iniciarServicioReproduccion(String nombreAudio, String urlAudio) {

        Intent intent = new Intent(getApplicationContext(), PlayService.class);
        intent.putExtra("nombreAudio", nombreAudio).putExtra("urlAudio", urlAudio);
        startService(intent); //Iniciar servicio

    }

    public void pararServicioReproduccion(){

        Intent intent = new Intent(getApplicationContext(), PlayService.class);
        stopService(intent); //Iniciar servicio
    }



    public class SectionPagerAdapter extends FragmentPagerAdapter {


        public SectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {


            switch (position){
                case 0:
                    return fragmentRec;
                case 1:
                    return fragmentPlay;
                default:
                    return fragmentRec;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position){

            switch (position){
                case 0:
                    return getResources().getString(R.string.grabarTab);
                case 1:
                    return getResources().getString(R.string.escucharTab);
                default:
                    return "";
            }
        }
    }

}
