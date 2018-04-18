package com.example.stasy.mushrooms

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import java.io.FileNotFoundException
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.widget.Toast
import java.io.InputStream
import android.provider.MediaStore
import android.Manifest.permission
import android.content.pm.PackageManager
import android.support.v4.app.NotificationCompat.getExtras
import android.support.v4.app.NotificationCompat.getExtras







class MainActivity : AppCompatActivity() {

    private val RESULT_OPEN_GALLERY = 1
    private val RESULT_OPEN_CAMERA = 0
    private var imgDecodableString: String? = null
    private val MY_CAMERA_PERMISSION_CODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var camera_button = findViewById(R.id.button) as Button
        var gallery_button = findViewById(R.id.button2) as Button
        camera_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, RESULT_OPEN_CAMERA)
                }

            }
        })
        gallery_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {


                val galleryIntent = Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, RESULT_OPEN_GALLERY);
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if(resultCode == Activity.RESULT_OK && null != data)
            {// When an Image is picked
            if (requestCode == RESULT_OPEN_GALLERY) {
                // Get the Image from data

                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                // Get the cursor
                val cursor = contentResolver.query(selectedImage!!,
                        filePathColumn, null, null, null)
                // Move to first row
                cursor!!.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                imgDecodableString = cursor.getString(columnIndex)
                cursor.close()
                Toast.makeText(this, "Image choosed",
                        Toast.LENGTH_LONG).show()

            }
            if (requestCode == RESULT_OPEN_CAMERA) {
                val extras = data.extras
                val imageBitmap = extras.get("data") as Bitmap
                Toast.makeText(this, "Image taken",
                        Toast.LENGTH_LONG).show()
            }
        }
        }catch (e: Exception) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show()
        }
    }
}