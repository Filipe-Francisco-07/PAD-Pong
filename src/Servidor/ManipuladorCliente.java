package Servidor;

import java.io.*;
import java.net.*;

public class ManipuladorCliente implements Runnable {
	
    private PrintWriter oSaida;
    private BufferedReader oEntrada;
    private int iIdJogador;

    public ManipuladorCliente(Socket oSocket, int iIdJogador) throws IOException {
        this.iIdJogador = iIdJogador;
        this.oSaida = new PrintWriter(oSocket.getOutputStream(), true);
        this.oEntrada = new BufferedReader(new InputStreamReader(oSocket.getInputStream()));
        enviar("ID:" + iIdJogador);
    }

    public void enviar(String sMensagem) {
        oSaida.println(sMensagem);
    }

    @Override
    public void run() {
        String sMensagem;
        try {
            while ((sMensagem = oEntrada.readLine()) != null) {
                if (sMensagem.startsWith("MOVE:")) {
                    int iNovaPosicaoY = Integer.parseInt(sMensagem.substring(5));
                    if (iIdJogador == 1) {
                        ServidorPong.iJogador1Y = iNovaPosicaoY;
                    } else if (iIdJogador == 2) {
                        ServidorPong.iJogador2Y = iNovaPosicaoY;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
