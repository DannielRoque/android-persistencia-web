package br.com.alura.estoque.BaseCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static br.com.alura.estoque.BaseCallback.MensagemCallback.MENSAGEM_ERRO_FALHA_COMUNICACAO;
import static br.com.alura.estoque.BaseCallback.MensagemCallback.MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA;

public class CallbackComRetorno<T> implements Callback<T> {
    private final RespostaCallback<T> callback;

    public CallbackComRetorno(RespostaCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            T resultado = response.body();
            if (resultado != null) {
                callback.quandoSucesso(resultado);
            }
        }else {
            callback.quandoErro(MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA);
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        callback.quandoErro(MENSAGEM_ERRO_FALHA_COMUNICACAO + t);
    }

    public interface RespostaCallback<T> {
        void quandoSucesso(T resultado);

        void quandoErro(String erro);
    }
}
