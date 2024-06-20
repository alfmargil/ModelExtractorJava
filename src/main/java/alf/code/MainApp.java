package alf.code;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MainApp {

    private WebDriver driver;
    private JTextField urlField;

    public MainApp() {
        // Configurar el sistema para encontrar el controlador Chrome
        System.setProperty("webdriver.chrome.driver", ".d/chromedriver");

        // Opciones de Chrome
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");

        // Inicializar el controlador Chrome
        driver = new ChromeDriver(options);

        // Crear la interfaz de usuario Swing
        JFrame frame = new JFrame("Extractor de Modelos");
        frame.setSize(600, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Extractor de modelos");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(titleLabel, BorderLayout.NORTH);

        // Panel para el campo de entrada de URL y botón
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new FlowLayout());

        JLabel urlLabel = new JLabel("URL:");
        inputPanel.add(urlLabel);

        urlField = new JTextField(40);
        inputPanel.add(urlField);

        JButton startButton = new JButton("Iniciar Automatización");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarAutomatizacion();
            }
        });
        inputPanel.add(startButton);

        panel.add(inputPanel, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void iniciarAutomatizacion() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Por favor ingresa una URL válida.");
            return;
        }

        try {
            // Navegar a la URL proporcionada
            driver.get(url);

            // Esperar hasta que el elemento h1 esté presente
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement h1Element = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
            String h1Text = h1Element.getText().trim();

            // Limpiar el texto del h1 para que sea un nombre de archivo válido
            String nombreArchivo = h1Text.replaceAll("[^A-Za-z0-9 ]+", "") + ".txt";

            // Esperar hasta que el desplegable de marcas esté presente
            WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-fits__brand")));

            // Obtener todas las opciones del desplegable
            Select select = new Select(selectElement);
            java.util.List<WebElement> options = select.getOptions();

            // Iterar sobre cada opción del desplegable (excepto la primera opción vacía)
            for (int i = 1; i < options.size(); i++) {
                WebElement option = options.get(i);
                String optionText = option.getText().trim();

                // Seleccionar la opción en el desplegable
                select.selectByIndex(i);

                // Esperar un tiempo suficiente para que se cargue el contenido dinámico
                Thread.sleep((long) (Math.random() * 3000) + 8000); // Simulación de tiempo de espera aleatorio

                // Extraer el contenido HTML de la página
                String contenidoHtml = driver.getPageSource();

                // Procesar el contenido HTML con Jsoup
                Document doc = Jsoup.parse(contenidoHtml);
                Elements elementos = doc.select(".product-fits__item[data-checkid]");

                // Lista para almacenar los valores de data-checkid con la marca
                List<String> dataCheckIdsConMarca = new ArrayList<>();

                for (Element elemento : elementos) {
                    String dataCheckIdValor = elemento.attr("data-checkid");
                    dataCheckIdsConMarca.add(optionText.trim() + " " + dataCheckIdValor);
                }

                // Escribir los datos en el archivo
                try (PrintWriter writer = new PrintWriter(new FileWriter(nombreArchivo, true))) {
                    for (String valor : dataCheckIdsConMarca) {
                        writer.println(valor);
                    }
                }
            }

            JOptionPane.showMessageDialog(null, "Extracción de datos finalizada. Datos guardados en " + nombreArchivo);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrió un error: " + e.getMessage());
        } finally {
            // Cerrar el navegador al finalizar
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainApp();
            }
        });
    }
}
