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

        panel = new PanelSimulacion();
        add(panel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        setupButton = new JButton("Setup");
        startButton = new JButton("Start");
        controlPanel.add(setupButton);
        controlPanel.add(startButton);
        add(controlPanel, BorderLayout.SOUTH);

        // Setup → parámetros iniciales aleatorios
        setupButton.addActionListener(e -> panel.setupSimulacion());

        // Start → corre simulación
        startButton.addActionListener(e -> panel.startSimulacion());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AterrizajedeAlien().setVisible(true);
        });
    }
}

// Panel donde se dibuja y simula
class PanelSimulacion extends JPanel implements ActionListener {
    private Nave nave;
    private Timer timer;
    private int generacion, maxGeneraciones, exitos;
    private boolean enEjecucion;
    private Random rand;
    private double gravedad, velocidadInicial;

    public PanelSimulacion() {
        rand = new Random();
        timer = new Timer(50, this); // refresco cada 50ms
        setupSimulacion();
    }

    // Setup inicial con valores aleatorios
    public void setupSimulacion() {
        generacion = 0;
        exitos = 0;
        maxGeneraciones = rand.nextInt(30) + 1; // entre 1 y 30 generaciones
        gravedad = 0.05 + rand.nextDouble() * 0.25; // gravedad inicial aleatoria
        velocidadInicial = 1 + rand.nextDouble() * 5; // velocidad inicial aleatoria
        nave = null;
        repaint();
    }

    // Inicia la simulación
    public void startSimulacion() {
        if (!enEjecucion) {
            enEjecucion = true;
            nuevaGeneracion();
            timer.start();
        }
    }

    // Nueva generación: ajusta parámetros si hubo choque
    private void nuevaGeneracion() {
        if (generacion >= maxGeneraciones) {
            timer.stop();
            enEjecucion = false;
            JOptionPane.showMessageDialog(this, "Fin de simulación. No hubo más intentos exitosos.");
            return;
        }
        generacion++;

        // Crear nave con los parámetros actuales
        nave = new Nave(getWidth() / 2, 50, gravedad, velocidadInicial);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (nave != null && !nave.terminada) {
            nave.actualizar();

            int sueloY = getHeight() - 100;

            if (nave.y >= sueloY) {
                nave.y = sueloY;

                if (nave.velocidadY <= 4) {
                    // Aterrizaje exitoso
                    nave.terminada = true;
                    exitos++;
                    repaint();
                    timer.stop();
                    JOptionPane.showMessageDialog(this, "¡Aterrizaje exitoso en la generación " + generacion + "!");
                } else {
                    // Choque → ajustamos parámetros para mejorar
                    nave.explotada = true;
                    nave.terminada = true;

                    // Ajuste: reducimos gravedad y velocidad inicial poco a poco
                    gravedad = Math.max(0.02, gravedad * 0.9); // baja gravedad
                    velocidadInicial = Math.max(0.5, velocidadInicial * 0.85); // baja velocidad inicial

                    // Espera antes de la siguiente generación
                    new Timer(800, evt -> {
                        ((Timer) evt.getSource()).stop();
                        nuevaGeneracion();
                    }).start();
                }
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Suelo
        g.setColor(Color.GREEN.darker());
        g.fillRect(0, getHeight() - 100, getWidth(), 100);

        // Nave o explosión
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

// Clase Nave
class Nave {
    public double x, y;
    public double velocidadY;
    public double gravedad;
    public boolean explotada;
    public boolean terminada;

    public Nave(double x, double y, double gravedad, double velocidadInicial) {
        this.x = x;
        this.y = y;
        this.gravedad = gravedad;
        this.velocidadY = velocidadInicial; // valor pasado desde el panel
        this.explotada = false;
        this.terminada = false;
    }

    public void actualizar() {
        velocidadY += gravedad;
        y += velocidadY;
    }
}