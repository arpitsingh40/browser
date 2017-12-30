package com.mercandalli.android.browser

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var webView: MainWebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.activity_main_web_view)
        webView!!.loadUrl("http://www.google.com/")
    }
}
