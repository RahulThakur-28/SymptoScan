package com.rahul.symptoscan.presentation.auth.register.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.ui.theme.SuccessGreen

@Composable
fun PasswordRequirement(
    text: String,
    isMet: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isMet) SuccessGreen else Color.Gray.copy(alpha = 0.5f),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isMet) SuccessGreen else Color.Gray
        )
    }
}

@Composable
fun PasswordRequirementsSection(
    password: String,
    modifier: Modifier = Modifier
) {
    val requirements = listOf(
        "8+ characters" to (password.length >= 8),
        "One uppercase letter" to password.any { it.isUpperCase() },
        "One lowercase letter" to password.any { it.isLowerCase() },
        "One number" to password.any { it.isDigit() },
        "One special character" to password.any { !it.isLetterOrDigit() }
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        requirements.forEach { (text, isMet) ->
            PasswordRequirement(text = text, isMet = isMet)
        }
    }
}
