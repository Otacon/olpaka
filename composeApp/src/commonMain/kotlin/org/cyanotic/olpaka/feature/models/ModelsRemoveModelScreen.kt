package org.cyanotic.olpaka.feature.models

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.core.Results.RESULT_REMOVE_MODEL_KEY
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModelsRemoveModelScreen(
    navController: NavController,
    modelName: String,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(Res.string.models_dialog_remove_model_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                stringResource(Res.string.models_dialog_remove_model_description, modelName)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        navController.popBackStack()
                    },
                ) {
                    Text(stringResource(Res.string.models_dialog_remove_model_action_negative))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val previousBackStackEntry = navController.previousBackStackEntry
                        previousBackStackEntry?.savedStateHandle?.set(RESULT_REMOVE_MODEL_KEY, modelName)
                        navController.popBackStack()
                    }
                ) {
                    Text(stringResource(Res.string.models_dialog_remove_model_action_positive))
                }
            }
        }
    }
}