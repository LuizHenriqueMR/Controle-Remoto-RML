import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;


public interface Jogador extends Remote {


    void iniciarRodada(int numeroRodada, int segundosParaJogar) throws RemoteException;

    void resultadoRodada(String resumo) throws RemoteException;

    void atualizarPlacar(String placarFormatado) throws RemoteException;

    void receberMensagem(String mensagem) throws RemoteException;

    void notificarVitoria(List<Jogador> perdedores, String minhaJogada) throws RemoteException;

    String getNome() throws RemoteException;
}
