import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PanelEstadisticas extends JPanel {
    // Constantes
    private static final String DB_URL = "jdbc:sqlite:football_manager.db";
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);
    
    // Componentes UI
    private DefaultTableModel modelEstadisticas;
    private JTable tableEstadisticas;
    private JComboBox<String> cbJugadores;
    private Map<String, JTextField> camposTexto = new HashMap<>();
    private JCheckBox cbJugadorPartido;
    
    public PanelEstadisticas() {
        setLayout(new BorderLayout(10, 10));
        setBackground(SECONDARY_COLOR);
        
        initTopPanel();
        initCenterPanel();
        
        cargarComboJugadores();
        cargarEstadisticas();
    }

    private void initTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(SECONDARY_COLOR);
        
        JLabel lblJugador = new JLabel("Jugador:");
        lblJugador.setFont(LABEL_FONT);
        
        cbJugadores = new JComboBox<>();
        cbJugadores.setPreferredSize(new Dimension(300, 25));
        
        JButton btnRefrescar = createStyledButton("Refrescar Jugadores");
        btnRefrescar.addActionListener(_ -> cargarComboJugadores());
        
        topPanel.add(lblJugador);
        topPanel.add(cbJugadores);
        topPanel.add(btnRefrescar);
        
        add(topPanel, BorderLayout.NORTH);
    }

    private void initCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(SECONDARY_COLOR);
        
        // Panel de formulario
        JPanel formPanel = createFormPanel();
        centerPanel.add(formPanel, BorderLayout.NORTH);
        
        // Panel de tabla
        initTable();
        centerPanel.add(new JScrollPane(tableEstadisticas), BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(SECONDARY_COLOR);
        
        // Definir campos
        String[] campos = {
            "Goles", "Asistencias", "+80", "+70", 
            "Intercepción", "Atajadas", "Penal Atajado", 
            "50%+ Entradas", "Pases Clave", "+1xG", "Recuperados"
        };
        
        // Crear campos de texto
        for (String campo : campos) {
            JLabel label = new JLabel(campo + ":");
            label.setFont(LABEL_FONT);
            
            JTextField textField = new JTextField();
            textField.setPreferredSize(new Dimension(100, 25));
            camposTexto.put(campo, textField);
            
            formPanel.add(label);
            formPanel.add(textField);
        }
        
        // Checkbox Jugador del Partido
        cbJugadorPartido = new JCheckBox("Jugador del Partido");
        cbJugadorPartido.setFont(LABEL_FONT);
        cbJugadorPartido.setBackground(SECONDARY_COLOR);
        formPanel.add(cbJugadorPartido);
        formPanel.add(new JLabel()); // Espacio vacío
        
        // Botón Agregar
        JButton btnAgregar = createStyledButton("Agregar Estadísticas");
        btnAgregar.addActionListener(_ -> agregarEstadisticas());
        formPanel.add(btnAgregar);
        formPanel.add(new JLabel()); // Espacio vacío
        
        return formPanel;
    }

    private void initTable() {
        modelEstadisticas = new DefaultTableModel();
        String[] columnas = {
            "ID", "Jugador", "Goles", "Asistencias", "+80", "+70",
            "Intercepción", "Atajadas", "Penal", "J. Partido",
            "Entradas 50", "Pases Clave", "+1xG", "Recuperados"
        };
        modelEstadisticas.setColumnIdentifiers(columnas);
        
        tableEstadisticas = new JTable(modelEstadisticas);
        tableEstadisticas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableEstadisticas.setFillsViewportHeight(true);
        tableEstadisticas.setRowHeight(25);
        tableEstadisticas.setShowGrid(true);
        tableEstadisticas.setGridColor(Color.LIGHT_GRAY);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(LABEL_FONT);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return button;
    }

    private void agregarEstadisticas() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int jugadorId = obtenerIdJugadorSeleccionado();
            if (jugadorId == -1) return;
            
            int[] nuevosValores = obtenerValoresDesdeCampos();
            boolean existe = verificarExistenciaEstadisticas(conn, jugadorId);
            
            if (existe) {
                actualizarEstadisticas(conn, jugadorId, nuevosValores);
            } else {
                insertarEstadisticas(conn, jugadorId, nuevosValores);
            }
            
            limpiarFormulario();
            cargarEstadisticas();
        } catch (Exception e) {
            mostrarError("Error al agregar estadísticas: " + e.getMessage());
        }
    }

    private int obtenerIdJugadorSeleccionado() {
        int idx = cbJugadores.getSelectedIndex();
        if (idx < 0) {
            mostrarError("Seleccione un jugador");
            return -1;
        }
        return Integer.parseInt(cbJugadores.getItemAt(idx).split(" - ")[0]);
    }

    private boolean verificarExistenciaEstadisticas(Connection conn, int jugadorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Estadisticas WHERE jugador_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jugadorId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void actualizarEstadisticas(Connection conn, int jugadorId, int[] nuevosValores) throws SQLException {
        int[] valoresActuales = obtenerValoresActuales(conn, jugadorId);
        for (int i = 0; i < nuevosValores.length; i++) {
            nuevosValores[i] += valoresActuales[i];
        }
        
        String sql = "UPDATE Estadisticas SET goles=?, asistencias=?, plus80=?, plus70=?, " +
                     "intercepcion=?, atajadas=?, penalAtajado=?, jugadorPartido=?, " +
                     "entradas50=?, pasesClave=?, plus1xG=?, recuperados=? WHERE jugador_id=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < nuevosValores.length; i++) {
                stmt.setInt(i + 1, nuevosValores[i]);
            }
            stmt.setInt(13, jugadorId);
            stmt.executeUpdate();
        }
    }

    private int[] obtenerValoresActuales(Connection conn, int jugadorId) throws SQLException {
        String sql = "SELECT goles, asistencias, plus80, plus70, intercepcion, atajadas, " +
                     "penalAtajado, jugadorPartido, entradas50, pasesClave, plus1xG, recuperados " +
                     "FROM Estadisticas WHERE jugador_id=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jugadorId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int[] valores = new int[12];
                for (int i = 0; i < valores.length; i++) {
                    valores[i] = rs.getInt(i + 1);
                }
                return valores;
            }
            return new int[12];
        }
    }

    private void insertarEstadisticas(Connection conn, int jugadorId, int[] valores) throws SQLException {
        String sql = "INSERT INTO Estadisticas(jugador_id, goles, asistencias, plus80, plus70, " +
                     "intercepcion, atajadas, penalAtajado, jugadorPartido, entradas50, " +
                     "pasesClave, plus1xG, recuperados) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, jugadorId);
            for (int i = 0; i < valores.length; i++) {
                stmt.setInt(i + 2, valores[i]);
            }
            stmt.executeUpdate();
        }
    }

    private int[] obtenerValoresDesdeCampos() {
        return new int[] {
            obtenerValorCampo("Goles"),
            obtenerValorCampo("Asistencias"),
            obtenerValorCampo("+80"),
            obtenerValorCampo("+70"),
            obtenerValorCampo("Intercepción"),
            obtenerValorCampo("Atajadas"),
            obtenerValorCampo("Penal Atajado"),
            cbJugadorPartido.isSelected() ? 1 : 0,
            obtenerValorCampo("50%+ Entradas"),
            obtenerValorCampo("Pases Clave"),
            obtenerValorCampo("+1xG"),
            obtenerValorCampo("Recuperados")
        };
    }

    private int obtenerValorCampo(String nombreCampo) {
        JTextField campo = camposTexto.get(nombreCampo);
        if (campo.getText().isEmpty()) {
            campo.setText("0");
            return 0;
        }
        return Integer.parseInt(campo.getText());
    }

    private void cargarComboJugadores() {
        cbJugadores.removeAllItems();
        String sql = "SELECT id, nombre, posicion, equipo FROM Jugadores";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String item = String.format("%d - %s (%s) [%s]", 
                    rs.getInt("id"), 
                    rs.getString("nombre"), 
                    rs.getString("posicion"), 
                    rs.getString("equipo"));
                cbJugadores.addItem(item);
            }
        } catch (SQLException e) {
            mostrarError("Error cargando lista de jugadores: " + e.getMessage());
        }
    }

    private void cargarEstadisticas() {
        modelEstadisticas.setRowCount(0);
        String sql = "SELECT e.id, j.nombre || ' ' || j.apellido AS nombre_jugador, " +
                     "e.goles, e.asistencias, e.plus80, e.plus70, e.intercepcion, " +
                     "e.atajadas, e.penalAtajado, e.jugadorPartido, e.entradas50, " +
                     "e.pasesClave, e.plus1xG, e.recuperados " +
                     "FROM Estadisticas e JOIN Jugadores j ON e.jugador_id = j.id";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                modelEstadisticas.addRow(new Object[] {
                    rs.getInt("id"),
                    rs.getString("nombre_jugador"),
                    rs.getInt("goles"),
                    rs.getInt("asistencias"),
                    rs.getInt("plus80"),
                    rs.getInt("plus70"),
                    rs.getInt("intercepcion"),
                    rs.getInt("atajadas"),
                    rs.getInt("penalAtajado"),
                    rs.getInt("jugadorPartido"),
                    rs.getInt("entradas50"),
                    rs.getInt("pasesClave"),
                    rs.getInt("plus1xG"),
                    rs.getInt("recuperados")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error cargando estadísticas: " + e.getMessage());
        }
    }

    private void limpiarFormulario() {
        camposTexto.values().forEach(tf -> tf.setText(""));
        cbJugadorPartido.setSelected(false);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}