package com.example.hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hive.ui.theme.Black
import com.example.hive.ui.theme.Gray
import com.example.hive.ui.theme.HiveTheme
import com.example.hive.ui.theme.LightGray
import com.example.hive.ui.theme.White
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiveTheme {
                HiveScreen()
            }
        }
    }
}

// 5 configurable vertex states
enum class VertexState {
    OFFLINE,      // empty
    CONNECTING,   // small dot
    ONLINE,       // filled dot
    ACTIVE,       // large dot
    ERROR         // ring
}

@Composable
fun HiveScreen() {
    // Local state for the 5 vertices - wire to real data later
    var vertexStates by remember {
        mutableStateOf(List(5) { VertexState.OFFLINE })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pentagon state visualizer
        PentagonVisualizer(
            states = vertexStates,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Single action button
        Button(
            onClick = {
                // Demo: cycle through states
                vertexStates = vertexStates.map { current ->
                    val nextOrdinal = (current.ordinal + 1) % VertexState.entries.size
                    VertexState.entries[nextOrdinal]
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Black,
                contentColor = White
            )
        ) {
            Text("PING NETWORK", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun PentagonVisualizer(
    states: List<VertexState>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = size.minDimension * 0.35f

        // Pentagon vertex angles (starting from top, going clockwise)
        // -90 degrees is top
        val angles = List(5) { i ->
            Math.toRadians(-90.0 + (i * 72.0)).toFloat()
        }

        // Calculate vertex positions
        val vertices = angles.map { angle ->
            Offset(
                centerX + radius * cos(angle),
                centerY + radius * sin(angle)
            )
        }

        // Draw connecting lines between vertices
        for (i in vertices.indices) {
            val start = vertices[i]
            val end = vertices[(i + 1) % vertices.size]
            drawLine(
                color = LightGray,
                start = start,
                end = end,
                strokeWidth = 1.5f
            )
        }

        // Draw vertex dots based on state
        vertices.forEachIndexed { index, pos ->
            when (states[index]) {
                VertexState.OFFLINE -> {
                    // Empty: small gray ring
                    drawCircle(
                        color = LightGray,
                        radius = 6f,
                        center = pos,
                        style = Stroke(width = 1.5f)
                    )
                }
                VertexState.CONNECTING -> {
                    // Small filled dot
                    drawCircle(
                        color = Gray,
                        radius = 5f,
                        center = pos
                    )
                }
                VertexState.ONLINE -> {
                    // Medium filled dot
                    drawCircle(
                        color = Black,
                        radius = 7f,
                        center = pos
                    )
                }
                VertexState.ACTIVE -> {
                    // Large filled dot
                    drawCircle(
                        color = Black,
                        radius = 10f,
                        center = pos
                    )
                    // Ring around it
                    drawCircle(
                        color = Black,
                        radius = 14f,
                        center = pos,
                        style = Stroke(width = 1.5f)
                    )
                }
                VertexState.ERROR -> {
                    // Ring (empty circle with thicker border)
                    drawCircle(
                        color = Black,
                        radius = 8f,
                        center = pos,
                        style = Stroke(width = 2f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HiveScreenPreview() {
    HiveTheme {
        HiveScreen()
    }
}
