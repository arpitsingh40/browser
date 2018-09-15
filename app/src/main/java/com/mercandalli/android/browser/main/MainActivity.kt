package com.mercandalli.android.browser.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.StringRes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.mercandalli.android.browser.R
import com.mercandalli.android.browser.browser.BrowserView
import com.mercandalli.android.browser.keyboard.KeyboardUtils
import com.mercandalli.android.browser.settings.SettingsActivity
import com.mercandalli.android.libs.monetization.MonetizationGraph

class MainActivity : AppCompatActivity(), MainActivityContract.Screen {

    private val toolbar: View by bind(R.id.activity_main_toolbar)
    private val toolbarShadow: View by bind(R.id.activity_main_toolbar_shadow)
    private val webView: BrowserView by bind(R.id.activity_main_web_view)
    private val emptyView: View by bind(R.id.activity_main_empty_view)
    private val emptyTextView: TextView by bind(R.id.activity_main_empty_view_text)
    private val progress: ProgressBar by bind(R.id.activity_main_progress)
    private val input: EditText by bind(R.id.activity_main_search)
    private val more: View by bind(R.id.activity_main_more)
    private val fabClear: FloatingActionButton by bind(R.id.activity_main_fab_clear)
    private val videoCheckBox: CheckBox by bind(R.id.activity_main_video_check_box)

    private val browserWebViewListener = createBrowserWebViewListener()
    private val userAction = createUserAction()
    private var forceDestroy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (MonetizationGraph.startOnBoardingIfNeeded(this)) {
            forceDestroy = true
            finish()
            MainApplication.onOnBoardingStarted()
            return
        }
        setContentView(R.layout.activity_main)
        more.setOnClickListener { showOverflowPopupMenu(more) }
        webView.browserWebViewListener = browserWebViewListener
        input.setOnEditorActionListener(createOnEditorActionListener())
        videoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            userAction.onVideoCheckedChanged(isChecked)
        }

        if (savedInstanceState == null) {
            navigateHome()
        }

        fabClear.setOnClickListener {
            userAction.onFabClearClicked()
        }
        userAction.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (forceDestroy) {
            return
        }
        webView.browserWebViewListener = null
        userAction.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        userAction.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (forceDestroy) {
            return
        }
        userAction.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        userAction.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            userAction.onBackPressed(emptyView.visibility == View.VISIBLE)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun showUrl(url: String) {
        webView.load(url)
    }

    override fun reload() {
        webView.reload()
    }

    override fun back() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    override fun quit() {
        finish()
    }

    override fun navigateHome() {
        webView.load("https://www.google.com/")
    }

    override fun navigateSettings() {
        SettingsActivity.start(this)
    }

    override fun clearData() {
        webView.clearData()
    }

    override fun showClearDataMessage() {
        showSnackbar(R.string.activity_main_data_clear, Snackbar.LENGTH_SHORT)
    }

    override fun showLoader(progressPercent: Int) {
        progress.visibility = VISIBLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(progressPercent, true)
        } else {
            progress.progress = progressPercent
        }
    }

    override fun hideLoader() {
        progress.visibility = GONE
    }

    override fun showKeyboard() {
        input.postDelayed({
            input.isFocusableInTouchMode = true
            input.requestFocus()
            KeyboardUtils.showSoftInput(input)
        }, 200)
    }

    override fun hideKeyboard() {
        KeyboardUtils.hideSoftInput(input)
    }

    override fun resetSearchInput() {
        input.setText("")
    }

    override fun setWindowBackgroundColorRes(@ColorRes colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        window.setBackgroundDrawable(ColorDrawable(color))
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun setStatusBarBackgroundColorRes(@ColorRes colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        window.statusBarColor = color
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun setStatusBarDark(statusBarDark: Boolean) {
        val flags = window.decorView.systemUiVisibility
        window.decorView.systemUiVisibility = if (statusBarDark)
            flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        else
            flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    override fun setToolbarBackgroundColorRes(@ColorRes colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        toolbar.setBackgroundColor(color)
    }

    override fun setInputTextColorRes(@ColorRes colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        input.setTextColor(color)
        emptyTextView.setTextColor(color)
    }

    override fun showFab() {
        if (!fabClear.isShown) {
            fabClear.show()
        }
    }

    override fun hideFab() {
        fabClear.hide()
    }

    override fun showWebView() {
        webView.visibility = View.VISIBLE
    }

    override fun hideWebView() {
        webView.visibility = View.GONE
    }

    override fun showEmptyView() {
        emptyView.visibility = View.VISIBLE
        videoCheckBox.visibility = View.VISIBLE
    }

    override fun hideEmptyView() {
        emptyView.visibility = View.GONE
        videoCheckBox.visibility = View.GONE
    }

    override fun showToolbar() {
        toolbar.visibility = View.VISIBLE
        toolbarShadow.visibility = View.VISIBLE
    }

    override fun hideToolbar() {
        toolbar.visibility = View.GONE
        toolbarShadow.visibility = View.GONE
    }

    private fun showSnackbar(@StringRes text: Int, duration: Int) {
        Snackbar.make(window.decorView.findViewById(android.R.id.content), text, duration).show()
    }

    private fun createBrowserWebViewListener() = object : BrowserView.BrowserWebViewListener {
        override fun onPageFinished() {
            userAction.onPageLoadProgressChanged(100)
        }

        override fun onProgressChanged() {
            userAction.onPageLoadProgressChanged(webView.progress)
        }

        override fun onPageTouched() {
            userAction.onPageTouched()
        }
    }

    private fun createOnEditorActionListener() = TextView.OnEditorActionListener { v, actionId, event ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                event.action == KeyEvent.ACTION_DOWN &&
                event.keyCode == KeyEvent.KEYCODE_ENTER) {
            userAction.onSearchPerformed(v!!.text.toString())
            return@OnEditorActionListener true
        }
        false
    }

    private fun showOverflowPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view, Gravity.END)
        popupMenu.menuInflater.inflate(R.menu.menu_browser, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener(createOnMenuItemClickListener())
        popupMenu.show()
    }

    private fun createOnMenuItemClickListener() = PopupMenu.OnMenuItemClickListener { item ->
        when (item!!.itemId) {
            R.id.menu_browser_home -> userAction.onHomeClicked()
            R.id.menu_browser_clear_data -> userAction.onClearDataClicked()
            R.id.menu_browser_settings -> userAction.onSettingsClicked()
        }
        false
    }

    private fun createUserAction(): MainActivityContract.UserAction {
        val themeManager = ApplicationGraph.getThemeManager()
        val searchEngineManager = ApplicationGraph.getSearchEngineManager()
        return MainActivityPresenter(
                this,
                themeManager,
                searchEngineManager
        )
    }

    private fun <T : View> bind(@IdRes res: Int): Lazy<T> {
        @Suppress("UNCHECKED_CAST")
        return lazy(LazyThreadSafetyMode.NONE) { findViewById<T>(res) }
    }

    companion object {

        @JvmStatic
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            if (context !is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }
}
