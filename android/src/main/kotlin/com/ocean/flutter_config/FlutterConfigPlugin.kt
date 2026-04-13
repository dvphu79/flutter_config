package com.ocean.flutter_config

import android.content.Context
import android.content.res.Resources
import androidx.annotation.NonNull
import io.flutter.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.lang.IllegalArgumentException
import java.lang.reflect.Field

class FlutterConfigPlugin : FlutterPlugin, MethodCallHandler {

  private lateinit var channel: MethodChannel
  private lateinit var applicationContext: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    applicationContext = flutterPluginBinding.applicationContext
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_config")
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "loadEnvVariables") {
      val variables = loadEnvVariables()
      result.success(variables)
    } else {
      result.notImplemented()
    }
  }

  private fun loadEnvVariables(): Map<String, Any?> {
    val variables = hashMapOf<String, Any?>()

    try {
      val resId = applicationContext.resources.getIdentifier("build_config_package", "string", applicationContext.packageName)
      val className: String = try {
        applicationContext.getString(resId)
      } catch (e: Resources.NotFoundException) {
        applicationContext.packageName
      }

      val clazz = Class.forName("$className.BuildConfig")

      clazz.declaredFields.forEach {
        try {
          variables[it.name] = it.get(null)
        } catch (e: IllegalArgumentException) {
        } catch (e: IllegalAccessException) {
        }
      }
    } catch (e: ClassNotFoundException) {
      Log.d("FlutterConfig", "Could not access BuildConfig")
    }

    return variables
  }
}
