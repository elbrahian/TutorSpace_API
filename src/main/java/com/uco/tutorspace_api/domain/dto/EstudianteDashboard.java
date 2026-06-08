package com.uco.tutorspace_api.domain.dto;

import java.util.List;

public class EstudianteDashboard {


    private long totalSesionesCompletadas;
    private double totalHorasTutoria;
    private String materiaMasConsultada;
    private String tutorFrecuente;
    private List<ActividadMensual> actividadMensual;

    public EstudianteDashboard() {}

    public long getTotalSesionesCompletadas() { return totalSesionesCompletadas; }
    public void setTotalSesionesCompletadas(long v) { this.totalSesionesCompletadas = v; }

    public double getTotalHorasTutoria() { return totalHorasTutoria; }
    public void setTotalHorasTutoria(double v) { this.totalHorasTutoria = v; }

    public String getMateriaMasConsultada() { return materiaMasConsultada; }
    public void setMateriaMasConsultada(String v) { this.materiaMasConsultada = v; }

    public String getTutorFrecuente() { return tutorFrecuente; }
    public void setTutorFrecuente(String v) { this.tutorFrecuente = v; }

    public List<ActividadMensual> getActividadMensual() { return actividadMensual; }
    public void setActividadMensual(List<ActividadMensual> v) { this.actividadMensual = v; }

}
