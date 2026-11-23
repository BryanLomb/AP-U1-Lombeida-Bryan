import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ICoordinadorRemoto extends Remote{

    Respuesta procesar(Solicitud solicitud) throws RemoteException;

}