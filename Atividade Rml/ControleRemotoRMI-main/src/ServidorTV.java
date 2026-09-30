import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class ServidorTV {
    public static void main(String[] args) {
        try {
            ControleRemotoTV tv = new TelevisaoImpl();

            Registry registro = LocateRegistry.createRegistry(1099);
            registro.rebind("TV", tv);

            System.out.println("=== SERVIDOR DA TV ===");
            System.out.println("TV registrada e pronta para receber comandos.");
            System.out.println("Aguardando o controle remoto...");

        } catch (Exception e) {
            System.out.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
