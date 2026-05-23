package com.streaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SpringBootApplication
public class StreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamingApplication.class, args);
    }

    // =========================================================
    // ENUM
    // =========================================================

    enum Genero {
        ROCK,
        POP,
        JAZZ,
        ELECTRONICA,
        CLASICA
    }

    // =========================================================
    // MODELOS
    // =========================================================

    static class Productora {

        private String id;
        private String nombre;

        public Productora(String nombre) {
            this.id = UUID.randomUUID().toString();
            this.nombre = nombre;
        }

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }

    static class Artista {

        private String id;
        private String nombre;

        public Artista(String nombre) {
            this.id = UUID.randomUUID().toString();
            this.nombre = nombre;
        }

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }

    static class Album {

        private String id;
        private String titulo;
        private LocalDate fecha;
        private Productora productora;
        private List<Cancion> canciones;

        public Album(String titulo,
                     LocalDate fecha,
                     Productora productora) {

            this.id = UUID.randomUUID().toString();
            this.titulo = titulo;
            this.fecha = fecha;
            this.productora = productora;
            this.canciones = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public Productora getProductora() {
            return productora;
        }

        public List<Cancion> getCanciones() {
            return canciones;
        }

        public void agregarCancion(Cancion c) {
            canciones.add(c);
        }
    }

    static class Cancion {

        private String id;
        private String titulo;
        private Artista artista;
        private Album album;
        private Genero genero;
        private int duracionSegundos;
        private AtomicInteger reproducciones;
        private double rating;
        private LocalDate fechaLanzamiento;

        public Cancion(String titulo,
                        Artista artista,
                        Album album,
                        Genero genero,
                        int duracionSegundos,
                        double rating,
                        LocalDate fechaLanzamiento) {

            this.id = UUID.randomUUID().toString();
            this.titulo = titulo;
            this.artista = artista;
            this.album = album;
            this.genero = genero;
            this.duracionSegundos = duracionSegundos;
            this.reproducciones = new AtomicInteger(0);
            this.rating = rating;
            this.fechaLanzamiento = fechaLanzamiento;
        }

        public String getId() {
            return id;
        }

        public String getTitulo() {
            return titulo;
        }

        public Artista getArtista() {
            return artista;
        }

        public Album getAlbum() {
            return album;
        }

        public Genero getGenero() {
            return genero;
        }

        public int getDuracionSegundos() {
            return duracionSegundos;
        }

        public AtomicInteger getReproducciones() {
            return reproducciones;
        }

        public double getRating() {
            return rating;
        }

        public LocalDate getFechaLanzamiento() {
            return fechaLanzamiento;
        }

        public void reproducir() {
            reproducciones.incrementAndGet();
        }
    }

    // =========================================================
    // STRATEGY
    // =========================================================

    interface EstrategiaRecomendacion {
        List<Cancion> recomendar(List<Cancion> catalogo, Cancion base);
    }

    static class RecomendacionPorGenero
            implements EstrategiaRecomendacion {

        @Override
        public List<Cancion> recomendar(List<Cancion> catalogo,
                                        Cancion base) {

            return catalogo.stream()
                    .filter(c -> c.getGenero() == base.getGenero())
                    .sorted(
                            Comparator.comparing(Cancion::getRating)
                                    .reversed())
                    .toList();
        }
    }

    static class RecomendacionPorPopularidad
            implements EstrategiaRecomendacion {

        @Override
        public List<Cancion> recomendar(List<Cancion> catalogo,
                                        Cancion base) {

            return catalogo.stream()
                    .sorted((a, b) ->
                            Integer.compare(
                                    b.getReproducciones().get(),
                                    a.getReproducciones().get()))
                    .limit(5)
                    .toList();
        }
    }

    static class RecomendacionDescubrimiento
            implements EstrategiaRecomendacion {

        @Override
        public List<Cancion> recomendar(List<Cancion> catalogo,
                                        Cancion base) {

            return catalogo.stream()
                    .filter(c -> c.getReproducciones().get() < 1000)
                    .filter(c ->
                            c.getFechaLanzamiento()
                                    .isAfter(LocalDate.now().minusYears(2)))
                    .filter(c -> c.getGenero() != base.getGenero())
                    .toList();
        }
    }

    // =========================================================
    // REPOSITORY
    // =========================================================

    @Repository
    static class StreamingRepository {

        private List<Cancion> canciones = new ArrayList<>();
        private List<Album> albumes = new ArrayList<>();
        private List<Artista> artistas = new ArrayList<>();
        private List<Productora> productoras = new ArrayList<>();

        public StreamingRepository() {

            Productora sony = new Productora("Sony Music");

            Artista queen = new Artista("Queen");

            Album album = new Album(
                    "Greatest Hits",
                    LocalDate.of(1981, 10, 26),
                    sony);

            Cancion c1 = new Cancion(
                    "Bohemian Rhapsody",
                    queen,
                    album,
                    Genero.ROCK,
                    354,
                    4.9,
                    LocalDate.of(1975, 10, 31));

            Cancion c2 = new Cancion(
                    "Don't Stop Me Now",
                    queen,
                    album,
                    Genero.ROCK,
                    210,
                    4.8,
                    LocalDate.of(1978, 11, 10));

            c1.reproducir();
            c1.reproducir();
            c1.reproducir();

            c2.reproducir();

            canciones.add(c1);
            canciones.add(c2);

            album.agregarCancion(c1);
            album.agregarCancion(c2);

            artistas.add(queen);
            albumes.add(album);
            productoras.add(sony);
        }

        public List<Cancion> getCanciones() {
            return canciones;
        }

        public List<Album> getAlbumes() {
            return albumes;
        }

        public List<Artista> getArtistas() {
            return artistas;
        }

        public List<Productora> getProductoras() {
            return productoras;
        }
    }

    // =========================================================
    // SERVICE
    // =========================================================

    @Service
    static class CancionService {

        private final StreamingRepository repository;

        public CancionService(StreamingRepository repository) {
            this.repository = repository;
        }

        // =====================================================
        // LISTAR
        // =====================================================

        public List<Cancion> listarTodas() {
            return repository.getCanciones();
        }

        // =====================================================
        // BUSCAR POR ID
        // =====================================================

        public Optional<Cancion> buscarPorId(String id) {

            return repository.getCanciones()
                    .stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst();
        }

        // =====================================================
        // BUSQUEDA FILTRADA
        // =====================================================

        public List<Cancion> buscar(String titulo,
                                    String artista) {

            return repository.getCanciones()
                    .stream()
                    .filter(c ->
                            (titulo == null ||
                                    c.getTitulo()
                                            .toLowerCase()
                                            .contains(titulo.toLowerCase()))
                                    &&
                                    (artista == null ||
                                            c.getArtista()
                                                    .getNombre()
                                                    .toLowerCase()
                                                    .contains(artista.toLowerCase()))
                    )
                    .toList();
        }

        // =====================================================
        // FILTRO COMPUESTO
        // =====================================================

        public List<Cancion> filtroCompuesto(
                Genero genero,
                int anioDesde,
                double ratingMinimo) {

            return repository.getCanciones()
                    .stream()
                    .filter(c -> c.getGenero() == genero)
                    .filter(c ->
                            c.getFechaLanzamiento()
                                    .getYear() >= anioDesde)
                    .filter(c -> c.getRating() >= ratingMinimo)
                    .toList();
        }

        // =====================================================
        // TOP 10
        // =====================================================

        public List<Cancion> top10() {

            return repository.getCanciones()
                    .stream()
                    .sorted((a, b) ->
                            Integer.compare(
                                    b.getReproducciones().get(),
                                    a.getReproducciones().get()))
                    .limit(10)
                    .toList();
        }

        // =====================================================
        // ORDENAMIENTO
        // =====================================================

        public List<Cancion> ordenar() {

            return repository.getCanciones()
                    .stream()
                    .sorted(
                            Comparator.comparing(
                                            (Cancion c) ->
                                                    c.getArtista().getNombre())
                                    .thenComparing(
                                            Cancion::getFechaLanzamiento)
                                    .reversed())
                    .toList();
        }

        // =====================================================
        // REPRODUCIR
        // =====================================================

        public void reproducir(String id) {

            buscarPorId(id)
                    .ifPresent(Cancion::reproducir);
        }

        // =====================================================
        // PROMEDIO DURACION POR GENERO
        // =====================================================

        public Map<Genero, Double> promedioPorGenero() {

            return repository.getCanciones()
                    .stream()
                    .collect(Collectors.groupingBy(
                            Cancion::getGenero,
                            Collectors.averagingInt(
                                    Cancion::getDuracionSegundos)
                    ));
        }

        // =====================================================
        // ARTISTA MAS POPULAR
        // =====================================================

        public Optional<String> artistaMasPopular() {

            return repository.getCanciones()
                    .stream()
                    .collect(Collectors.groupingBy(
                            c -> c.getArtista().getNombre(),
                            Collectors.summingInt(
                                    c -> c.getReproducciones().get())
                    ))
                    .entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);
        }

        // =====================================================
        // DISTRIBUCION POR DECADAS
        // =====================================================

        public Map<Integer, List<Cancion>> distribucionDecadas() {

            return repository.getCanciones()
                    .stream()
                    .collect(Collectors.groupingBy(
                            c ->
                                    (c.getFechaLanzamiento().getYear() / 10) * 10
                    ));
        }

        // =====================================================
        // BUSQUEDA BINARIA
        // =====================================================

        public Cancion busquedaBinaria(String titulo) {

            List<Cancion> ordenadas = repository.getCanciones()
                    .stream()
                    .sorted(Comparator.comparing(Cancion::getTitulo))
                    .toList();

            int izquierda = 0;
            int derecha = ordenadas.size() - 1;

            while (izquierda <= derecha) {

                int medio = (izquierda + derecha) / 2;

                Cancion actual = ordenadas.get(medio);

                int comparacion =
                        actual.getTitulo()
                                .compareToIgnoreCase(titulo);

                if (comparacion == 0) {
                    return actual;
                }

                if (comparacion < 0) {
                    izquierda = medio + 1;
                } else {
                    derecha = medio - 1;
                }
            }

            return null;
        }
    }

    // =========================================================
    // CONTROLLER
    // =========================================================

    @RestController
    @RequestMapping("/api/canciones")
    static class CancionController {

        private final CancionService service;

        public CancionController(CancionService service) {
            this.service = service;
        }

        @GetMapping
        public ResponseEntity<List<Cancion>> listarTodas() {

            return ResponseEntity.ok(
                    service.listarTodas());
        }

        @GetMapping("/{id}")
        public ResponseEntity<Cancion> buscarPorId(
                @PathVariable String id) {

            return service.buscarPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        }

        @GetMapping("/buscar")
        public ResponseEntity<List<Cancion>> buscar(
                @RequestParam(required = false) String titulo,
                @RequestParam(required = false) String artista) {

            return ResponseEntity.ok(
                    service.buscar(titulo, artista));
        }

        @PostMapping("/{id}/reproducir")
        public ResponseEntity<String> reproducir(
                @PathVariable String id) {

            service.reproducir(id);

            return ResponseEntity.ok(
                    "Reproduccion incrementada");
        }

        @GetMapping("/top10")
        public ResponseEntity<List<Cancion>> top10() {

            return ResponseEntity.ok(
                    service.top10());
        }

        @GetMapping("/ordenadas")
        public ResponseEntity<List<Cancion>> ordenadas() {

            return ResponseEntity.ok(
                    service.ordenar());
        }

        @GetMapping("/estadisticas/promedio-genero")
        public ResponseEntity<Map<Genero, Double>>
        promedioGenero() {

            return ResponseEntity.ok(
                    service.promedioPorGenero());
        }

        @GetMapping("/estadisticas/artista-popular")
        public ResponseEntity<String>
        artistaPopular() {

            return ResponseEntity.ok(
                    service.artistaMasPopular().orElse("Sin datos"));
        }

        @GetMapping("/estadisticas/decadas")
        public ResponseEntity<Map<Integer, List<Cancion>>>
        decadas() {

            return ResponseEntity.ok(
                    service.distribucionDecadas());
        }

        @GetMapping("/binaria")
        public ResponseEntity<Cancion>
        busquedaBinaria(@RequestParam String titulo) {

            Cancion c =
                    service.busquedaBinaria(titulo);

            if (c == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(c);
        }
    }
}