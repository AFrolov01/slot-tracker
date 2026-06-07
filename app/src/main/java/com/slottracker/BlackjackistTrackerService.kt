package com.slottracker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BlackjackistTrackerService : AccessibilityService() {

    companion object {
        var isRunning = false
        var lastBalance = 0
        var currentBalance = 0
    }

    private lateinit var statsManager: StatsManager
    private val handler = Handler(Looper.getMainLooper())
    private var lastDetectedBalance = 0
    private var stableCount = 0
    private var lastSpinTime = 0L
    private var spinCounter = 0
    private var bonusFlag = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        statsManager = StatsManager(this)
        isRunning = true
        lastBalance = statsManager.startBalance
        currentBalance = lastBalance
        lastDetectedBalance = lastBalance
        spinCounter = statsManager.getSpins().size

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                         AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            packageNames = arrayOf("com.kamagames.blackjack")
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        Log.d("SlotTracker", "Сервис подключен. Начальный баланс: $lastBalance")
        broadcastUpdate()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode = rootInActiveWindow ?: return
        try {
            val balance = findBalanceInNode(rootNode)
            if (balance != null && balance > 0) {
                processBalance(balance)
            }
        } finally {
            rootNode.recycle()
        }
    }

    private fun findBalanceInNode(node: AccessibilityNodeInfo): Int? {
        // Стратегия 1: ищем TextView с числами похожими на баланс
        val text = node.text?.toString() ?: ""
        val content = node.contentDescription?.toString() ?: ""

        // Проверяем, похоже ли это на число баланса (обычно 1000+ или формат с K/M)
        val combined = "$text $content"
        val number = parseBalance(combined)
        if (number != null && number >= 100) {
            return number
        }

        // Рекурсивный обход детей
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            try {
                val result = findBalanceInNode(child)
                if (result != null) return result
            } finally {
                child.recycle()
            }
        }
        return null
    }

    private fun parseBalance(text: String): Int? {
        // Очищаем строку
        val clean = text.replace(",", "")
                        .replace(" ", "")
                        .replace("$", "")
                        .replace("₽", "")
                        .replace(" chips", "")
                        .replace(" balance", "")
                        .trim()

        // Проверяем K/M
        val multiplier = when {
            clean.endsWith("K", true) -> 1000
            clean.endsWith("M", true) -> 1000000
            else -> 1
        }

        val numStr = if (multiplier > 1) clean.dropLast(1) else clean

        return try {
            (numStr.toDouble() * multiplier).toInt()
        } catch (e: Exception) {
            null
        }
    }

    private fun processBalance(newBalance: Int) {
        if (newBalance == lastDetectedBalance) {
            stableCount++
        } else {
            stableCount = 0
            lastDetectedBalance = newBalance
            return  // Ждем стабилизации
        }

        if (stableCount < 2) return  // Нужно 2 одинаковых значения подряд
        if (newBalance == currentBalance) return  // Ничего не изменилось

        val now = System.currentTimeMillis()
        if (now - lastSpinTime < 1500) return  // Защита от дублей
        lastSpinTime = now

        val diff = newBalance - currentBalance
        val betSize = statsManager.betSize

        // Определяем тип спина
        val spinType: String
        val bet: Int
        val win: Int

        when {
            diff < -betSize * 0.8 -> {
                // Проигрыш
                spinType = "loss"
                bet = betSize
                win = 0
            }
            diff > betSize * 10 -> {
                // Бонуска
                spinType = "bonus"
                bet = 0
                win = diff
            }
            diff > 0 -> {
                // Выигрыш
                spinType = "win"
                bet = betSize
                win = diff
            }
            diff >= -betSize * 0.8 && diff < 0 -> {
                // Частичный возврат или мелкий выигрыш
                spinType = "win"
                bet = betSize
                win = diff + betSize  // Чистый выигрыш
            }
            else -> {
                // Ничья или шум
                spinType = "win"
                bet = betSize
                win = betSize
            }
        }

        // Если стоит флаг бонуски
        if (bonusFlag && win > 0) {
            spinType = "bonus"
            bet = 0
            bonusFlag = false
        }

        val mult = if (bet > 0) win.toDouble() / bet else win.toDouble() / betSize

        spinCounter++
        val spin = SpinData(
            id = spinCounter,
            timestamp = now,
            type = spinType,
            bet = bet,
            win = win,
            balanceAfter = newBalance,
            multiplier = mult
        )

        statsManager.addSpin(spin)
        currentBalance = newBalance
        isRunning = true

        Log.d("SlotTracker", "Спин #$spinCounter: $spinType | Bet=$bet | Win=$win | Balance=$newBalance | x${String.format("%.2f", mult)}")
        broadcastUpdate()
    }

    private fun broadcastUpdate() {
        val intent = Intent("com.slottracker.UPDATE")
        intent.putExtra("balance", currentBalance)
        sendBroadcast(intent)
    }

    fun setBonusFlag() {
        bonusFlag = true
    }

    override fun onInterrupt() {
        isRunning = false
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }
}
