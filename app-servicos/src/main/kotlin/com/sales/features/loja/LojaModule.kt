package com.sales.features.loja

import org.koin.dsl.module
import com.sales.features.user.UserDao

val lojaModule = module {
    single { LojaRepository() }
    single<LojaMembershipChecker> { LojaMembershipCheckerExposed(get<UserDao>()) }
    single { LojaService(get(), get()) }
}