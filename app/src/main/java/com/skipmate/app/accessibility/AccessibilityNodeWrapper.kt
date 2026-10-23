package com.skipmate.app.accessibility

interface AccessibilityNodeWrapper {
    val text: CharSequence?
    val contentDescription: CharSequence?
    val viewIdResourceName: String?
    val packageName: CharSequence?
    val className: CharSequence?
    val isVisibleToUser: Boolean
    val isEnabled: Boolean
    val isClickable: Boolean
    val childCount: Int

    fun getChild(index: Int): AccessibilityNodeWrapper?
    fun getParent(): AccessibilityNodeWrapper?
    fun performAction(action: Int): Boolean
    fun recycle()

    companion object {
        const val ACTION_CLICK = 0x00000010
    }
}
