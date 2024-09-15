package org.cyanotic.olpaka.feature.onboarding

import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {
    viewModelOf(::OnboardingViewModel)
}