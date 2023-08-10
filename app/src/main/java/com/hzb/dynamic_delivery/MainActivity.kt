package com.hzb.dynamic_delivery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : BaseSplitActivity() {

    private val dynamicModuleManager by lazy {
        DynamicModuleManager(
            this,
            this,
            activityResultLauncher
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 下载功能模块
        findViewById<View>(R.id.requestDynamicModule).setOnClickListener {
            dynamicModuleManager.requestDynamicModule(DYNAMIC_MODULE_NAME)
        }

        // 访问功能模块中的Activity
        findViewById<View>(R.id.useDynamicModule).setOnClickListener {
            if (dynamicModuleManager.isDynamicModuleInstalled(DYNAMIC_MODULE_NAME)) {
                startActivity(
                    Intent()
                        .setClassName(
                            "com.hzb.dynamic_delivery",
                            "com.hzb.dynamicfeature.DynamicActivity"
                        )
                )
            } else {
                Toast.makeText(this, "动态模块未下载", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        val code = result.resultCode
        val data = result.data
        // 处理活动结果，例如根据结果继续下载和安装动态模块
    }


    companion object {
        const val TAG = "MainActivity"
        const val DYNAMIC_MODULE_NAME = "dynamicfeature"
    }
}