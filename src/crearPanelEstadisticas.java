
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class crearPanelEstadisticas extends JPanel {

    private DefaultTableModel modelEstadisticas;
    private JTable tableEstadisticas;
    private JComboBox<String> cbJugadores;
    private JTextField tfGoles, tfAsistencias, tfPlus80, tfPlus70, tfIntercepcion, tfAtajadas, tfPenalAtajado;
    private JCheckBox cbJugadorPartido;
    private JTextField tfEntradas50, tfPasesClave, tfPlus1xG, tfRecuperados;

    private static final String DB_URL = "jdbc:sqlite:football_manager.db";

    public JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Selector de jugador
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbJugadores = new JComboBox<>();
        top.add(new JLabel("Jugador:"));
        top.add(cbJugadores);
        JButton btnRefrescar = new JButton("Refrescar Lista");
        btnRefrescar.addActionListener(_ -> cargarComboJugadores());
        top.add(btnRefrescar);
        panel.add(top, BorderLayout.NORTH);

        // Formulario de estadísticas
        JPanel form = new JPanel(new GridLayout(8, 4, 5, 5));
        tfGoles = new JTextField();
        tfAsistencias = new JTextField();
        tfPlus80 = new JTextField();
        tfPlus70 = new JTextField();
        tfIntercepcion = new JTextField();
        tfAtajadas = new JTextField();
        tfPenalAtajado = new JTextField();
        cbJugadorPartido = new JCheckBox("Jugador del Partido");
        tfEntradas50 = new JTextField();
        tfPasesClave = new JTextField();
        tfPlus1xG = new JTextField();
        tfRecuperados = new JTextField();

        form.add(new JLabel("Goles:"));
        form.add(tfGoles);
        form.add(new JLabel("Asistencias:"));
        form.add(tfAsistencias);
        form.add(new JLabel("+80:"));
        form.add(tfPlus80);
        form.add(new JLabel("+70:"));
        form.add(tfPlus70);
        form.add(new JLabel("Intercepción:"));
        form.add(tfIntercepcion);
        form.add(new JLabel("Atajadas:"));
        form.add(tfAtajadas);
        form.add(new JLabel("Penal Atajado:"));
        form.add(tfPenalAtajado);
        form.add(cbJugadorPartido);
        form.add(new JLabel());
        form.add(new JLabel("50%+ Entradas:"));
        form.add(tfEntradas50);
        form.add(new JLabel("Pases Clave:"));
        form.add(tfPasesClave);
        form.add(new JLabel("+1xG:"));
        form.add(tfPlus1xG);
        form.add(new JLabel("Recuperados:"));
        form.add(tfRecuperados);

        JButton btnAgregarE = new JButton("Agregar Estadísticas");
        btnAgregarE.addActionListener(_ -> agregarEstadisticas());
        form.add(btnAgregarE);
        form.add(new JLabel());

        panel.add(form, BorderLayout.CENTER);

        // Tabla Estadísticas
        modelEstadisticas = new DefaultTableModel();
        String[] colsEst = {"ID", "Jugador", "Goles", "Asist", "+80", "+70", "Inter", "Ataj", "PenalAt", "JugPart", "50Entr", "PasesCl", "+1xG", "Recup"};
        modelEstadisticas.setColumnIdentifiers(colsEst);
        tableEstadisticas = new JTable(modelEstadisticas);
        tableEstadisticas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        panel.add(new JScrollPane(tableEstadisticas), BorderLayout.SOUTH);

        cargarComboJugadores();
        cargarEstadisticas();
        return panel;
    }

    private void agregarEstadisticas() {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int idx = cbJugadores.getSelectedIndex();
            if (idx < 0) {
                return;
            }
            int jugadorId = Integer.parseInt(cbJugadores.getItemAt(idx).split(" - ")[0]);

            // Primero verificamos si ya existen estadísticas para este jugador
            String verificarSql = "SELECT COUNT(*) FROM Estadisticas WHERE jugador_id = ?";
            PreparedStatement verificarStmt = conn.prepareStatement(verificarSql);
            verificarStmt.setInt(1, jugadorId);
            ResultSet rs = verificarStmt.executeQuery();
            rs.next();
            int existe = rs.getInt(1);

            String sql;
            if (existe > 0) {
                // Si existe, actualizamos las estadísticas
                sql = "UPDATE Estadisticas SET "
                        + "goles = ?, "
                        + "asistencias = ?, "
                        + "plus80 = ?, "
                        + "plus70 = ?, "
                        + "intercepcion = ?, "
                        + "atajadas = ?, "
                        + "penalAtajado = ?, "
                        + "jugadorPartido = ?, "
                        + "entradas50 = ?, "
                        + "pasesClave = ?, "
                        + "plus1xG = ?, "
                        + "recuperados = ? "
                        + "WHERE jugador_id = ?";
            } else {
                // Si no existe, insertamos nuevas estadísticas
                sql = "INSERT INTO Estadisticas("
                        + "jugador_id, goles, asistencias, plus80, plus70, "
                        + "intercepcion, atajadas, penalAtajado, jugadorPartido, entradas50, "
                        + "pasesClave, plus1xG, recuperados) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            }

            PreparedStatement p = conn.prepareStatement(sql);

            // Configuramos los parámetros según sea UPDATE o INSERT
            if (existe > 0) {
                // Para UPDATE, los parámetros van en orden diferente y el jugador_id al final
                setParametrosEstadisticas(p, 1);
                p.setInt(13, jugadorId);
            } else {
                // Para INSERT, el jugador_id va primero
                p.setInt(1, jugadorId);
                setParametrosEstadisticas(p, 2);
            }

            p.executeUpdate();
            limpiarEstForm();
            cargarEstadisticas();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al agregar estadísticas: " + e.getMessage());
        }
    }

