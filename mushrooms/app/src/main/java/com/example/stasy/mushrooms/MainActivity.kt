package com.example.stasy.mushrooms

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.Toast
import android.provider.MediaStore
import java.io.File
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import pub.devrel.easypermissions.EasyPermissions


class MainActivity : AppCompatActivity() {

    private val RESULT_OPEN_GALLERY = 1
    private val REQUEST_OPEN_CAMERA = 0
    private var imgDecodableString: String? = null
    private val MY_CAMERA_PERMISSION_CODE = 100

    private val handler = Handler()
    private lateinit var classifier: Classifier
    private var photoFilePath = ""
    private val galleryPermissions = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_FILE)

        var camera_button = findViewById(R.id.button) as Button
        var gallery_button = findViewById(R.id.button2) as Button
        createClassifier()
        camera_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                //photoFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/${System.currentTimeMillis()}.jpg"
                //val currentPhotoUri = getUriFromFilePath(this, photoFilePath)

                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
               // takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_OPEN_CAMERA)
                }

            }
        })
        gallery_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {


                val galleryIntent = Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(galleryIntent, RESULT_OPEN_GALLERY);
            }
        })
    }

    private fun createClassifier() {
        classifier = ImageClassifierFactory.create(
                assets,
                GRAPH_FILE_PATH,
                LABELS_FILE_PATH,
                IMAGE_SIZE,
                GRAPH_INPUT_NAME,
                GRAPH_OUTPUT_NAME
        )
    }
    private fun classifyPhoto(imageBitmap: Bitmap) {
       val photoBitmap = imageBitmap
        // / val photoBitmap = BitmapFactory.decodeFile(file.absolutePath)
        val croppedBitmap = getCroppedBitmap(photoBitmap)
        classifyAndShowResult(croppedBitmap)
    }

    private fun classifyAndShowResult(croppedBitmap: Bitmap) {
        runInBackground(
                Runnable {
                    val result = classifier.recognizeImage(croppedBitmap)
                    showResult(result)
                })
    }

    @Synchronized
    private fun runInBackground(runnable: Runnable) {
        handler.post(runnable)
    }

    private fun showResult(result: Result) {
        val intent = Intent(applicationContext, Info::class.java)
        intent.putExtra("Mushroom", result.result)
        startActivity(intent)
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
                    if (EasyPermissions.hasPermissions(this, *galleryPermissions)) {
                        val imageBitmap = BitmapFactory.decodeFile(imgDecodableString)
                        classifyPhoto(imageBitmap)
                    } else {
                        EasyPermissions.requestPermissions(this, "Access for storage",
                                101, *galleryPermissions);
                    }
                }
                if (requestCode == REQUEST_OPEN_CAMERA) {
                    val extras = data.extras
                    val imageBitmap = extras.get("data") as Bitmap
                    classifyPhoto(imageBitmap)
                }
           }
        }catch (e: Exception) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show()
        }
    }
}