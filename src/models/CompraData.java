package models;

public class CompraData {
    public final String jugador;
    public final double costo;
    public final String fecha;

    public CompraData(String jugador, double costo, String fecha) {
        this.jugador = jugador;
        this.costo = costo;
        this.fecha = fecha;
    }
}
