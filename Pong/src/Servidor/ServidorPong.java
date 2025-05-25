package Servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorPong {
    private static List<ManipuladorCliente> clientes = new ArrayList<>();
    public static int bolaX = 400;
    public static int bolaY = 300;
    public static int velocidadeBolaX = 5;
    public static int velocidadeBolaY = 5;

    public static int jogador1Y = 240;
    public static int jogador2Y = 240;

    private static final int ALTURA_RAQUETE = 120;

    private static int placar1 = 0;
    private static int placar2 = 0;

    private static ExecutorService executor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        try (ServerSocket servidorSocket = new ServerSocket(12345)) {
            System.out.println("Servidor iniciado na porta 12345...");

            while (clientes.size() < 2) {
                Socket socket = servidorSocket.accept();
                int idJogador = clientes.size() + 1;
                ManipuladorCliente manipulador = new ManipuladorCliente(socket, idJogador);
                clientes.add(manipulador);
                executor.submit(manipulador);
                System.out.println("Jogador " + idJogador + " conectado.");
            }

            while (true) {
                bolaX += velocidadeBolaX;
                bolaY += velocidadeBolaY;

                if (bolaY <= 0 || bolaY >= 580) {
                    velocidadeBolaY = -velocidadeBolaY;
                }

                if (bolaX <= 50 && bolaY + 20 >= jogador1Y && bolaY <= jogador1Y + ALTURA_RAQUETE) {
                    velocidadeBolaX = -velocidadeBolaX;
                    bolaX = 50;
                }

                if (bolaX >= 730 && bolaY + 20 >= jogador2Y && bolaY <= jogador2Y + ALTURA_RAQUETE) {
                    velocidadeBolaX = -velocidadeBolaX;
                    bolaX = 730;
                }

                if (bolaX <= 0) {
                    placar2++;
                    reiniciarBola();
                }

                if (bolaX >= 780) {
                    placar1++;
                    reiniciarBola();
                }

                String estadoJogo = bolaX + "," + bolaY + "," + jogador1Y + "," + jogador2Y + "," + placar1 + "," + placar2;

                for (ManipuladorCliente cliente : clientes) {
                    cliente.enviar(estadoJogo);
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                    break; 
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
            System.out.println("Executor finalizado.");
        }
    }

    private static void reiniciarBola() {
        bolaX = 400;
        bolaY = 300;
        velocidadeBolaX = 4 * (Math.random() > 0.5 ? 1 : -1);
        velocidadeBolaY = 4 * (Math.random() > 0.5 ? 1 : -1);
    }
}
