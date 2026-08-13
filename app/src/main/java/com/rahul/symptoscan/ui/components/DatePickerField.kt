package com.rahul.symptoscan.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    placeholder: String = "Select Date"
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Formatters using UTC to avoid timezone-related date shifts
    val backendFormat = remember { 
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val displayFormat = remember { 
        SimpleDateFormat("dd MMM yyyy", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    
    // Parse existing value for initial picker state
    val initialDateMillis = remember(value) {
        try {
            if (value.isBlank()) System.currentTimeMillis()
            else backendFormat.parse(value)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Prevent future dates
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // Update picker state if value changes externally
    LaunchedEffect(value) {
        if (value.isNotBlank()) {
            try {
                backendFormat.parse(value)?.time?.let {
                    datePickerState.selectedDateMillis = it
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    val displayValue = remember(value) {
        try {
            if (value.isBlank()) ""
            else {
                val date = backendFormat.parse(value)
                if (date != null) displayFormat.format(date) else value
            }
        } catch (e: Exception) {
            value
        }
    }

    AppTextField(
        value = displayValue,
        onValueChange = {},
        label = label,
        placeholder = placeholder,
        modifier = modifier,
        error = error,
        readOnly = true,
        onClick = { showDatePicker = true },
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Select Date",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Date(millis)
                            onDateSelected(backendFormat.format(date))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
