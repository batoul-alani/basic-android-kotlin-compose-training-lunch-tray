/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.lunchtray

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lunchtray.datasource.DataSource
import com.example.lunchtray.ui.AccompanimentMenuScreen
import com.example.lunchtray.ui.CheckoutScreen
import com.example.lunchtray.ui.EntreeMenuPreview
import com.example.lunchtray.ui.EntreeMenuScreen
import com.example.lunchtray.ui.OrderViewModel
import com.example.lunchtray.ui.SideDishMenuScreen
import com.example.lunchtray.ui.StartOrderScreen
import kotlin.contracts.contract

enum class LunchTrayScreen(@StringRes val title: Int) {
    Start(R.string.start_order),
    EntreeMenu(R.string.choose_entree),
    SideDishMenu(R.string.choose_side_dish),
    AccompanimentMenu(R.string.choose_accompaniment),
    Checkout(R.string.order_checkout)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchTrayApp(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = LunchTrayScreen.valueOf(backStackEntry?.destination?.route ?: LunchTrayScreen.Start.name)

    // Create ViewModel
    val viewModel: OrderViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(currentScreen.title))
                },
                navigationIcon = {
                    if(navController.previousBackStackEntry != null){
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button) )}

                    }
                }
            )

        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

      NavHost(
                navController = navController,
          modifier = Modifier.padding(innerPadding),
                startDestination = LunchTrayScreen.Start.name
                ){
                composable(route = LunchTrayScreen.Start.name){
                    StartOrderScreen(
                        onStartOrderButtonClicked = {
                            navController.navigate(LunchTrayScreen.EntreeMenu.name)
                        }
                    )
                }
                composable(route = LunchTrayScreen.EntreeMenu.name){

                    EntreeMenuScreen(
                        onNextButtonClicked = {
                            navController.navigate(LunchTrayScreen.SideDishMenu.name)
                        },
                        onCancelButtonClicked = {
                            onCancelApplied(navController = navController, viewModel = viewModel)
                        },
                        onSelectionChanged = {
                            viewModel.updateEntree(it)
                        },
                        options = DataSource.entreeMenuItems
                    )
                }
                composable(route = LunchTrayScreen.SideDishMenu.name){
                    SideDishMenuScreen(
                        onNextButtonClicked = {
                            navController.navigate(LunchTrayScreen.AccompanimentMenu.name)
                        },
                        onSelectionChanged = {
                            viewModel.updateSideDish(it)
                        },
                        onCancelButtonClicked = {
                            onCancelApplied(navController = navController, viewModel = viewModel)
                        },
                        options = DataSource.sideDishMenuItems
                    )
                }
                composable(route = LunchTrayScreen.AccompanimentMenu.name){
                    AccompanimentMenuScreen(
                        onCancelButtonClicked = {
                            onCancelApplied(navController = navController, viewModel = viewModel)
                        },
                        onSelectionChanged = {
                            viewModel.updateAccompaniment(it)
                        },
                        onNextButtonClicked = {
                            navController.navigate(LunchTrayScreen.Checkout.name)
                        },
                        options = DataSource.accompanimentMenuItems
                    )
                }
                composable(route = LunchTrayScreen.Checkout.name){
                    CheckoutScreen(
                        orderUiState = uiState,
                        onNextButtonClicked = {
                            onCancelApplied(navController = navController, viewModel = viewModel)
                        },

                        onCancelButtonClicked = {
                            onCancelApplied(navController = navController, viewModel = viewModel)
                        })
                }
            }
    }
}

private fun onCancelApplied (navController: NavHostController, viewModel: OrderViewModel) {
    viewModel.resetOrder()
    navController.popBackStack(LunchTrayScreen.Start.name, inclusive = false)
}
