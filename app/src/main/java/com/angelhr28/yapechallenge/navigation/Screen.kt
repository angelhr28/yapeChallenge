package com.angelhr28.yapechallenge.navigation

import kotlinx.serialization.Serializable

@Serializable
object DocumentsRoute

@Serializable
data class DetailRoute(val documentId: Long)

@Serializable
object CameraRoute
