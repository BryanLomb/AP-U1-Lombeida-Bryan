import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Configuracion {

    private final Properties props = new Properties();

    public Configuracion(String ruta) throws IOException{
        try (InputStream entrada = Files.newInputStream(Path.of(ruta))){
            props.load(entrada);
        }
    }

    public String requerido (String clave){

        String valor = props.getProperty(clave);

        if(valor == null || valor.isBlank()){
            throw new IllegalArgumentException("Falta la propiedad: " + clave);
        }

        return valor.trim();
    }

    private int entero(String clave, int minimo, int maximo){
        int valor = Integer.parseInt(requerido(clave));

        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException("Valor fuera del rango: " + clave);
        }

        return valor;

    }

    public String host(){
        return requerido("coordinador.host");
    }

    public int puertoRegistro() {
        return entero("puerto.registro",1,65535);
    }

    public int puertaServicio(){
        return entero("puerto.servicio",1,65535);
    }

    public int aforo(){
        return entero("aforo.maximo", 1, Integer.MAX_VALUE);
    }

    public String token(){
        return requerido("token.acceso");
    }
}
