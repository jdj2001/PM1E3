package com.example.pm1e3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;

public class PantallaGrabacionAudioActivity extends AppCompatActivity {

    private static final int PERMISSION_RECORD_AUDIO = 102;
    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private TextView tvEstadoGrabacion;
    private Button btnDetenerGrabacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_grabacion_audio);

        tvEstadoGrabacion = findViewById(R.id.tvEstadoGrabacion);
        btnDetenerGrabacion = findViewById(R.id.btnDetenerGrabacion);

        btnDetenerGrabacion.setOnClickListener(v -> detenerGrabacionAudio());

        checkAudioPermission();
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_RECORD_AUDIO);
        } else {
            iniciarGrabacionAudio();
        }
    }

    private void iniciarGrabacionAudio() {
        audioFilePath = getExternalCacheDir().getAbsolutePath() + "/entrevista_audio.3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(audioFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            tvEstadoGrabacion.setText("Grabando audio...");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al iniciar la grabación", Toast.LENGTH_SHORT).show();
        }
    }

    private void detenerGrabacionAudio() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            tvEstadoGrabacion.setText("Grabación finalizada");

            File audioFile = new File(audioFilePath);
            if (audioFile.exists()) {
                Log.d("DEBUG", "Archivo de audio guardado en: " + audioFilePath);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("audioFilePath", audioFilePath);
                setResult(RESULT_OK, resultIntent);
                finish();

                /*Intent resultIntent = new Intent();
                resultIntent.setData(Uri.fromFile(audioFile));
                setResult(RESULT_OK, resultIntent);
                finish();*/
            } else {
                Log.d("DEBUG", "El archivo de audio no se encontró en la ruta: " + audioFilePath);
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarGrabacionAudio();
        } else {
            Toast.makeText(this, "Permiso de grabación de audio denegado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

