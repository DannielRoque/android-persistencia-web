package br.com.alura.estoque.BaseCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class BaseCallback<T> implements Callback<T> {

    private final RespostaCallback<T> callback;

    public BaseCallback(RespostaCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()){
            T resultado =response.body();
            if(resultado != null){
                callback.quandoSucesso(resultado);
            }
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        callback.quandoErro("Busca não obteve retorno "+t);
    }

    public  interface RespostaCallback<T>{
        void quandoSucesso(T resultado);
        void quandoErro(String erro);
    }
}
