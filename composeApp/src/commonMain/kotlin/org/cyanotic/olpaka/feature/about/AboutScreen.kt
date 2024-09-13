package org.cyanotic.olpaka.feature.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import olpaka.composeapp.generated.resources.*
import org.cyanotic.olpaka.ui.theme.olpakaColorOlpaka
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AboutScreen(navController: NavHostController) {
    Surface(
        modifier = Modifier.wrapContentHeight(),
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row {
                Box(
                    modifier = Modifier
                        .size(192.dp)
                        .background(
                            color = olpakaColorOlpaka,
                            shape = RoundedCornerShape(48.dp)
                        )
                ) {
                    Image(
                        imageVector = vectorResource(Res.drawable.olpaka_logo),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(192.dp)
                    )
                }
                Spacer(Modifier.width(24.dp))
                Column {
                    Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(Res.string.app_version), style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(Res.string.about_description), style = MaterialTheme.typography.bodySmall)
                }
            }
            TextButton(
                onClick = navController::popBackStack,
                Modifier.align(Alignment.End)
            ) {
                Text(stringResource(Res.string.about_close_cta))
            }
        }
    }
}