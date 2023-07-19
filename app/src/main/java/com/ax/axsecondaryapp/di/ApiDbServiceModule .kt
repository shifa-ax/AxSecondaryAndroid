package com.ax.axsecondaryapp.di

import com.ax.axsecondaryapp.service.ApiDbService
import com.ax.axsecondaryapp.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@Module
@InstallIn(ServiceComponent::class)
object ApiDbServiceModule {

    @Provides
    fun provideApiDbService(userRepository: UserRepository): ApiDbService {
        return ApiDbService()
    }
}
