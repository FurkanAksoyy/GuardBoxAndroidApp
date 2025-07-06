package com.flare.guardbox

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flare.guardbox.adapters.ActivityLogAdapter
import com.flare.guardbox.model.ActivityLog
import com.flare.guardbox.model.User
import com.flare.guardbox.utils.FirebaseHelper
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.chip.Chip
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList
import java.util.Calendar

class HistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var chipToday: Chip
    private lateinit var chipWeek: Chip
    private lateinit var chipMonth: Chip
    private lateinit var chipAll: Chip
    private val firebaseHelper = FirebaseHelper.getInstance()
    private val activityLogs = mutableListOf<ActivityLog>()
    private val allActivityLogs = mutableListOf<ActivityLog>()
    private lateinit var activityLogAdapter: ActivityLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize views
        rvHistory = findViewById(R.id.rvHistory)
        chipToday = findViewById(R.id.chipToday)
        chipWeek = findViewById(R.id.chipWeek)
        chipMonth = findViewById(R.id.chipMonth)
        chipAll = findViewById(R.id.chipAll)

        // Setup RecyclerView
        activityLogAdapter = ActivityLogAdapter(activityLogs)
        rvHistory.layoutManager = LinearLayoutManager(this)
        rvHistory.adapter = activityLogAdapter

        // Setup chip listeners
        setupChipListeners()

        // Get user's device ID and load logs
        getUserDeviceId()
    }

    private fun setupChipListeners() {
        chipToday.setOnClickListener {
            filterLogsByDate(FilterType.TODAY)
        }

        chipWeek.setOnClickListener {
            filterLogsByDate(FilterType.WEEK)
        }

        chipMonth.setOnClickListener {
            filterLogsByDate(FilterType.MONTH)
        }

        chipAll.setOnClickListener {
            // Tüm aktiviteleri göster
            activityLogs.clear()
            activityLogs.addAll(allActivityLogs)
            activityLogAdapter.notifyDataSetChanged()
        }
    }

    private fun getUserDeviceId() {
        val userId = firebaseHelper.getCurrentUser()?.uid
        if (userId != null) {
            firebaseHelper.getUserProfile(userId, object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null && user.deviceIds.isNotEmpty()) {
                        loadAllActivityLogs(user.deviceIds[0])
                    } else {
                        // Henüz bir cihaz kaydedilmemiş, demo verilerle göster
                        loadDemoActivityLogs()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@HistoryActivity,
                        "Kullanıcı bilgileri yüklenemedi: ${databaseError.message}",
                        Toast.LENGTH_SHORT).show()
                    loadDemoActivityLogs()
                }
            })
        } else {
            // Kullanıcı giriş yapmamış, demo verilerle göster
            loadDemoActivityLogs()
        }
    }

    private fun loadDemoActivityLogs() {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L // 1 gün (milisaniye)

        allActivityLogs.add(ActivityLog("PACKAGE_DELIVERED", now, "Paket teslim edildi", "guardbox1"))
        allActivityLogs.add(ActivityLog("BOX_OPENED", now - (2 * oneDay), "Kutu açıldı", "guardbox1"))
        allActivityLogs.add(ActivityLog("BOX_LOCKED", now - (2 * oneDay + 10 * 60 * 1000), "Kutu kilitlendi", "guardbox1"))
        allActivityLogs.add(ActivityLog("TAMPERING_DETECTED", now - (3 * oneDay), "Kurcalama tespit edildi", "guardbox1"))
        allActivityLogs.add(ActivityLog("PACKAGE_DELIVERED", now - (5 * oneDay), "Paket teslim edildi", "guardbox1"))
        allActivityLogs.add(ActivityLog("BOX_OPENED", now - (5 * oneDay + 3 * 60 * 60 * 1000), "Kutu açıldı", "guardbox1"))
        allActivityLogs.add(ActivityLog("BOX_LOCKED", now - (5 * oneDay + 3 * 60 * 60 * 1000 + 5 * 60 * 1000), "Kutu kilitlendi", "guardbox1"))

        // Listeyi tarihe göre sırala (en yeni başta)
        allActivityLogs.sortByDescending { it.timestamp }

        // Tüm kayıtları göster
        activityLogs.clear()
        activityLogs.addAll(allActivityLogs)
        activityLogAdapter.notifyDataSetChanged()

        // Varsayılan olarak bugünün kayıtlarını göster
        chipToday.isChecked = true
        filterLogsByDate(FilterType.TODAY)
    }

    // MPAndroidChart eklemek isterseniz chart layout'a eklenip bu fonksiyon çağrılmalı
    /*
    private fun setupActivityChart() {
        val chart = findViewById<LineChart>(R.id.activityChart)

        // Örnek veri
        val entries = ArrayList<Entry>()
        for (i in 0..6) {
            entries.add(Entry(i.toFloat(), (0..5).random().toFloat()))
        }

        val dataSet = LineDataSet(entries, "Haftalık Aktiviteler")
        dataSet.color = Color.parseColor("#4D8FFB")
        dataSet.valueTextColor = Color.WHITE

        val lineData = LineData(dataSet)
        chart.data = lineData
        chart.description.isEnabled = false
        chart.axisLeft.textColor = Color.WHITE
        chart.axisRight.isEnabled = false
        chart.xAxis.textColor = Color.WHITE

        chart.invalidate()
    }
    */

    private fun loadAllActivityLogs(deviceId: String) {
        firebaseHelper.getActivityLogs(deviceId, object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                allActivityLogs.clear()
                for (snapshot in dataSnapshot.children) {
                    val log = snapshot.getValue(ActivityLog::class.java)
                    if (log != null) {
                        allActivityLogs.add(log)
                    }
                }

                // Eğer veri yoksa demo veriler yükle
                if (allActivityLogs.isEmpty()) {
                    loadDemoActivityLogs()
                    return
                }

                // Tarihe göre sırala (en yeni başta)
                allActivityLogs.sortByDescending { it.timestamp }

                // Tüm kayıtları göster
                activityLogs.clear()
                activityLogs.addAll(allActivityLogs)
                activityLogAdapter.notifyDataSetChanged()

                // Varsayılan olarak bugünün kayıtlarını göster
                chipToday.isChecked = true
                filterLogsByDate(FilterType.TODAY)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@HistoryActivity,
                    "Aktivite kayıtları yüklenemedi: ${databaseError.message}",
                    Toast.LENGTH_SHORT).show()

                // Hata durumunda demo veriler yükle
                loadDemoActivityLogs()
            }
        })
    }

    private enum class FilterType {
        TODAY, WEEK, MONTH
    }

    private fun filterLogsByDate(filterType: FilterType) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startOfDay = calendar.timeInMillis

        val startTime = when (filterType) {
            FilterType.TODAY -> startOfDay
            FilterType.WEEK -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                calendar.timeInMillis
            }
            FilterType.MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.timeInMillis
            }
        }

        // Filtrelenmiş aktivite günlükleri
        val filteredLogs = allActivityLogs.filter { it.timestamp >= startTime }

        // Listeyi güncelle
        activityLogs.clear()
        activityLogs.addAll(filteredLogs)
        activityLogAdapter.notifyDataSetChanged()

        // Eğer bu filtrede hiç veri yoksa kullanıcıyı bilgilendir
        if (filteredLogs.isEmpty()) {
            Toast.makeText(this,
                when(filterType) {
                    FilterType.TODAY -> "Bugün için aktivite kaydı bulunamadı"
                    FilterType.WEEK -> "Bu hafta için aktivite kaydı bulunamadı"
                    FilterType.MONTH -> "Bu ay için aktivite kaydı bulunamadı"
                },
                Toast.LENGTH_SHORT).show()
        }
    }
}