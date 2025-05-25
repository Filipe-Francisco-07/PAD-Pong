package Cliente;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ClientePong extends JPanel implements KeyListener, Runnable {
    private static final long serialVersionUID = 1L;

    private int jogadorY = 240;
    private int oponenteY = 240;
    private int bolaX = 400;
    private int bolaY = 300;
    private int idJogador;
    private PrintWriter saida;
    private BufferedReader entrada;
    private Socket socket;

    private final int larguraRaquete = 20;
    private final int alturaRaquete = 120;
    private final int tamanhoBola = 20;
    private final int velocidadeMovimento = 5;

    private boolean movendoCima = false;
    private boolean movendoBaixo = false;

    private int placar1 = 0;
    private int placar2 = 0;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public ClientePong(String enderecoServidor) throws IOException {
        socket = new Socket(enderecoServidor, 12345);
        saida = new PrintWriter(socket.getOutputStream(), true);
        entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        JFrame janela = new JFrame("Pong Multiplayer");
        janela.setSize(800, 600);
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.add(this);
        janela.setVisible(true);
        janela.addKeyListener(this);

        new Timer(15, e -> {
            if (movendoCima) {
                jogadorY = Math.max(jogadorY - velocidadeMovimento, 0);
                saida.println("MOVE:" + jogadorY);
                repaint();
            }
            if (movendoBaixo) {
                jogadorY = Math.min(jogadorY + velocidadeMovimento, getHeight() - alturaRaquete);
                saida.println("MOVE:" + jogadorY);
                repaint();
            }
        }).start();

        executor.execute(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.BLACK);
        g.setColor(Color.WHITE);

        g.fillOval(bolaX, bolaY, tamanhoBola, tamanhoBola);

        if (idJogador == 1) {
            g.fillRect(30, jogadorY, larguraRaquete, alturaRaquete);
            g.fillRect(750, oponenteY, larguraRaquete, alturaRaquete);
        } else if (idJogador == 2) {
            g.fillRect(750, jogadorY, larguraRaquete, alturaRaquete);
            g.fillRect(30, oponenteY, larguraRaquete, alturaRaquete);
        }

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("P1", 30, 30);
        g.drawString("P2", getWidth() - 50, 30);

        String placar = placar1 + " : " + placar2;
        int larguraTexto = g.getFontMetrics().stringWidth(placar);
        g.drawString(placar, (getWidth() - larguraTexto) / 2, 30);
    }

    @Override
    public void run() {
        try {
            String mensagem;
            while ((mensagem = entrada.readLine()) != null) {
                if (mensagem.startsWith("ID:")) {
                    idJogador = Integer.parseInt(mensagem.substring(3));
                } else {
                    String[] partes = mensagem.split(",");
                    bolaX = Integer.parseInt(partes[0]);
                    bolaY = Integer.parseInt(partes[1]);

                    int jogador1Y = Integer.parseInt(partes[2]);
                    int jogador2Y = Integer.parseInt(partes[3]);

                    placar1 = Integer.parseInt(partes[4]);
                    placar2 = Integer.parseInt(partes[5]);

                    if (idJogador == 1) {
                        oponenteY = jogador2Y;
                    } else {
                        oponenteY = jogador1Y;
                    }

                    repaint();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (tecla == KeyEvent.VK_UP) {
            movendoCima = true;
        } else if (tecla == KeyEvent.VK_DOWN) {
            movendoBaixo = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int tecla = e.getKeyCode();
        if (tecla == KeyEvent.VK_UP) {
            movendoCima = false;
        } else if (tecla == KeyEvent.VK_DOWN) {
            movendoBaixo = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) throws IOException {
        String enderecoServidor = "127.0.0.1";
        new ClientePong(enderecoServidor);
    }
}
