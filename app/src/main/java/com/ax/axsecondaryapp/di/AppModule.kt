package com.ax.axsecondaryapp.di

import android.content.Context
import androidx.room.Room
import com.androidnetworking.interceptors.HttpLoggingInterceptor
import com.ax.axsecondaryapp.Urls
import com.ax.axsecondaryapp.api.Api
import com.ax.axsecondaryapp.db.CallLogDatabase
import com.ax.axsecondaryapp.db.CallLogDetailsDao
import com.ax.axsecondaryapp.repository.UserRepository
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [ApiDbServiceModule::class])
object AppModule {
    @Singleton
    @Provides
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofitClient(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl(Urls.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideApi(retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }

    @Singleton
    @Provides
    fun provideCallLogDatabase(@ApplicationContext context: Context): CallLogDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            CallLogDatabase::class.java,
            "call_log_database"
        ).fallbackToDestructiveMigration() // Enable destructive migrations

            .build()
    }

    @Provides
    fun provideCallLogDetailsDao(database: CallLogDatabase): CallLogDetailsDao {
        return database.callLogDetailsDao()
    }

    @Provides
    fun provideUserRepository(api: Api, dao: CallLogDetailsDao): UserRepository {
        return UserRepository(api, dao)
    }
    @Singleton
    @Provides
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }
}