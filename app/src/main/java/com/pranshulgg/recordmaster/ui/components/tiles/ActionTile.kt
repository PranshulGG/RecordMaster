package com.pranshulgg.recordmaster.ui.components.tiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun ActionTile(
    headline: String,
    description: String? = null,
    leading: @Composable (() -> Unit)? = null,
    shapes: RoundedCornerShape,
    onClick: () -> Unit,
    colorDesc: Color = Color.Unspecified,
    danger: Boolean = false
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shapes,
    ) {
        ListItem(
            modifier = Modifier.clickable(
                onClick = onClick
            ),
            leadingContent = leading,
            colors = ListItemDefaults.colors(
                containerColor = if(danger) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            headlineContent = { Text(headline, color = if(danger) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface) },
            supportingContent = {

                if (description != null) {
                    Text(description, color = colorDesc)
                }
            }
        )
    }
}
