package com.a4nt0n64r.cahetest.data.repository

import android.util.Log
import com.a4nt0n64r.cahetest.domain.model.CloudPlayer
import com.a4nt0n64r.cahetest.domain.model.Player
import com.a4nt0n64r.cahetest.network.ApiService
import com.a4nt0n64r.cahetest.network.NetworkRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkRepoImpl(private val apiService: ApiService) :NetworkRepository{

    override fun getPlayer(callback: (CloudPlayer) -> Unit){
        val call = apiService.getPlayerFromCloud()

        call.enqueue(object : Callback<CloudPlayer> {

            override fun onFailure(call: Call<CloudPlayer>, t: Throwable) {
                Log.d("FAIL", "FAIL что-то не так!")

            }

            override fun onResponse(call: Call<CloudPlayer>, response: Response<CloudPlayer>) {

                //TODO( вот тут мы можем получить: {"errors":[{"title":"Not Found","detail":"No Players Found Matching Criteria"}]})
                // Это надо обработать как "ИГРОК НЕ НАЙДЕН"
                val response = response.body()
                if (response != null) callback.invoke(response)

            }
        })
    }


}