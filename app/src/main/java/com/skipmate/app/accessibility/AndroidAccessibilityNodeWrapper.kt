package com.skipmate.app.accessibility

import android.view.accessibility.AccessibilityNodeInfo

class AndroidAccessibilityNodeWrapper(
    private val realNode: AccessibilityNodeInfo?
) : AccessibilityNodeWrapper {

    override val text: CharSequence?
        get() = realNode?.text

    override val contentDescription: CharSequence?
        get() = realNode?.contentDescription

    override val viewIdResourceName: String?
        get() = realNode?.viewIdResourceName

    override val packageName: CharSequence?
        get() = realNode?.packageName

    override val className: CharSequence?
        get() = realNode?.className

    override val isVisibleToUser: Boolean
        get() = realNode?.isVisibleToUser ?: false

    override val isEnabled: Boolean
        get() = realNode?.isEnabled ?: false

    override val isClickable: Boolean
        get() = realNode?.isClickable ?: false

    override val childCount: Int
        get() = realNode?.childCount ?: 0

    override fun getChild(index: Int): AccessibilityNodeWrapper? {
        val child = realNode?.getChild(index) ?: return null
        return AndroidAccessibilityNodeWrapper(child)
    }

    override fun getParent(): AccessibilityNodeWrapper? {
        val parent = realNode?.parent ?: return null
        return AndroidAccessibilityNodeWrapper(parent)
    }

    override fun performAction(action: Int): Boolean {
        return realNode?.performAction(action) ?: false
    }

    override fun recycle() {
        @Suppress("DEPRECATION")
        realNode?.recycle()
    }

    val rawNode: AccessibilityNodeInfo?
        get() = realNode
}
