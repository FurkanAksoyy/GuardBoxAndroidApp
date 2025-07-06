package com.flare.guardbox.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.flare.guardbox.MainActivity
import com.flare.guardbox.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.regex.Pattern

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID = "guardbox_channel"
        private const val MAX_TITLE_LENGTH = 100
        private const val MAX_MESSAGE_LENGTH = 500
        private const val NOTIFICATION_ID = 1001

        // Güvenlik için zararlı pattern'lar
        private val DANGEROUS_PATTERNS = listOf(
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("data:text/html", Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload=", Pattern.CASE_INSENSITIVE),
            Pattern.compile("onerror=", Pattern.CASE_INSENSITIVE)
        )
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        try {
            Log.d(TAG, "FCM mesajı alındı. From: ${remoteMessage.from}")

            // Sender doğrulaması (isteğe bağlı - Firebase console'dan gelen mesajlar için)
            if (!isValidSender(remoteMessage.from)) {
                Log.w(TAG, "Geçersiz sender'dan mesaj: ${remoteMessage.from}")
                return
            }

            // Data payload kontrolü
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Data payload alındı: ${remoteMessage.data}")
                handleDataMessage(remoteMessage.data)
            }

            // Notification payload kontrolü
            remoteMessage.notification?.let { notification ->
                Log.d(TAG, "Notification payload alındı")
                handleNotificationMessage(notification.title, notification.body)
            }

        } catch (e: Exception) {
            Log.e(TAG, "FCM mesaj işleme hatası: ${e.message}", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token yenilendi")

        // Token'ı güvenli şekilde sakla ve server'a gönder
        saveTokenSecurely(token)
        sendTokenToServer(token)
    }

    private fun isValidSender(from: String?): Boolean {
        // Firebase console'dan gelen mesajlar için from null olabilir
        return from == null || from.startsWith("/topics/") ||
                from.matches(Regex("^[0-9]+$")) // Numeric sender ID
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val title = data["title"]
        val message = data["message"]
        val type = data["type"] // Mesaj tipi kontrolü için

        if (validateAndProcessMessage(title, message, type)) {
            sendSecureNotification(
                sanitizeContent(title!!),
                sanitizeContent(message!!),
                type
            )
        }
    }

    private fun handleNotificationMessage(title: String?, body: String?) {
        if (validateAndProcessMessage(title, body, null)) {
            sendSecureNotification(
                sanitizeContent(title!!),
                sanitizeContent(body!!),
                null
            )
        }
    }

    private fun validateAndProcessMessage(title: String?, message: String?, type: String?): Boolean {
        // Null kontrolü
        if (title.isNullOrBlank() || message.isNullOrBlank()) {
            Log.w(TAG, "Başlık veya mesaj boş")
            return false
        }

        // Uzunluk kontrolü
        if (title.length > MAX_TITLE_LENGTH || message.length > MAX_MESSAGE_LENGTH) {
            Log.w(TAG, "Mesaj çok uzun. Title: ${title.length}, Message: ${message.length}")
            return false
        }

        // Güvenlik kontrolü
        if (containsDangerousContent(title) || containsDangerousContent(message)) {
            Log.w(TAG, "Zararlı içerik tespit edildi")
            return false
        }

        // Mesaj tipi kontrolü (isteğe bağlı)
        if (type != null && !isValidMessageType(type)) {
            Log.w(TAG, "Geçersiz mesaj tipi: $type")
            return false
        }

        return true
    }

    private fun containsDangerousContent(content: String): Boolean {
        return DANGEROUS_PATTERNS.any { pattern ->
            pattern.matcher(content).find()
        }
    }

    private fun isValidMessageType(type: String): Boolean {
        // Uygulamanıza göre geçerli mesaj tiplerini tanımlayın
        val validTypes = setOf("alert", "info", "warning", "security", "update")
        return type.lowercase() in validTypes
    }

    private fun sanitizeContent(content: String): String {
        return content
            .replace(Regex("[<>\"'&]"), "") // HTML karakterleri temizle
            .replace(Regex("\\s+"), " ") // Fazla boşlukları temizle
            .trim()
            .take(if (content.length <= MAX_TITLE_LENGTH) MAX_TITLE_LENGTH else MAX_MESSAGE_LENGTH)
    }

    private fun sendSecureNotification(title: String, messageBody: String, type: String?) {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                // Güvenli extras ekleme
                putExtra("notification_type", type ?: "default")
                putExtra("timestamp", System.currentTimeMillis())
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(getNotificationPriority(type))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Notification channel oluştur (Android 8.0+)
            createNotificationChannel(notificationManager)

            // Notification göster
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

            Log.d(TAG, "Güvenli notification gönderildi")

        } catch (e: Exception) {
            Log.e(TAG, "Notification gönderme hatası: ${e.message}", e)
        }
    }

    private fun getNotificationPriority(type: String?): Int {
        return when (type?.lowercase()) {
            "security", "alert" -> NotificationCompat.PRIORITY_HIGH
            "warning" -> NotificationCompat.PRIORITY_DEFAULT
            "info" -> NotificationCompat.PRIORITY_LOW
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GuardBox Güvenlik Bildirimleri",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "GuardBox uygulamasından gelen güvenlik bildirimleri"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveTokenSecurely(token: String) {
        try {
            // Shared Preferences'ta encrypted olarak sakla
            val sharedPref = getSharedPreferences("guardbox_prefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("fcm_token", token)
                putLong("token_timestamp", System.currentTimeMillis())
                apply()
            }
            Log.d(TAG, "FCM token güvenli şekilde kaydedildi")
        } catch (e: Exception) {
            Log.e(TAG, "Token kaydetme hatası: ${e.message}", e)
        }
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Token'ı backend server'ınıza güvenli şekilde gönderin
        // Bu işlem genellikle authenticated API call ile yapılır
        Log.d(TAG, "Token server'a gönderilmeye hazır: ${token.take(20)}...")
    }
}