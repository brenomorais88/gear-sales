package com.sales.features.loja

import org.koin.dsl.module

val lojaModule = module {
    single { LojaRepository() }
    single { LojaService(get()) }
}