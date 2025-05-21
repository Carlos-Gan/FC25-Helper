import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class FootballManagerGUI extends JFrame {
    // Constantes
    private static final String DB_URL = "jdbc:sqlite:football_manager.db";
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 12);
    private static final String[] POSICIONES = {
        "POR", "DFI", "DFC", "DFD", "MCD", "MI", "MD", 
        "MC", "MCO", "MP", "EI", "DC", "ED"
    };
    
    // Componentes UI
    private JTabbedPane tabPane;
    private Map<String, JTextField> camposJugador = new HashMap<>();
    private JComboBox<String> cbPosicion;
    private DefaultTableModel modelJugadores;
    private JTable tableJugadores;

    public FootballManagerGUI() {
        super("Football Manager");
        configurarVentana();
        inicializarBaseDeDatos();
        inicializarUI();
    }

    private void configurarVentana() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 500));
    }

    private void inicializarBaseDeDatos() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            crearTablaJugadores(stmt);
            crearTablaEstadisticas(stmt);
            
        } catch (SQLException e) {
            mostrarError("Error inicializando base de datos", e.getMessage());
        }
    }

    private void crearTablaJugadores(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Jugadores (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "apellido TEXT NOT NULL, " +
                "posicion TEXT, " +
                "equipo TEXT, " +
                "media INTEGER, ataque INTEGER, habilidad INTEGER, " +
                "movimiento INTEGER, poder INTEGER, mentalidad INTEGER, " +
                "defensa INTEGER, porteria INTEGER)";
        stmt.execute(sql);
    }

    private void crearTablaEstadisticas(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS Estadisticas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "jugador_id INTEGER, " +
                "goles INTEGER, asistencias INTEGER, plus80 INTEGER, plus70 INTEGER, " +
                "intercepcion INTEGER, atajadas INTEGER, penalAtajado INTEGER, " +
                "jugadorPartido INTEGER, entradas50 INTEGER, pasesClave INTEGER, " +
                "plus1xG INTEGER, recuperados INTEGER, " +
                "FOREIGN KEY(jugador_id) REFERENCES Jugadores(id))";
        stmt.execute(sql);
    }

    private void inicializarUI() {
        tabPane = new JTabbedPane();
        tabPane.addTab("Jugadores", crearPanelJugadores());
        tabPane.addTab("Estadísticas", new PanelEstadisticas());
        tabPane.addTab("Ventas o Compras", new PanelTransacciones());
        
        add(tabPane);
    }

    private JPanel crearPanelJugadores() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(SECONDARY_COLOR);
        
        panel.add(crearFormularioJugador(), BorderLayout.NORTH);
        panel.add(crearTablaJugadores(), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel crearFormularioJugador() {
        JPanel formPanel = new JPanel(new GridLayout(0, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        formPanel.setBackground(SECONDARY_COLOR);
        
        // Campos básicos
        agregarCampo(formPanel, "Nombre", "nombre");
        agregarCampo(formPanel, "Apellido", "apellido");
        
        // ComboBox de posición
        JLabel lblPosicion = new JLabel("Posición:");
        lblPosicion.setFont(LABEL_FONT);
        cbPosicion = new JComboBox<>(POSICIONES);
        formPanel.add(lblPosicion);
        formPanel.add(cbPosicion);
        
        agregarCampo(formPanel, "Equipo", "equipo");
        
        // Atributos del jugador
        agregarCampo(formPanel, "Media", "media");
        agregarCampo(formPanel, "Ataque", "ataque");
        agregarCampo(formPanel, "Habilidad", "habilidad");
        agregarCampo(formPanel, "Movimiento", "movimiento");
        agregarCampo(formPanel, "Poder", "poder");
        agregarCampo(formPanel, "Mentalidad", "mentalidad");
        agregarCampo(formPanel, "Defensa", "defensa");
        agregarCampo(formPanel, "Portería", "porteria");
        
        // Botón de acción
        JButton btnAgregar = createStyledButton("Agregar Jugador");
        btnAgregar.addActionListener(_ -> agregarJugador());
        formPanel.add(btnAgregar);
        formPanel.add(new JLabel()); // Espacio vacío
        
        return formPanel;
    }

    private void agregarCampo(JPanel panel, String etiqueta, String nombreCampo) {
        JLabel label = new JLabel(etiqueta + ":");
        label.setFont(LABEL_FONT);
        
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(100, 25));
        camposJugador.put(nombreCampo, textField);
        
        panel.add(label);
        panel.add(textField);
    }

    private JScrollPane crearTablaJugadores() {
        modelJugadores = new DefaultTableModel();
        String[] columnas = {
            "ID", "Nombre", "Apellido", "Posición", "Equipo", 
            "Media", "Ataque", "Habilidad", "Movimiento", 
            "Poder", "Mentalidad", "Defensa", "Portería"
        };
        modelJugadores.setColumnIdentifiers(columnas);
        
        tableJugadores = new JTable(modelJugadores);
        configurarTabla(tableJugadores);
        
        cargarJugadores();
        return new JScrollPane(tableJugadores);
    }

    private void configurarTabla(JTable tabla) {
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(25);
        tabla.setShowGrid(true);
        tabla.setGridColor(Color.LIGHT_GRAY);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

    private void agregarJugador() {
        try {
            validarCamposObligatorios();
            
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                insertarJugador(conn);
                limpiarFormulario();
                cargarJugadores();
            }
        } catch (NumberFormatException e) {
            mostrarError("Error de formato", "Todos los atributos deben ser números enteros");
        } catch (SQLException e) {
            mostrarError("Error de base de datos", e.getMessage());
        } catch (IllegalArgumentException e) {
            mostrarError("Validación", e.getMessage());
        }
    }

    private void validarCamposObligatorios() {
        if (camposJugador.get("nombre").getText().trim().isEmpty() || 
            camposJugador.get("apellido").getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios");
        }
    }

    private void insertarJugador(Connection conn) throws SQLException {
        String sql = "INSERT INTO Jugadores(nombre, apellido, posicion, equipo, " +
                     "media, ataque, habilidad, movimiento, poder, " +
                     "mentalidad, defensa, porteria) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, camposJugador.get("nombre").getText().trim());
            stmt.setString(2, camposJugador.get("apellido").getText().trim());
            stmt.setString(3, cbPosicion.getSelectedItem().toString());
            stmt.setString(4, camposJugador.get("equipo").getText().trim());
            
            // Atributos numéricos
            stmt.setInt(5, Integer.parseInt(camposJugador.get("media").getText().trim()));
            stmt.setInt(6, Integer.parseInt(camposJugador.get("ataque").getText().trim()));
            stmt.setInt(7, Integer.parseInt(camposJugador.get("habilidad").getText().trim()));
            stmt.setInt(8, Integer.parseInt(camposJugador.get("movimiento").getText().trim()));
            stmt.setInt(9, Integer.parseInt(camposJugador.get("poder").getText().trim()));
            stmt.setInt(10, Integer.parseInt(camposJugador.get("mentalidad").getText().trim()));
            stmt.setInt(11, Integer.parseInt(camposJugador.get("defensa").getText().trim()));
            stmt.setInt(12, Integer.parseInt(camposJugador.get("porteria").getText().trim()));
            
            stmt.executeUpdate();
        }
    }

    private void cargarJugadores() {
        modelJugadores.setRowCount(0);
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Jugadores")) {
            
            while (rs.next()) {
                modelJugadores.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("posicion"),
                    rs.getString("equipo"),
                    rs.getInt("media"),
                    rs.getInt("ataque"),
                    rs.getInt("habilidad"),
                    rs.getInt("movimiento"),
                    rs.getInt("poder"),
                    rs.getInt("mentalidad"),
                    rs.getInt("defensa"),
                    rs.getInt("porteria")
                });
            }
        } catch (SQLException e) {
            mostrarError("Error cargando jugadores", e.getMessage());
        }
    }

    private void limpiarFormulario() {
        camposJugador.values().forEach(tf -> tf.setText(""));
        cbPosicion.setSelectedIndex(0);
    }

    private void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FootballManagerGUI gui = new FootballManagerGUI();
            gui.setVisible(true);
        });
    }
}