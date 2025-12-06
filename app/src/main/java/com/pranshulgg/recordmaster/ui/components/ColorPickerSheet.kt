package com.pranshulgg.recordmaster.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.drawColorIndicator
import com.pranshulgg.recordmaster.helpers.PreferencesHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorPickerSheetTheme(
    onShowSheet: Boolean,
    sheetState: SheetState,
    initialColorInt: Color,
    controller: ColorPickerController,
    onPickedColor: (String) -> Unit,
    onExpressiveColorChanged: (Boolean) -> Unit,
    useExpressiveColor: Boolean,
    onSeedChanged: (String) -> Unit,
    hideColorSheet: () -> Unit
){

    var pickedColor = PreferencesHelper.getString("seedColor") ?: "0xff0000FF"

    ModalBottomSheet(
        onDismissRequest = {
//            showBottomSheet = false
            onShowSheet
        },
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier.padding(top = 22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant,
                            RoundedCornerShape(50.dp)
                        )
                )
            }
        }
    ) {

        Column(
            Modifier.padding(bottom = 16.dp)
        ) {

            Spacer(Modifier.height(12.dp))


            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(vertical = 10.dp),
                initialColor = initialColorInt,
                controller = controller,
                drawOnPosSelected = {
                    drawColorIndicator(
                        controller.selectedPoint.value,
                        controller.selectedColor.value,
                    )
                },
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    val hexColor = colorEnvelope.hexCode

                    val argbHex = "0xFF$hexColor"

//                    pickedColor = argbHex
                    onPickedColor(argbHex)

                }
            )
            Spacer(Modifier.height(12.dp))

            SettingSection(
                tiles = listOf(


                    SettingTile.SingleSwitchTile(
                        title = "Use expressive palette",
                        checked = useExpressiveColor,
                        onCheckedChange = { checked ->
                            PreferencesHelper.setBool("useExpressiveColor", checked)

                            onExpressiveColorChanged(checked)
                        }
                    )

                )
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, start = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.defaultMinSize(minWidth = 90.dp, minHeight = 45.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    onClick = {
                        hideColorSheet()
                    }, shapes = ButtonDefaults.shapes()
                ) {
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = {
                        onSeedChanged(pickedColor)
                        hideColorSheet()
                    },

                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.defaultMinSize(minWidth = 90.dp, minHeight = 45.dp),
                ) {
                    Text("Save", fontSize = 16.sp)
                }

            }
        }

    }

}
