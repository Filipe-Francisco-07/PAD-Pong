package Servidor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ServidorPong {
	
    private static List<ManipuladorCliente> lClientes = new ArrayList<>();
    public static int iBolaX = 400;
    public static int iBolaY = 300;
    public static int iVelocidadeBolaX = 5;
    public static int iVelocidadeBolaY = 5;
    public static int iJogador1Y = 240;
    public static int iJogador2Y = 240;
    private static int iPlacar1 = 0;
    private static int iPlacar2 = 0;
    private static final int iAlturaRaquete = 120;
    private static ExecutorService oExecutor = Executors.newFixedThreadPool(2);

    public static void main(String[] args) {
        try (ServerSocket oServidorSocket = new ServerSocket(12345)) {
            System.out.println("Servidor iniciado na porta 12345...");

            while (lClientes.size() < 2) {
                Socket oSocket = oServidorSocket.accept();
                int iIdJogador = lClientes.size() + 1;
                ManipuladorCliente oManipulador = new ManipuladorCliente(oSocket, iIdJogador);
                lClientes.add(oManipulador);
                oExecutor.submit(oManipulador);
                System.out.println("Jogador " + iIdJogador + " conectado.");
            }

            while (true) {
                iBolaX += iVelocidadeBolaX;
                iBolaY += iVelocidadeBolaY;

                if (iBolaY <= 0 || iBolaY >= 580) {
                    iVelocidadeBolaY = -iVelocidadeBolaY;
                }

                if (iBolaX <= 50 && iBolaY + 20 >= iJogador1Y && iBolaY <= iJogador1Y + iAlturaRaquete) {
                    iVelocidadeBolaX = -iVelocidadeBolaX;
                    iBolaX = 50;
                }

                if (iBolaX >= 730 && iBolaY + 20 >= iJogador2Y && iBolaY <= iJogador2Y + iAlturaRaquete) {
                    iVelocidadeBolaX = -iVelocidadeBolaX;
                    iBolaX = 730;
                }

                if (iBolaX <= 0) {
                    iPlacar2++;
                    reiniciarBola();
                }

                if (iBolaX >= 780) {
                    iPlacar1++;
                    reiniciarBola();
                }

                String sEstadoJogo = iBolaX + "," + iBolaY + "," + iJogador1Y + "," + iJogador2Y + "," + iPlacar1 + "," + iPlacar2;

                for (ManipuladorCliente oCliente : lClientes) {
                    oCliente.enviar(sEstadoJogo);
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
            oExecutor.shutdown();
            System.out.println("Executor finalizado.");
        }
    }

    private static void reiniciarBola() {
        iBolaX = 400;
        iBolaY = 300;
        iVelocidadeBolaX = 5 * (Math.random() > 0.5 ? 1 : -1);
        iVelocidadeBolaY = 5 * (Math.random() > 0.5 ? 1 : -1);
    }
}
