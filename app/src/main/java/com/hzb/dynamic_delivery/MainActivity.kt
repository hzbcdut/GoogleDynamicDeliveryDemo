package com.hzb.dynamic_delivery

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.splitinstall.SplitInstallException
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallErrorCode
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

class MainActivity : AppCompatActivity() {


    private val context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 下载功能模块
        findViewById<View>(R.id.requestDynamicModule).setOnClickListener {
            requestDynamicModule()
        }

        // 访问功能模块中的Activity
        findViewById<View>(R.id.useDynamicModule).setOnClickListener {
            if (isDynamicModuleInstalled(DYNAMIC_MODULE_NAME)) {
                startActivity(
                    Intent()
                        .setClassName(
                            "com.hzb.dynamicfeature",
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

    // Creates an instance of SplitInstallManager.
    private val splitInstallManager by lazy { SplitInstallManagerFactory.create(context) }


    // Initializes a variable to later track the session ID for a given request.
    private var mySessionId = 0

    // Creates a listener for request status updates.
    private val listener = SplitInstallStateUpdatedListener { state ->
        if (state.status() == SplitInstallSessionStatus.FAILED
            && state.errorCode() == SplitInstallErrorCode.SERVICE_DIED
        ) {
            // Retry the request.

            return@SplitInstallStateUpdatedListener
        }
        if (state.sessionId() == mySessionId) {
            // Read the status of the request to handle the state update.

            when (state.status()) {
//                已接受该请求，即将开始下载。
                SplitInstallSessionStatus.DOWNLOADING -> {
                    val totalBytes = state.totalBytesToDownload()
                    val progress = state.bytesDownloaded()
                    // Update progress bar.
                }
//                该模块已安装在设备上。
                SplitInstallSessionStatus.INSTALLED -> {

                    Toast.makeText(this, "动态模块安装成功", Toast.LENGTH_SHORT).show()

                    // After a module is installed, you can start accessing its content or
                    // fire an intent to start an activity in the installed module.
                    // For other use cases, see access code and resources from installed modules.

                    // If the request is an on demand module for an Android Instant App
                    // running on Android 8.0 (API level 26) or higher, you need to
                    // update the app context using the SplitInstallHelper API.
                }
                // 获取用户确认
                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
// Displays a confirmation for the user to confirm the request.
                    splitInstallManager.startConfirmationDialogForResult(
                        state,
                        // an activity result launcher registered via registerForActivityResult
                        activityResultLauncher
                    )
                }
            }
        }
    }


    private fun requestDynamicModule() {

// Creates a request to install a module.
        val request =
            SplitInstallRequest
                .newBuilder()
                // You can download multiple on demand modules per
                // request by invoking the following method for each
                // module you want to install.
                .addModule(DYNAMIC_MODULE_NAME)
                .build()

        splitInstallManager
            // Submits the request to install the module through the
            // asynchronous startInstall() task. Your app needs to be
            // in the foreground to submit the request.
            .startInstall(request)
            // You should also be able to gracefully handle
            // request state changes and errors. To learn more, go to
            // the section about how to Monitor the request state.
            .addOnSuccessListener { sessionId ->
                Toast.makeText(this, "开始下载", Toast.LENGTH_SHORT).show()

                mySessionId = sessionId
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "requestDynamicModule:  errorCode = ${(exception as SplitInstallException).errorCode}")
                when ((exception as SplitInstallException).errorCode) {
//                    由于出现网络连接错误，请求失败。
                    SplitInstallErrorCode.NETWORK_ERROR -> {
                        // Display a message that requests the user to establish a
                        // network connection.
                    }
//                    请求遭到拒绝，因为当前至少有一个请求正在下载。
                    SplitInstallErrorCode.ACTIVE_SESSIONS_LIMIT_EXCEEDED -> checkForActiveDownloads()

//                    Play 商店内发生内部错误。  请重试请求。
                    SplitInstallErrorCode.INTERNAL_ERROR -> {

                    }

                    else -> {}

                }
            }


        // Registers the listener.
        splitInstallManager.registerListener(listener)
    }

    private fun checkForActiveDownloads() {
        splitInstallManager
            // Returns a SplitInstallSessionState object for each active session as a List.
            .sessionStates
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Check for active sessions.
                    for (state in task.result) {
                        if (state.status() == SplitInstallSessionStatus.DOWNLOADING) {
                            // Cancel the request, or request a deferred installation.
                        }
                    }
                }
            }
    }

    /**
     * 取消安装请求
     */
    private fun cancelInstall() {
        splitInstallManager
            // Cancels the request for the given session ID.
            .cancelInstall(mySessionId)
    }

    private fun isDynamicModuleInstalled(moduleName: String): Boolean {
        val installedModules = splitInstallManager.installedModules
        return installedModules.contains(moduleName)
    }

    override fun onDestroy() {
        super.onDestroy()
        splitInstallManager.unregisterListener(listener)
    }

    companion object{
        const val TAG = "MainActivity"
        const val DYNAMIC_MODULE_NAME = "dynamicfeature"
    }
}