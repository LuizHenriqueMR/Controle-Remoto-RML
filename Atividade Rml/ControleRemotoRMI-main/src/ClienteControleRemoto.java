import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class ClienteControleRemoto {
    public static void main(String[] args) {

        String host = (args.length >= 1) ? args[0] : "localhost";

        try {
            Registry registro = LocateRegistry.getRegistry(host, 1099);
            ControleRemotoTV tv = (ControleRemotoTV) registro.lookup("TV");

            Scanner teclado = new Scanner(System.in);
            int opcao = -1;

            System.out.println("=== CONTROLE REMOTO (conectado a " + host + ") ===");

            while (opcao != 0) {
                System.out.println("\n1 - Ligar");
                System.out.println("2 - Desligar");
                System.out.println("3 - Aumentar volume");
                System.out.println("4 - Diminuir volume");
                System.out.println("5 - Mudar canal");
                System.out.println("6 - Ver status");
                System.out.println("0 - Sair");
                System.out.print("Escolha: ");

                if (!teclado.hasNextInt()) {
                    teclado.next();
                    continue;
                }
                opcao = teclado.nextInt();

                switch (opcao) {
                    case 1:
                        tv.ligar();
                        break;
                    case 2:
                        tv.desligar();
                        break;
                    case 3:
                        tv.aumentarVolume();
                        break;
                    case 4:
                        tv.diminuirVolume();
                        break;
                    case 5:
                        System.out.print("Novo canal: ");
                        int canal = teclado.nextInt();
                        tv.mudarCanal(canal);
                        break;
                    case 6:
                        System.out.println("Status remoto: " + tv.obterStatus());
                        break;
                    case 0:
                        System.out.println("Encerrando o controle remoto.");
                        break;
                    default:
                        System.out.println("Opcao invalida.");
                }
            }

        } catch (Exception e) {
            System.out.println("Erro no controle remoto: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
