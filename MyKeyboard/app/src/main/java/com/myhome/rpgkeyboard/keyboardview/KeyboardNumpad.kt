package com.myhome.rpgkeyboard.keyboardview

import android.content.Context
import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import android.media.AudioManager
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import com.myhome.rpgkeyboard.*

class KeyboardNumpad{

    companion object {
        lateinit var numpadLayout:LinearLayout
        lateinit var inputConnection: InputConnection
        lateinit var keyboardInterationListener: KeyboardInterationListener
        lateinit var context: Context
        lateinit var vibrator: Vibrator
        var buttons:MutableList<Button> = mutableListOf<Button>()

        val firstLineText = listOf<String>("1", "2", "3","DEL")
        val secondLineText = listOf<String>("4", "5", "6", "Enter")
        val thirdLineText = listOf<String>("7","8","9",".")
        val fourthLineText = listOf<String>("-", "0", ",", "")
        val myKeysText = ArrayList<List<String>>()
        val layoutLines = ArrayList<LinearLayout>()

        fun newInstance(context:Context, layoutInflater: LayoutInflater, inputConnection: InputConnection, keyboardInterationListener: KeyboardInterationListener): LinearLayout {
            numpadLayout = layoutInflater.inflate(R.layout.keyboard_numpad, null) as LinearLayout
            Companion.inputConnection = inputConnection
            Companion.keyboardInterationListener = keyboardInterationListener
            Companion.context = context
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
            val height = sharedPreferences.getInt("keyboardHeight", 150)
            val config = context.getResources().configuration

            val firstLine = numpadLayout.findViewById<LinearLayout>(
                R.id.first_line
            )
            val secondLine = numpadLayout.findViewById<LinearLayout>(
                R.id.second_line
            )
            val thirdLine = numpadLayout.findViewById<LinearLayout>(
                R.id.third_line
            )
            val fourthLine = numpadLayout.findViewById<LinearLayout>(
                R.id.fourth_line
            )

            if(config.orientation == Configuration.ORIENTATION_LANDSCAPE){
                firstLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height*0.7).toInt())
                secondLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height*0.7).toInt())
                thirdLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height*0.7).toInt())
                fourthLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height*0.7).toInt())
            }else{
                firstLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
                secondLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
                thirdLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
                fourthLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
            }

            myKeysText.clear()
            myKeysText.add(firstLineText)
            myKeysText.add(secondLineText)
            myKeysText.add(thirdLineText)
            myKeysText.add(fourthLineText)

            layoutLines.clear()
            layoutLines.add(firstLine)
            layoutLines.add(secondLine)
            layoutLines.add(thirdLine)
            layoutLines.add(fourthLine)

            setLayoutComponents()

            return numpadLayout
        }

        private fun playClick(i: Int) {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            when (i) {
                32 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
                Keyboard.KEYCODE_DONE, 10 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN)
                Keyboard.KEYCODE_DELETE -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE)
                else -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1.toFloat())
            }
        }


        private fun setLayoutComponents(){
            val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
            val sound = sharedPreferences.getInt("keyboardSound", -1)
            val vibrate = sharedPreferences.getInt("keyboardVibrate", -1)
            for(line in layoutLines.indices){
                val children = layoutLines[line].children.toList()
                val myText = myKeysText[line]
                for(item in children.indices){
                    val actionButton = children[item].findViewById<Button>(R.id.key_button)
                    actionButton.text = myText[item]

                    buttons.add(actionButton)

                    val clickListener = (View.OnClickListener {
                        if(vibrate > 0){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(VibrationEffect.createOneShot(70, vibrate))
                            }
                            else{
                                vibrator.vibrate(70)
                            }
                        }



                        when (actionButton.text.toString()) {

                            "DEL" -> {
                                inputConnection.deleteSurroundingText(1,0)
                            }
                            "Enter" -> {
                                val eventTime = SystemClock.uptimeMillis()
                                inputConnection.sendKeyEvent(KeyEvent(eventTime, eventTime,
                                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                                    KeyEvent.FLAG_SOFT_KEYBOARD))
                                inputConnection.sendKeyEvent(KeyEvent(
                                    SystemClock.uptimeMillis(), eventTime,
                                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                                    KeyEvent.FLAG_SOFT_KEYBOARD))
                            }
                            else -> {
                                playClick(
                                    actionButton.text.toString().toCharArray().get(
                                        0
                                    ).toInt()
                                )
                                inputConnection.commitText(actionButton.text,1)
                            }
                        }
                    })

                    actionButton.setOnClickListener(clickListener)
                    children[item].setOnClickListener(clickListener)

                }
            }
        }

    }
}

