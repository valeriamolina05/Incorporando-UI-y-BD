import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Aplicación de escritorio (Swing) que permite probar una conexión JDBC.
 *
 * El usuario escribe la URL de conexión, el usuario y la contraseña, pulsa
 * "Probar conexión" y la aplicación informa en un JTextArea si la conexión fue
 * exitosa (texto verde) o fallida (texto rojo). La conexión se abre con
 * try-with-resources, de modo que siempre se cierra.
 *
 * Solo utiliza la API estándar de Java (java.sql y javax.swing). El driver
 * JDBC de la base de datos (por ejemplo sqlite-jdbc) debe estar en el classpath
 * únicamente en tiempo de ejecución.
 */
public class ConexionTester extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color VERDE = new Color(0, 128, 0);
    private static final Color ROJO = new Color(190, 0, 0);

    private final JTextField txtUrl = new JTextField("jdbc:sqlite:inventario.db", 30);
    private final JTextField txtUsuario = new JTextField(30);
    private final JPasswordField txtPassword = new JPasswordField(30);
    private final JButton btnProbar = new JButton("Probar conexión");
    private final JTextArea areaResultado = new JTextArea(10, 40);

    public ConexionTester() {
        super("Probador de conexión JDBC");
        construirInterfaz();
        configurarEventos();
    }

    // ------------------------------------------------------------------
    // Interfaz gráfica
    // ------------------------------------------------------------------
    private void construirInterfaz() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        // Panel de datos: GridLayout de 3 filas x 2 columnas (etiqueta | campo)
        JPanel panelDatos = new JPanel(new GridLayout(3, 2, 6, 6));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Datos de conexión"));
        panelDatos.add(new JLabel("URL de conexión:"));
        panelDatos.add(txtUrl);
        panelDatos.add(new JLabel("Usuario (vacío si es SQLite):"));
        panelDatos.add(txtUsuario);
        panelDatos.add(new JLabel("Contraseña (vacío si es SQLite):"));
        panelDatos.add(txtPassword);

        // Panel del botón
        JPanel panelBoton = new JPanel();
        panelBoton.add(btnProbar);

        // Panel superior: datos + botón
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        panelNorte.add(panelDatos, BorderLayout.CENTER);
        panelNorte.add(panelBoton, BorderLayout.SOUTH);

        // Área de resultados con scroll
        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);
        areaResultado.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        JScrollPane scroll = new JScrollPane(areaResultado);
        scroll.setBorder(BorderFactory.createTitledBorder("Resultado de la prueba"));

        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        panelCentro.add(scroll, BorderLayout.CENTER);

        add(panelNorte, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);

        setMinimumSize(new Dimension(620, 420));
        pack();
        setLocationRelativeTo(null);
    }

    // ------------------------------------------------------------------
    // Eventos
    // ------------------------------------------------------------------
    private void configurarEventos() {
        btnProbar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                probarConexion();
            }
        });
    }

    // ------------------------------------------------------------------
    // Lógica JDBC
    // ------------------------------------------------------------------
    private void probarConexion() {
        String url = txtUrl.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (url.isEmpty()) {
            mostrarResultado("Debe ingresar la URL de conexión.", ROJO);
            return;
        }

        try {
            // Carga explícita del driver (puede lanzar ClassNotFoundException)
            String claseDriver = obtenerClaseDriver(url);
            if (claseDriver != null) {
                Class.forName(claseDriver);
            }

            // try-with-resources: la conexión se cierra siempre, con éxito o con fallo
            try (Connection conexion = abrirConexion(url, usuario, password)) {
                DatabaseMetaData meta = conexion.getMetaData();
                mostrarResultado("Conexión exitosa\n"
                        + "Base de datos: " + meta.getDatabaseProductName()
                        + " " + meta.getDatabaseProductVersion() + "\n"
                        + "Driver: " + meta.getDriverName()
                        + " " + meta.getDriverVersion() + "\n"
                        + "URL: " + url, VERDE);
            }
        } catch (ClassNotFoundException ex) {
            mostrarResultado("Fallo del driver: no se encontró la clase "
                    + ex.getMessage() + "\n"
                    + "Agregue el .jar del driver JDBC al classpath.", ROJO);
        } catch (SQLException ex) {
            mostrarResultado("Fallo de conexión\n"
                    + "Mensaje técnico: " + ex.getMessage(), ROJO);
        }
    }

    private Connection abrirConexion(String url, String usuario, String password)
            throws SQLException {
        if (usuario.isEmpty() && password.isEmpty()) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, usuario, password);
    }

    /** Devuelve la clase del driver según el prefijo de la URL (null si no se conoce). */
    private String obtenerClaseDriver(String url) {
        String u = url.toLowerCase();
        if (u.startsWith("jdbc:sqlite:")) {
            return "org.sqlite.JDBC";
        } else if (u.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (u.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (u.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        }
        return null;
    }

    private void mostrarResultado(String mensaje, Color color) {
        areaResultado.setForeground(color);
        areaResultado.setText(mensaje);
        areaResultado.setCaretPosition(0);
    }

    // ------------------------------------------------------------------
    // Punto de entrada
    // ------------------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignorada) {
                    // Se usa el aspecto por defecto
                }
                new ConexionTester().setVisible(true);
            }
        });
    }
}
