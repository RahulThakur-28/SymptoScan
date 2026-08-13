package com.rahul.symptoscan.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun NumberTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    suffix: String,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    AppTextField(
        value = value,
        onValueChange = {
            if (it.all { char -> char.isDigit() || char == '.' }) {
                onValueChange(it)
            }
        },
        label = label,
        placeholder = placeholder,
        modifier = modifier,
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            Text(
                text = suffix,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )
}
