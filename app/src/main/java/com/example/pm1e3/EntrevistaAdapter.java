package com.example.pm1e3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class EntrevistaAdapter extends ArrayAdapter<Entrevista> {
    private Activity context;
    private List<Entrevista> listaEntrevistas;

    public EntrevistaAdapter(Activity context, List<Entrevista> listaEntrevistas) {
        super(context, R.layout.entrevista_item, listaEntrevistas);
        this.context = context;
        this.listaEntrevistas = listaEntrevistas;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.entrevista_item, null, true);

        TextView textViewDescripcion = listViewItem.findViewById(R.id.textViewDescripcion);
        ImageView imageViewEntrevista = listViewItem.findViewById(R.id.imageViewEntrevista);

        Entrevista entrevista = listaEntrevistas.get(position);
        textViewDescripcion.setText(entrevista.getDescripcion());

        // Cargar imagen con Glide o Picasso
        Glide.with(context).load(entrevista.getImagenUrl()).into(imageViewEntrevista);

        return listViewItem;
    }
}




