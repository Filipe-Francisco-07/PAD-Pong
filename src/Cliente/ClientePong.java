package Cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ClientePong extends JPanel implements KeyListener, Runnable {

    private static final long serialVersionUID = 1L;
    private PrintWriter oSaida;
    private BufferedReader oEntrada;
    private Socket oSocket;
    private int iJogadorY = 240;
    private int iOponenteY = 240;
    private int iBolaX = 400;
    private int iBolaY = 300;
    private int iScore1 = 0;
    private int iScore2 = 0;
    private int iIdJogador;
    private final int iLarguraRaquete = 20;
    private final int iAlturaRaquete = 120;
    private final int iTamanhoBola = 20;
    private final int iVelocidadeRaquete = 5;
    private boolean bCima = false;
    private boolean bBaixo = false;
    private ExecutorService oExecutor = Executors.newSingleThreadExecutor();

    public ClientePong(String sEnderecoServidor) throws IOException {
        oSocket = new Socket(sEnderecoServidor, 12345);
        oSaida = new PrintWriter(oSocket.getOutputStream(), true);
        oEntrada = new BufferedReader(new InputStreamReader(oSocket.getInputStream()));

        JFrame oJanela = new JFrame("Pong Multiplayer");
        oJanela.setSize(800, 600);
        oJanela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        oJanela.add(this);
        oJanela.setVisible(true);
        oJanela.addKeyListener(this);

        new Timer(15, e -> {
            if (bCima) {
                iJogadorY = Math.max(iJogadorY - iVelocidadeRaquete, 0);
                oSaida.println("MOVE:" + iJogadorY);
                repaint();
            }
            if (bBaixo) {
                iJogadorY = Math.min(iJogadorY + iVelocidadeRaquete, getHeight() - iAlturaRaquete);
                oSaida.println("MOVE:" + iJogadorY);
                repaint();
            }
        }).start();

        oExecutor.execute(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        g.setColor(Color.WHITE);

        g.fillOval(iBolaX, iBolaY, iTamanhoBola, iTamanhoBola);

        if (iIdJogador == 1) {
            g.fillRect(30, iJogadorY, iLarguraRaquete, iAlturaRaquete);
            g.fillRect(750, iOponenteY, iLarguraRaquete, iAlturaRaquete);
        } else if (iIdJogador == 2) {
            g.fillRect(750, iJogadorY, iLarguraRaquete, iAlturaRaquete);
            g.fillRect(30, iOponenteY, iLarguraRaquete, iAlturaRaquete);
        }

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("P1", 30, 30);
        g.drawString("P2", getWidth() - 50, 30);

        String sPlacar = iScore1 + " : " + iScore2;
        int iLarguraTexto = g.getFontMetrics().stringWidth(sPlacar);
        g.drawString(sPlacar, (getWidth() - iLarguraTexto) / 2, 30);
    }

    @Override
    public void run() {
        try {
            String sMensagem;
            while ((sMensagem = oEntrada.readLine()) != null) {
                if (sMensagem.startsWith("ID:")) {
                    iIdJogador = Integer.parseInt(sMensagem.substring(3));
                } else {
                    String[] aPartes = sMensagem.split(",");
                    iBolaX = Integer.parseInt(aPartes[0]);
                    iBolaY = Integer.parseInt(aPartes[1]);

                    int iJogador1Y = Integer.parseInt(aPartes[2]);
                    int iJogador2Y = Integer.parseInt(aPartes[3]);

                    iScore1 = Integer.parseInt(aPartes[4]);
                    iScore2 = Integer.parseInt(aPartes[5]);

                    iOponenteY = (iIdJogador == 1) ? iJogador2Y : iJogador1Y;

                    repaint();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            oExecutor.shutdown();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int iTecla = e.getKeyCode();
        if (iTecla == KeyEvent.VK_UP) {
            bCima = true;
        } else if (iTecla == KeyEvent.VK_DOWN) {
            bBaixo = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int iTecla = e.getKeyCode();
        if (iTecla == KeyEvent.VK_UP) {
            bCima = false;
        } else if (iTecla == KeyEvent.VK_DOWN) {
            bBaixo = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) throws IOException {
        String sEnderecoServidor = "127.0.0.1";
        new ClientePong(sEnderecoServidor);
    }
}
