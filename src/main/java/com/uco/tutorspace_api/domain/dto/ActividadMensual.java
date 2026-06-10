package com.uco.tutorspace_api.domain.dto;

public class ActividadMensual {

    private int mes;       // 1–12
    private String nombreMes; // "Enero", "Febrero"...
    private long sesiones;

    public ActividadMensual(int mes, String nombreMes, long sesiones) {
        this.mes = mes;
        this.nombreMes = nombreMes;
        this.sesiones = sesiones;
    }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public String getNombreMes() { return nombreMes; }
    public void setNombreMes(String nombreMes) { this.nombreMes = nombreMes; }

    public long getSesiones() { return sesiones; }
    public void setSesiones(long sesiones) { this.sesiones = sesiones; }
}

