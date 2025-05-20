package BD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite {

    private static final String DB_URL = "jdbc:sqlite:fc25_manager.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                System.out.println("Conexión a la base de datos establecida.");

                Statement stmt = conn.createStatement();

                String sql1 = "CREATE TABLE IF NOT EXISTS Jugadores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "nombre TEXT NOT NULL," +
                        "apellido TEXT NOT NULL," +
                        "posicion TEXT," +
                        "equipo TEXT," +
                        "media INTEGER," +
                        "ataque INTEGER," +
                        "habilidad INTEGER," +
                        "movimiento INTEGER," +
                        "poder INTEGER," +
                        "mentalidad INTEGER," +
                        "defensa INTEGER," +
                        "porteria INTEGER" +
                        ");";

                stmt.execute(sql1);
                System.out.println("Tabla Jugadores creada o ya existe.");

                String sql2 = "CREATE TABLE IF NOT EXISTS Estadisticas (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "jugador_id INTEGER NOT NULL"+
                        "goles INTEGER," +
                        "asistencias INTEGER," +
                        "plus80 INTEGER," + // +80
                        "plus70 INTEGER," + // +70
                        "intercepcion INTEGER," +
                        "atajadas INTEGER," +
                        "penalAtajado INTEGER," +
                        "jugadorDelPartido INTEGER," + // puede ser 0/1 booleano
                        "cincuentaPorcientoEntradas INTEGER," + // 50%+ entradas
                        "pasesClave INTEGER," +
                        "plus1xG INTEGER," + // + 1xG
                        "recuperados INTEGER," +
                        "FOREIGN KEY(jugador_id) REFERENCES Jugadores(id)"+
                        ");";

                stmt.execute(sql2);
                System.out.println("Tabla Estadisticas creada o ya existe.");
            }
        } catch (SQLException e) {
            System.out.println("Error al conectar o crear tablas: " + e.getMessage());
        }
    }
}
