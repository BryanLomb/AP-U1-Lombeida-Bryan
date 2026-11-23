import java.rmi.server.UnicastRemoteObject;
import java.time.format.DateTimeFormatter;

public class CoordinadorServicio extends UnicastRemoteObject implements ICoordinadorRemoto {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

}
