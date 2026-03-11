package com.logseq.app

import android.appwidget.AppWidgetManager
import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "PageWidgetPlugin")
class PageWidgetPlugin : Plugin() {
    
    @PluginMethod
    fun updateWidget(call: PluginCall) {
        val pageName = call.getString("pageName")
        val content = call.getString("content")
        val widgetId = call.getInt("widgetId")
        
        if (pageName == null || content == null) {
            call.reject("pageName and content are required")
            return
        }
        
        val context = context ?: run {
            call.reject("No context")
            return
        }
        
        if (widgetId == null || widgetId == -1) {
            call.reject("widgetId is required")
            return
        }
        
        LogseqPageWidgetProvider.updateWidget(context, widgetId, pageName, content)
        
        val result = JSObject()
        result.put("success", true)
        call.resolve(result)
    }
    
    @PluginMethod
    fun getActiveWidgets(call: PluginCall) {
        val appWidgetManager = AppWidgetManager.getInstance(context ?: run {
            call.reject("No context")
            return
        })
        
        val componentName = android.content.ComponentName(
            context!!.packageName,
            LogseqPageWidgetProvider::class.java.name
        )
        
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        val result = JSObject()
        result.put("widgetIds", appWidgetIds.toList())
        call.resolve(result)
    }
    
    @PluginMethod
    fun deleteWidget(call: PluginCall) {
        val widgetId = call.getInt("widgetId")
        
        if (widgetId == null || widgetId == -1) {
            call.reject("widgetId is required")
            return
        }
        
        val context = context ?: run {
            call.reject("No context")
            return
        }
        
        val prefs = context.getSharedPreferences("LogseqWidgetPrefs", Context.MODE_PRIVATE)
        prefs.edit().remove("widget_data_$widgetId").apply()
        
        val appWidgetManager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(widgetId, null)
        
        val result = JSObject()
        result.put("success", true)
        call.resolve(result)
    }
}
