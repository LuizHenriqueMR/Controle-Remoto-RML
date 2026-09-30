import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServidorJogo extends UnicastRemoteObject implements Jogo {

    private static final int SEGUNDOS_PARA_JOGAR = 5;
    private static final int SEGUNDOS_ENTRE_RODADAS = 10;
    private static final int MIN_JOGADORES = 2;
    private static final int MAX_JOGADORES = 5;

    private static final Map<String, List<String>> REGRAS = new HashMap<>();
    static {
        REGRAS.put("tesoura", Arrays.asList("papel", "lagarto"));
        REGRAS.put("papel", Arrays.asList("pedra", "spock"));
        REGRAS.put("pedra", Arrays.asList("lagarto", "tesoura"));
        REGRAS.put("lagarto", Arrays.asList("spock", "papel"));
        REGRAS.put("spock", Arrays.asList("tesoura", "pedra"));
    }

    private final Map<String, Jogador> jogadores = new ConcurrentHashMap<>();
    private final Map<String, String> jogadasRodadaAtual = new ConcurrentHashMap<>();
    private final Map<String, Integer> placar = new ConcurrentHashMap<>();
    private int empatesTotal = 0;
    private int numeroRodada = 0;

    public ServidorJogo() throws RemoteException {
        super();
    }

    @Override
    public synchronized void conectar(Jogador jogador, String nome) throws RemoteException {
        if (jogadores.size() >= MAX_JOGADORES) {
            jogador.receberMensagem("Servidor cheio (maximo " + MAX_JOGADORES + " jogadores). Tente mais tarde.");
            return;
        }
        jogadores.put(nome, jogador);
        placar.putIfAbsent(nome, 0);
        System.out.println("[SERVIDOR] " + nome + " conectou-se. Total de jogadores: " + jogadores.size());
        broadcast("Jogador '" + nome + "' entrou na partida! (" + jogadores.size() + " jogadores conectados)");
    }

    @Override
    public void jogar(String nomeJogador, String jogada) throws RemoteException {
        String normalizada = jogada.trim().toLowerCase();
        if (!REGRAS.containsKey(normalizada)) {
            Jogador j = jogadores.get(nomeJogador);
            if (j != null) j.receberMensagem("Jogada invalida: " + jogada);
            return;
        }
        jogadasRodadaAtual.put(nomeJogador, normalizada);
        System.out.println("[SERVIDOR] " + nomeJogador + " jogou: " + normalizada);
    }

    private void broadcast(String mensagem) {
        for (Map.Entry<String, Jogador> entry : jogadores.entrySet()) {
            try {
                entry.getValue().receberMensagem(mensagem);
            } catch (RemoteException e) {
                System.out.println("[SERVIDOR] Falha ao notificar " + entry.getKey() + ": " + e.getMessage());
            }
        }
    }

    public void iniciarLacoDoJogo() {
        new Thread(() -> {
            while (true) {
                if (jogadores.size() < MIN_JOGADORES) {

                    aguardar(2000);
                    continue;
                }

                jogarRodada();

                aguardar(SEGUNDOS_ENTRE_RODADAS * 1000L);
            }
        }).start();
    }

    private void jogarRodada() {
        numeroRodada++;
        jogadasRodadaAtual.clear();

        System.out.println("\n===== RODADA " + numeroRodada + " =====");

        for (Map.Entry<String, Jogador> entry : jogadores.entrySet()) {
            try {
                entry.getValue().iniciarRodada(numeroRodada, SEGUNDOS_PARA_JOGAR);
            } catch (RemoteException e) {
                System.out.println("[SERVIDOR] Falha ao iniciar rodada para " + entry.getKey());
            }
        }

        aguardar(SEGUNDOS_PARA_JOGAR * 1000L);

        Set<String> participantesDaRodada = new HashSet<>(jogadores.keySet());
        Set<String> jogaram = new HashSet<>(jogadasRodadaAtual.keySet());
        Set<String> desclassificados = new HashSet<>(participantesDaRodada);
        desclassificados.removeAll(jogaram);

        for (String nome : desclassificados) {
            Jogador j = jogadores.get(nome);
            if (j != null) {
                try {
                    j.receberMensagem("Voce nao jogou a tempo e foi desclassificado desta rodada.");
                } catch (RemoteException ignored) { }
            }
            System.out.println("[SERVIDOR] " + nome + " desclassificado na rodada " + numeroRodada + " (nao jogou a tempo).");
        }

        if (jogaram.size() < 2) {
            String resumo = "Rodada " + numeroRodada + ": jogadores insuficientes jogaram a tempo. Rodada anulada.";
            System.out.println("[SERVIDOR] " + resumo);
            broadcast(resumo);
            transmitirPlacar();
            return;
        }

        List<String> candidatos = new ArrayList<>(jogaram);
        List<String> vencedores = new ArrayList<>();

        for (String nome : candidatos) {
            String minhaJogada = jogadasRodadaAtual.get(nome);
            boolean perdeuDeAlguem = false;
            for (String outro : candidatos) {
                if (outro.equals(nome)) continue;
                String jogadaOutro = jogadasRodadaAtual.get(outro);

                if (REGRAS.get(jogadaOutro).contains(minhaJogada)) {
                    perdeuDeAlguem = true;
                    break;
                }
            }
            if (!perdeuDeAlguem) {
                vencedores.add(nome);
            }
        }

        StringBuilder jogadas = new StringBuilder();
        for (String nome : candidatos) {
            jogadas.append(nome).append("=").append(jogadasRodadaAtual.get(nome)).append("  ");
        }

        boolean todosJogaramIgual = candidatos.stream()
                .map(jogadasRodadaAtual::get)
                .distinct()
                .count() == 1;

        if (vencedores.size() == 1 && !todosJogaramIgual) {
            String vencedor = vencedores.get(0);
            String jogadaVencedora = jogadasRodadaAtual.get(vencedor);

            placar.merge(vencedor, 1, Integer::sum);

            String resumo = "Rodada " + numeroRodada + " - Jogadas: " + jogadas
                    + " | VENCEDOR: " + vencedor + " (jogou " + jogadaVencedora + ")";
            System.out.println("[SERVIDOR] " + resumo);
            broadcast(resumo);

            List<Jogador> perdedoresStubs = new ArrayList<>();
            for (String nome : candidatos) {
                if (!nome.equals(vencedor)) {
                    perdedoresStubs.add(jogadores.get(nome));
                }
            }
            try {
                jogadores.get(vencedor).notificarVitoria(perdedoresStubs, jogadaVencedora);
            } catch (RemoteException e) {
                System.out.println("[SERVIDOR] Falha ao notificar vitoria de " + vencedor);
            }

        } else {

            empatesTotal++;
            String resumo = "Rodada " + numeroRodada + " - Jogadas: " + jogadas + " | EMPATE!";
            System.out.println("[SERVIDOR] " + resumo);
            broadcast(resumo);
        }

        transmitirPlacar();
    }

    private void transmitirPlacar() {
        StringBuilder sb = new StringBuilder("=== PLACAR ===  ");
        for (Map.Entry<String, Integer> entry : placar.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" pts   ");
        }
        sb.append("| Empates: ").append(empatesTotal);

        String placarFormatado = sb.toString();
        System.out.println("[SERVIDOR] " + placarFormatado);

        for (Jogador j : jogadores.values()) {
            try {
                j.atualizarPlacar(placarFormatado);
            } catch (RemoteException ignored) { }
        }
    }

    private void aguardar(long milissegundos) {
        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException ignored) {
        }
    }
}
