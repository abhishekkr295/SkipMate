package com.skipmate.app.accessibility

class TestAccessibilityNode(
    override var text: CharSequence? = null,
    override var contentDescription: CharSequence? = null,
    override var viewIdResourceName: String? = null,
    override var packageName: CharSequence? = "com.google.android.youtube",
    override var className: CharSequence? = "android.widget.Button",
    override var isVisibleToUser: Boolean = true,
    override var isEnabled: Boolean = true,
    override var isClickable: Boolean = true,
    private val childrenList: MutableList<AccessibilityNodeWrapper> = mutableListOf(),
    private var parentNode: AccessibilityNodeWrapper? = null,
    var actionClickReturn: Boolean = true,
    var shouldThrowOnAction: Boolean = false
) : AccessibilityNodeWrapper {

    var clickActionCount: Int = 0
        private set

    var recycleCount: Int = 0
        private set

    override val childCount: Int
        get() = childrenList.size

    override fun getChild(index: Int): AccessibilityNodeWrapper? {
        return if (index in childrenList.indices) childrenList[index] else null
    }

    override fun getParent(): AccessibilityNodeWrapper? {
        return parentNode
    }

    fun addChild(child: TestAccessibilityNode) {
        childrenList.add(child)
        child.parentNode = this
    }

    override fun performAction(action: Int): Boolean {
        if (shouldThrowOnAction) {
            throw IllegalStateException("Node is stale or detached from window")
        }
        if (action == AccessibilityNodeWrapper.ACTION_CLICK) {
            clickActionCount++
            return actionClickReturn
        }
        return false
    }

    override fun recycle() {
        recycleCount++
    }
}
