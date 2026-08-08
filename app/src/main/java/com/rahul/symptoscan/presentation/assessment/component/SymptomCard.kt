package com.rahul.symptoscan.presentation.assessment.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.rahul.symptoscan.domain.model.Symptom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomCard(
    symptom: Symptom,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (symptom.isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.2f)
    val backgroundColor = if (symptom.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.White

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (symptom.isSelected) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = symptom.icon,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = symptom.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = if (symptom.isSelected) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SymptomCardPreview() {
    SymptomCard(
        symptom = Symptom("1", "Headache", "🤕", "General", isSelected = true),
        onClick = {}
    )
}
