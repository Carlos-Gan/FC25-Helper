import com.toedter.calendar.JDateChooser;

import models.BalanceData;
import models.CompraData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

public class PanelTransacciones extends JPanel {

    private static final String DB_URL = "jdbc:sqlite:football_manager.db";

    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240);

    private JComboBox<String> cbTipo;
    private JComboBox<String> cbJugadores;
    private JComboBox<String> cbPosicion;
    private JTextField tfJugador, tfEquipoOrigen, tfEquipoDestino, tfPrecio;
    private JTextField tfApellido, tfMedia, tfValorMercado;
    private JLabel lblGananciaCosto, lblValorGananciaCosto;

    private JTabbedPane tabbedPane;

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

        tabbedPane = new JTabbedPane();

        JPanel panelTransacciones = new JPanel(new BorderLayout(10, 10));
        panelTransacciones.add(crearFormulario(), BorderLayout.NORTH);
        panelTransacciones.add(crearTablaConScroll(), BorderLayout.CENTER);

        tabbedPane.addTab("Transacciones", panelTransacciones);
        tabbedPane.addTab("Graficas", crearPanelGraficas());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel crearPanelGraficas() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Combo con estilo moderno
        String[] opciones = { "Barras", "Líneas" };
        JComboBox<String> comboTipoGrafica = new JComboBox<>(opciones);
        comboTipoGrafica.setSelectedIndex(0);
        comboTipoGrafica.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboTipoGrafica.setBackground(new Color(240, 240, 240));
        comboTipoGrafica.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Panel de gráficas
        JPanel panelGraficas = new JPanel();
        panelGraficas.setLayout(new BoxLayout(panelGraficas, BoxLayout.Y_AXIS));
        panelGraficas.setBackground(Color.WHITE);

        // Función para actualizar gráficos
        Runnable actualizarGraficas = () -> {
            panelGraficas.removeAll();
            String tipo = (String) comboTipoGrafica.getSelectedItem();

            panelGraficas.add(espaciadoSuperior(crearGraficoVentas(tipo)));
            panelGraficas.add(espaciadoSuperior(crearGraficoCompras(tipo)));
            panelGraficas.add(espaciadoSuperior(crearGraficoBalancePorFecha(tipo)));
            panelGraficas.add(espaciadoSuperior(crearGraficoResumen(tipo)));

            panelGraficas.revalidate();
            panelGraficas.repaint();
        };

        // Listener del combo
        comboTipoGrafica.addActionListener(e -> actualizarGraficas.run());

        // Inicial
        actualizarGraficas.run();

        JScrollPane scroll = new JScrollPane(panelGraficas);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panelPrincipal.add(comboTipoGrafica, BorderLayout.NORTH);
        panelPrincipal.add(scroll, BorderLayout.CENTER);

        return panelPrincipal;
    }

    private Component espaciadoSuperior(JComponent grafico) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.add(grafico, BorderLayout.CENTER);
        return panel;
    }

    private void configurarGrafico(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(245, 245, 245));
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(200, 200, 200));

        NumberAxis axis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
        axis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
        axis.setNumberFormatOverride(NumberFormat.getCurrencyInstance(Locale.GERMANY));

        if (plot.getRenderer() instanceof BarRenderer renderer) {
            renderer.setDefaultItemLabelsVisible(true);
            renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
            renderer.setDefaultPositiveItemLabelPosition(
                    new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));
            renderer.setSeriesPaint(0, new Color(72, 133, 237)); // Azul moderno
            if (plot.getDataset().getRowCount() > 1)
                renderer.setSeriesPaint(1, new Color(219, 68, 55)); // Rojo moderno
        }
    }

    private ChartPanel crearGraficoResumen(String tipo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> resumen = obtenerResumenFinanciero();

        dataset.addValue(resumen.getOrDefault("total_ingresos", 0.0), "Ingresos", "Total");
        dataset.addValue(resumen.getOrDefault("total_gastos", 0.0), "Gastos", "Total");
        dataset.addValue(resumen.getOrDefault("balance_neto", 0.0), "Balance Neto", "Total");

        JFreeChart chart = crearGrafico("Resumen Financiero Total", "", "Cantidad (€)", dataset, tipo);
        return new ChartPanel(chart);
    }

    private ChartPanel crearGraficoBalancePorFecha(String tipo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<BalanceData> datos = obtenerDatosBalance();

        for (BalanceData dato : datos) {
            double balanceNeto = dato.ganancia - dato.costo;
            dataset.addValue(balanceNeto, "Balance Neto", dato.fecha);
        }

        JFreeChart chart = crearGrafico("Balance Neto por Fecha", "Fecha", "Balance (€)", dataset, tipo);
        return new ChartPanel(chart);
    }

    private ChartPanel crearGraficoCompras(String tipo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        List<CompraData> compras = obtenerDatosCompras();

        for (CompraData compra : compras) {
            dataset.addValue(compra.costo, compra.jugador, compra.fecha);
        }

        JFreeChart chart = crearGrafico("Costos por Compras", "Fecha", "Costo (€)", dataset, tipo);
        return new ChartPanel(chart);
    }

    private ChartPanel crearGraficoVentas(String tipo) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String sql = """
                SELECT t.fecha, SUM(t.ganancia_costo) AS ganancia
                FROM Transacciones t
                WHERE t.tipo = 'Venta'
                GROUP BY t.fecha
                ORDER BY t.fecha
                """;

        try (var conn = DriverManager.getConnection(DB_URL);
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fecha = rs.getString("fecha");
                double ganancia = rs.getDouble("ganancia");
                dataset.addValue(ganancia, "Ganancias", fecha);
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar datos de ganancias: " + e.getMessage());
        }

        JFreeChart chart = crearGrafico("Ganancias por Ventas", "Fecha", "Ganancia (€)", dataset, tipo);
        return new ChartPanel(chart);
    }

    private JFreeChart crearGrafico(String titulo, String categoria, String valorY, DefaultCategoryDataset dataset,
            String tipo) {
        if (tipo.equals("Líneas")) {
            JFreeChart chart = ChartFactory.createLineChart(
                    titulo, categoria, valorY, dataset,
                    PlotOrientation.VERTICAL, true, true, false);
            configurarGrafico(chart);
            return chart;
        } else {
            JFreeChart chart = ChartFactory.createBarChart(
                    titulo, categoria, valorY, dataset,
                    PlotOrientation.VERTICAL, true, true, false);
            configurarGrafico(chart);
            return chart;
        }
    }

    private JPanel crearFormulario() {
        cbTipo = new JComboBox<>(new String[] { "Compra", "Venta" });
        cbTipo.addActionListener(_ -> actualizarCamposJugador());

        tfJugador = new JTextField();
        cbJugadores = new JComboBox<>();
        cbJugadores.addActionListener(_ -> calcularGananciaCosto());

        tfApellido = new JTextField();
        tfMedia = new JTextField();
        tfValorMercado = new JTextField();

        tfEquipoOrigen = new JTextField();
        tfEquipoDestino = new JTextField();
        tfPrecio = new JTextField();
        tfPrecio.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calcularGananciaCosto();
            }
        });

        dateChooser = new JDateChooser(new Date());
        dateChooser.setDateFormatString("dd-MM-yyyy");

        // Labels para mostrar ganancia/costo
        lblGananciaCosto = new JLabel("Ganancia/Costo:");
        lblGananciaCosto.setFont(LABEL_FONT);
        lblValorGananciaCosto = new JLabel("€0.00");
        lblValorGananciaCosto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblValorGananciaCosto.setForeground(PRIMARY_COLOR);

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
        JLabel lblValorMercado = nuevaEtiqueta("Valor de Mercado:");
        JLabel lblOrigen = nuevaEtiqueta("Equipo Origen:");
        JLabel lblDestino = nuevaEtiqueta("Equipo Destino:");
        JLabel lblPrecio = nuevaEtiqueta("Precio:");
        JLabel lblFecha = nuevaEtiqueta("Fecha:");

        cbPosicion = new JComboBox<>(
                new String[] { "POR", "DFI", "DFC", "DFD", "MCD", "MC", "MI", "MD", "MCO", "MP", "EI", "ED", "DC" });

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblTipo)
                        .addComponent(lblJugador)
                        .addComponent(lblApellido)
                        .addComponent(lblPosicion)
                        .addComponent(lblMedia)
                        .addComponent(lblValorMercado)
                        .addComponent(lblOrigen)
                        .addComponent(lblDestino)
                        .addComponent(lblPrecio)
                        .addComponent(lblFecha)
                        .addComponent(lblGananciaCosto))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(cbTipo)
                        .addComponent(panelCampoJugador)
                        .addComponent(tfApellido)
                        .addComponent(cbPosicion)
                        .addComponent(tfMedia)
                        .addComponent(tfValorMercado)
                        .addComponent(tfEquipoOrigen)
                        .addComponent(tfEquipoDestino)
                        .addComponent(tfPrecio)
                        .addComponent(dateChooser)
                        .addComponent(lblValorGananciaCosto)
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
                        .addComponent(cbPosicion))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblMedia)
                        .addComponent(tfMedia))
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblValorMercado)
                        .addComponent(tfValorMercado))
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
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(lblGananciaCosto)
                        .addComponent(lblValorGananciaCosto))
                .addGap(15)
                .addComponent(btnRegistrar));

        return panel;
    }

    private JScrollPane crearTablaConScroll() {
        var columnas = new String[] { "ID", "Tipo", "Jugador", "Origen", "Destino", "Precio", "Valor Mercado",
                "Ganancia/Costo", "Fecha" };
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
            lblGananciaCosto.setText("Costo Total:");
        } else {
            cargarJugadoresEnCombo();
            panelCampoJugador.add(cbJugadores, BorderLayout.CENTER);
            setJugadorCamposVisible(false);
            lblGananciaCosto.setText("Ganancia:");
        }
        panelCampoJugador.revalidate();
        panelCampoJugador.repaint();
        calcularGananciaCosto();
    }

    private void setJugadorCamposVisible(boolean visible) {
        tfApellido.setVisible(visible);
        cbPosicion.setVisible(visible);
        tfMedia.setVisible(visible);
        tfValorMercado.setVisible(visible);
    }

    private void calcularGananciaCosto() {
        String tipo = cbTipo.getSelectedItem().toString();
        String precioStr = tfPrecio.getText().trim();

        if (precioStr.isEmpty()) {
            mostrarGananciaCosto(0.0, PRIMARY_COLOR);
            return;
        }

        try {
            double precio = Double.parseDouble(precioStr);

            if ("Compra".equals(tipo)) {
                // Muestra el costo de compra como valor negativo (opcional)
                mostrarGananciaCosto(precio, Color.RED);
            } else {
                // Para venta: ganancia = precio venta - valor mercado
                double valorMercado = obtenerValorMercadoJugadorSeleccionado();
                double ganancia = precio - valorMercado;
                Color color = ganancia >= 0 ? new Color(0, 150, 0) : Color.RED;
                mostrarGananciaCosto(ganancia, color);
            }
        } catch (NumberFormatException e) {
            mostrarGananciaCosto(0.0, PRIMARY_COLOR);
        }
    }

    private void mostrarGananciaCosto(double valor, Color color) {
        lblValorGananciaCosto.setText("€" + formatearValorDecimal(valor));
        lblValorGananciaCosto.setForeground(color);
    }

    private double obtenerValorMercadoJugador(Connection conn, int jugadorId) throws SQLException {
        String sql = "SELECT valor_mercado FROM Jugadores WHERE id = ?";
        try (var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, jugadorId);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_mercado");
            }
        }
        return 0.0;
    }

    private double obtenerValorMercadoJugadorSeleccionado() {
        if (cbJugadores.getSelectedItem() == null)
            return 0.0;

        int jugadorId = obtenerIdJugador(cbJugadores.getSelectedItem());
        if (jugadorId == -1)
            return 0.0;

        String sql = "SELECT valor_mercado FROM Jugadores WHERE id = ?";
        try (var conn = DriverManager.getConnection(DB_URL);
                var pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, jugadorId);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("valor_mercado");
            }
        } catch (SQLException e) {
            // Error silencioso para no interrumpir la experiencia del usuario
        }
        return 0.0;
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
            double valorMercado = 0.0;
            double gananciaCosto = 0.0;

            if ("Compra".equals(tipo)) {
                jugador_id = insertarJugadorSiNoExiste(conn, jugador);
                if (jugador_id == -1) {
                    conn.rollback();
                    mostrarError("No se pudo registrar el jugador.");
                    return;
                }
                // Para compra, el costo es el precio pagado
                gananciaCosto = precio;
                valorMercado = Double.parseDouble(tfValorMercado.getText().trim());
            } else {
                jugador_id = obtenerIdJugador(jugador);
                if (jugador_id == -1) {
                    conn.rollback();
                    mostrarError("No se pudo encontrar el jugador para la venta.");
                    return;
                }
                // Para venta, obtener valor de mercado y calcular ganancia
                valorMercado = obtenerValorMercadoJugador(conn, jugador_id);
                gananciaCosto = precio - valorMercado;
            }

            var sql = """
                    INSERT INTO Transacciones
                    (tipo, jugador_id, equipo_origen, equipo_destino, precio, valor_mercado, ganancia_costo, fecha)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;

            try (var pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, tipo);
                pstmt.setInt(2, jugador_id);
                pstmt.setString(3, origen);
                pstmt.setString(4, destino);
                pstmt.setDouble(5, precio);
                pstmt.setDouble(6, valorMercado);
                pstmt.setDouble(7, gananciaCosto);
                pstmt.setString(8, fecha);

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
        if (cbPosicion.getSelectedItem() == null) {
            mostrarError("Posición es un campo obligatorio");
            return -1;
        }
        if (tfJugador.getText().trim().isEmpty()) {
            mostrarError("Nombre es un campo obligatorio");
            return -1;
        }
        if (tfValorMercado.getText().trim().isEmpty()) {
            mostrarError("Valor de Mercado es un campo obligatorio");
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
        if (!tfValorMercado.getText().matches("\\d+(\\.\\d+)?")) {
            mostrarError("Valor de Mercado inválido (usa solo números)");
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
        if (Integer.parseInt(tfMedia.getText().trim()) <= 0 || Integer.parseInt(tfMedia.getText().trim()) > 100) {
            mostrarError("Media inválida (debe ser mayor a 0 y menor o igual a 100)");
            return -1;
        }
        if (Double.parseDouble(tfValorMercado.getText().trim()) < 0) {
            mostrarError("Valor de Mercado inválido (debe ser mayor o igual a 0)");
            return -1;
        }

        var insertSQL = "INSERT INTO Jugadores (nombre, apellido, posicion, media, valor_mercado, equipo) VALUES (?, ?, ?, ?, ?, ?)";
        try (var pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tfJugador.getText().trim());
            pstmt.setString(2, tfApellido.getText().trim());
            pstmt.setString(3, cbPosicion.getSelectedItem().toString().trim());
            pstmt.setInt(4, Integer.parseInt(tfMedia.getText().trim()));
            pstmt.setDouble(5, Double.parseDouble(tfValorMercado.getText().trim()));
            pstmt.setString(6, tfEquipoDestino.getText().trim());
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
        cbPosicion.setSelectedIndex(-1);
        tfMedia.setText("");
        tfValorMercado.setText("");
        tfEquipoOrigen.setText("");
        tfEquipoDestino.setText("");
        tfPrecio.setText("");
        dateChooser.setDate(new Date());
        cbJugadores.setSelectedIndex(-1);
        lblValorGananciaCosto.setText("€0.00");
        lblValorGananciaCosto.setForeground(PRIMARY_COLOR);
    }

    private void cargarTransacciones() {
        modeloTransacciones.setRowCount(0);

        NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.GERMANY);

        var sql = """
                SELECT t.id, t.tipo, j.nombre, t.equipo_origen, t.equipo_destino, t.precio,
                       COALESCE(t.valor_mercado, j.valor_mercado) as valor_mercado,
                       COALESCE(t.ganancia_costo,
                           CASE WHEN t.tipo = 'Compra' THEN t.precio
                                ELSE t.precio - COALESCE(t.valor_mercado, j.valor_mercado)
                           END) as ganancia_costo,
                       t.fecha
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
                float precio = rs.getFloat("precio");
                float valorMercado = rs.getFloat("valor_mercado");
                float gananciaCosto = rs.getFloat("ganancia_costo");

                fila.add(formatoMoneda.format(precio));
                fila.add(formatoMoneda.format(valorMercado));
                fila.add(formatoMoneda.format(gananciaCosto));

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
                    nombre TEXT NOT NULL UNIQUE,
                    apellido TEXT,
                    posicion TEXT,
                    media INTEGER,
                    valor_mercado REAL DEFAULT 0,
                    equipo TEXT
                );
                CREATE TABLE IF NOT EXISTS Transacciones (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT NOT NULL CHECK(tipo IN ('Compra', 'Venta')),
                    jugador_id INTEGER NOT NULL,
                    equipo_origen TEXT NOT NULL,
                    equipo_destino TEXT NOT NULL,
                    precio REAL NOT NULL,
                    valor_mercado REAL DEFAULT 0,
                    ganancia_costo REAL DEFAULT 0,
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

            // Agregar columnas si no existen (para compatibilidad con DBs existentes)
            try {
                stmt.execute("ALTER TABLE Jugadores ADD COLUMN valor_mercado REAL DEFAULT 0");
            } catch (SQLException e) {
                // La columna ya existe, ignorar error
            }

            try {
                stmt.execute("ALTER TABLE Transacciones ADD COLUMN valor_mercado REAL DEFAULT 0");
            } catch (SQLException e) {
                // La columna ya existe, ignorar error
            }

            try {
                stmt.execute("ALTER TABLE Transacciones ADD COLUMN ganancia_costo REAL DEFAULT 0");
            } catch (SQLException e) {
                // La columna ya existe, ignorar error
            }

        } catch (SQLException e) {
            mostrarError("Error al crear tablas: " + e.getMessage());
        }
    }

    public double[] obtenerGananciasVentas() {
        String sql = "SELECT ganancia_costo FROM Transacciones WHERE tipo = 'Venta' ORDER BY fecha";
        ArrayList<Double> ganancias = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                double valor = rs.getDouble("ganancia_costo");
                ganancias.add(formatearValorDecimal(valor));
            }
        } catch (SQLException e) {
            mostrarError("Error al obtener ganancias: " + e.getMessage());
        }

        return ganancias.isEmpty() ? new double[] { 0.0 }
                : ganancias.stream().mapToDouble(Double::doubleValue).toArray();
    }

    // Método para obtener datos de costos para gráficas
    public double[] obtenerCostosCompras() {
        String sql = "SELECT ganancia_costo FROM Transacciones WHERE tipo = 'Compra' ORDER BY fecha";
        ArrayList<Double> costos = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL);
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                double valor = rs.getDouble("ganancia_costo");
                costos.add(formatearValorDecimal(valor));
            }
        } catch (SQLException e) {
            mostrarError("Error al obtener costos: " + e.getMessage());
        }

        return costos.isEmpty() ? new double[] { 0.0 } : costos.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public List<CompraData> obtenerDatosCompras() {
        var sql = """
                SELECT j.nombre AS jugador, t.ganancia_costo AS costo, t.fecha
                FROM Transacciones t
                JOIN Jugadores j ON t.jugador_id = j.id
                WHERE t.tipo = 'Compra'
                ORDER BY t.fecha
                """;

        var lista = new ArrayList<CompraData>();
        try (var conn = DriverManager.getConnection(DB_URL);
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()) {

            while (rs.next()) {
                var jugador = rs.getString("jugador");
                var costo = rs.getDouble("costo");
                var fecha = rs.getString("fecha");
                lista.add(new CompraData(jugador, costo, fecha));
            }

        } catch (SQLException e) {
            mostrarError("Error al obtener datos de compras: " + e.getMessage());
        }

        return lista;
    }

    public String[] obtenerFechasTransacciones(String tipo) {
        var sql = "SELECT fecha FROM Transacciones WHERE tipo = ? ORDER BY fecha";
        var fechas = new java.util.ArrayList<String>();

        try (var conn = DriverManager.getConnection(DB_URL);
                var pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tipo);
            var rs = pstmt.executeQuery();

            while (rs.next()) {
                fechas.add(rs.getString("fecha"));
            }
        } catch (SQLException e) {
            mostrarError("Error al obtener fechas: " + e.getMessage());
        }

        return fechas.isEmpty() ? new String[] { "No hay datos" } : fechas.toArray(new String[0]);
    }

    private double formatearValorDecimal(double valor) {
        DecimalFormat formato = new DecimalFormat("#.##");
        return Double.parseDouble(formato.format(valor).replace(",", "."));
    }

    // Método para obtener resumen financiero
    public java.util.Map<String, Double> obtenerResumenFinanciero() {
        var resumen = new java.util.HashMap<String, Double>();

        var sql = """
                SELECT
                    SUM(CASE WHEN tipo = 'Compra' THEN ganancia_costo ELSE 0 END) as total_gastos,
                    SUM(CASE WHEN tipo = 'Venta' THEN precio ELSE 0 END) as total_ingresos,
                    SUM(CASE WHEN tipo = 'Venta' THEN ganancia_costo ELSE 0 END) as total_ganancias,
                    COUNT(CASE WHEN tipo = 'Compra' THEN 1 END) as total_compras,
                    COUNT(CASE WHEN tipo = 'Venta' THEN 1 END) as total_ventas
                FROM Transacciones
                """;

        try (var conn = DriverManager.getConnection(DB_URL);
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()) {

            if (rs.next()) {
                resumen.put("total_gastos", rs.getDouble("total_gastos"));
                resumen.put("total_ingresos", rs.getDouble("total_ingresos"));
                resumen.put("total_ganancias", rs.getDouble("total_ganancias"));
                resumen.put("total_compras", (double) rs.getInt("total_compras"));
                resumen.put("total_ventas", (double) rs.getInt("total_ventas"));
                resumen.put("balance_neto", rs.getDouble("total_ingresos") - rs.getDouble("total_gastos"));
            }
        } catch (SQLException e) {
            mostrarError("Error al obtener resumen financiero: " + e.getMessage());
        }

        return resumen;
    }

    public List<BalanceData> obtenerDatosBalance() {
        String sql = """
                SELECT t.fecha,
                       SUM(CASE WHEN t.tipo = 'Venta' THEN t.ganancia_costo ELSE 0 END) AS ganancia,
                       SUM(CASE WHEN t.tipo = 'Compra' THEN t.ganancia_costo ELSE 0 END) AS costo
                FROM Transacciones t
                GROUP BY t.fecha
                ORDER BY t.fecha
                """;

        List<BalanceData> lista = new ArrayList<>();
        try (var conn = DriverManager.getConnection(DB_URL);
                var stmt = conn.prepareStatement(sql);
                var rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fecha = rs.getString("fecha");
                double ganancia = rs.getDouble("ganancia");
                double costo = rs.getDouble("costo");
                lista.add(new BalanceData(fecha, ganancia, costo));
            }

        } catch (SQLException e) {
            mostrarError("Error al obtener datos de balance: " + e.getMessage());
        }
        return lista;
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

}