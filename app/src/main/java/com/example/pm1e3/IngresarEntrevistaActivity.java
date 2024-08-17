package com.example.pm1e3;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class IngresarEntrevistaActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_AUDIO_REQUEST = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int REQUEST_AUDIO_RECORD = 4;
    private static final int REQUEST_AUDIO_CAPTURE = 5;
    private static final int PERMISSION_RECORD_AUDIO = 102;

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private EditText etDescripcion, etPeriodista, etFecha;
    private ImageView ivImagen;
    private TextView tvAudioSeleccionado;
    private Uri imagenUri, audioUri;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Button btnSeleccionarImagen, btnSeleccionarAudio, btnGrabarAudio, btnGuardarEntrevista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresar_entrevista);
        FirebaseApp.initializeApp(/*context=*/ this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        databaseReference = FirebaseDatabase.getInstance().getReference("Entrevistas");
        storageReference = FirebaseStorage.getInstance().getReference("Entrevistas");

        etDescripcion = findViewById(R.id.etDescripcion);
        etPeriodista = findViewById(R.id.etPeriodista);
        etFecha = findViewById(R.id.etFecha);
        ivImagen = findViewById(R.id.ivImagen);
        tvAudioSeleccionado = findViewById(R.id.tvAudioSeleccionado);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);
        btnSeleccionarAudio = findViewById(R.id.btnSeleccionarAudio);
        btnGrabarAudio = findViewById(R.id.btnGrabarAudio);
        btnGuardarEntrevista = findViewById(R.id.btnGuardarEntrevista);

        btnSeleccionarImagen.setOnClickListener(v -> mostrarDialogoSeleccionImagen());
        btnSeleccionarAudio.setOnClickListener(v -> seleccionarAudio());
        btnGrabarAudio.setOnClickListener(v -> checkAudioPermission());
        btnGuardarEntrevista.setOnClickListener(v -> guardarEntrevista());

        etFecha.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String yyyymmdd = "YYYYMMDD";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    String cleanC = current.replaceAll("[^\\d]", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8) {
                        clean = clean + yyyymmdd.substring(clean.length());
                    } else {
                        int year = Integer.parseInt(clean.substring(0, 4));
                        int month = Integer.parseInt(clean.substring(4, 6));
                        int day = Integer.parseInt(clean.substring(6, 8));

                        month = month < 1 ? 1 : month > 12 ? 12 : month;
                        cal.set(Calendar.MONTH, month - 1);
                        year = (year < 1900) ? 1900 : (year > 2100) ? 2100 : year;
                        cal.set(Calendar.YEAR, year);
                        day = (day > cal.getActualMaximum(Calendar.DATE)) ? cal.getActualMaximum(Calendar.DATE) : day;
                        clean = String.format("%04d%02d%02d", year, month, day);
                    }

                    clean = String.format("%s-%s-%s", clean.substring(0, 4), clean.substring(4, 6), clean.substring(6, 8));

                    current = clean;
                    etFecha.setText(current);
                    etFecha.setSelection(sel < current.length() ? sel : current.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        Button btnVerEntrevistas = findViewById(R.id.btnVerEntrevistas);
        btnVerEntrevistas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IngresarEntrevistaActivity.this, ListaEntrevistasActivity.class);
                startActivity(intent);
            }
        });
    }

    private void mostrarDialogoSeleccionImagen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen");
        builder.setItems(new CharSequence[]{"Seleccionar de la galería", "Capturar foto"},
                (dialog, which) -> {
                    if (which == 0) {
                        seleccionarImagen();
                    } else if (which == 1) {
                        checkCameraPermission();
                    }
                });
        builder.show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
        } else {
            capturarImagen();
        }
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_AUDIO_RECORD);
        } else {
            iniciarGrabacionAudio();
        }
    }

    private void capturarImagen() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void iniciarGrabacionAudio() {
        try {
            audioFilePath = getExternalCacheDir().getAbsolutePath() + "/entrevista_audio.3gp";
            File audioFile = new File(audioFilePath);

            if (audioFile.exists()) {
                audioFile.delete();
            }

            audioFile.createNewFile();

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.prepare();
            mediaRecorder.start();
            establecerFechaActual();

            Intent intent = new Intent(this, PantallaGrabacionAudioActivity.class);
            intent.putExtra("audioFilePath", audioFilePath);
            startActivity(intent);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void seleccionarImagen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Imagen"), PICK_IMAGE_REQUEST);
    }

    private void seleccionarAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Seleccionar Audio"), PICK_AUDIO_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_AUDIO_RECORD && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarGrabacionAudio();
        } else {
            Toast.makeText(this, "Permiso de grabación de audio denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ivImagen.setImageBitmap(imageBitmap);
                    imagenUri = getImageUri(this, imageBitmap);
                    break;

                case PICK_IMAGE_REQUEST:
                    imagenUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagenUri);
                        ivImagen.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case PICK_AUDIO_REQUEST:
                    audioUri = data.getData();
                    tvAudioSeleccionado.setText("Audio seleccionado");
                    establecerFechaActual();
                    break;

                case REQUEST_AUDIO_CAPTURE:
                    String filePath = data.getStringExtra("audioFilePath");
                    if (filePath != null) {
                        audioUri = Uri.fromFile(new File(filePath));
                        tvAudioSeleccionado.setText("Audio grabado");
                        establecerFechaActual();
                    } else {
                        Toast.makeText(this, "Error al grabar audio", Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void establecerFechaActual() {
        String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        etFecha.setText(fechaActual);
    }



    private Uri getImageUri(Context context, Bitmap image) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), image, "EntrevistaImage", null);
        return Uri.parse(path);
    }

    private void guardarEntrevista() {
        final String descripcion = etDescripcion.getText().toString().trim();
        final String periodista = etPeriodista.getText().toString().trim();
        final String fecha = etFecha.getText().toString().trim();

        Log.d("DEBUG", "Descripción: " + descripcion);
        Log.d("DEBUG", "Periodista: " + periodista);
        Log.d("DEBUG", "Fecha: " + fecha);
        Log.d("DEBUG", "Imagen URI: " + imagenUri);
        Log.d("DEBUG", "Audio URI: " + audioUri);

        if (TextUtils.isEmpty(descripcion) || TextUtils.isEmpty(periodista) || TextUtils.isEmpty(fecha) || imagenUri == null || audioUri == null) {
            Toast.makeText(this, "Por favor, complete todos los campos y seleccione una imagen y un audio", Toast.LENGTH_SHORT).show();
            return;
        }

        final int idOrden = (int) (System.currentTimeMillis() % 100000);

        StorageReference imagenRef = storageReference.child(idOrden + "/imagen.jpg");
        imagenRef.putFile(imagenUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imagenRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri imagenUrl) {
                        // Subir audio a Firebase Storage
                        StorageReference audioRef = storageReference.child(idOrden + "/audio.mp3");
                        audioRef.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                audioRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri audioUrl) {
                                        String idEntrevista = databaseReference.push().getKey();
                                        Entrevista entrevista = new Entrevista(idEntrevista, descripcion, periodista, fecha, imagenUrl.toString(), audioUrl.toString());
                                        databaseReference.child(idEntrevista).setValue(entrevista)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(IngresarEntrevistaActivity.this, "Entrevista guardada exitosamente", Toast.LENGTH_SHORT).show();

                                                        } else {
                                                            Toast.makeText(IngresarEntrevistaActivity.this, "Error al guardar entrevista", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

}




