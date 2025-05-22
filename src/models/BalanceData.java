package models;

public class BalanceData {
    public final String fecha;
    public final double ganancia;
    public final double costo;

    public BalanceData(String fecha, double ganancia, double costo) {
        this.fecha = fecha;
        this.ganancia = ganancia;
        this.costo = costo;
    }
}
