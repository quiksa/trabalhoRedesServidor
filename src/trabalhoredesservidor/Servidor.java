package trabalhoredesservidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Servidor {

    public static void main(String[] args) throws IOException, TwitterException {
        new Servidor().startServer();
    }

    public void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

        Runnable serverTask = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(1234);
                    System.out.println("Waiting for clients to connect...");
                    while (!serverSocket.isClosed()) {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("Conexão de " + clientSocket.getInetAddress().getHostName());
                        clientProcessingPool.submit(new ClientTask(clientSocket));

                    }
                } catch (IOException e) {
                    System.err.println("Unable to process client request");
                    e.printStackTrace();
                }
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();

    }

    private class ClientTask implements Runnable {

        private final Socket clientSocket;
        private String mensagemRecebida;
        private String retorno;

        private ClientTask(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            System.out.println("Client connected!");

            try {
                InputStream input = clientSocket.getInputStream();
                OutputStream output = clientSocket.getOutputStream();
                PrintStream outToClient;

                try (BufferedReader in = new BufferedReader(new InputStreamReader(input))) {

                    outToClient = new PrintStream(output);
                    while (true) {
                        mensagemRecebida = in.readLine();

                        System.out.println("Mensagem recebida :" + mensagemRecebida);

                        if (mensagemRecebida == null) { // cliente se desconextou
                            break;
                        }

                        String temp = getEscolha(mensagemRecebida);

                        if (temp.equals("a")) {
                            retorno = tipoAutores(mensagemRecebida);
                        } else if (temp.equals("c")) {
                            retorno = getCidade(mensagemRecebida);
                        } else if (temp.equals("t")) {
                            retorno = getTwitter();
                        } else {
                            System.out.println("Não Possui buscas para esta Consulta");
                            retorno = "Consultar Incorreta";
                        }

                        outToClient.println(retorno);

                    }

                    System.out.println("Conexão encerrada com: " + clientSocket.getInetAddress().getHostName());

                    outToClient.close();
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String tipoAutores(String caracter) {
        String tipo = "";

        for (int x = 0; x < caracter.length(); x++) { // for para separar a string em char 

            char ch = caracter.charAt(x);
            if (x == 0) {

            } else {
                tipo = tipo + ch;
            }
        }
        System.out.println(tipo);
        if (tipo.equals("autores") == true) {

            return "Autores: Fabio , Calvin , Iago e Guilherme";
        }
        return "Busca Autores Incorreta";
    }

    public static String getTwitter() throws TwitterException {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("SYFF37zumNgisXwmfzuU62wz2")
                .setOAuthConsumerSecret("bJE6uUaDrcE82wzHNSj05TL7GJaFyaTCcN3bpLYEJ7B9LERZAb")
                .setOAuthAccessToken("870283961972527104-k1HbrgIdZVgzZ7UB3ldenLhA1EpC5Og")
                .setOAuthAccessTokenSecret("SkN5TuIUU08VO7fp9hOKnfJjnFHUiL5ZLItknsD6OFT77");

        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter4j.Twitter twitter = tf.getInstance();

        ResponseList<twitter4j.Status> status = twitter.getHomeTimeline();
        String TimeLine = "";
        int contadorNoticia = 0;

        for (twitter4j.Status st : status) {
            contadorNoticia++;
            if (contadorNoticia <= 8) {

                TimeLine = TimeLine + (st.getUser().getName() + "-----" + st.getText() + "*");

            }
        }

        return TimeLine;
    }

    public static String getCidade(String CEP) throws IOException {
        String tipoCEP = "";
        int cont = 0;
        for (int x = 0; x < CEP.length(); x++) { // for para separar a string em char 

            char ch = CEP.charAt(x);

            if (cont == 1) {
                tipoCEP = tipoCEP + ch;
            }
            if (ch == ' ') {
                cont = 1;
            }
        }
        System.out.println("" + tipoCEP);
        try {
            Document doc = (Document) Jsoup.connect("http://www.qualocep.com/busca-cep/" + tipoCEP)
                    .timeout(120000).get();
            Elements urlPesquisa = doc.select("span[itemprop=addressLocality]");
            for (Element urlCidade : urlPesquisa) {
                return "CEP: " + tipoCEP + " da cidade de: " + urlCidade.text();
            }
        } catch (SocketTimeoutException e) {
        } catch (HttpStatusException w) {
        }
        return "Código CEP Inexistente";
    }

    public static String getEscolha(String escolha) throws IOException {
        String tipoEscolha = "";
        for (int x = 0; x < escolha.length(); x++) { // for para separar a string em char 

            char ch = escolha.charAt(x);
            if (x == 1) {
                tipoEscolha = tipoEscolha + ch;
            }
        }
        return tipoEscolha;
    }

}
