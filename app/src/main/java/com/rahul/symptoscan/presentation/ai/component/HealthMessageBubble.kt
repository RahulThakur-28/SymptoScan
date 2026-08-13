package com.rahul.symptoscan.presentation.ai.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahul.symptoscan.domain.model.HealthMessage
import com.rahul.symptoscan.ui.theme.BluePrimary
import com.rahul.symptoscan.ui.theme.TextDark

@Composable
fun HealthMessageBubble(
    message: HealthMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isUser) BluePrimary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            if (!isUser) {
                Text(
                    text = "Health Assistant",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }
            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = if (isUser) 1.dp else 0.dp
            ) {
                Text(
                    text = message.content,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HealthMessageBubbleUserPreview() {
    HealthMessageBubble(
        message = HealthMessage(
            id = "1",
            conversationId = "c1",
            userId = "u1",
            role = "user",
            content = "What are common causes of headache?",
            createdAt = null
        )
    )
}

@Preview(showBackground = true)
@Composable
fun HealthMessageBubbleAssistantPreview() {
    HealthMessageBubble(
        message = HealthMessage(
            id = "2",
            conversationId = "c1",
            userId = "u1",
            role = "assistant",
            content = "Common causes of headaches include stress, tension, dehydration, or lack of sleep. They can also be associated with underlying health conditions.",
            createdAt = null
        )
    )
}
