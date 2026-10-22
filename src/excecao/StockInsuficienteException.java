package excecao;

public class StockInsuficienteException extends Exception {

    public StockInsuficienteException(String mensagem) {
        super(mensagem);
    }
}
