package com.example.hive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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

        // Two buttons side by side with arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Up arrow button
            Button(
                onClick = {
                    // Demo: advance states forward
                    vertexStates = vertexStates.map { current ->
                        val nextOrdinal = (current.ordinal + 1) % VertexState.entries.size
                        VertexState.entries[nextOrdinal]
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Black,
                    contentColor = White
                )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "Up"
                )
            }

            // Down arrow button
            Button(
                onClick = {
                    // Demo: reverse states
                    vertexStates = vertexStates.map { current ->
                        val prevOrdinal = if (current.ordinal - 1 < 0) {
                            VertexState.entries.size - 1
                        } else {
                            current.ordinal - 1
                        }
                        VertexState.entries[prevOrdinal]
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Black,
                    contentColor = White
                )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Down"
                )
            }
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
        val outerRadius = size.minDimension * 0.38f
        val innerRadius = size.minDimension * 0.20f

        // Pentagon vertex angles (starting from top, going clockwise)
        val angles = List(5) { i ->
            Math.toRadians(-90.0 + (i * 72.0)).toFloat()
        }

        // Outer pentagon vertices
        val outerVertices = angles.map { angle ->
            Offset(
                centerX + outerRadius * cos(angle),
                centerY + outerRadius * sin(angle)
            )
        }

        // Inner pentagon vertices (for star lines)
        val innerVertices = angles.map { angle ->
            Offset(
                centerX + innerRadius * cos(angle),
                centerY + innerRadius * sin(angle)
            )
        }

        // Draw outer pentagon lines
        for (i in outerVertices.indices) {
            val start = outerVertices[i]
            val end = outerVertices[(i + 1) % outerVertices.size]
            drawLine(
                color = LightGray,
                start = start,
                end = end,
                strokeWidth = 1.5f
            )
        }

        // Draw inner star lines (connecting every other outer vertex)
        for (i in outerVertices.indices) {
            val start = outerVertices[i]
            val end = outerVertices[(i + 2) % outerVertices.size]
            drawLine(
                color = LightGray.copy(alpha = 0.5f),
                start = start,
                end = end,
                strokeWidth = 1f
            )
        }

        // Draw center dot
        drawCircle(
            color = if (states.count { it == VertexState.ONLINE || it == VertexState.ACTIVE } >= 3) Black else LightGray,
            radius = 5f,
            center = Offset(centerX, centerY)
        )

        // Draw vertex dots based on state
        outerVertices.forEachIndexed { index, pos ->
            when (states[index]) {
                VertexState.OFFLINE -> {
                    drawCircle(
                        color = LightGray,
                        radius = 6f,
                        center = pos,
                        style = Stroke(width = 1.5f)
                    )
                }
                VertexState.CONNECTING -> {
                    drawCircle(
                        color = Gray,
                        radius = 5f,
                        center = pos
                    )
                }
                VertexState.ONLINE -> {
                    drawCircle(
                        color = Black,
                        radius = 7f,
                        center = pos
                    )
                }
                VertexState.ACTIVE -> {
                    drawCircle(
                        color = Black,
                        radius = 10f,
                        center = pos
                    )
                    drawCircle(
                        color = Black,
                        radius = 14f,
                        center = pos,
                        style = Stroke(width = 1.5f)
                    )
                }
                VertexState.ERROR -> {
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
