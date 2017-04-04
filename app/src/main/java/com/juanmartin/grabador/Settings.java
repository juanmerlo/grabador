package com.juanmartin.grabador;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class Settings extends AppCompatActivity {


    String directorio;
    MainFragmentsInterface mainFragmentsInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        directorio = getIntent().getStringExtra("directorio");

        Button cambiarDirectorio = (Button) findViewById(R.id.cambiarDirectorio);
        cambiarDirectorio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Alert Dialog setting up
                // get prompts.xml view
                LayoutInflater layoutInflater = LayoutInflater.from(Settings.this);
                View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Settings.this);
                alertDialogBuilder.setView(promptView);


                final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
                final TextView textView = (TextView) promptView.findViewById(R.id.textView);
                textView.setText(getResources().getString(R.string.ingresarNuevoDirectorio));
                editText.setText(directorio);

                // setup a dialog window
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SharedPreferences preferencias=getSharedPreferences("datos", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor=preferencias.edit();
                                editor.putString("directorio", editText.getText().toString());
                                editor.commit();
                                finish();

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
            }
        });

    }



}
