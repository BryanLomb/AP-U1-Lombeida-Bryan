import java.io.Serializable;

public class Respuesta implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private final String resultado;
    private final String motivo;
    private final int dentro;
    private final int aforo;

    public Respuesta (String resultado, String motivo, int dentro, int aforo){
        this.resultado = resultado;
        this.motivo = motivo;
        this.dentro = dentro;
        this.aforo = aforo;
    }

    public String getResultado(){
        return resultado;
    }

    public String getMotivo(){
        return motivo;
    }

    public int getDentro(){
        return dentro;
    }

    public int getAforo(){
        return aforo;
    }

}
