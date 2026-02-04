package com.jareth.npd.devo.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.jareth.npd.devo.model.Devo;
import com.jareth.npd.devo.repo.DevoRepo;

@Service
public class DevoService {

    @Autowired
    private DevoRepo repo;

    private final RestTemplate restTemplate = new RestTemplate();

    public Devo getDevoByDate(LocalDate date) {
        return repo.findByFecha(date).orElseGet(
                () -> {
                    Devo devo = getByExternalApi(date);
                    return repo.save(devo);
                });
    }

    private Devo getByExternalApi(LocalDate date) {
        // Formateamos la fecha como MM-dd-yyyy para la URL de la API
        String fechaUrl = String.format("%02d-%02d-%d",
                date.getMonthValue(), date.getDayOfMonth(), date.getYear());

        String url = "https://api.experience.odb.org/devotionals/v2?site_id=2&country=MX&on=" + fechaUrl;

        try {
            // 1. Consumimos la API (nos devuelve una lista de devocionales)
            Map<String, Object>[] response = restTemplate.getForObject(url, Map[].class);

            if (response != null && response.length > 0) {
                Map<String, Object> data = response[0]; // Tomamos el primero

                Devo devo = new Devo();
                devo.setDate(date);
                devo.setTitle((String) data.get("title"));
                devo.setVerse((String) data.get("verse"));
                devo.setAuthor((String) data.get("author_name"));
                devo.setThought((String) data.get("thought"));
                devo.setResponse((String) data.get("response"));
                devo.setPassage((String) data.get("passage_reference"));

                String bibleYearRaw = (String) data.get("bible_in_a_year_references");

                if (bibleYearRaw != null) {
                    devo.setBibleInYear(bibleYearRaw.replace("; ", "\n"));
                }

                devo.setContent(cleanContent((String) data.get("content")));
                return devo;
            }
        } catch (Exception e) {
            System.err.println("Error al obtener devocional externo: " + e.getMessage());
        }

        return null;
    }

    public String getDevoByPlatform(Devo devo, String plataforma) {
        // Valores por defecto para evitar nulos (equivalente al 'or' de Python)
        String date = (devo.getDate() != null) ? devo.getDate().toString() : "";
        String title = (devo.getTitle() != null) ? devo.getTitle() : "Sin título";
        String bibleInYear = (devo.getBibleInYear() != null && !devo.getBibleInYear().isEmpty()) ? devo.getBibleInYear()
                : "No disponible";
        String passage = (devo.getPassage() != null) ? devo.getPassage() : "No disponible";
        String biblicalText = getBibleText(devo.getPassage());
        String verse = (devo.getVerse() != null) ? devo.getVerse() : "No disponible";
        String content = (devo.getContent() != null) ? devo.getContent() : "";
        String author = (devo.getAuthor() != null) ? devo.getAuthor() : "Autor desconocido";
        String response = (devo.getResponse() != null) ? devo.getResponse() : "";
        String thought = (devo.getThought() != null) ? devo.getThought() : "";

        if ("whatsapp".equalsIgnoreCase(plataforma)) {
            return """
                    *NUESTRO PAN DIARIO*
                    %s
                    *%s*
                    〰〰〰〰〰〰〰
                    *La Biblia en un año:* %s
                    〰〰〰〰〰〰〰
                    *La escritura de hoy: %s NTV*
                    %s
                    〰〰〰〰〰〰〰
                    ```%s```
                    〰〰〰〰〰〰〰
                    %s

                    De: %s

                    Reflexiona y ora
                    _%s_

                    *%s*
                    """.formatted(date, title, bibleInYear, passage, biblicalText, verse, content, author, response,
                    thought);
        } else {
            // Formato para Facebook, Telegram o Web
            return """
                    NUESTRO PAN DIARIO
                    %s
                    %s
                    〰〰〰〰〰〰〰
                    La Biblia en un año:
                    %s
                    〰〰〰〰〰〰〰
                    La escritura de hoy: %s NTV
                    %s
                    〰〰〰〰〰〰〰
                    %s
                    〰〰〰〰〰〰〰
                    %s

                    De: %s

                    Reflexiona y ora
                    %s

                    %s
                    """.formatted(date, title, bibleInYear, passage, biblicalText, verse, content, author, response,
                    thought);
        }
    }

    private String cleanContent(String rawHtml) {
        Document doc = Jsoup.parse(rawHtml);

        Elements paragraph = doc.select("p");

        return paragraph.stream()
                .map(Element::text) // Extrae el texto de cada <p>
                .filter(t -> !t.isBlank()) // Filtra párrafos vacíos
                .collect(Collectors.joining("\n\n")); // Une con doble salto para mejor legibilidad

    }

    private String getBibleText(String pasaje) {
        try {
            // Separar por punto y coma (;)
            String[] parts = pasaje.split(";");
            StringBuilder result = new StringBuilder();

            String currentBook = "";
            String currentChapter = "";

            // RegEx para separar Libro de los números (ej: "Apocalipsis 7:9-10")
            Pattern pattern = Pattern.compile("^([A-Za-zÁÉÍÓÚÑáéíóúñ\\s]+)\\s+(\\d+[:\\d\\-\\,\\s]*)$");

            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty())
                    continue;

                String left;
                Matcher matcher = pattern.matcher(part);

                if (matcher.find()) {
                    currentBook = matcher.group(1).trim();
                    left = matcher.group(2).trim();
                } else {
                    left = part;
                }

                // Dividir subrangos por coma (,)
                String[] subPart = left.split(",");
                for (String sub : subPart) {
                    sub = sub.trim();
                    if (sub.isEmpty())
                        continue;

                    if (sub.contains(":")) {
                        currentChapter = sub.split(":")[0];
                    } else if (!currentChapter.isEmpty()) {
                        // Usar capítulo anterior si no hay ":"
                        sub = currentChapter + ":" + sub;
                    }

                    String req = (currentBook + " " + sub).trim();
                    // Llamamos a la función de scraping (Jsoup)
                    result.append(getPassage(req)).append("\n");
                }
            }
            return result.toString().trim();

        } catch (Exception e) {
            return "Error al procesar pasaje: " + e.getMessage();
        }
    }

    private String getPassage(String passage) {
        try {
            String url = "https://www.biblegateway.com/passage/?search=" +
                    passage.replace(" ", "+") + "&version=NTV";

            // Conectamos y obtenemos el HTML
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            Element container = doc.selectFirst(".passage-text");
            if (container == null)
                return "No se encontró el texto para " + passage;

            // Limpieza de elementos innecesarios (igual que tus .decompose() en Python)
            container.select(".chapternum, sup, .footnotes, .crossrefs, h3").remove();

            // Extraer texto y limpiar números de versículos con RegEx
            String texto = container.text();
            texto = texto.replaceAll("\\[\\w+\\]", ""); // Elimina [a], [b]
            texto = texto.replaceAll("\\b\\d+\\b", ""); // Elimina números de versículo

            return texto.replaceAll("\\s+", " ").trim();

        } catch (IOException e) {
            return "Error descargando " + passage + ": " + e.getMessage();
        }
    }

}
