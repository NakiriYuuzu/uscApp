package tw.edu.finalproject.yuuzu.websocket

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import org.json.JSONObject
import tw.edu.finalproject.R
import tw.edu.finalproject.ui.login.LoginActivity
import tw.edu.finalproject.ui.login.LoginDto
import tw.edu.finalproject.util.Constants
import tw.edu.finalproject.util.YStore
import tw.edu.finalproject.yuuzu.YuuzuShare


class WebSocketService : Service() {

    companion object {
        const val TAG = "WebSocketService"
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    private lateinit var yuuzuShare: YuuzuShare

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        yuuzuShare = YuuzuShare(this)
        connect()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> connect()
            ACTION_STOP -> disconnect()
        }
        //return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    private fun connect() {
        val notifyIntent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "WebSocket")
            .setContentTitle(getString(R.string.app_name))
            .setContentText("事件偵測中")
            .setSmallIcon(R.mipmap.logo)
            .setContentIntent(notifyPendingIntent)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        WebSocketManager.init(Constants.WS_NOTIFY, object: MessageListener {
            override fun onConnectSuccess() {

            }

            override fun onConnectFailed() {

            }

            override fun onClose() {

            }

            override fun onMessage(text: String?) {
                val result = String(text?.toByteArray(Charsets.ISO_8859_1)!!, Charsets.UTF_8)
                val myMail = yuuzuShare.get<String>(YStore.YS_TOKEN)?.split("|")?.get(1)
                val userName = yuuzuShare.get<LoginDto>(YStore.YS_CURRENT_GROUP)?.user_name

                Log.e(TAG, "onMessage: $result $myMail $userName")

                try {
                    val jsonObject = JSONObject(result)
                    val type = jsonObject.getString("type")
                    val message = jsonObject.getString("message")
                    val userMail = jsonObject.get("user_mail")

                    if (type == "Remind") {
                        if (userMail == myMail) {
                            Log.e(TAG, "onMessage: Notify!")
                            val notify = notification.setContentText("$userName $message")
                            notificationManager.notify(1, notify.build())
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "onMessage: ${e.message}")
                }
            }
        })

        startForeground(1, notification.build())
        WebSocketManager.connect()
    }

    private fun disconnect() {
        WebSocketManager.close()
    }
}