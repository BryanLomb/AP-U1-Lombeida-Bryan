import java.io.Serializable;

public class Solicitud implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String tipo;
    private final String token;
    private final String idPuerta;

    public Solicitud(String tipo, String token, String idPuerta){
        this.tipo = tipo;
        this.token = token;
        this.idPuerta = idPuerta;
    }

    public String getTipo(){
        return tipo;
    }

    public String getToken(){
        return token;
    }

    public String getIdPuerta(){
        return idPuerta;
    }

}
