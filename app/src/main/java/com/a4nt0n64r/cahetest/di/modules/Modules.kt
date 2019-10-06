package com.a4nt0n64r.cahetest.di.modules

import android.arch.persistence.room.Room
import com.a4nt0n64r.cahetest.BuildConfig
import com.a4nt0n64r.cahetest.data.repository.NetworkRepoImpl
import com.a4nt0n64r.cahetest.data.repository.RepoImpl
import com.a4nt0n64r.cahetest.data.source.db.MyDatabase
import com.a4nt0n64r.cahetest.data.source.db.PlayerDao
import com.a4nt0n64r.cahetest.domain.repository.Repository
import com.a4nt0n64r.cahetest.network.ApiService
import com.a4nt0n64r.cahetest.network.NetworkRepository
import com.a4nt0n64r.cahetest.ui.PresenterImpl
import com.a4nt0n64r.cahetest.ui.base.AbstractPresenter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val REPOSITORY = "repo"
const val CLOUD_REPOSITORY = "cloud_repo"
const val DATABASE = "database"
const val DAO = "dao"
const val INTERCEPTOR = "interceptor"
const val GSON = "gson"
const val OK_HTTP_CLIENT = "ok_http_client"
const val RETROFIT = "retrofit"
const val API_SERVICE = "api_service"


val applicationModules = module(override = true) {

//    presenter
    factory<AbstractPresenter> {
        PresenterImpl(
            get(REPOSITORY),
            get(CLOUD_REPOSITORY)
        )
    }

//    repository
    factory<Repository>(REPOSITORY) { RepoImpl(get(DAO)) }

//    database
    single<MyDatabase>(DATABASE) {
        Room
            .databaseBuilder(
                androidContext(), MyDatabase::class.java, MyDatabase.DB_NAME
            ).build()
    }
    single<PlayerDao>(DAO) { get<MyDatabase>().playerDao() }

//    network
    single<HttpLoggingInterceptor>(INTERCEPTOR) { provideInterceptor() }
    single<Gson>(GSON) { provideGson() }
    single<OkHttpClient>(OK_HTTP_CLIENT) { provideDefaultOkhttpClient(get(INTERCEPTOR)) }
    single<Retrofit>(RETROFIT) { provideRetrofit(get(GSON),get(OK_HTTP_CLIENT)) }
    single<ApiService>(API_SERVICE) { provideApiService(get(RETROFIT)) }
    factory<NetworkRepository>(CLOUD_REPOSITORY) { NetworkRepoImpl(get(API_SERVICE)) }

}

fun provideGson():Gson{
    val gsonBuilder = GsonBuilder()
    return gsonBuilder.create()
}

fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}

fun provideInterceptor(): HttpLoggingInterceptor {
    val interceptor = HttpLoggingInterceptor()
    interceptor.level =
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    return interceptor
}

fun provideDefaultOkhttpClient(interceptor: HttpLoggingInterceptor): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()
}

fun provideRetrofit(gson:Gson, client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl("https://raw.githubusercontent.com/4nt0n64r/CaheTest/master/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}