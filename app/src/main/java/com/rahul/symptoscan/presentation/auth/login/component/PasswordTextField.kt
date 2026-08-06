package com.rahul.symptoscan.presentation.auth.login.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.rahul.symptoscan.ui.components.AppTextField

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    AppTextField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        modifier = modifier,
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                EyeIcon(isVisible = isVisible, modifier = Modifier.size(24.dp))
            }
        }
    )
}

@Composable
fun EyeIcon(isVisible: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val color = Color.Gray
        
        // Eye shape
        val path = Path().apply {
            moveTo(w * 0.1f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.1f, w * 0.9f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.9f, w * 0.1f, h * 0.5f)
        }
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx()))
        
        // Pupil
        drawCircle(color = color, radius = w * 0.15f, center = center)
        
        if (!isVisible) {
            // Slash
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.2f),
                end = androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.8f),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}