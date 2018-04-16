package com.example.stasy.mushrooms

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.json.JSONObject
import android.R.raw
import android.util.Log
import android.widget.TextView
import java.io.*


class Info : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val mushroom = intent.getStringExtra("Mushroom")
        val info : ArrayList<String> = get_info(mushroom)

        val mushroomView = findViewById(R.id.textView) as TextView
        val poisonousView = findViewById(R.id.textView2) as TextView
        val dimensionView = findViewById(R.id.textView4) as TextView
        val descriptionView = findViewById(R.id.textView6) as TextView

        mushroomView.setText(info[0])
        poisonousView.setText(info[1])
        dimensionView.setText(info[2])
        descriptionView.setText(info[3])
    }


    fun get_info(mushroom : String): ArrayList<String> {
        //Get Data From Text Resource File Contains Json Data.
        val inputStream = resources.openRawResource(R.raw.data)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val info : ArrayList<String> = ArrayList<String>()

        var ctr: Int
        try
        {
            ctr = inputStream.read()
            while (ctr != -1)
            {
                byteArrayOutputStream.write(ctr)
                ctr = inputStream.read()
            }
            inputStream.close()
        }
        catch (e: IOException)
        {
            e.printStackTrace()
        }

        Log.v("Text Data", byteArrayOutputStream.toString())
        try
        {
            // Parse the data into jsonobject to get original data in form of json.
            val jObject = JSONObject(byteArrayOutputStream.toString())
            val description = jObject.getString(mushroom)

            val tokens = description.split(" ")
            info.add(tokens[0] + " " + tokens[1])
            info.add(tokens[2])

            val dimension_word :String = "Dimensions"
            val description_word :String = "Description"

            val dim_ind :Int = description.lastIndexOf(dimension_word)+2
            val descr_ind :Int = description.lastIndexOf(description_word)+2
            info.add(description.substring(dim_ind, descr_ind-description_word.length-1))
            info.add(description.substring(descr_ind))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return info
    }
}
