package br.com.alura.estoque.repository;

import android.content.Context;

import java.util.List;

import br.com.alura.estoque.BaseCallback.CallbackComRetorno;
import br.com.alura.estoque.BaseCallback.CallbackSemRetorno;
import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(Context context) {
        service = new EstoqueRetrofit().getProdutoService();
        EstoqueDatabase db = EstoqueDatabase.getInstance(context);
        dao = db.getProdutoDAO();
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

        call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<List<Produto>>() {
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

    private void salvaProdutoAPI(Produto produto,
                                 DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<Produto>() {
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

    private void salvaProdutoInterno(Produto produto,
                                     DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, callback::quandoSucesso).execute();
    }

    public void edita(Produto produto,
                      DadosCarregadosCallback<Produto> callback) {
        editaNaApi(produto, callback);
    }

    private void editaNaApi(Produto produto,
                            DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.edita(produto.getId(), produto);
        call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                editaInterno(produto, callback);
            }

            @Override
            public void quandoErro(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void editaInterno(Produto produto,
                              DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callback::quandoSucesso).execute();
    }

    public void remove(Produto produto,
                       DadosCarregadosCallback<Void> callback) {
        removeNaApi(produto, callback);
    }

    private void removeNaApi(Produto produto, DadosCarregadosCallback<Void> callback) {
        Call<Void> call = service.remove(produto.getId());
        call.enqueue(new CallbackSemRetorno(
                new CallbackSemRetorno.RespostaCallback() {
                    @Override
                    public void quandoSucesso() {
                        removeInterno(produto, callback);
                    }

                    @Override
                    public void quandoErro(String erro) {
                        callback.quandoFalha(erro);
                    }
                }));
    }

    private void removeInterno(Produto produto, DadosCarregadosCallback<Void> callback) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produto);
            return null;
        }, callback::quandoSucesso).execute();
    }

    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);

        void quandoFalha(String erro);
    }
}
