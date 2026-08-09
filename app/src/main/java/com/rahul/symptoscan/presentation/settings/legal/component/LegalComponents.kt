package com.rahul.symptoscan.presentation.settings.legal.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.ui.theme.TextDark

@Composable
fun LegalSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun LegalParagraph(
    text: String
) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color.Gray,
        lineHeight = 22.sp,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun LegalBulletList(
    items: List<String>
) {
    Column(modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)) {
        items.forEach { item ->
            Row(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(text = "•", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item, fontSize = 14.sp, color = Color.Gray, lineHeight = 22.sp)
            }
        }
    }
}
