package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=" + System.getProperty("API_KEY");

    private List<DadosSerie> dadosSeries = new ArrayList<>();

    private SerieRepository serieRepository;

    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository serieRepository) {
        this.serieRepository = serieRepository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar Séries
                    4 - Buscar série por titulo
                    5 - Buscar série por autor
                    6 - Top 5 Séries
                    7 - Buscar série por categoria
                    8 - Buscar séries por quantidade de temporadas
                    9 - Buscar séries por quantidade de temporadas e avaliação
                    10 - Buscar episódio por trecho
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorNumeroDeTemporada();
                    break;
                case 9:
                    buscarSeriesPorAvaliacaoENumeroDeTemporadas();
                    break;
                case 10:
                    buscarEpisodePorTrecho();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        // dadosSeries.add(dados);
        serieRepository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(dadosTemporada -> dadosTemporada.episodios().stream()
                            .map(e -> new Episodio(dadosTemporada.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            serieRepository.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        series = serieRepository.findAll();

        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serieBuscada = serieRepository.findByTituloContainingIgnoreCase(nomeSerie);
        if (serieBuscada.isPresent()) {
            System.out.println(serieBuscada.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Digite o nome do(a) ator(a) para busca: ");
        var nomeAtor = leitura.nextLine();

        System.out.println("Avaliações a partir de que valor? ");
        var avaliacaoMinima = leitura.nextDouble();

        List<Serie> seriesEncontradas = serieRepository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacaoMinima);

        System.out.println("Séries em que aparece o(a) ator(a) " + nomeAtor + ": ");
        seriesEncontradas.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> top5Series = serieRepository.findTop5ByOrderByAvaliacaoDesc();
        top5Series.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Digite a categoria para busca: ");
        var categoria = leitura.nextLine();

        Categoria categoriaBuscada = Categoria.fromStringPortugues(categoria);

        List<Serie> seriesPorCategoria = serieRepository.findByGenero(categoriaBuscada);
        System.out.println("Séries da categoria " + categoria + ": ");

        seriesPorCategoria.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getAvaliacao()));
    }

    private void buscarSeriesPorNumeroDeTemporada() {
        System.out.println("Digite a quantidade de temporadas máxima: ");
        var temporadasQt = leitura.nextInt();

        System.out.println("Digite a quantidade de séries máxima: ");
        var seriesQt = leitura.nextInt();

        List<Serie> seriesPorQtTemporada = serieRepository.findByTotalTemporadasLessThanEqual(temporadasQt, PageRequest.of(0, seriesQt, Sort.by(Sort.Direction.DESC, "totalTemporadas")));
        seriesPorQtTemporada.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getAvaliacao()));
    }

    private void buscarSeriesPorAvaliacaoENumeroDeTemporadas() {
        System.out.println("Digite a quantidade de temporadas máxima: ");
        var temporadasQt = leitura.nextInt();

        System.out.println("Digite a avaliação minima: ");
        var avaliacao = leitura.nextDouble();

        List<Serie> seriesPorQtTemporada = serieRepository.seriesPorTemporadaEAvaliacao(temporadasQt, avaliacao);

        seriesPorQtTemporada.forEach(s -> System.out.println(s.getTitulo() + " - " + s.getAvaliacao()));
    }

    private void buscarEpisodePorTrecho() {
        System.out.println("Digite o nome do episódio para busca: ");
        var trecho = leitura.nextLine();

        List<Episodio> episodiosEncontrados = serieRepository.episodiosPorTrecho(trecho);

        episodiosEncontrados.forEach(System.out::println);
    }
}