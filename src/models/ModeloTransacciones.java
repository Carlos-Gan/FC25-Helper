package models;

import java.util.*;

public class ModeloTransacciones {
    private List<Transaccion> transacciones;

    public ModeloTransacciones() {
        transacciones = new ArrayList<>();
    }

    public void agregarTransaccion(Transaccion t) {
        transacciones.add(t);
    }

    public List<Transaccion> obtenerTransacciones() {
        return transacciones;
    }

    public Transaccion getTransaccionMasCara(String tipo) {
        return transacciones.stream()
            .filter(t -> t.getTipo().equalsIgnoreCase(tipo))
            .max(Comparator.comparingDouble(Transaccion::getPrecio))
            .orElse(null);
    }

    // Puedes agregar métodos para guardar y cargar CSV aquí si lo necesitas
}
