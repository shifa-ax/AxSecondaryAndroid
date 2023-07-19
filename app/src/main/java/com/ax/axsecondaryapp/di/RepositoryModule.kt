package com.ax.axsecondaryapp.di

import com.ax.axsecondaryapp.api.Api
import com.ax.axsecondaryapp.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import retrofit2.Retrofit

@Module
@InstallIn(ActivityRetainedComponent::class)
object RepositoryModule {


}
