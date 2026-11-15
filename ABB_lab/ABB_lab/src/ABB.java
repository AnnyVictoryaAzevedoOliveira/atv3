import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ABB<K, V> implements IMapeamento<K, V> {

	private No<K, V> raiz;
	private Comparator<K> comparador;
	private int tamanho;
	private long comparacoes;
	private long inicio;
	private long termino;

	private void init(Comparator<K> comparador) {
		this.raiz = null;
		this.tamanho = 0;
		this.comparador = comparador;
	}

	@SuppressWarnings("unchecked")
	public ABB() {
		init((Comparator<K>) Comparator.naturalOrder());
	}

	public ABB(Comparator<K> comparador) {
		init(comparador);
	}

	@SuppressWarnings("unchecked")
	public ABB(ABB<?, V> original, Function<V, K> funcaoChave) {
		init((Comparator<K>) Comparator.naturalOrder());
		copiarArvore(original.raiz, funcaoChave);
	}

	private void copiarArvore(No<?, V> raizArvore, Function<V, K> funcaoChave) {

		if (raizArvore != null) {
			copiarArvore(raizArvore.getEsquerda(), funcaoChave);
			V item = raizArvore.getItem();
			inserir(funcaoChave.apply(item), item);
			copiarArvore(raizArvore.getDireita(), funcaoChave);
		}
	}

	public boolean vazia() {
		return (this.raiz == null);
	}

	@Override
	public V pesquisar(K chave) {

		V procurado;

		comparacoes = 0;
		inicio = System.nanoTime();
		procurado = pesquisar(this.raiz, chave);
		termino = System.nanoTime();

		if (procurado == null)
			throw new NoSuchElementException("O item não foi localizado na árvore!");

		return procurado;
	}

	private V pesquisar(No<K, V> raizArvore, K procurado) {

		int comparacao;

		if (raizArvore == null)
			return null;

		comparacoes++;
		comparacao = comparador.compare(procurado, raizArvore.getChave());

		if (comparacao == 0)
			return raizArvore.getItem();
		else if (comparacao < 0)
			return pesquisar(raizArvore.getEsquerda(), procurado);
		else
			return pesquisar(raizArvore.getDireita(), procurado);
	}

	@Override
	public int inserir(K chave, V item) {

		comparacoes = 0;
		inicio = System.nanoTime();
		this.raiz = inserir(this.raiz, chave, item);
		termino = System.nanoTime();

		return tamanho;
	}

	private No<K, V> inserir(No<K, V> raizArvore, K chave, V item) {

		int comparacao;

		if (raizArvore == null) {
			tamanho++;
			return new No<>(chave, item);
		}

		comparacoes++;
		comparacao = comparador.compare(chave, raizArvore.getChave());

		if (comparacao < 0)
			raizArvore.setEsquerda(inserir(raizArvore.getEsquerda(), chave, item));
		else if (comparacao > 0)
			raizArvore.setDireita(inserir(raizArvore.getDireita(), chave, item));
		else
			throw new IllegalArgumentException("O item já foi inserido anteriormente na árvore.");

		return raizArvore;
	}

	@Override
	public V remover(K chave) {

		V removido;

		comparacoes = 0;
		inicio = System.nanoTime();

		removido = pesquisar(this.raiz, chave);
		if (removido == null) {
			termino = System.nanoTime();
			throw new NoSuchElementException("O item a ser removido não foi localizado na árvore!");
		}

		this.raiz = remover(this.raiz, chave);
		tamanho--;
		termino = System.nanoTime();

		return removido;
	}

	private No<K, V> remover(No<K, V> raizArvore, K chaveRemover) {

		int comparacao;

		if (raizArvore == null)
			throw new NoSuchElementException("O item a ser removido não foi localizado na árvore!");

		comparacoes++;
		comparacao = comparador.compare(chaveRemover, raizArvore.getChave());

		if (comparacao == 0) {
			if (raizArvore.getDireita() == null)
				raizArvore = raizArvore.getEsquerda();
			else if (raizArvore.getEsquerda() == null)
				raizArvore = raizArvore.getDireita();
			else
				raizArvore.setEsquerda(removerNoAntecessor(raizArvore, raizArvore.getEsquerda()));
		} else if (comparacao < 0)
			raizArvore.setEsquerda(remover(raizArvore.getEsquerda(), chaveRemover));
		else
			raizArvore.setDireita(remover(raizArvore.getDireita(), chaveRemover));

		return raizArvore;
	}

	private No<K, V> removerNoAntecessor(No<K, V> itemRetirar, No<K, V> raizArvore) {

		if (raizArvore.getDireita() != null)
			raizArvore.setDireita(removerNoAntecessor(itemRetirar, raizArvore.getDireita()));
		else {
			itemRetirar.setChave(raizArvore.getChave());
			itemRetirar.setItem(raizArvore.getItem());
			raizArvore = raizArvore.getEsquerda();
		}

		return raizArvore;
	}

	@Override
	public int tamanho() {
		return tamanho;
	}

	@Override
	public String percorrer() {
		return caminhamentoEmOrdem();
	}

	public String caminhamentoEmOrdem() {

		StringBuilder resultado = new StringBuilder();

		if (vazia())
			return "A árvore está vazia!";

		caminhamentoEmOrdem(this.raiz, resultado);
		return resultado.toString();
	}

	private void caminhamentoEmOrdem(No<K, V> raizArvore, StringBuilder resultado) {

		if (raizArvore != null) {
			caminhamentoEmOrdem(raizArvore.getEsquerda(), resultado);
			resultado.append(raizArvore.getItem()).append("\n");
			caminhamentoEmOrdem(raizArvore.getDireita(), resultado);
		}
	}

	public Lista<V> recortar(K chaveInicio, K chaveFinal) {

		Lista<V> recorte = new Lista<>();

		if (comparador.compare(chaveInicio, chaveFinal) > 0) {
			K aux = chaveInicio;
			chaveInicio = chaveFinal;
			chaveFinal = aux;
		}

		recortar(this.raiz, chaveInicio, chaveFinal, recorte);
		return recorte;
	}

	private void recortar(No<K, V> raizArvore, K chaveInicio, K chaveFinal, Lista<V> recorte) {

		int comparacaoInicio, comparacaoFinal;

		if (raizArvore != null) {
			comparacaoInicio = comparador.compare(chaveInicio, raizArvore.getChave());
			comparacaoFinal = comparador.compare(chaveFinal, raizArvore.getChave());

			if (comparacaoInicio < 0)
				recortar(raizArvore.getEsquerda(), chaveInicio, chaveFinal, recorte);

			if ((comparacaoInicio <= 0) && (comparacaoFinal >= 0))
				recorte.inserir(raizArvore.getItem());

			if (comparacaoFinal > 0)
				recortar(raizArvore.getDireita(), chaveInicio, chaveFinal, recorte);
		}
	}

	@Override
	public String toString() {
		return percorrer();
	}

	@Override
	public long getComparacoes() {
		return comparacoes;
	}

	@Override
	public double getTempo() {
		return (termino - inicio) / 1_000_000.0;
	}
}
