package com.example.stasy.mushrooms

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


class MainActivity : AppCompatActivity() {

    val IMAGE_GALLERY_REQUEST = 20
    val CAMERA_REQUEST_CODE = 228
    val CAMERA_PERMISSION_REQUEST_CODE = 4192
    private val imgPicture: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var camera_button = findViewById(R.id.button) as Button
        var gallery_button = findViewById(R.id.button2) as Button
        camera_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                val scrollingActivity = Intent(applicationContext, Camera::class.java)
                startActivity(scrollingActivity);
            }
        })
        gallery_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                // invoke the image gallery using an implict intent.
                val photoPickerIntent = Intent(Intent.ACTION_PICK)

                // where do we want to find the data?
                val pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val pictureDirectoryPath = pictureDirectory.getPath()
                // finally, get a URI representation
                val data = Uri.parse(pictureDirectoryPath)

                // set the data and type.  Get all image types.
                photoPickerIntent.setDataAndType(data, "image/*")

                // we will invoke this activity, and get something back from it.
                startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE) {
                Toast.makeText(this, "Image Saved.", Toast.LENGTH_LONG).show()
            }
            // if we are here, everything processed successfully.
            if (requestCode == IMAGE_GALLERY_REQUEST)
            {
                // if we are here, we are hearing back from the image gallery.

                // the address of the image on the SD Card.
                val imageUri = data.data

                // declare a stream to read the image data from the SD Card.
                val inputStream: InputStream?

                // we are getting an input stream, based on the URI of the image.
                try {
                    inputStream = contentResolver.openInputStream(imageUri!!)

                    // get a bitmap from the stream.
                    val image = BitmapFactory.decodeStream(inputStream)


                    // show the image to the user
                    if (imgPicture != null) {
                        imgPicture.setImageBitmap(image)
                        Toast.makeText(this, "Open IMAGE.", Toast.LENGTH_LONG).show()
                    }

                }
                catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    // show a message to the user indictating that the image is unavailable.
                    Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show()
                }

            }
        }
    }
}