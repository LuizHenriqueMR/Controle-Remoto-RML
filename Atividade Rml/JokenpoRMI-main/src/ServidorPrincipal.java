import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorPrincipal {
    public static void main(String[] args) {
        try {
            ServidorJogo jogo = new ServidorJogo();

            Registry registro = LocateRegistry.createRegistry(1099);
            registro.rebind("Jogo", jogo);

            System.out.println("=== SERVIDOR - PEDRA, PAPEL, TESOURA, LAGARTO, SPOCK ===");
            System.out.println("Aguardando jogadores (minimo 2, maximo 5)...");
            System.out.println("A cada rodada: 5s para jogar, 10s de intervalo entre rodadas.\n");

            jogo.iniciarLacoDoJogo();

        } catch (Exception e) {
            System.out.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
