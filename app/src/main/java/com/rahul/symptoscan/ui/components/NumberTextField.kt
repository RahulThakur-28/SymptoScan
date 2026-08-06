package com.rahul.symptoscan.ui.components

import androidx.compose.foundation.text.KeyboardOptions
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
            if (it.all { char -> char.isDigit() }) {
                onValueChange(it)
            }
        },
        label = label,
        placeholder = placeholder,
        modifier = modifier,
        error = error,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            androidx.compose.material3.Text(
                text = suffix,
                color = androidx.compose.ui.graphics.Color.Gray,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    )
}