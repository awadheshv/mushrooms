package com.example.stasy.mushrooms

import android.graphics.Bitmap

interface Classifier {
    fun recognizeImage(bitmap: Bitmap): Result
}