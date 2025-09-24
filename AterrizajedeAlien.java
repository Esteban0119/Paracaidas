import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

// Clase principal con botones Setup y Start
public class AterrizajedeAlien extends JFrame {
    private PanelSimulacion panel;
    private JButton setupButton, startButton;

    public AterrizajedeAlien() {
        setTitle("Simulación de Aterrizaje Alien - Evolutiva");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        panel = new PanelSimulacion(); // Panel donde ocurre la simulación
        add(panel, BorderLayout.CENTER);

        // Panel de controles con los botones
        JPanel controlPanel = new JPanel();
        setupButton = new JButton("Setup");
        startButton = new JButton("Start");
        controlPanel.add(setupButton);
        controlPanel.add(startButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Botón Setup → inicializa parámetros aleatorios
        setupButton.addActionListener(e -> panel.setupSimulacion());

        // Botón Start → comienza la simulación
        startButton.addActionListener(e -> panel.startSimulacion());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AterrizajedeAlien().setVisible(true); // Arranca el programa
        });
    }
}

// Panel donde se dibuja y se actualiza la simulación
class PanelSimulacion extends JPanel implements ActionListener {
    private Nave nave;              // Objeto nave
    private Timer timer;            // Temporizador para animación
    private int generacion, maxGeneraciones, exitos; // Estadísticas
    private boolean enEjecucion;    // Controla si la simulación está corriendo
    private Random rand;            // Para generar aleatorios
    private double gravedad, velocidadInicial; // Parámetros de simulación

    public PanelSimulacion() {
        rand = new Random();
        timer = new Timer(50, this); // Cada 50ms llama a actionPerformed()
        setupSimulacion();           // Al inicio hace un setup
    }

    // Setup inicial con valores aleatorios
    public void setupSimulacion() {
        generacion = 0;
        exitos = 0;
        maxGeneraciones = rand.nextInt(30) + 1; // Número máximo de intentos
        gravedad = 0.05 + rand.nextDouble() * 0.25; // Gravedad aleatoria
        velocidadInicial = 1 + rand.nextDouble() * 5; // Velocidad inicial aleatoria
        nave = null; // Se reinicia la nave
        repaint();   // Se vuelve a dibujar
    }

    // Inicia la simulación
    public void startSimulacion() {
        if (!enEjecucion) {      // Solo si no se está ejecutando ya
            enEjecucion = true;
            nuevaGeneracion();   // Crea la primera nave
            timer.start();       // Arranca el timer (animación)
        }
    }

    // Crea una nueva generación de nave (un nuevo intento)
    private void nuevaGeneracion() {
        if (generacion >= maxGeneraciones) { // Si ya pasó el límite
            timer.stop();
            enEjecucion = false;
            JOptionPane.showMessageDialog(this, 
                "Fin de simulación. No hubo más intentos exitosos.");
            return;
        }
        generacion++;

        // Nueva nave con parámetros actuales
        nave = new Nave(getWidth() / 2, 50, gravedad, velocidadInicial);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (nave != null && !nave.terminada) {
            nave.actualizar(); // Se mueve la nave

            int sueloY = getHeight() - 100; // Posición del suelo

            // Si la nave toca el suelo
            if (nave.y >= sueloY) {
                nave.y = sueloY;

                if (nave.velocidadY <= 4) { // Condición de aterrizaje suave
                    nave.terminada = true;
                    exitos++; // Contamos éxito
                    repaint();
                    timer.stop(); // Paramos la simulación en éxito
                    JOptionPane.showMessageDialog(this, 
                        "¡Aterrizaje exitoso en la generación " + generacion + "!");
                } else {
                    // Si se estrella
                    nave.explotada = true;
                    nave.terminada = true;

                    // Ajustamos parámetros para "aprender"
                    gravedad = Math.max(0.02, gravedad * 0.9);
                    velocidadInicial = Math.max(0.5, velocidadInicial * 0.85);

                    // Esperamos 800ms antes de crear la siguiente nave
                    new Timer(800, evt -> {
                        ((Timer) evt.getSource()).stop();
                        nuevaGeneracion();
                    }).start();
                }
            }
        }
        repaint(); // Redibuja la pantalla
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dibujar el suelo
        g.setColor(Color.GREEN.darker());
        g.fillRect(0, getHeight() - 100, getWidth(), 100);

        // Dibujar nave o explosión
        if (nave != null) {
            if (nave.explotada) {
                g.setColor(Color.ORANGE);
                g.fillOval((int) nave.x - 30, (int) nave.y - 30, 60, 60);
                g.setColor(Color.RED);
                g.fillOval((int) nave.x - 20, (int) nave.y - 20, 40, 40);
            } else {
                g.setColor(Color.WHITE);
                g.fillRect((int) nave.x - 10, (int) nave.y - 20, 20, 40);
                g.setColor(Color.GRAY);
                g.fillRect((int) nave.x - 15, (int) nave.y + 20, 30, 10);
            }
        }

        // Estadísticas en pantalla
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("Generación: " + generacion + "/" + maxGeneraciones, 20, 30);
        g.drawString("Éxitos: " + exitos, 20, 50);
        g.drawString(String.format("Velocidad actual: %.2f", (nave != null ? nave.velocidadY : 0)), 20, 70);
        g.drawString(String.format("Velocidad inicial: %.2f", velocidadInicial), 20, 90);
        g.drawString(String.format("Gravedad: %.3f", gravedad), 20, 110);
    }
}

// Clase Nave (representa la nave en la simulación)
class Nave {
    public double x, y;           // Posición
    public double velocidadY;     // Velocidad vertical
    public double gravedad;       // Gravedad que la afecta
    public boolean explotada;     // Si se estrelló
    public boolean terminada;     // Si ya terminó su intento

    public Nave(double x, double y, double gravedad, double velocidadInicial) {
        this.x = x;
        this.y = y;
        this.gravedad = gravedad;
        this.velocidadY = velocidadInicial; // Empieza con velocidad inicial
        this.explotada = false;
        this.terminada = false;
    }

    // Cada frame se actualiza la física
    public void actualizar() {
        velocidadY += gravedad; // Se incrementa la velocidad por la gravedad
        y += velocidadY;        // Se mueve la nave hacia abajo
    }
}
