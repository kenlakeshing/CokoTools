package com.yorick.cokotools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yorick.cokotools.ui.navigation.CookToolsRoute
import com.yorick.cokotools.ui.screens.AboutScreen
import com.yorick.cokotools.ui.screens.MainScreen
import com.yorick.cokotools.ui.screens.ToolScreen
import com.yorick.cokotools.ui.theme.CokoToolsTheme
import com.yorick.cokotools.ui.viewmodels.ContributorViewModel
import com.yorick.cokotools.ui.viewmodels.HomeViewModel
import com.yorick.cokotools.ui.viewmodels.SettingViewModel
import com.yorick.cokotools.ui.viewmodels.ShellViewModel

@Composable
fun CokoToolsApp(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    shellViewModel: ShellViewModel,
    contributorViewModel: ContributorViewModel,
    settingViewModel: SettingViewModel
) {
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    NavHost(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        navController = navController,
        startDestination = CookToolsRoute.MAIN,
    ) {
        composable(route = CookToolsRoute.MAIN) {
            MainScreen(
                homeViewModel = homeViewModel,
                shellViewModel = shellViewModel,
                settingViewModel = settingViewModel,
                navController = navController
            )
        }
        composable(
            route = CookToolsRoute.ABOUT,
        ) {
            AboutScreen(
                contributorViewModel = contributorViewModel,
                scope = scope,
                hostState = hostState,
                navController = navController
            )
        }
        composable(route = CookToolsRoute.TOOL) {
            ToolScreen(
                homeViewModel = homeViewModel,
                addNewTool = homeViewModel::addNewTool,
                upLoadTool = homeViewModel::uploadTool,
                deleteTool = homeViewModel::deleteTool,
                downLoadTool = homeViewModel::downloadTool,
                scope = scope,
                navController = navController
            )
        }
    }
}


@Preview
@Composable
fun CokoToolsAppPreview() {
    CokoToolsTheme {
//        CokoToolsApp()
    }
}