// Método auxiliar para configurar los parámetros de las estadísticas
    private void setParametrosEstadisticas(PreparedStatement p, int startIndex) throws SQLException {
        if (tfGoles.getText().trim().isEmpty()) {
            p.setInt(startIndex, 0);
        } else {
            p.setInt(startIndex, Integer.parseInt(tfGoles.getText().trim()));
        }
        if (tfAsistencias.getText().trim().isEmpty()) {
            p.setInt(startIndex + 1, 0);
        } else {
            p.setInt(startIndex + 1, Integer.parseInt(tfAsistencias.getText().trim()));
        }
        if (tfPlus80.getText().trim().isEmpty()) {
            p.setInt(startIndex + 2, 0);
        } else {
            p.setInt(startIndex + 2, Integer.parseInt(tfPlus80.getText().trim()));
        }
        if (tfPlus70.getText().trim().isEmpty()) {
            p.setInt(startIndex + 3, 0);
        } else {
            p.setInt(startIndex + 3, Integer.parseInt(tfPlus70.getText().trim()));
        }
        if (tfIntercepcion.getText().trim().isEmpty()) {
            p.setInt(startIndex + 4, 0);
        } else {
            p.setInt(startIndex + 4, Integer.parseInt(tfIntercepcion.getText().trim()));
        }
        if (tfAtajadas.getText().trim().isEmpty()) {
            p.setInt(startIndex + 5, 0);
        } else {
            p.setInt(startIndex + 5, Integer.parseInt(tfAtajadas.getText().trim()));
        }
        if (tfPenalAtajado.getText().trim().isEmpty()) {
            p.setInt(startIndex + 6, 0);
        } else {
            p.setInt(startIndex + 6, Integer.parseInt(tfPenalAtajado.getText().trim()));
        }
        p.setInt(startIndex + 7, cbJugadorPartido.isSelected() ? 1 : 0);
        if (tfEntradas50.getText().trim().isEmpty()) {
            p.setInt(startIndex + 8, 0);
        } else {
            p.setInt(startIndex + 8, Integer.parseInt(tfEntradas50.getText().trim()));
        }
        if (tfPasesClave.getText().trim().isEmpty()) {
            p.setInt(startIndex + 9, 0);
        } else {
            p.setInt(startIndex + 9, Integer.parseInt(tfPasesClave.getText().trim()));
        }
        if (tfPlus1xG.getText().trim().isEmpty()) {
            p.setInt(startIndex + 10, 0);
        } else {
            p.setInt(startIndex + 10, Integer.parseInt(tfPlus1xG.getText().trim()));
        }
        if (tfRecuperados.getText().trim().isEmpty()) {
            p.setInt(startIndex + 11, 0);
        } else {
            p.setInt(startIndex + 11, Integer.parseInt(tfRecuperados.getText().trim()));
        }
    }

    private void cargarComboJugadores() {
        cbJugadores.removeAllItems();
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT id, nombre, posicion, equipo FROM Jugadores")) {
            while (rs.next()) {
                String item = rs.getInt("id") + " - " + rs.getString("nombre") + " (" + rs.getString("posicion") + ") [" + rs.getString("equipo") + "]";
                cbJugadores.addItem(item);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error cargando lista de jugadores: " + e.getMessage());
        }
    }

    private void cargarEstadisticas() {
        modelEstadisticas.setRowCount(0); // Limpiar la tabla antes de cargar nuevos datos
        try (Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(
                "SELECT e.id, j.nombre || ' ' || j.apellido AS nombre_jugador, "
                + "e.goles, e.asistencias, e.plus80, e.plus70, e.intercepcion, "
                + "e.atajadas, e.penalAtajado, e.jugadorPartido, e.entradas50, "
                + "e.pasesClave, e.plus1xG, e.recuperados "
                + "FROM Estadisticas e "
                + "JOIN Jugadores j ON e.jugador_id = j.id")) {  // Relación por jugador_id

            while (rs.next()) {
                modelEstadisticas.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nombre_jugador"), // Nombre completo (nombre + apellido)
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
            JOptionPane.showMessageDialog(this, "Error cargando estadísticas: " + e.getMessage());
        }
    }

    private void limpiarEstForm() {
        tfGoles.setText("");
        tfAsistencias.setText("");
        tfPlus80.setText("");
        tfPlus70.setText("");
        tfIntercepcion.setText("");
        tfAtajadas.setText("");
        tfPenalAtajado.setText("");
        cbJugadorPartido.setSelected(false);
        tfEntradas50.setText("");
        tfPasesClave.setText("");
        tfPlus1xG.setText("");
        tfRecuperados.setText("");
    }
}
