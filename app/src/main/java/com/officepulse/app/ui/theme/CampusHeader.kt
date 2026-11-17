package com.officepulse.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampusHeader(title: String) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF102C43), Color(0xFF14545A))))) {
        // An abstract architectural elevation, scaled to the header rather than a campus map.
        Canvas(Modifier.align(Alignment.BottomEnd).size(132.dp, 94.dp)) {
            val unit = size.width / 140
            val ink = Color(0xFF91DAC7).copy(alpha = .45f)
            fun edge(x: Int, y: Int, xx: Int, yy: Int) = drawLine(ink,
                Offset(x * unit, y * unit), Offset(xx * unit, yy * unit), unit)
            for (tower in 0..2) {
                val x = 18 + tower * 43
                val y = 52 - tower * 14
                edge(x,y,x+24,y-12); edge(x+24,y-12,x+43,y-4); edge(x+43,y-4,x+19,y+8)
                edge(x+19,y+8,x,y); edge(x,y,x,106); edge(x+19,y+8,x+19,106)
                edge(x+43,y-4,x+43,106)
                for (floor in 1..6) edge(x+19,y+8+floor*9,x+43,y-4+floor*9)
            }
            drawCircle(Color(0xFFF1BA77), 6*unit, Offset(32*unit, 20*unit))
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Akademia WSB", color = Color(0xFFB5F3D9), fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp, style = MaterialTheme.typography.labelLarge)
            Text(title, color = Color.White, fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium)
        }
    }
}
