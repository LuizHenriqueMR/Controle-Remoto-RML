import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ControleRemotoTV extends Remote {

    void ligar() throws RemoteException;

    void desligar() throws RemoteException;

    void aumentarVolume() throws RemoteException;

    void diminuirVolume() throws RemoteException;

    void mudarCanal(int canal) throws RemoteException;

    String obterStatus() throws RemoteException;
}
