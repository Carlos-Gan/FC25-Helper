import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Date;
import java.util.Optional;
import java.util.Vector;

public class PanelTransacciones extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:football_manager.db";

    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240);

    private JComboBox<String> cbTipo;
    private JComboBox<String> cbJugadores;
    private JTextField tfJugador, tfEquipoOrigen, tfEquipoDestino, tfPrecio;
    private JTextField tfApellido, tfPosicion, tfMedia;

    private JTable tablaTransacciones;
    private DefaultTableModel modeloTransacciones;

    private JPanel panelCampoJugador;

    private JDateChooser dateChooser;

    public PanelTransacciones() {
        initComponents();
        crearTablaSiNoExiste();
        cargarTransacciones();
        actualizarCamposJugador();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBackground(SECONDARY_COLOR);

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTablaConScroll(), BorderLayout.CENTER);
    }

    private JPanel crearFormulario() {
        cbTipo = new JComboBox<>(new String[] { "Compra", "Venta" });
        cbTipo.addActionListener(_ -> actualizarCamposJugador());

        tfJugador = new JTextField();
        cbJugadores = new JComboBox<>();

        tfApellido = new JTextField();
        tfPosicion = new JTextField();
        tfMedia = new JTextField();

        tfEquipoOrigen = new JTextField();
        tfEquipoDestino = new JTextField();
        tfPrecio = new JTextField();

        dateChooser = new JDateChooser(new Date());
        dateChooser.setDateFormatString("dd-MM-yyyy");

        panelCampoJugador = new JPanel(new BorderLayout());
        panelCampoJugador.setBackground(SECONDARY_COLOR);

        JButton btnRegistrar = crearBoton("Registrar");
        btnRegistrar.addActionListener(_ -> registrarTransaccion());

        // Usamos GroupLayout para diseño más flexible y moderno
        var panel = new JPanel();
        panel.setBackground(SECONDARY_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("Registrar Transacción"));
        var layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel lblTipo = nuevaEtiqueta("Tipo:");
        JLabel lblJugador = nuevaEtiqueta("Jugador:");
        JLabel lblApellido = nuevaEtiqueta("Apellido:");
        JLabel lblPosicion = nuevaEtiqueta("Posición:");
        JLabel lblMedia = nuevaEtiqueta("Media:");
        JLabel lblOrigen = nuevaEtiqueta("Equipo Origen:");
        JLabel lblDestino = nuevaEtiqueta("Equipo Destino:");
        JLabel lblPrecio = nuevaEtiqueta("Precio:");
        JLabel lblFecha = nuevaEtiqueta("Fecha:");

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblTipo)
                        .addComponent(lblJugador)
                        .addComponent(lblApellido)
                        .addComponent(lblPosicion)
                        .addComponent(lblMedia)
                        .addComponent(lblOrigen)
                        .addComponent(lblDestino)
                        .addComponent(lblPrecio)
                        .addComponent(lblFecha))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(cbTipo)
                        .addComponent(panelCampoJugador)
                        .addComponent(tfApellido)
                        .addComponent(tfPosicion)
                        .addComponent(tfMedia)
                        .addComponent(tfEquipoOrigen)
                        .addComponent(tfEquipoDestino)
                        .addComponent(tfPrecio)
                        .addComponent(dateChooser)
                        .addComponent(btnRegistrar, GroupLayout.Alignment.TRAILING)));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblTipo)
                        .addComponent(cbTipo))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblJugador)
                        .addComponent(panelCampoJugador))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblApellido)
                        .addComponent(tfApellido))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPosicion)
                        .addComponent(tfPosicion))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMedia)
                        .addComponent(tfMedia))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblOrigen)
                        .addComponent(tfEquipoOrigen))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblDestino)
                        .addComponent(tfEquipoDestino))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblPrecio)
                        .addComponent(tfPrecio))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblFecha)
                        .addComponent(dateChooser))
                .addGap(15)
                .addComponent(btnRegistrar));

        return panel;
    }

    private JScrollPane crearTablaConScroll() {
        var columnas = new String[] { "ID", "Tipo", "Jugador", "Origen", "Destino", "Precio", "Fecha" };
        modeloTransacciones = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable
            }
        };

        tablaTransacciones = new JTable(modeloTransacciones);
        tablaTransacciones.setFillsViewportHeight(true);
        tablaTransacciones.setRowHeight(28);
        tablaTransacciones.setGridColor(Color.LIGHT_GRAY);
        tablaTransacciones.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(tablaTransacciones);
    }

    private JLabel nuevaEtiqueta(String texto) {
        var label = new JLabel(texto);
        label.setFont(LABEL_FONT);
        return label;
    }

    private JButton crearBoton(String texto) {
        var btn = new JButton(texto);
        btn.setFont(LABEL_FONT);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    private void actualizarCamposJugador() {
        var tipo = cbTipo.getSelectedItem().toString();
        panelCampoJugador.removeAll();

        if ("Compra".equals(tipo)) {
            panelCampoJugador.add(tfJugador, BorderLayout.CENTER);
            setJugadorCamposVisible(true);
        } else {
            cargarJugadoresEnCombo();
            panelCampoJugador.add(cbJugadores, BorderLayout.CENTER);
            setJugadorCamposVisible(false);
        }
        panelCampoJugador.revalidate();
        panelCampoJugador.repaint();
    }

    private void setJugadorCamposVisible(boolean visible) {
        tfApellido.setVisible(visible);
        tfPosicion.setVisible(visible);
        tfMedia.setVisible(visible);
    }

    private void cargarJugadoresEnCombo() {
        cbJugadores.removeAllItems();
        var sql = """
                SELECT id, nombre FROM Jugadores
                WHERE id NOT IN (
                    SELECT jugador_id FROM Transacciones WHERE tipo = 'Venta'
                )
                """;

        try (var conn = DriverManager.getConnection(DB_URL);
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                cbJugadores.addItem(id + " - " + nombre);
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar jugadores: " + e.getMessage());
        }
    }

    private void registrarTransaccion() {
        var tipo = cbTipo.getSelectedItem().toString();

        var jugador = tipo.equals("Venta")
                ? Optional.ofNullable(cbJugadores.getSelectedItem()).map(Object::toString).orElse("")
                : tfJugador.getText().trim();

        var origen = tfEquipoOrigen.getText().trim();
        var destino = tfEquipoDestino.getText().trim();
        var precioStr = tfPrecio.getText().trim();

        var selectedDate = dateChooser.getDate();
        if (selectedDate == null) {
            mostrarError("Selecciona una fecha válida");
            return;
        }
        var fecha = new java.sql.Date(selectedDate.getTime()).toString();

        if (jugador.isEmpty() || origen.isEmpty() || destino.isEmpty() || precioStr.isEmpty()) {
            mostrarError("Todos los campos son obligatorios");
            return;
        }

        double precio;
        try {
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException e) {
            mostrarError("Precio inválido (usa solo números)");
            return;
        }

        try (var conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            try (var pragmaStmt = conn.createStatement()) {
                pragmaStmt.execute("PRAGMA foreign_keys=ON");
            }

            int jugador_id;
            if ("Compra".equals(tipo)) {
                jugador_id = insertarJugadorSiNoExiste(conn, jugador);
                if (jugador_id == -1) {
                    conn.rollback();
                    mostrarError("No se pudo registrar el jugador.");
                    return;
                }
            } else {
                jugador_id = obtenerIdJugador(jugador);
                if (jugador_id == -1) {
                    conn.rollback();
                    mostrarError("No se pudo encontrar el jugador para la venta.");
                    return;
                }
            }

            var sql = """
                    INSERT INTO Transacciones
                    (tipo, jugador_id, equipo_origen, equipo_destino, precio, fecha)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tipo);
                pstmt.setInt(2, jugador_id);
                pstmt.setString(3, origen);
                pstmt.setString(4, destino);
                pstmt.setDouble(5, precio);
                pstmt.setString(6, fecha);

                pstmt.executeUpdate();
            }

            conn.commit();
            limpiarCampos();
            cargarTransacciones();
            JOptionPane.showMessageDialog(this, "Transacción registrada con éxito");

        } catch (SQLException e) {
            mostrarError("Error al registrar transacción: " + e.getMessage());
        }
    }

    private int insertarJugadorSiNoExiste(Connection conn, Object jugador) throws SQLException {
        var selectSQL = "SELECT id FROM Jugadores WHERE nombre = ?";
        try (var pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, jugador.toString());
            var rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt("id");
        }

        if (tfMedia.getText().trim().isEmpty()) {
            mostrarError("Media es un campo obligatorio");
            return -1;
        }
        if (tfApellido.getText().trim().isEmpty()) {
            mostrarError("Apellido es un campo obligatorio");
            return -1;
        }
        if (tfPosicion.getText().trim().isEmpty()) {
            mostrarError("Posición es un campo obligatorio");
            return -1;
        }
        if (tfJugador.getText().trim().isEmpty()) {
            mostrarError("Nombre es un campo obligatorio");
            return -1;
        }
        if (tfEquipoOrigen.getText().trim().isEmpty()) {
            mostrarError("Equipo Origen es un campo obligatorio");
            return -1;
        }
        if (tfEquipoDestino.getText().trim().isEmpty()) {
            mostrarError("Equipo Destino es un campo obligatorio");
            return -1;
        }
        if (tfPrecio.getText().trim().isEmpty()) {
            mostrarError("Precio es un campo obligatorio");
            return -1;
        }
        if (!tfPrecio.getText().matches("\\d+(\\.\\d+)?")) {
            mostrarError("Precio inválido (usa solo números)");
            return -1;
        }
        if (!tfMedia.getText().matches("\\d+")) {
            mostrarError("Media inválida (usa solo números)");
            return -1;
        }
        if (!tfApellido.getText().matches("[a-zA-Z]+")) {
            mostrarError("Apellido inválido (usa solo letras)");
            return -1;
        }
        if (tfMedia.getText().trim().isEmpty()) {
            mostrarError("Media es un campo obligatorio");
            return -1;
        }
        if (!tfMedia.getText().matches("\\d+")) {
            mostrarError("Media inválida (usa solo números)");
            return -1;
        }
        if (Integer.parseInt(tfMedia.getText().trim()) <= 0 || Integer.parseInt(tfMedia.getText().trim()) > 100) {
            mostrarError("Media inválida (debe ser mayor a 0 y menor o igual a 100)");
            return -1;
        }

        var insertSQL = "INSERT INTO Jugadores (nombre, apellido, posicion, media, equipo) VALUES (?, ?, ?, ?, ?)";
        try (var pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, (String) tfJugador.getText().trim());
            pstmt.setString(2, (String) tfApellido.getText().trim());
            pstmt.setString(3, (String) tfPosicion.getText().trim());
            pstmt.setInt(4, Integer.parseInt(tfMedia.getText().trim()));
            pstmt.setString(5, (String) tfEquipoDestino.getText().trim());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0)
                return -1;

            var rs = pstmt.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);
        }
        return -1;
    }

    private int obtenerIdJugador(Object jugador) {
        if (jugador == null || jugador.toString().isEmpty())
            return -1;
        var idStr = jugador.toString().split(" - ")[0].trim();
        try {
            return Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void limpiarCampos() {
        tfJugador.setText("");
        tfApellido.setText("");
        tfPosicion.setText("");
        tfMedia.setText("");
        tfEquipoOrigen.setText("");
        tfEquipoDestino.setText("");
        tfPrecio.setText("");
        dateChooser.setDate(new Date());
        cbJugadores.setSelectedIndex(-1);
    }

    private void cargarTransacciones() {
        modeloTransacciones.setRowCount(0);
        var sql = """
                SELECT t.id, t.tipo, j.nombre, t.equipo_origen, t.equipo_destino, t.precio, t.fecha
                FROM Transacciones t
                JOIN Jugadores j ON t.jugador_id = j.id
                ORDER BY t.fecha DESC
                """;

        try (var conn = DriverManager.getConnection(DB_URL);
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()) {

            while (rs.next()) {
                var fila = new Vector<Object>();
                fila.add(rs.getInt("id"));
                fila.add(rs.getString("tipo"));
                fila.add(rs.getString("nombre"));
                fila.add(rs.getString("equipo_origen"));
                fila.add(rs.getString("equipo_destino"));
                fila.add(rs.getDouble("precio"));
                fila.add(rs.getString("fecha"));

                modeloTransacciones.addRow(fila);
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar transacciones: " + e.getMessage());
        }
    }

    private void crearTablaSiNoExiste() {
        var sql = """
                CREATE TABLE IF NOT EXISTS Jugadores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT NOT NULL UNIQUE
                );
                CREATE TABLE IF NOT EXISTS Transacciones (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL CHECK(tipo IN ('Compra', 'Venta')),
                    jugador_id INTEGER NOT NULL,
                    equipo_origen TEXT NOT NULL,
                    equipo_destino TEXT NOT NULL,
                    precio REAL NOT NULL,
                    fecha TEXT NOT NULL,
                    FOREIGN KEY (jugador_id) REFERENCES Jugadores(id)
                );
                """;

        try (var conn = DriverManager.getConnection(DB_URL);
                var stmt = conn.createStatement()) {

            for (var sqlStmt : sql.split(";")) {
                if (!sqlStmt.trim().isEmpty())
                    stmt.execute(sqlStmt.trim());
            }
        } catch (SQLException e) {
            mostrarError("Error al crear tablas: " + e.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
