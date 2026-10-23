package com.skipmate.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skipmate.app.ui.theme.Primary

@Composable
fun SkipMateSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = if (checked) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Primary
                )
            }
        } else {
            null
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Primary,
            checkedBorderColor = Color.Transparent,
            checkedIconColor = Primary,
            uncheckedThumbColor = Color(0xFFCBD5E1),
            uncheckedTrackColor = Color(0xFF334155),
            uncheckedBorderColor = Color.Transparent,
            uncheckedIconColor = Color.Transparent
        )
    )
}
