package com.logseq.app

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log
import org.json.JSONObject

class LogseqPageWidgetProvider : AppWidgetProvider() {
    
    companion object {
        const val TAG = "LogseqPageWidget"
        const val ACTION_OPEN_PAGE = "com.logseq.app.ACTION_OPEN_PAGE"
        const val EXTRA_PAGE_NAME = "page_name"
        const val EXTRA_WIDGET_ID = "widget_id"
        
        private const val PREFS_NAME = "LogseqWidgetPrefs"
        private const val KEY_WIDGET_DATA = "widget_data_"
        
        fun updateWidget(context: Context, appWidgetId: Int, pageName: String, content: String, isPreview: Boolean = false) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val widgetData = JSONObject().apply {
                put("pageName", pageName)
                put("content", content)
                put("isPreview", isPreview)
            }
            prefs.edit().putString(KEY_WIDGET_DATA + appWidgetId, widgetData.toString()).apply()
            
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val views = RemoteViews(context.packageName, R.layout.logseq_page_widget).apply {
                setTextViewText(R.id.widget_page_title, pageName)
                setTextViewText(R.id.widget_content, content.take(200))
                
                val intent = Intent(context, LogseqPageWidgetProvider::class.java).apply {
                    action = ACTION_OPEN_PAGE
                    putExtra(EXTRA_PAGE_NAME, pageName)
                    putExtra(EXTRA_WIDGET_ID, appWidgetId)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
        
        fun getWidgetData(context: Context, appWidgetId: Int): JSONObject? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val data = prefs.getString(KEY_WIDGET_DATA + appWidgetId, null) ?: return null
            return try {
                JSONObject(data)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            val widgetData = getWidgetData(context, appWidgetId)
            if (widgetData != null) {
                val pageName = widgetData.optString("pageName", "Logseq")
                val content = widgetData.optString("content", "Tap to open")
                val views = RemoteViews(context.packageName, R.layout.logseq_page_widget).apply {
                    setTextViewText(R.id.widget_page_title, pageName)
                    setTextViewText(R.id.widget_content, content.take(200))
                    
                    val intent = Intent(context, LogseqPageWidgetProvider::class.java).apply {
                        action = ACTION_OPEN_PAGE
                        putExtra(EXTRA_PAGE_NAME, pageName)
                        putExtra(EXTRA_WIDGET_ID, appWidgetId)
                    }
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        appWidgetId,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } else {
                val views = RemoteViews(context.packageName, R.layout.logseq_page_widget).apply {
                    setTextViewText(R.id.widget_page_title, "Logseq Page")
                    setTextViewText(R.id.widget_content, "Add a page widget from Logseq")
                }
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_OPEN_PAGE) {
            val pageName = intent.getStringExtra(EXTRA_PAGE_NAME) ?: return
            Log.d(TAG, "Opening page: $pageName")
            
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            launchIntent?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                data = android.net.Uri.parse("logseq://page/${android.net.Uri.encode(pageName)}")
            }?.let { context.startActivity(it) }
        }
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        for (appWidgetId in appWidgetIds) {
            editor.remove(KEY_WIDGET_DATA + appWidgetId)
        }
        editor.apply()
    }
}
