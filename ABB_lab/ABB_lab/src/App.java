import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    static String nomeArquivoDados;

    static Scanner teclado;

    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;

    static ABB<Integer, Produto> produtosCadastradosPorId;

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }

    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {

        T valor;

        System.out.println(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }

    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar produto, por nome");
        System.out.println("3 - Procurar produto, por id");
        System.out.println("4 - Remover produto, por nome");
        System.out.println("5 - Remover produto, por id");
        System.out.println("6 - Recortar a lista de produtos, por nome");
        System.out.println("7 - Recortar a lista de produtos, por id");
        System.out.println("0 - Finalizar");

        return lerOpcao("Digite sua opção: ", Integer.class);
    }

    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {

        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        ABB<K, Produto> produtosCadastrados;

        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));

            numProdutos = Integer.parseInt(arquivo.nextLine());
            produtosCadastrados = new ABB<K, Produto>();

            for (int i = 0; i < numProdutos; i++) {
                linha = arquivo.nextLine();
                produto = Produto.criarDoTexto(linha);
                K chave = extratorDeChave.apply(produto);
                produtosCadastrados.inserir(chave, produto);
            }
            quantosProdutos = numProdutos;

        } catch (IOException excecaoArquivo) {
            produtosCadastrados = null;
        } finally {
            arquivo.close();
        }

        return produtosCadastrados;
    }

    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {

        Produto produto;

        try {
            produto = produtosCadastrados.pesquisar(procurado);
        } catch (NoSuchElementException excecao) {
            produto = null;
        }

        System.out.println("Número de comparações realizadas: " + produtosCadastrados.getComparacoes());
        System.out.println(String.format("Tempo de execução da busca: %.4f ms", produtosCadastrados.getTempo()));

        return produto;
    }

    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {

        Integer idProduto;

        cabecalho();
        System.out.println("LOCALIZAR PRODUTO, POR ID");
        idProduto = lerOpcao("Digite o id do produto: ", Integer.class);

        if (idProduto == null) {
            System.out.println("Id inválido. O id deve ser um número inteiro.");
            return null;
        }

        return localizarProduto(produtosCadastrados, idProduto);
    }

    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {

        String nomeProduto;

        cabecalho();
        System.out.println("LOCALIZAR PRODUTO, POR NOME");
        System.out.println("Digite o nome do produto: ");
        nomeProduto = teclado.nextLine().trim().toLowerCase();

        return localizarProduto(produtosCadastrados, nomeProduto);
    }

    private static void mostrarProduto(Produto produto) {

        cabecalho();
        StringBuilder mensagem = new StringBuilder("Produto não encontrado.\n");

        if (produto != null) {
            mensagem = new StringBuilder(String.format("%s\n", produto));
        }

        System.out.println(mensagem.toString());
    }

    static <K> void listarTodosOsProdutos(ABB<K, Produto> produtosCadastrados) {

        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        System.out.println(produtosCadastrados.toString());
    }

    static Produto removerProdutoId(ABB<Integer, Produto> produtosCadastrados) {

        Integer idProduto;
        Produto removido;

        cabecalho();
        System.out.println("REMOVER PRODUTO, POR ID");
        idProduto = lerOpcao("Digite o id do produto a ser removido: ", Integer.class);

        if (idProduto == null) {
            System.out.println("Id inválido. O id deve ser um número inteiro.");
            return null;
        }

        removido = removerProduto(produtosCadastrados, idProduto);

        if (removido != null) {
            removerProduto(produtosCadastradosPorNome, removido.descricao.toLowerCase());
            quantosProdutos--;
            System.out.println("Produto removido com sucesso.");
        }

        return removido;
    }

    static Produto removerProdutoNome(ABB<String, Produto> produtosCadastrados) {

        String nomeProduto;
        Produto removido;

        cabecalho();
        System.out.println("REMOVER PRODUTO, POR NOME");
        System.out.println("Digite o nome do produto a ser removido: ");
        nomeProduto = teclado.nextLine().trim().toLowerCase();

        removido = removerProduto(produtosCadastrados, nomeProduto);

        if (removido != null) {
            removerProduto(produtosCadastradosPorId, removido.idProduto);
            quantosProdutos--;
            System.out.println("Produto removido com sucesso.");
        }

        return removido;
    }

    static <K> Produto removerProduto(ABB<K, Produto> produtosCadastrados, K chave) {

        Produto removido;

        try {
            removido = produtosCadastrados.remover(chave);
        } catch (NoSuchElementException excecao) {
            removido = null;
        }

        return removido;
    }

    private static <K> void recortarProduto(ABB<K, Produto> produtosCadastrados, K deOnde, K ateOnde) {

        Lista<Produto> recorte = produtosCadastrados.recortar(deOnde, ateOnde);

        System.out.println("\nPRODUTOS ENTRE \"" + deOnde + "\" E \"" + ateOnde + "\":");
        System.out.println(recorte);
    }

    private static void recortarProdutosNome(ABB<String, Produto> produtosCadastrados) {

        String nomeInicio, nomeFinal;

        cabecalho();
        System.out.println("RECORTE DE PRODUTOS, POR NOME");
        System.out.println("Digite o nome inicial do recorte: ");
        nomeInicio = teclado.nextLine().trim().toLowerCase();
        System.out.println("Digite o nome final do recorte: ");
        nomeFinal = teclado.nextLine().trim().toLowerCase();

        recortarProduto(produtosCadastrados, nomeInicio, nomeFinal);
    }

    private static void recortarProdutosId(ABB<Integer, Produto> produtosCadastrados) {

        Integer idInicio, idFinal;

        cabecalho();
        System.out.println("RECORTE DE PRODUTOS, POR ID");
        idInicio = lerOpcao("Digite o id inicial do recorte: ", Integer.class);
        idFinal = lerOpcao("Digite o id final do recorte: ", Integer.class);

        if ((idInicio == null) || (idFinal == null)) {
            System.out.println("Id inválido. O id deve ser um número inteiro.");
        } else {
            recortarProduto(produtosCadastrados, idInicio, idFinal);
        }
    }

    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao.toLowerCase()));
        produtosCadastradosPorId = new ABB<Integer, Produto>(produtosCadastradosPorNome, (p -> p.idProduto));

        int opcao = -1;

        do {
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos(produtosCadastradosPorNome);
                case 2 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 3 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
                case 4 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
                case 5 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
                case 6 -> recortarProdutosNome(produtosCadastradosPorNome);
                case 7 -> recortarProdutosId(produtosCadastradosPorId);
                case 0 -> System.out.println("FLW VLW OBG VLT SMP.");
            }
            pausa();
        } while (opcao != 0);

        teclado.close();
    }
}