package models;

import java.time.LocalDate;

public class Transaccion {
    private String nombreJugador;
    private String tipo; // "Compra" o "Venta"
    private double precio;
    private LocalDate fecha;

    public Transaccion(String nombreJugador, String tipo, double precio, LocalDate fecha) {
        this.nombreJugador = nombreJugador;
        this.tipo = tipo;
        this.precio = precio;
        this.fecha = fecha;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public String getTipo() {
        return tipo;
    }

    public double getPrecio() {
        return precio;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
