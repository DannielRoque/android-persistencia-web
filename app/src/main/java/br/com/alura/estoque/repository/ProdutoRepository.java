package br.com.alura.estoque.repository;

import java.util.List;

import br.com.alura.estoque.BaseCallback.BaseCallback;
import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        service = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> callback) {
        buscaProdutosInterno(callback);
    }

    private void buscaProdutosInterno(DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    callback.quandoSucesso(resultado);
                    buscaProdutosNaAPI(callback);
                }).execute();
    }

    private void buscaProdutosNaAPI(DadosCarregadosCallback<List<Produto>> callback) {
        Call<List<Produto>> call = service.buscaTodos();

        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> resultado) {
                atualizaInterno(resultado, callback);
            }

            @Override
            public void quandoErro(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void atualizaInterno(List<Produto> produtosNovos, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(() -> {
            dao.salva(produtosNovos);
            return dao.buscaTodos();
        }, callback::quandoSucesso).execute();
    }

    public void salva(Produto produto, DadosCarregadosCallback<Produto> callback) {
        salvaProdutoAPI(produto, callback);
    }

    private void salvaProdutoAPI(Produto produto, DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new BaseCallback<>(new BaseCallback.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                salvaProdutoInterno(resultado, callback);
            }

            @Override
            public void quandoErro(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void salvaProdutoInterno(Produto produto, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, callback::quandoSucesso);
    }

    public interface DadosCarregadosCallback<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
