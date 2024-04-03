package com.example.booky

import android.content.Context
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


class DatabaseManager(private val context: Context) {
    var queue: RequestQueue = Volley.newRequestQueue(context)

}