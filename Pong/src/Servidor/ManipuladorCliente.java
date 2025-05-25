package Servidor;

import java.io.*;
import java.net.*;

public class ManipuladorCliente implements Runnable {
    private PrintWriter saida;
    private BufferedReader entrada;
    private int idJogador;

    public ManipuladorCliente(Socket socket, int idJogador) throws IOException {
        this.idJogador = idJogador;
        this.saida = new PrintWriter(socket.getOutputStream(), true);
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        enviar("ID:" + idJogador);
    }

    public void enviar(String mensagem) {
        saida.println(mensagem);
    }

    @Override
    public void run() {
        String mensagem;
        try {
            while ((mensagem = entrada.readLine()) != null) {
                if (mensagem.startsWith("MOVE:")) {
                    int novaPosicaoY = Integer.parseInt(mensagem.substring(5));
                    if (idJogador == 1) {
                        ServidorPong.jogador1Y = novaPosicaoY;
                    } else if (idJogador == 2) {
                        ServidorPong.jogador2Y = novaPosicaoY;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
