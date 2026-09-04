package com.example.hive.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.hive.MainActivity
import com.example.hive.R

class HiveWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                HiveWidgetContent()
            }
        }
    }
}

class HiveWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = HiveWidget()
}

@Composable
fun HiveWidgetContent() {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(40.dp)
            .background(ColorProvider(R.color.black))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Up arrow button
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .background(ColorProvider(R.color.white))
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▲",
                style = TextStyle(
                    color = ColorProvider(R.color.black)
                )
            )
        }

        Spacer(GlanceModifier.width(4.dp))

        // Down arrow button
        Box(
            modifier = GlanceModifier
                .defaultWeight()
                .fillMaxHeight()
                .background(ColorProvider(R.color.white))
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▼",
                style = TextStyle(
                    color = ColorProvider(R.color.black)
                )
            )
        }
    }
}