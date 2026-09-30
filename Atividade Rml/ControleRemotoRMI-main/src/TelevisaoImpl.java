import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class TelevisaoImpl extends UnicastRemoteObject implements ControleRemotoTV {

    private boolean ligada = false;
    private int volume = 10;
    private int canal = 2;

    public TelevisaoImpl() throws RemoteException {
        super();
    }

    @Override
    public synchronized void ligar() throws RemoteException {
        ligada = true;
        System.out.println("[TV] Ligada. Canal " + canal + ", volume " + volume + ".");
    }

    @Override
    public synchronized void desligar() throws RemoteException {
        ligada = false;
        System.out.println("[TV] Desligada.");
    }

    @Override
    public synchronized void aumentarVolume() throws RemoteException {
        if (!ligada) {
            System.out.println("[TV] Comando ignorado: TV esta desligada.");
            return;
        }
        if (volume < 100) volume += 5;
        System.out.println("[TV] Volume aumentado para " + volume + ".");
    }

    @Override
    public synchronized void diminuirVolume() throws RemoteException {
        if (!ligada) {
            System.out.println("[TV] Comando ignorado: TV esta desligada.");
            return;
        }
        if (volume > 0) volume -= 5;
        System.out.println("[TV] Volume diminuido para " + volume + ".");
    }

    @Override
    public synchronized void mudarCanal(int novoCanal) throws RemoteException {
        if (!ligada) {
            System.out.println("[TV] Comando ignorado: TV esta desligada.");
            return;
        }
        canal = novoCanal;
        System.out.println("[TV] Canal alterado para " + canal + ".");
    }

    @Override
    public synchronized String obterStatus() throws RemoteException {
        String status = ligada
                ? String.format("TV LIGADA - Canal: %d | Volume: %d", canal, volume)
                : "TV DESLIGADA";
        System.out.println("[TV] Status consultado: " + status);
        return status;
    }
}
