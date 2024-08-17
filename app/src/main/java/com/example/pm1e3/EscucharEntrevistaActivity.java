package com.example.pm1e3;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

public class EscucharEntrevistaActivity extends AppCompatActivity {
    private ImageView imageViewEntrevista;
    private Button buttonReproducirAudio;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escuchar_entrevista);

        imageViewEntrevista = findViewById(R.id.imageViewEntrevista);
        buttonReproducirAudio = findViewById(R.id.buttonReproducirAudio);

        Entrevista entrevista = (Entrevista) getIntent().getSerializableExtra("Entrevista");

        if (entrevista != null) {
            cargarImagen(entrevista.getImagenUrl());
            configurarReproduccionAudio(entrevista.getAudioUrl());
        }

        buttonReproducirAudio.setOnClickListener(v -> {
            reproducirAudio();
        });
    }

    private void cargarImagen(String imagenUrl) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReferenceFromUrl(imagenUrl);

        Glide.with(this)
                .load(storageReference)
                .into(imageViewEntrevista);
    }

    private void configurarReproduccionAudio(String audioUrl) {
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> buttonReproducirAudio.setEnabled(true));
            mediaPlayer.setOnCompletionListener(mp -> {
                buttonReproducirAudio.setText("Reproducir Audio");
                mediaPlayer.seekTo(0);
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar el audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void reproducirAudio() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            buttonReproducirAudio.setText("Pausar Audio");
        } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            buttonReproducirAudio.setText("Reproducir Audio");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}





