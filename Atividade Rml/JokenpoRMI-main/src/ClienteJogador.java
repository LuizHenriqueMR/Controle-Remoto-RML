import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;


public class ClienteJogador extends UnicastRemoteObject implements Jogador {

    private final String nome;
    private Jogo servidor;

    private final List<String> jogadasAutomaticas;
    private int indiceAutomatico = 0;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scanner teclado = new Scanner(System.in);

    protected ClienteJogador(String nome, List<String> jogadasAutomaticas) throws RemoteException {
        super();
        this.nome = nome;
        this.jogadasAutomaticas = jogadasAutomaticas;
    }

    @Override
    public String getNome() throws RemoteException {
        return nome;
    }

    @Override
    public void iniciarRodada(int numeroRodada, int segundosParaJogar) throws RemoteException {
        System.out.println("\n----- RODADA " + numeroRodada + " -----");
        System.out.println("Voce tem " + segundosParaJogar + " segundos para jogar!");
        System.out.println("Opcoes: pedra, papel, tesoura, lagarto, spock");

        String jogada;

        if (jogadasAutomaticas != null && indiceAutomatico < jogadasAutomaticas.size()) { 

            jogada = jogadasAutomaticas.get(indiceAutomatico++);
            System.out.println("[modo automatico] jogando: " + jogada);
            enviarJogada(jogada);
            return;
        }

        Future<String> futuro = executor.submit(() -> {
            System.out.print("Sua jogada: ");
            return teclado.nextLine();
        });

        try {
            jogada = futuro.get(segundosParaJogar, TimeUnit.SECONDS);
            enviarJogada(jogada);
        } catch (TimeoutException e) {
            futuro.cancel(true);
            System.out.println("Tempo esgotado! Voce sera desclassificado nesta rodada.");
        } catch (Exception e) {
            System.out.println("Erro ao ler jogada: " + e.getMessage());
        }
    }

    private void enviarJogada(String jogada) {
        try {
            servidor.jogar(nome, jogada);
        } catch (RemoteException e) {
            System.out.println("Erro ao enviar jogada: " + e.getMessage());
        }
    }

    @Override
    public void resultadoRodada(String resumo) throws RemoteException {
        System.out.println(">> " + resumo);
    }

    @Override
    public void atualizarPlacar(String placarFormatado) throws RemoteException {
        System.out.println(">> " + placarFormatado);
    }

    @Override
    public void receberMensagem(String mensagem) throws RemoteException {
        System.out.println(">> [MENSAGEM] " + mensagem);
    }

    @Override
    public void notificarVitoria(List<Jogador> perdedores, String minhaJogada) throws RemoteException {
        System.out.println(">> Voce VENCEU esta rodada com '" + minhaJogada + "'! Provocando os perdedores...");

        for (Jogador perdedor : perdedores) {
            try {
                perdedor.receberMensagem("Perdeu Loser... Tente na proxima.");
            } catch (RemoteException e) {
                System.out.println("Nao foi possivel notificar um dos perdedores: " + e.getMessage());
            }
        }
    }

    public void conectarAoServidor(String host) throws Exception {
        Registry registro = LocateRegistry.getRegistry(host, 1099);
        servidor = (Jogo) registro.lookup("Jogo");
        servidor.conectar(this, nome);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java ClienteJogador <nome> [ip-do-servidor] [--auto=jogada1,jogada2,...]");
            return;
        }

        String nome = args[0];
        String host = "localhost";
        List<String> jogadasAutomaticas = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--auto=")) {
                jogadasAutomaticas = java.util.Arrays.asList(args[i].substring(7).split(","));
            } else {
                host = args[i];
            }
        }

        try {
            ClienteJogador cliente = new ClienteJogador(nome, jogadasAutomaticas);
            cliente.conectarAoServidor(host);

            System.out.println("=== " + nome + " conectado ao servidor em " + host + " ===");
            System.out.println("Aguardando o inicio das rodadas...");


            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            System.out.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
