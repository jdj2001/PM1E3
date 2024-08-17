package com.example.pm1e3;

import java.io.Serializable;

public class Entrevista implements Serializable{
    private String idOrden;
    private String descripcion;
    private String periodista;
    private String fecha;
    private String imagenUrl;
    private String audioUrl;

    public Entrevista() {}

    public Entrevista(String idOrden, String descripcion, String periodista, String fecha, String imagenUrl, String audioUrl) {
        this.idOrden = idOrden;
        this.descripcion = descripcion;
        this.periodista = periodista;
        this.fecha = fecha;
        this.imagenUrl = imagenUrl;
        this.audioUrl = audioUrl;
    }

    public String getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(String idOrden) {
        this.idOrden = idOrden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }
}

