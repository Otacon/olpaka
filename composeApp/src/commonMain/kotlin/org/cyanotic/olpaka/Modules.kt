package org.cyanotic.olpaka

import org.cyanotic.olpaka.core.coreModule
import org.cyanotic.olpaka.feature.chat.chatModule
import org.cyanotic.olpaka.feature.main.mainModule
import org.cyanotic.olpaka.feature.models.modelsModule
import org.cyanotic.olpaka.feature.onboarding.onboardingModule
import org.cyanotic.olpaka.feature.settings.settingsModule
import org.cyanotic.olpaka.network.networkModule
import org.cyanotic.olpaka.repository.repositoryModule

val koinModules = listOf(
    coreModule,
    networkModule,
    repositoryModule,
    mainModule,
    onboardingModule,
    chatModule,
    modelsModule,
    settingsModule,
)