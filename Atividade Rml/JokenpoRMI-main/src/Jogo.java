import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Jogo extends Remote {

    void conectar(Jogador jogador, String nome) throws RemoteException;

    void jogar(String nomeJogador, String jogada) throws RemoteException;
}
