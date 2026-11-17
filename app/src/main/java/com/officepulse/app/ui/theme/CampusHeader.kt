package com.officepulse.app.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.officepulse.app.R
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
        .background(Brush.linearGradient(listOf(Color(0xFFFFF8FB), Color(0xFFFFDBE9))))) {
        // An abstract architectural elevation, scaled to the header rather than a campus map.
        Canvas(Modifier.align(Alignment.BottomEnd).size(132.dp, 94.dp)) {
            val unit = size.width / 140
            val ink = Color(0xFFCC0C61).copy(alpha = .45f)
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
            drawCircle(Color(0xFFA30650), 6*unit, Offset(32*unit, 20*unit))
        }
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(painterResource(R.drawable.akademia_wsb), contentDescription = "Akademia WSB",
                modifier = Modifier.size(154.dp, 53.dp))
            Text(title, color = Color(0xFF351329), fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium)
        }
    }
}
