package com.angelhr28.yapechallenge.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.angelhr28.yapechallenge.feature.detail.DetailScreen
import com.angelhr28.yapechallenge.feature.documents.DocumentsScreen

@Composable
fun YapeChallengeNavGraph(
    navController: NavHostController,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DocumentsRoute,
        modifier = modifier
    ) {
        composable<DocumentsRoute> {
            DocumentsScreen(
                onNavigateToDetail = { documentId ->
                    navController.navigate(DetailRoute(documentId))
                },
                onTakePhoto = onTakePhoto
            )
        }

        composable<DetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DetailRoute>()
            DetailScreen(
                documentId = route.documentId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
