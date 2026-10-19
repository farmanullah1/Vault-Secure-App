package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.repository.ImportProgress
import com.example.ui.theme.DarkCarbon
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.VaultEmerald

@Composable
fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit
) {
    val fraction = if (progress.total > 0) progress.current.toFloat() / progress.total.toFloat() else 0f

    Dialog(onDismissRequest = {
        if (progress.isFinished) onDismiss()
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (progress.isFinished) "Import Complete" else "Securing Media Files...",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (progress.isFinished) {
                    Text(
                        text = "Successfully imported ${progress.total} files into private storage with .nomedia protection.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = "Processing ${progress.current} of ${progress.total}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = VaultEmerald
                    )

                    if (progress.currentFileName.isNotEmpty()) {
                        Text(
                            text = progress.currentFileName,
                            fontSize = 12.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = VaultEmerald,
                        trackColor = DarkCarbon,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (progress.isFinished) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = VaultEmerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("import_done_button")
                    ) {
                        Text("Done", color = DarkCarbon, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
