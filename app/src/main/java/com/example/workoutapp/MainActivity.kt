package com.example.workoutapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.Toast
import com.example.workoutapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // view binding
    //ActivityMain is the xml file name and Binding is the keyword that follows
    // e.g. if there is an activity named exercise then it should be ActivityExerciseBinding
    private var binding: ActivityMainBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // using the layout inflater to inflate the main activity
        // the binding object contains the entire activity_main xml file
        binding= ActivityMainBinding.inflate(layoutInflater)

        //setContentView(R.layout.activity_main)  -->  original
        setContentView(binding?.root)

        //val flStartButton : FrameLayout=findViewById(R.id.flStart)
        // instead of using findViewById we can now directly use binding object to access the ids

        binding?.flStart?.setOnClickListener {
            //Toast.makeText(this, "Start the exercise", Toast.LENGTH_SHORT).show()

            // move to ExerciseActivity after clicking on Start btn by using Intent
            val intent=Intent(this, ExerciseActivity::class.java)
            startActivity(intent)
        }

        binding?.flBMI?.setOnClickListener {
            val intent=Intent(this, BMIActivity::class.java)
            startActivity(intent)
        }

        binding?.flHistory?.setOnClickListener {
            val intent=Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // unassigning binding to avoid memory leak
        binding=null
    }
}