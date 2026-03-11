package br.com.scrubs.presentation.biometric

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

/**
 * Mantém uma WeakReference para a Activity atual.
 * Atualizado em MainActivity.onResume() / onPause().
 * WeakReference evita vazamento de memória.
 */
object ActivityProvider {
    private var ref: WeakReference<FragmentActivity>? = null

    fun set(activity: FragmentActivity) {
        ref = WeakReference(activity)
    }

    fun clear() {
        ref = null
    }

    fun get(): FragmentActivity? = ref?.get()
}