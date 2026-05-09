package org.example.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.App;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Carga FXML desde el classpath usando {@link InputStream} + {@link FXMLLoader#setLocation(URL)},
 * para que rutas relativas ({@code @styles/...}, {@code @images/...}) resuelvan bien incluso con espacios en la ruta del proyecto.
 */
public final class FxmlUtil {

    private FxmlUtil() {
    }

    /**
     * @param classpathAbsolute ruta absoluta desde la raíz del classpath, p. ej. {@code /org/example/ui/OrderDetailView.fxml}
     */
    public static Parent loadRoot(String classpathAbsolute) throws IOException {
        URL location = App.class.getResource(classpathAbsolute);
        Objects.requireNonNull(location,
                "No se encontró el recurso FXML (¿Compilar proyecto?). Ruta: " + classpathAbsolute);

        FXMLLoader loader = newLoader(location);

        try (InputStream in = App.class.getResourceAsStream(classpathAbsolute)) {
            Objects.requireNonNull(in,
                    "No se pudo abrir el FXML como stream: " + classpathAbsolute);
            return loader.load(in);
        }
    }

    /**
     * Igual que {@link #loadRoot(String)} pero permite obtener el controlador tras la carga.
     */
    public static FXMLLoader loadRootAndKeepLoader(String classpathAbsolute) throws IOException {
        URL location = App.class.getResource(classpathAbsolute);
        Objects.requireNonNull(location,
                "No se encontró el recurso FXML (¿Compilar proyecto?). Ruta: " + classpathAbsolute);

        FXMLLoader loader = newLoader(location);

        try (InputStream in = App.class.getResourceAsStream(classpathAbsolute)) {
            Objects.requireNonNull(in,
                    "No se pudo abrir el FXML como stream: " + classpathAbsolute);
            loader.load(in);
        }
        return loader;
    }

    /** Mensaje legible con la cadena de causas (hasta 6 niveles). */
    public static String causaCadena(Throwable e) {
        StringBuilder sb = new StringBuilder();
        Throwable t = e;
        int depth = 0;
        while (t != null && depth < 6) {
            if (depth > 0) {
                sb.append("\n\nCausa: ");
            }
            sb.append(t.getClass().getSimpleName()).append(": ");
            String msg = t.getMessage();
            sb.append(msg != null ? msg : "(sin mensaje)");
            t = t.getCause();
            depth++;
        }
        return sb.toString();
    }

    private static FXMLLoader newLoader(URL location) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(location);
        loader.setCharset(StandardCharsets.UTF_8);
        ClassLoader cl = App.class.getClassLoader();
        if (cl != null) {
            loader.setClassLoader(cl);
        }
        return loader;
    }
}
