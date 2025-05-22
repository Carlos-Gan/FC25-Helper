import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PanelCalculadora extends JPanel {

    private final Map<String, JTextField> camposAtributos = new HashMap<>();
    private final JTextArea resultadoArea;
    private JComboBox<String> vistaComboBox;
    private JPanel panelAtributosDinamico;
    private JTextField overallField;

    public PanelCalculadora() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de configuración
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        configPanel.setBorder(BorderFactory.createTitledBorder("Configuración"));
        
        configPanel.add(new JLabel("Vista de atributos:"));
        vistaComboBox = new JComboBox<>(new String[]{
            "Vista FIFA Clásico", 
            "Vista Physical/Mental/Technical", 
            "Vista PAC/SHO/PAS/DRI/DEF/PHY"
        });
        vistaComboBox.addActionListener(_ -> cambiarVistaAtributos());
        configPanel.add(vistaComboBox);
        
        configPanel.add(new JLabel("Overall:"));
        overallField = new JTextField(3);
        overallField.setEditable(false);
        configPanel.add(overallField);

        // Panel de atributos dinámico
        panelAtributosDinamico = new JPanel();
        panelAtributosDinamico.setLayout(new BorderLayout());
        
        // Panel de resultados
        JPanel panelResultados = new JPanel(new BorderLayout());
        panelResultados.setBorder(BorderFactory.createTitledBorder("Resultados"));
        panelResultados.setPreferredSize(new Dimension(300, 0));

        resultadoArea = new JTextArea();
        resultadoArea.setEditable(false);
        resultadoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultadoArea);
        panelResultados.add(scrollPane, BorderLayout.CENTER);

        // Botón de limpiar
        JButton btnLimpiar = new JButton("Limpiar Campos");
        btnLimpiar.addActionListener(_ -> limpiarCampos());
        panelResultados.add(btnLimpiar, BorderLayout.SOUTH);

        // Agregar componentes al panel principal
        add(configPanel, BorderLayout.NORTH);
        add(panelAtributosDinamico, BorderLayout.CENTER);
        add(panelResultados, BorderLayout.EAST);

        // Inicializar con la primera vista
        cambiarVistaAtributos();
    }

    private void cambiarVistaAtributos() {
        panelAtributosDinamico.removeAll();

        int vistaSeleccionada = vistaComboBox.getSelectedIndex();
        JPanel panelAtributos = null;
        
        switch (vistaSeleccionada) {
            case 0:
                panelAtributos = crearVistaFifaClasico();
                break;
            case 1:
                panelAtributos = crearVistaPhysicalMentalTechnical();
                break;
            case 2:
                panelAtributos = crearVistaPacShoPasDriDefPhy();
                break;
        }
        
        panelAtributosDinamico.add(new JScrollPane(panelAtributos), BorderLayout.CENTER);
        panelAtributosDinamico.revalidate();
        panelAtributosDinamico.repaint();
    }

    private JPanel crearVistaFifaClasico() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        
        // Attacking
        panelContenedor.add(crearPanelCategoria("Attacking", new String[]{
            "Crossing", "Finishing", "Heading accuracy", 
            "Short passing", "Volleys"
        }));
        
        // Skill
        panelContenedor.add(crearPanelCategoria("Skill", new String[]{
            "Dribbling", "Curve", "FK Accuracy", 
            "Long passing", "Ball control"
        }));
        
        // Movement
        panelContenedor.add(crearPanelCategoria("Movement", new String[]{
            "Acceleration", "Sprint speed", "Agility", 
            "Reactions", "Balance"
        }));
        
        // Power
        panelContenedor.add(crearPanelCategoria("Power", new String[]{
            "Shot power", "Jumping", "Stamina", 
            "Strength", "Long shots"
        }));
        
        // Mentality
        panelContenedor.add(crearPanelCategoria("Mentality", new String[]{
            "Aggression", "Interceptions", "Att. Position", 
            "Vision", "Penalties", "Composure"
        }));
        
        // Defending
        panelContenedor.add(crearPanelCategoria("Defending", new String[]{
            "Defensive awareness", "Standing tackle", "Sliding tackle"
        }));
        
        // Goalkeeping
        panelContenedor.add(crearPanelCategoria("Goalkeeping", new String[]{
            "GK Diving", "GK Handling", "GK Kicking", 
            "GK Positioning", "GK Reflexes"
        }));
        
        panelPrincipal.add(panelContenedor, BorderLayout.NORTH);
        return panelPrincipal;
    }

    private JPanel crearVistaPhysicalMentalTechnical() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        
        // Physical
        panelContenedor.add(crearPanelCategoria("Physical", new String[]{
            "Acceleration", "Agility", "Balance", 
            "Jumping", "Reactions", "Sprint speed",
            "Stamina", "Strength"
        }));
        
        // Mental
        panelContenedor.add(crearPanelCategoria("Mental", new String[]{
            "Aggression", "Att. Position", "Composure", 
            "Interceptions", "Vision"
        }));
        
        // Technical
        panelContenedor.add(crearPanelCategoria("Technical", new String[]{
            "Ball control", "Crossing", "Curve", 
            "Defensive awareness", "Dribbling", "FK Accuracy",
            "Finishing", "Heading accuracy", "Long passing",
            "Long shots", "Penalties", "Short passing",
            "Shot power", "Sliding tackle", "Standing tackle",
            "Volleys"
        }));
        
        // Goalkeeping
        panelContenedor.add(crearPanelCategoria("Goalkeeping", new String[]{
            "GK Diving", "GK Handling", "GK Kicking", 
            "GK Positioning", "GK Reflexes"
        }));
        
        panelPrincipal.add(panelContenedor, BorderLayout.NORTH);
        return panelPrincipal;
    }

    private JPanel crearVistaPacShoPasDriDefPhy() {
        JPanel panelPrincipal = new JPanel(new BorderLayout());
        JPanel panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        
        // PAC
        panelContenedor.add(crearPanelCategoria("PAC", new String[]{
            "Sprint speed", "Acceleration"
        }));
        
        // SHO
        panelContenedor.add(crearPanelCategoria("SHO", new String[]{
            "Finishing", "Att. Position", "Shot power", 
            "Long shots", "Penalties", "Volleys"
        }));
        
        // PAS
        panelContenedor.add(crearPanelCategoria("PAS", new String[]{
            "Vision", "Crossing", "FK Accuracy", 
            "Long passing", "Short passing", "Curve"
        }));
        
        // DRI
        panelContenedor.add(crearPanelCategoria("DRI", new String[]{
            "Agility", "Balance", "Reactions", 
            "Composure", "Ball control", "Dribbling"
        }));
        
        // DEF
        panelContenedor.add(crearPanelCategoria("DEF", new String[]{
            "Interceptions", "Heading accuracy", "Defensive awareness", 
            "Standing tackle", "Sliding tackle"
        }));
        
        // PHY
        panelContenedor.add(crearPanelCategoria("PHY", new String[]{
            "Jumping", "Stamina", "Strength", "Aggression"
        }));
        
        // Goalkeeping
        panelContenedor.add(crearPanelCategoria("Goalkeeping", new String[]{
            "GK Diving", "GK Handling", "GK Kicking", 
            "GK Positioning", "GK Reflexes"
        }));
        
        panelPrincipal.add(panelContenedor, BorderLayout.NORTH);
        return panelPrincipal;
    }

    private JPanel crearPanelCategoria(String titulo, String[] atributos) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 2, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        
        for (String atributo : atributos) {
            JLabel label = new JLabel(atributo + ":");
            JTextField textField = camposAtributos.get(atributo);
            
            if (textField == null) {
                textField = new JTextField();
                textField.setHorizontalAlignment(JTextField.RIGHT);
                
                textField.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) { actualizarResultados(); }
                    public void removeUpdate(DocumentEvent e) { actualizarResultados(); }
                    public void changedUpdate(DocumentEvent e) { actualizarResultados(); }
                });
                
                camposAtributos.put(atributo, textField);
            }
            
            panel.add(label);
            panel.add(textField);
        }
        
        return panel;
    }

    private void actualizarResultados() {
        try {
            // Obtener valores de los campos
            Map<String, Integer> atributos = new HashMap<>();
            for (Map.Entry<String, JTextField> entry : camposAtributos.entrySet()) {
                String texto = entry.getValue().getText().trim();
                int valor = texto.isEmpty() ? 0 : Integer.parseInt(texto);
                valor = Math.max(0, Math.min(100, valor));
                atributos.put(entry.getKey(), valor);
            }

            // Calcular puntuaciones para cada posición según el algoritmo proporcionado
            Map<String, Integer> puntuaciones = new HashMap<>();
            
            // Portero (GK)
            int gk = (int) Math.round(
                atributos.getOrDefault("Reactions", 0) * 0.11 +
                atributos.getOrDefault("GK Diving", 0) * 0.21 +
                atributos.getOrDefault("GK Handling", 0) * 0.21 +
                atributos.getOrDefault("GK Kicking", 0) * 0.05 +
                atributos.getOrDefault("GK Positioning", 0) * 0.21 +
                atributos.getOrDefault("GK Reflexes", 0) * 0.21
            );
            puntuaciones.put("Portero (GK)", gk);
            
            // Defensa central (SW)
            int sw = (int) Math.round(
                atributos.getOrDefault("Jumping", 0) * 0.04 +
                atributos.getOrDefault("Strength", 0) * 0.10 +
                atributos.getOrDefault("Reactions", 0) * 0.05 +
                atributos.getOrDefault("Aggression", 0) * 0.08 +
                atributos.getOrDefault("Interceptions", 0) * 0.08 +
                atributos.getOrDefault("Ball control", 0) * 0.05 +
                atributos.getOrDefault("Heading accuracy", 0) * 0.10 +
                atributos.getOrDefault("Short passing", 0) * 0.05 +
                atributos.getOrDefault("Defensive awareness", 0) * 0.15 +
                atributos.getOrDefault("Standing tackle", 0) * 0.15 +
                atributos.getOrDefault("Sliding tackle", 0) * 0.15
            );
            puntuaciones.put("Defensa Central (SW)", sw);
            
            // Lateral Derecho/Izquierdo (RWB/LWB)
            int rwb = (int) Math.round(
                atributos.getOrDefault("Acceleration", 0) * 0.04 +
                atributos.getOrDefault("Sprint speed", 0) * 0.06 +
                atributos.getOrDefault("Stamina", 0) * 0.10 +
                atributos.getOrDefault("Reactions", 0) * 0.08 +
                atributos.getOrDefault("Interceptions", 0) * 0.12 +
                atributos.getOrDefault("Ball control", 0) * 0.08 +
                atributos.getOrDefault("Crossing", 0) * 0.12 +
                atributos.getOrDefault("Dribbling", 0) * 0.04 +
                atributos.getOrDefault("Short passing", 0) * 0.10 +
                atributos.getOrDefault("Defensive awareness", 0) * 0.07 +
                atributos.getOrDefault("Standing tackle", 0) * 0.08 +
                atributos.getOrDefault("Sliding tackle", 0) * 0.11
            );
            puntuaciones.put("Lateral Ofensivo (RWB/LWB)", rwb);
            
            // Defensa Derecho/Izquierdo (RB/LB)
            int rb = (int) Math.round(
                atributos.getOrDefault("Acceleration", 0) * 0.05 +
                atributos.getOrDefault("Sprint speed", 0) * 0.07 +
                atributos.getOrDefault("Stamina", 0) * 0.08 +
                atributos.getOrDefault("Reactions", 0) * 0.08 +
                atributos.getOrDefault("Interceptions", 0) * 0.12 +
                atributos.getOrDefault("Ball control", 0) * 0.07 +
                atributos.getOrDefault("Crossing", 0) * 0.09 +
                atributos.getOrDefault("Heading accuracy", 0) * 0.04 +
                atributos.getOrDefault("Short passing", 0) * 0.07 +
                atributos.getOrDefault("Defensive awareness", 0) * 0.08 +
                atributos.getOrDefault("Standing tackle", 0) * 0.11 +
                atributos.getOrDefault("Sliding tackle", 0) * 0.14
            );
            puntuaciones.put("Lateral Defensivo (RB/LB)", rb);
            
            // Defensa Central (RCB/CB/LCB)
            int rcb = (int) Math.round(
                atributos.getOrDefault("Sprint speed", 0) * 0.02 +
                atributos.getOrDefault("Jumping", 0) * 0.03 +
                atributos.getOrDefault("Strength", 0) * 0.10 +
                atributos.getOrDefault("Reactions", 0) * 0.05 +
                atributos.getOrDefault("Aggression", 0) * 0.07 +
                atributos.getOrDefault("Interceptions", 0) * 0.13 +
                atributos.getOrDefault("Ball control", 0) * 0.04 +
                atributos.getOrDefault("Heading accuracy", 0) * 0.10 +
                atributos.getOrDefault("Short passing", 0) * 0.05 +
                atributos.getOrDefault("Defensive awareness", 0) * 0.14 +
                atributos.getOrDefault("Standing tackle", 0) * 0.17 +
                atributos.getOrDefault("Sliding tackle", 0) * 0.10
            );
            puntuaciones.put("Defensa Central (RCB/CB/LCB)", rcb);
            
            // Medio Defensivo (RDM/CDM/LDM)
            int rdm = (int) Math.round(
                atributos.getOrDefault("Stamina", 0) * 0.06 +
                atributos.getOrDefault("Strength", 0) * 0.04 +
                atributos.getOrDefault("Reactions", 0) * 0.07 +
                atributos.getOrDefault("Aggression", 0) * 0.05 +
                atributos.getOrDefault("Interceptions", 0) * 0.14 +
                atributos.getOrDefault("Vision", 0) * 0.04 +
                atributos.getOrDefault("Ball control", 0) * 0.10 +
                atributos.getOrDefault("Long passing", 0) * 0.10 +
                atributos.getOrDefault("Short passing", 0) * 0.14 +
                atributos.getOrDefault("Defensive awareness", 0) * 0.09 +
                atributos.getOrDefault("Standing tackle", 0) * 0.12 +
                atributos.getOrDefault("Sliding tackle", 0) * 0.05
            );
            puntuaciones.put("Medio Defensivo (RDM/CDM/LDM)", rdm);
            
            // Medio Derecho/Izquierdo (RM/LM)
            int rm = (int) Math.round(
                atributos.getOrDefault("Acceleration", 0) * 0.07 +
                atributos.getOrDefault("Sprint speed", 0) * 0.06 +
                atributos.getOrDefault("Stamina", 0) * 0.05 +
                atributos.getOrDefault("Reactions", 0) * 0.07 +
                atributos.getOrDefault("Att. Position", 0) * 0.08 +
                atributos.getOrDefault("Vision", 0) * 0.07 +
                atributos.getOrDefault("Ball control", 0) * 0.13 +
                atributos.getOrDefault("Crossing", 0) * 0.10 +
                atributos.getOrDefault("Dribbling", 0) * 0.15 +
                atributos.getOrDefault("Finishing", 0) * 0.06 +
                atributos.getOrDefault("Long passing", 0) * 0.05 +
                atributos.getOrDefault("Short passing", 0) * 0.11
            );
            puntuaciones.put("Medio Derecho/Izquierdo (RM/LM)", rm);
            
            // Medio Centro (RCM/CM/LCM)
            int rcm = (int) Math.round(
                atributos.getOrDefault("Stamina", 0) * 0.06 +
                atributos.getOrDefault("Reactions", 0) * 0.08 +
                atributos.getOrDefault("Interceptions", 0) * 0.05 +
                atributos.getOrDefault("Att. Position", 0) * 0.06 +
                atributos.getOrDefault("Vision", 0) * 0.13 +
                atributos.getOrDefault("Ball control", 0) * 0.14 +
                atributos.getOrDefault("Dribbling", 0) * 0.07 +
                atributos.getOrDefault("Finishing", 0) * 0.02 +
                atributos.getOrDefault("Long passing", 0) * 0.13 +
                atributos.getOrDefault("Short passing", 0) * 0.17 +
                atributos.getOrDefault("Long shots", 0) * 0.04 +
                atributos.getOrDefault("Standing tackle", 0) * 0.05
            );
            puntuaciones.put("Medio Centro (RCM/CM/LCM)", rcm);
            
            // Medio Ofensivo (RAM/CAM/LAM)
            int ram = (int) Math.round(
                atributos.getOrDefault("Acceleration", 0) * 0.04 +
                atributos.getOrDefault("Sprint speed", 0) * 0.03 +
                atributos.getOrDefault("Agility", 0) * 0.03 +
                atributos.getOrDefault("Reactions", 0) * 0.07 +
                atributos.getOrDefault("Att. Position", 0) * 0.09 +
                atributos.getOrDefault("Vision", 0) * 0.14 +
                atributos.getOrDefault("Ball control", 0) * 0.15 +
                atributos.getOrDefault("Dribbling", 0) * 0.13 +
                atributos.getOrDefault("Finishing", 0) * 0.07 +
                atributos.getOrDefault("Long passing", 0) * 0.04 +
                atributos.getOrDefault("Short passing", 0) * 0.16 +
                atributos.getOrDefault("Long shots", 0) * 0.05
            );
            puntuaciones.put("Medio Ofensivo (RAM/CAM/LAM)", ram);
            
            // Delantero (RF/CF/LF)
            int rf = (int) Math.round(
                atributos.getOrDefault("Acceleration", 0) * 0.05 +
                atributos.getOrDefault("Sprint speed", 0) * 0.05 +
                atributos.getOrDefault("Reactions", 0) * 0.09 +
                atributos.getOrDefault("Att. Position", 0) * 0.13 +
                atributos.getOrDefault("Vision", 0) * 0.08 +
                atributos.getOrDefault("Ball control", 0) * 0.15 +
                atributos.getOrDefault("Dribbling", 0) * 0.14 +
                atributos.getOrDefault("Finishing", 0) * 0.11 +
                atributos.getOrDefault("Heading accuracy", 0) * 0.02 +
                atributos.getOrDefault("Short passing", 0) * 0.09 +
                atributos.getOrDefault("Shot power", 0) * 0.05 +
                atributos.getOrDefault("Long shots", 0) * 0.04
            );
            puntuaciones.put("Delantero (RF/CF/LF)", rf);
            
            // Extremo (RW/LW)
            int rw = (int) Math.round(
                atributos.getOrDefault("Acceleration", 0) * 0.07 +
                atributos.getOrDefault("Sprint speed", 0) * 0.06 +
                atributos.getOrDefault("Agility", 0) * 0.03 +
                atributos.getOrDefault("Reactions", 0) * 0.07 +
                atributos.getOrDefault("Att. Position", 0) * 0.09 +
                atributos.getOrDefault("Vision", 0) * 0.06 +
                atributos.getOrDefault("Ball control", 0) * 0.14 +
                atributos.getOrDefault("Crossing", 0) * 0.09 +
                atributos.getOrDefault("Dribbling", 0) * 0.16 +
                atributos.getOrDefault("Finishing", 0) * 0.10 +
                atributos.getOrDefault("Short passing", 0) * 0.09 +
                atributos.getOrDefault("Long shots", 0) * 0.04
            );
            puntuaciones.put("Extremo (RW/LW)", rw);
            
            // Delantero Centro (RS/ST/LS)
            int rs = (int) Math.round(
                atributos.getOrDefault("Acceleration", 0) * 0.04 +
                atributos.getOrDefault("Sprint speed", 0) * 0.05 +
                atributos.getOrDefault("Strength", 0) * 0.05 +
                atributos.getOrDefault("Reactions", 0) * 0.08 +
                atributos.getOrDefault("Att. Position", 0) * 0.13 +
                atributos.getOrDefault("Ball control", 0) * 0.10 +
                atributos.getOrDefault("Dribbling", 0) * 0.07 +
                atributos.getOrDefault("Finishing", 0) * 0.18 +
                atributos.getOrDefault("Heading accuracy", 0) * 0.10 +
                atributos.getOrDefault("Short passing", 0) * 0.05 +
                atributos.getOrDefault("Shot power", 0) * 0.10 +
                atributos.getOrDefault("Long shots", 0) * 0.03 +
                atributos.getOrDefault("Volleys", 0) * 0.02
            );
            puntuaciones.put("Delantero Centro (RS/ST/LS)", rs);

            // Determinar overall (máxima puntuación)
            int overall = puntuaciones.values().stream().max(Integer::compare).orElse(0);
            overallField.setText(String.valueOf(overall));

            // Mostrar resultados
            StringBuilder sb = new StringBuilder();
            sb.append("Puntuaciones por posición:\n\n");
            
            puntuaciones.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    sb.append(String.format("%-35s: %3d\n", entry.getKey(), entry.getValue()));
                });

            resultadoArea.setText(sb.toString());

        } catch (NumberFormatException ex) {
            // Ignorar errores de formato temporalmente
        }
    }

    private void limpiarCampos() {
        for (JTextField field : camposAtributos.values()) {
            field.setText("");
        }
        overallField.setText("");
        resultadoArea.setText("Ingrese los atributos del jugador\npara ver su mejor posición.");
    }

    /*
     public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Calculadora de Posiciones FC25 - Vistas Personalizadas");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new PanelCalculadora());
                frame.setSize(1200, 800);
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    */
}