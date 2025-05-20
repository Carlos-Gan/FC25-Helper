import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class FootballManagerGUI extends JFrame {

    private static final String DB_URL = "jdbc:sqlite:football_manager.db";

    crearPanelEstadisticas panelEstadisticas = new crearPanelEstadisticas();

    // Components for Jugadores tab
    private JTextField tfNombre, tfApellido, tfEquipo;
    private JComboBox tfPosicion;
    private JTextField tfMedia, tfAtaque, tfHabilidad, tfMovimiento, tfPoder, tfMentalidad, tfDefensa, tfPorteria;
    private JTable tableJugadores;
    private DefaultTableModel modelJugadores;

    String pos[] = {"POR","DFI", "DFC", "DFD", "MCD", "MI", "MD", "MC","MCO","MP","EI","DC","ED"};

    // Components for Estadísticas tab

    public FootballManagerGUI() {
        super("Football Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Initialize DB and tables
        crearTablasSiNoExisten();

        // Create tab pane
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("Jugadores", crearPanelJugadores());
        tabPane.addTab("Estadísticas", panelEstadisticas.crearPanelEstadisticas());

        add(tabPane);
    }

    private void crearTablasSiNoExisten() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Tabla Jugadores
            String sqlJug = "CREATE TABLE IF NOT EXISTS Jugadores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre TEXT NOT NULL, " +
                    "apellido TEXT NOT NULL, " +
                    "posicion TEXT, " +
                    "equipo TEXT, " +
                    "media INTEGER, ataque INTEGER, habilidad INTEGER, movimiento INTEGER, " +
                    "poder INTEGER, mentalidad INTEGER, defensa INTEGER, porteria INTEGER" +
                    ");";
            stmt.execute(sqlJug);

            // Tabla Estadísticas
            String sqlEst = "CREATE TABLE IF NOT EXISTS Estadisticas (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "jugador_id INTEGER, " +
                    "goles INTEGER, asistencias INTEGER, plus80 INTEGER, plus70 INTEGER, " +
                    "intercepcion INTEGER, atajadas INTEGER, penalAtajado INTEGER, jugadorPartido INTEGER, " +
                    "entradas50 INTEGER, pasesClave INTEGER, plus1xG INTEGER, recuperados INTEGER, " +
                    "FOREIGN KEY(jugador_id) REFERENCES Jugadores(id)" +
                    ");";
            stmt.execute(sqlEst);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error creando tablas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel crearPanelJugadores() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Formulario
        JPanel form = new JPanel(new GridLayout(13, 2, 5, 5));
        tfNombre = new JTextField(); tfApellido = new JTextField();
        tfPosicion = new JComboBox(pos); tfEquipo = new JTextField();
        tfMedia = new JTextField(); tfAtaque = new JTextField();
        tfHabilidad = new JTextField(); tfMovimiento = new JTextField();
        tfPoder = new JTextField(); tfMentalidad = new JTextField();
        tfDefensa = new JTextField(); tfPorteria = new JTextField();

        form.add(new JLabel("Nombre:")); form.add(tfNombre);
        form.add(new JLabel("Apellido:")); form.add(tfApellido);
        form.add(new JLabel("Posición:")); form.add(tfPosicion);
        form.add(new JLabel("Equipo:")); form.add(tfEquipo);
        form.add(new JLabel("Media:")); form.add(tfMedia);
        form.add(new JLabel("Ataque:")); form.add(tfAtaque);
        form.add(new JLabel("Habilidad:")); form.add(tfHabilidad);
        form.add(new JLabel("Movimiento:")); form.add(tfMovimiento);
        form.add(new JLabel("Poder:")); form.add(tfPoder);
        form.add(new JLabel("Mentalidad:")); form.add(tfMentalidad);
        form.add(new JLabel("Defensa:")); form.add(tfDefensa);
        form.add(new JLabel("Portería:")); form.add(tfPorteria);

        JButton btnAgregarJ = new JButton("Agregar Jugador");
        btnAgregarJ.addActionListener(_ -> agregarJugador());
        form.add(btnAgregarJ);
        form.add(new JLabel());

        panel.add(form, BorderLayout.NORTH);

        // Tabla Jugadores
        modelJugadores = new DefaultTableModel();
        String[] colsJug = {"ID","Nombre","Apellido","Posición","Equipo","Media","Ataque","Habilidad","Movimiento","Poder","Mentalidad","Defensa","Portería"};
        modelJugadores.setColumnIdentifiers(colsJug);
        tableJugadores = new JTable(modelJugadores);
        panel.add(new JScrollPane(tableJugadores), BorderLayout.CENTER);

        cargarJugadores();
        return panel;
    }

    private void agregarJugador() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO Jugadores(nombre,apellido,posicion,equipo,media,ataque,habilidad,movimiento,poder,mentalidad,defensa,porteria) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, tfNombre.getText().trim());
            p.setString(2, tfApellido.getText().trim());
            p.setString(3, tfPosicion.getSelectedItem().toString().trim());
            p.setString(4, tfEquipo.getText().trim());
            p.setInt(5, Integer.parseInt(tfMedia.getText().trim()));
            p.setInt(6, Integer.parseInt(tfAtaque.getText().trim()));
            p.setInt(7, Integer.parseInt(tfHabilidad.getText().trim()));
            p.setInt(8, Integer.parseInt(tfMovimiento.getText().trim()));
            p.setInt(9, Integer.parseInt(tfPoder.getText().trim()));
            p.setInt(10, Integer.parseInt(tfMentalidad.getText().trim()));
            p.setInt(11, Integer.parseInt(tfDefensa.getText().trim()));
            p.setInt(12, Integer.parseInt(tfPorteria.getText().trim()));
            p.executeUpdate();
            limpiarJugForm();
            cargarJugadores();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al agregar jugador: " + e.getMessage());
        }
    }


    private void cargarJugadores() {
        modelJugadores.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Jugadores")) {
            while (rs.next()) {
                modelJugadores.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("nombre"), rs.getString("apellido"),
                        rs.getString("posicion"), rs.getString("equipo"), rs.getInt("media"),
                        rs.getInt("ataque"), rs.getInt("habilidad"), rs.getInt("movimiento"),
                        rs.getInt("poder"), rs.getInt("mentalidad"), rs.getInt("defensa"), rs.getInt("porteria")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando jugadores: " + e.getMessage());
        }
    }


    private void limpiarJugForm() {
        tfNombre.setText(""); tfApellido.setText(""); tfPosicion.setSelectedIndex(0); tfEquipo.setText("");
        tfMedia.setText(""); tfAtaque.setText(""); tfHabilidad.setText(""); tfMovimiento.setText("");
        tfPoder.setText(""); tfMentalidad.setText(""); tfDefensa.setText(""); tfPorteria.setText("");
    }

    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FootballManagerGUI().setVisible(true));
    }
}
