package com.example.pm1e3;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

// ListaEntrevistasActivity
public class ListaEntrevistasActivity extends AppCompatActivity {
    private ListView listViewEntrevistas;
    private List<Entrevista> listaEntrevistas;
    private EntrevistaAdapter entrevistaAdapter;
    private DatabaseReference databaseEntrevistas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_entrevistas);

        listViewEntrevistas = findViewById(R.id.listViewEntrevistas);
        listaEntrevistas = new ArrayList<>();
        databaseEntrevistas = FirebaseDatabase.getInstance().getReference("Entrevistas");

        entrevistaAdapter = new EntrevistaAdapter(this, listaEntrevistas);
        listViewEntrevistas.setAdapter(entrevistaAdapter);

        databaseEntrevistas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listaEntrevistas.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Entrevista entrevista = postSnapshot.getValue(Entrevista.class);
                    listaEntrevistas.add(entrevista);
                }
                entrevistaAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Manejo de errores
            }
        });

        listViewEntrevistas.setOnItemClickListener((adapterView, view, position, id) -> {
            Entrevista entrevistaSeleccionada = listaEntrevistas.get(position);
            mostrarOpciones(entrevistaSeleccionada);
        });
    }

    private void mostrarOpciones(Entrevista entrevista) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones para " + entrevista.getDescripcion());

        String[] opciones = {"Modificar", "Eliminar", "Escuchar"};
        builder.setItems(opciones, (dialog, which) -> {
            switch (which) {
                case 0: // Modificar
                    if (entrevista != null) {
                        Intent modificarIntent = new Intent(ListaEntrevistasActivity.this, ModificarEntrevistaActivity.class);
                        modificarIntent.putExtra("EntrevistaId", entrevista.getIdOrden()); // Usa el ID en lugar del objeto
                        startActivity(modificarIntent);
                    } else {
                        Toast.makeText(ListaEntrevistasActivity.this, "Entrevista no válida", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1: // Eliminar
                    eliminarEntrevista(entrevista);
                    break;
                case 2: // Escuchar
                    Intent escucharIntent = new Intent(ListaEntrevistasActivity.this, EscucharEntrevistaActivity.class);
                    escucharIntent.putExtra("Entrevista", entrevista);
                    startActivity(escucharIntent);
                    break;
            }
        });
        builder.show();
    }

    private void eliminarEntrevista(Entrevista entrevista) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Entrevistas").child(String.valueOf(entrevista.getIdOrden()));
        dbRef.removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(ListaEntrevistasActivity.this, "Entrevista eliminada", Toast.LENGTH_SHORT).show();
            listaEntrevistas.remove(entrevista);
            entrevistaAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            Toast.makeText(ListaEntrevistasActivity.this, "Error al eliminar la entrevista", Toast.LENGTH_SHORT).show();
        });

        // También se debe eliminar el archivo de imagen y audio de Firebase Storage
        StorageReference imagenRef = FirebaseStorage.getInstance().getReferenceFromUrl(entrevista.getImagenUrl());
        StorageReference audioRef = FirebaseStorage.getInstance().getReferenceFromUrl(entrevista.getAudioUrl());

        imagenRef.delete().addOnSuccessListener(aVoid -> {
            // Imagen eliminada
        }).addOnFailureListener(e -> {
            Toast.makeText(ListaEntrevistasActivity.this, "Error al eliminar la imagen", Toast.LENGTH_SHORT).show();
        });

        audioRef.delete().addOnSuccessListener(aVoid -> {
            // Audio eliminado
        }).addOnFailureListener(e -> {
            Toast.makeText(ListaEntrevistasActivity.this, "Error al eliminar el audio", Toast.LENGTH_SHORT).show();
        });
    }
}





