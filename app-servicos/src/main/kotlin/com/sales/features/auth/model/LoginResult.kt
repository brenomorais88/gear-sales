package com.sales.features.auth.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginResult(val access: String)