package com.example.mediconnect24x7

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.mediconnect24x7.ui.theme.DarkGreen

@Composable
fun MedicinesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Medicines Screen Coming Soon",
            color = DarkGreen,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
