package com.zrj.png.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat


fun View.setVisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

object Debounced {
    @Volatile
    private var enabled: Boolean = true
    private val enableAgain = Runnable { enabled = true }

    fun canPerform(view: View): Boolean {
        if (enabled) {
            enabled = false
            view.post(enableAgain)
            return true
        }
        return false
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : View> T.click(click: (view: T) -> Unit) {
    setOnClickListener {
        if (Debounced.canPerform(it)) {
            click(it as T)
        }
    }
}

fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes message: Int) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.colorCompat(color: Int) = ContextCompat.getColor(this, color)

fun Context.drawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)