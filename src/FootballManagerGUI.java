import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class FootballManagerGUI extends JFrame {

    private static final String DB_URL = "jdbc:sqlite:football_manager.db";

    // Components for Jugadores tab
    private JTextField tfNombre, tfApellido, tfPosicion, tfEquipo;
    private JTextField tfMedia, tfAtaque, tfHabilidad, tfMovimiento, tfPoder, tfMentalidad, tfDefensa, tfPorteria;
    private JTable tableJugadores;
    private DefaultTableModel modelJugadores;

    // Components for Estadísticas tab
    private JComboBox<String> cbJugadores;
    private JTable tableEstadisticas;
    private DefaultTableModel modelEstadisticas;
    private JTextField tfGoles, tfAsistencias, tfPlus80, tfPlus70, tfIntercepcion, tfAtajadas, tfPenalAtajado;
    private JCheckBox cbJugadorPartido;
    private JTextField tfEntradas50, tfPasesClave, tfPlus1xG, tfRecuperados;

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
        tabPane.addTab("Estadísticas", crearPanelEstadisticas());

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
        tfPosicion = new JTextField(); tfEquipo = new JTextField();
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
        btnAgregarJ.addActionListener(e -> agregarJugador());
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

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Selector de jugador
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbJugadores = new JComboBox<>();
        top.add(new JLabel("Jugador:")); top.add(cbJugadores);
        JButton btnRefrescar = new JButton("Refrescar Lista");
        btnRefrescar.addActionListener(e -> cargarComboJugadores());
        top.add(btnRefrescar);
        panel.add(top, BorderLayout.NORTH);

        // Formulario de estadísticas
        JPanel form = new JPanel(new GridLayout(8, 4, 5, 5));
        tfGoles = new JTextField(); tfAsistencias = new JTextField(); tfPlus80 = new JTextField();
        tfPlus70 = new JTextField(); tfIntercepcion = new JTextField(); tfAtajadas = new JTextField();
        tfPenalAtajado = new JTextField(); cbJugadorPartido = new JCheckBox("Jugador del Partido");
        tfEntradas50 = new JTextField(); tfPasesClave = new JTextField();
        tfPlus1xG = new JTextField(); tfRecuperados = new JTextField();

        form.add(new JLabel("Goles:")); form.add(tfGoles);
        form.add(new JLabel("Asistencias:")); form.add(tfAsistencias);
        form.add(new JLabel("+80:")); form.add(tfPlus80);
        form.add(new JLabel("+70:")); form.add(tfPlus70);
        form.add(new JLabel("Intercepción:")); form.add(tfIntercepcion);
        form.add(new JLabel("Atajadas:")); form.add(tfAtajadas);
        form.add(new JLabel("Penal Atajado:")); form.add(tfPenalAtajado);
        form.add(cbJugadorPartido); form.add(new JLabel());
        form.add(new JLabel("50%+ Entradas:")); form.add(tfEntradas50);
        form.add(new JLabel("Pases Clave:")); form.add(tfPasesClave);
        form.add(new JLabel("+1xG:")); form.add(tfPlus1xG);
        form.add(new JLabel("Recuperados:")); form.add(tfRecuperados);

        JButton btnAgregarE = new JButton("Agregar Estadísticas");
        btnAgregarE.addActionListener(e -> agregarEstadisticas());
        form.add(btnAgregarE); form.add(new JLabel());

        panel.add(form, BorderLayout.CENTER);

        // Tabla Estadísticas
        modelEstadisticas = new DefaultTableModel();
        String[] colsEst = {"ID","JugadorID","Goles","Asist","+80","+70","Inter","Ataj","PenalAt","JugPart","50Entr","PasesCl","+1xG","Recup"};
        modelEstadisticas.setColumnIdentifiers(colsEst);
        tableEstadisticas = new JTable(modelEstadisticas);
        panel.add(new JScrollPane(tableEstadisticas), BorderLayout.SOUTH);

        cargarComboJugadores();
        cargarEstadisticas();
        return panel;
    }

    private void agregarJugador() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT INTO Jugadores(nombre,apellido,posicion,equipo,media,ataque,habilidad,movimiento,poder,mentalidad,defensa,porteria) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, tfNombre.getText().trim());
            p.setString(2, tfApellido.getText().trim());
            p.setString(3, tfPosicion.getText().trim());
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
            cargarComboJugadores();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al agregar jugador: " + e.getMessage());
        }
    }

    private void agregarEstadisticas() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int idx = cbJugadores.getSelectedIndex();
            if(idx<0) return;
            int jugadorId = Integer.parseInt(cbJugadores.getItemAt(idx).split(" - ")[0]);
            String sql = "INSERT INTO Estadisticas(jugador_id,goles,asistencias,plus80,plus70,intercepcion,atajadas,penalAtajado,jugadorPartido,entradas50,pasesClave,plus1xG,recuperados) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement p = conn.prepareStatement(sql);
            p.setInt(1, jugadorId);
            p.setInt(2, Integer.parseInt(tfGoles.getText().trim()));
            p.setInt(3, Integer.parseInt(tfAsistencias.getText().trim()));
            p.setInt(4, Integer.parseInt(tfPlus80.getText().trim()));
            p.setInt(5, Integer.parseInt(tfPlus70.getText().trim()));
            p.setInt(6, Integer.parseInt(tfIntercepcion.getText().trim()));
            p.setInt(7, Integer.parseInt(tfAtajadas.getText().trim()));
            p.setInt(8, Integer.parseInt(tfPenalAtajado.getText().trim()));
            p.setInt(9, cbJugadorPartido.isSelected()?1:0);
            p.setInt(10, Integer.parseInt(tfEntradas50.getText().trim()));
            p.setInt(11, Integer.parseInt(tfPasesClave.getText().trim()));
            p.setInt(12, Integer.parseInt(tfPlus1xG.getText().trim()));
            p.setInt(13, Integer.parseInt(tfRecuperados.getText().trim()));
            p.executeUpdate();
            limpiarEstForm();
            cargarEstadisticas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al agregar estadísticas: " + e.getMessage());
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

    private void cargarComboJugadores() {
        cbJugadores.removeAllItems();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, nombre, posicion, equipo FROM Jugadores")) {
            while (rs.next()) {
                String item = rs.getInt("id") + " - " + rs.getString("nombre") + " (" + rs.getString("posicion") + ") [" + rs.getString("equipo") + "]";
                cbJugadores.addItem(item);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando lista de jugadores: " + e.getMessage());
        }
    }

    private void cargarEstadisticas() {
        modelEstadisticas.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Estadisticas")) {
            while (rs.next()) {
                modelEstadisticas.addRow(new Object[]{
                        rs.getInt("id"), rs.getInt("jugador_id"), rs.getInt("goles"), rs.getInt("asistencias"),
                        rs.getInt("plus80"), rs.getInt("plus70"), rs.getInt("intercepcion"), rs.getInt("atajadas"),
                        rs.getInt("penalAtajado"), rs.getInt("jugadorPartido"), rs.getInt("entradas50"),
                        rs.getInt("pasesClave"), rs.getInt("plus1xG"), rs.getInt("recuperados")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando estadísticas: " + e.getMessage());
        }
    }

    private void limpiarJugForm() {
        tfNombre.setText(""); tfApellido.setText(""); tfPosicion.setText(""); tfEquipo.setText("");
        tfMedia.setText(""); tfAtaque.setText(""); tfHabilidad.setText(""); tfMovimiento.setText("");
        tfPoder.setText(""); tfMentalidad.setText(""); tfDefensa.setText(""); tfPorteria.setText("");
    }

    private void limpiarEstForm() {
        tfGoles.setText(""); tfAsistencias.setText(""); tfPlus80.setText(""); tfPlus70.setText("");
        tfIntercepcion.setText(""); tfAtajadas.setText(""); tfPenalAtajado.setText(""); cbJugadorPartido.setSelected(false);
        tfEntradas50.setText(""); tfPasesClave.setText(""); tfPlus1xG.setText(""); tfRecuperados.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FootballManagerGUI().setVisible(true));
    }
}
