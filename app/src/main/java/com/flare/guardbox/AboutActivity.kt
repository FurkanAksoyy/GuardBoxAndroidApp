package com.flare.guardbox

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupFAQSection()
        setupContactInfo()
        setupVersionInfo()
    }

    private fun setupFAQSection() {
        // FAQ bölümü kurulumu
        val faqList = listOf(
            Pair("GuardBox nedir?", "GuardBox, paketlerinizi güvende tutan akıllı bir IoT cihazıdır."),
            Pair("Nasıl çalışır?", "GuardBox, teslimat personelinin paketi güvenli şekilde bırakmasını sağlar ve sahibi gelene kadar korur."),
            Pair("Paket geldiğini nasıl anlarım?", "Uygulama üzerinden anlık bildirimler alacaksınız."),
            Pair("Pil ömrü ne kadardır?", "Tam şarjla yaklaşık 30 gün çalışır."),
            Pair("Ürünü nasıl satın alabilirim?", "www.guardbox.com adresinden sipariş verebilirsiniz.")
        )

        val faqContainer = findViewById<LinearLayout>(R.id.faqContainer)

        for (faq in faqList) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_faq, faqContainer, false)
            view.findViewById<TextView>(R.id.tvQuestion).text = faq.first
            view.findViewById<TextView>(R.id.tvAnswer).text = faq.second

            faqContainer.addView(view)
        }
    }

    private fun setupContactInfo() {
        // İletişim bilgileri
        val tvContactInfo = findViewById<TextView>(R.id.tvContactInfo)
        tvContactInfo.text = "Email: iletisim@guardbox.com\nTel: +90 555 123 4567\nAdres: Teknoloji Vadisi, 34000 İstanbul"
    }

    private fun setupVersionInfo() {
        // Sürüm bilgileri
        val tvVersionInfo = findViewById<TextView>(R.id.tvVersionInfo)
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        tvVersionInfo.text = "Sürüm: $versionName\nCopyright © 2025 GuardBox\nTüm hakları saklıdır."
    }
}