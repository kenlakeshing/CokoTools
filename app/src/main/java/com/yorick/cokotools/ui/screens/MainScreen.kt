package com.yorick.cokotools.ui.screens

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yorick.cokotools.ui.components.CokoToolsAppBar
import com.yorick.cokotools.ui.navigation.CokoToolsNavigationBottomBar
import com.yorick.cokotools.ui.navigation.CookToolsRoute
import com.yorick.cokotools.ui.navigation.NavigationActions
import com.yorick.cokotools.ui.viewmodels.HomeViewModel
import com.yorick.cokotools.ui.viewmodels.SettingViewModel
import com.yorick.cokotools.ui.viewmodels.ShellViewModel
import com.yorick.cokotools.util.Utils

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
    shellViewModel: ShellViewModel,
    settingViewModel: SettingViewModel,
    navController: NavController,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hostState = remember { SnackbarHostState() }
    val mainNavController = rememberNavController()
    val navigationActions = remember(mainNavController) { NavigationActions(mainNavController) }
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val selectedDestination = navBackStackEntry?.destination?.route ?: CookToolsRoute.HOME
    Scaffold(
        modifier = modifier.navigationBarsPadding(),
        topBar = {
            CokoToolsAppBar(
                onClickDonate = {
                    navController.navigate(
                        CookToolsRoute.ABOUT
                    ) {
                        launchSingleTop = true
                    }
                },
                onClickHelp = {
                    Utils.openUrl(Utils.HELP_DOC_URL, context)
                }
            )
        },
        bottomBar = {
            CokoToolsNavigationBottomBar(
                selectedDestination = selectedDestination,
                navigateToTopLevelDestination = navigationActions::navigateTo
            )
        },
        snackbarHost = { SnackbarHost(hostState = hostState) }
    ) {
        NavHost(
            modifier = Modifier.padding(it),
            navController = mainNavController,
            startDestination = CookToolsRoute.HOME,
        ) {
            composable(
                route = CookToolsRoute.HOME,
            ) {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    onClickFab = {
                        navController.navigate(CookToolsRoute.TOOL)
                        homeViewModel.getAllRemoteTools()
                    },
                    scope = scope,
                    hostState = hostState
                )
            }
            composable(route = CookToolsRoute.SHELL) {
                ShellScreen(
                    shellViewModel = shellViewModel,
                    scope = scope,
                    hostState = hostState
                )
            }
            composable(route = CookToolsRoute.SETTING) {
                SettingScreen(settingViewModel = settingViewModel)
            }
        }
    }
}