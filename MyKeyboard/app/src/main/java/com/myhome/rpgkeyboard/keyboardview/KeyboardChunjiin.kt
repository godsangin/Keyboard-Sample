package com.myhome.rpgkeyboard.keyboardview

import android.content.Context
import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import android.media.AudioManager
import android.os.*
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.children
import com.myhome.rpgkeyboard.*
import java.lang.NumberFormatException

class KeyboardChunjiin{

    companion object {
        lateinit var chunjiinLayout:LinearLayout
        lateinit var inputConnection: InputConnection
        lateinit var keyboardInterationListener: KeyboardInterationListener
        lateinit var context: Context
        lateinit var vibrator: Vibrator
        lateinit var chunjiinMaker: ChunjiinMaker
        var buttons:MutableList<Button> = mutableListOf<Button>()

        val firstLineText = listOf<String>("ㅣ", "·", "ㅡ","DEL")
        val secondLineText = listOf<String>("ㄱㅋ", "ㄴㄹ", "ㄷㅌ", "Enter")
        val thirdLineText = listOf<String>("ㅂㅍ","ㅅㅎ","ㅈㅊ",".,?!")
        val fourthLineText = listOf<String>("한/영", "ㅇㅁ", "space", "!#1")
        val myKeysText = ArrayList<List<String>>()
        val layoutLines = ArrayList<LinearLayout>()
        var downView:View? = null
        var sound = 0
        var vibrate = 0

        fun newInstance(context:Context, layoutInflater: LayoutInflater, inputConnection: InputConnection, keyboardInterationListener: KeyboardInterationListener): LinearLayout {
            chunjiinLayout = layoutInflater.inflate(R.layout.keyboard_chunjiin, null) as LinearLayout
            Companion.inputConnection = inputConnection
            Companion.keyboardInterationListener = keyboardInterationListener
            Companion.context = context
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            chunjiinMaker = ChunjiinMaker(inputConnection)

            val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
            val height = sharedPreferences.getInt("keyboardHeight", 150)
            sound = sharedPreferences.getInt("keyboardSound", -1)
            vibrate = sharedPreferences.getInt("keyboardVibrate", -1)
            val config = context.getResources().configuration

            val firstLine = chunjiinLayout.findViewById<LinearLayout>(
                R.id.first_line
            )
            val secondLine = chunjiinLayout.findViewById<LinearLayout>(
                R.id.second_line
            )
            val thirdLine = chunjiinLayout.findViewById<LinearLayout>(
                R.id.third_line
            )
            val fourthLine = chunjiinLayout.findViewById<LinearLayout>(
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

            return chunjiinLayout
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

        private fun playVibrate(){
            if(vibrate > 0){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(70, vibrate))
                }
                else{
                    vibrator.vibrate(70)
                }
            }
        }

        private fun getMyClickListener(actionButton:Button):View.OnClickListener{

            val clickListener = (View.OnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    inputConnection.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
                }
                playVibrate()
                val cursorcs:CharSequence? =  inputConnection.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
                if(cursorcs != null && cursorcs.length >= 2){

                    val eventTime = SystemClock.uptimeMillis()
                    inputConnection.finishComposingText()
                    inputConnection.sendKeyEvent(KeyEvent(eventTime, eventTime,
                        KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD))
                    inputConnection.sendKeyEvent(KeyEvent(SystemClock.uptimeMillis(), eventTime,
                        KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                        KeyEvent.FLAG_SOFT_KEYBOARD))
                    chunjiinMaker.clear()
                }
                when (actionButton.text.toString()) {
                    "한/영" -> {
//                        inputConnection.finishComposingText()
                        keyboardInterationListener.modechange(0)
                        chunjiinMaker.clear()
                        chunjiinMaker.clearChunjiin()
                    }
                    "!#1" -> {
//                        inputConnection.finishComposingText()
                        keyboardInterationListener.modechange(2)
                        chunjiinMaker.clear()
                        chunjiinMaker.clearChunjiin()
                    }
                    ".,?!" -> {
                        chunjiinMaker.commonKeywordCommit()
                    }
                    else -> {
                        playClick(actionButton.text.toString().toCharArray().get(0).toInt())
                        try{
                            val myText = Integer.parseInt(actionButton.text.toString())
                            chunjiinMaker.directlyCommit()
                            inputConnection.commitText(actionButton.text.toString(), 1)
                        }catch (e: NumberFormatException){
                            chunjiinMaker.commit(actionButton.text.toString().toCharArray().get(0))
                        }
                    }
                }
            })
            actionButton.setOnClickListener(clickListener)
            return clickListener
        }

        fun getOnTouchListener(clickListener: View.OnClickListener):View.OnTouchListener{
            val handler = Handler()
            val initailInterval = 500
            val normalInterval = 100
            val handlerRunnable = object: Runnable{
                override fun run() {
                    handler.postDelayed(this, normalInterval.toLong())
                    clickListener.onClick(downView)
                }
            }
            val onTouchListener = object:View.OnTouchListener {
                override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
                    when (motionEvent?.getAction()) {
                        MotionEvent.ACTION_DOWN -> {
                            handler.removeCallbacks(handlerRunnable)
                            handler.postDelayed(handlerRunnable, initailInterval.toLong())
                            downView = view!!
                            clickListener.onClick(view)
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            handler.removeCallbacks(handlerRunnable)
                            downView = null
                            return true
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            handler.removeCallbacks(handlerRunnable)
                            downView = null
                            return true
                        }
                    }
                    return false
                }
            }

            return onTouchListener
        }

        private fun setLayoutComponents(){
            for(line in layoutLines.indices){
                val children = layoutLines[line].children.toList()
                val myText = myKeysText[line]
                for(item in children.indices){
                    val actionButton = children[item].findViewById<Button>(R.id.key_button)
                    val spacialKey = children[item].findViewById<ImageView>(R.id.spacial_key)
                    var myOnClickListener:View.OnClickListener? = null
                    when(myText[item]){
                        "space" -> {
                            spacialKey.setImageResource(R.drawable.ic_space_bar)
                            spacialKey.visibility = View.VISIBLE
                            actionButton.visibility = View.GONE
                            myOnClickListener = getSpaceAction()
                            spacialKey.setOnClickListener(myOnClickListener)
                            spacialKey.setBackgroundResource(R.drawable.key_background)
                        }
                        "DEL" -> {
                            spacialKey.setImageResource(R.drawable.del)
                            spacialKey.visibility = View.VISIBLE
                            actionButton.visibility = View.GONE
                            myOnClickListener = getDeleteAction()
                            spacialKey.setOnClickListener(myOnClickListener)
                            spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener))
                        }
                        "Enter" -> {
                            spacialKey.setImageResource(R.drawable.ic_enter)
                            spacialKey.visibility = View.VISIBLE
                            actionButton.visibility = View.GONE
                            myOnClickListener = getEnterAction()
                            spacialKey.setOnClickListener(myOnClickListener)
                            spacialKey.setBackgroundResource(R.drawable.key_background)
                        }
                        else -> {
                            actionButton.text = myText[item]
                            buttons.add(actionButton)
                            myOnClickListener = getMyClickListener(actionButton)
                        }
                    }
                    children[item].setOnClickListener(myOnClickListener)
                }
            }
        }
        fun getSpaceAction():View.OnClickListener{
            return View.OnClickListener{
                playClick('ㅂ'.toInt())
                playVibrate()
                if(chunjiinMaker.keywordExpect){
                    inputConnection.finishComposingText()
                    chunjiinMaker.keywordExpect = false
                }
                else if(chunjiinMaker.isEmpty()){
                    inputConnection.commitText(" ", 1)
                }
                else{
                    chunjiinMaker.directlyCommit()
                }
                chunjiinMaker.clearChunjiin()
            }
        }

        fun getDeleteAction():View.OnClickListener{
            return View.OnClickListener{
                playVibrate()
                chunjiinMaker.delete()
            }
        }


        fun getEnterAction():View.OnClickListener{
            return View.OnClickListener{
                playVibrate()
                chunjiinMaker.directlyCommit()
                val eventTime = SystemClock.uptimeMillis()
                inputConnection.sendKeyEvent(KeyEvent(eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))
                inputConnection.sendKeyEvent(KeyEvent(SystemClock.uptimeMillis(), eventTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))
            }
        }


    }
}

