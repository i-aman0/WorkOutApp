package com.example.workoutapp

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.workoutapp.databinding.ActivityExerciseBinding
import com.example.workoutapp.databinding.DialogCustomBackConfirmationBinding
import org.w3c.dom.Text
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private var restTimer : CountDownTimer?=null
    private var restProgress=0
    private var restTimerDuration : Long=10

    private var exerciseTimer : CountDownTimer?=null
    private var exerciseProgress=0
    private var exerciseTimerDuration : Long=30

    private var exerciseList: ArrayList<ExerciseModel>?=null
    private var currentExercisePosition=-1

    private var tts : TextToSpeech?=null

    private var player: MediaPlayer?=null

    private var exerciseAdapter : ExerciseStatusAdapter?=null

    private var binding : ActivityExerciseBinding?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        // adding toolbar to the activity
        setSupportActionBar(binding?.toolbarExercise)

        // adding back btn to the toolbar in the activity
        if(supportActionBar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        // initialising the exerciseList with the default exercise list created inside Constants
        exerciseList=Constants.defaultExerciseList()

        tts= TextToSpeech(this, this)

        binding?.toolbarExercise?.setNavigationOnClickListener {

            // when the back btn in the toolbar is clicked, do the same thing as if back btn is clicked on the device
            customDialogForBackButton()
        }

        setUpRestView()
        setUpExerciseStatusRecyclerView()
    }

    override fun onBackPressed() {
        customDialogForBackButton()
        //super.onBackPressed() --> closes the current activity by default, so not needed here
    }

    private fun customDialogForBackButton(){
        val customDialog=Dialog(this)

        // creating the binding for custom dialog
        val dialogBinding=DialogCustomBackConfirmationBinding.inflate(layoutInflater)

        // custom dialog should look like the xml file
        customDialog.setContentView(dialogBinding.root)

        // the dialog should not be canceled when the user clicks outside it
        customDialog.setCanceledOnTouchOutside(false)

        // setting the onclick listener to the Yes and No buttons
        dialogBinding.btnYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }

        dialogBinding.btnNo.setOnClickListener {
            customDialog.dismiss()
        }

        // showing the custom dialog
        customDialog.show()
    }

    private fun setUpExerciseStatusRecyclerView(){
        binding?.rvExerciseStatus?.layoutManager=LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter= ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter=exerciseAdapter
    }

    private fun setUpRestView(){

        binding?.flRestView?.visibility=View.VISIBLE
        binding?.tvTitle?.visibility=View.VISIBLE
        binding?.tvExerciseName?.visibility=View.INVISIBLE
        binding?.flExerciseView?.visibility=View.INVISIBLE
        binding?.ivImage?.visibility=View.INVISIBLE
        binding?.tvUpcomingLabel?.visibility=View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility=View.VISIBLE

        binding?.tvUpcomingExerciseName?.text=exerciseList!![currentExercisePosition+1].getName()


        if(restTimer!=null){
            restTimer?.cancel()
            restProgress=0
        }

        //speakText("Take Rest")

        try {
            val soundURI = Uri.parse("android.resource://com.example.workoutapp/"+R.raw.press_start)
            player=MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping=false
            player?.start()

        }catch (e: Exception){
            e.printStackTrace()
        }

        setRestProgressBar()
    }

    private fun setUpExerciseView(){
        binding?.flRestView?.visibility=View.INVISIBLE
        binding?.tvTitle?.visibility=View.INVISIBLE
        binding?.tvExerciseName?.visibility=View.VISIBLE
        binding?.flExerciseView?.visibility=View.VISIBLE
        binding?.ivImage?.visibility=View.VISIBLE
        binding?.tvUpcomingLabel?.visibility=View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility=View.INVISIBLE

        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseProgress=0
        }

        speakText(exerciseList!![currentExercisePosition].getName())

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text=exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }

    private fun setRestProgressBar(){
        binding?.progressBar?.progress=restProgress

        restTimer = object : CountDownTimer(restTimerDuration*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text=(10-restProgress).toString()
            }

            override fun onFinish() {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()

                // when the rest view is finished, call the method to set up exercise view
                setUpExerciseView()
            }
        }.start()
    }

    private fun setExerciseProgressBar(){
        binding?.progressBarExercise?.progress=exerciseProgress

        exerciseTimer = object : CountDownTimer(exerciseTimerDuration*1000, 1000){
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding?.progressBarExercise?.progress = 30 - exerciseProgress
                binding?.tvTimerExercise?.text=(30-exerciseProgress).toString()
            }

            override fun onFinish() {



                if(currentExercisePosition < exerciseList?.size!!-1){
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseAdapter!!.notifyDataSetChanged()
                    setUpRestView()
                }
                else{
                    finish()
                    val intent=Intent(this@ExerciseActivity, FinishActivity::class.java)
                    startActivity(intent)
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()

        if(restTimer!=null){
            restTimer?.cancel()
            restProgress=0
        }

        if(exerciseTimer!=null){
            exerciseTimer?.cancel()
            exerciseProgress=0
        }

        // shutting down the text to speech feature when activity is destroyed
        if(tts!=null){
            tts!!.stop()
            tts!!.shutdown()
        }

        if(player!=null){
            player!!.stop()
        }

        binding=null
    }

    override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS){
            val result=tts?.setLanguage(Locale.US)

            if(result==TextToSpeech.LANG_NOT_SUPPORTED || result==TextToSpeech.LANG_MISSING_DATA){
                Log.e("TTS", "The language specified is not supported")
            }
        }
        else{
            Log.e("TTS", "Initialisation failed")
        }
    }

    private fun speakText(text: String){
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}