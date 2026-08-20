package com.cinavault.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cinavault.android.data.AppDestination
import com.cinavault.android.ui.CinaVaultApp
import com.cinavault.android.ui.CinaVaultRecoveryHost
import com.cinavault.android.ui.detectPreviousAbnormalExit
import com.cinavault.android.ui.theme.CinaVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialRecoveryDiagnostic = detectPreviousAbnormalExit(this)
        enableEdgeToEdge()
        setContent {
            val viewModel: CinaVaultViewModel = viewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()
            CinaVaultTheme {
                CinaVaultRecoveryHost(
                    initialDiagnostic = initialRecoveryDiagnostic,
                    onRecoverToLibrary = {
                        viewModel.clearError()
                        viewModel.navigate(AppDestination.Library)
                    },
                ) {
                    CinaVaultApp(
                        state = state,
                        onPasswordLogin = viewModel::loginWithPassword,
                        onAccessKeyLogin = viewModel::loginWithAccessKey,
                        onLogout = viewModel::logout,
                        onNavigate = viewModel::navigate,
                        onSearch = viewModel::setSearchQuery,
                        onOpenMedia = viewModel::openMedia,
                        onRefresh = viewModel::refreshLibrary,
                        onControlAction = viewModel::runControlAction,
                        onToggleAutopilot = viewModel::toggleAutopilot,
                        onRunAutopilot = viewModel::runAutopilotNow,
                        onRefreshLumaSift = viewModel::refreshLumaSift,
                        onStartLumaSift = viewModel::startLumaSift,
                        onApplyLumaSiftPlan = viewModel::applyLumaSiftPlan,
                        onDismissError = viewModel::clearError,
                        absoluteMediaUrl = viewModel::absoluteMediaUrl,
                        sessionToken = viewModel::sessionToken,
                    )
                }
            }
        }
    }
}
