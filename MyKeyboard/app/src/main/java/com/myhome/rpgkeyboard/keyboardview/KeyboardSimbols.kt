package com.myhome.rpgkeyboard.keyboardview

import android.content.Context
import android.content.res.Configuration
import android.inputmethodservice.Keyboard
import android.media.AudioManager
import android.os.*
import android.util.Log
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

class KeyboardSimbols constructor(var context:Context, var layoutInflater: LayoutInflater, var keyboardInterationListener: KeyboardInterationListener) {
    lateinit var simbolsLayout: LinearLayout
    var inputConnection:InputConnection? = null
        set(inputConnection){
            field = inputConnection
        }
    var isCaps:Boolean = false
    var buttons:MutableList<Button> = mutableListOf<Button>()
    lateinit var vibrator: Vibrator



    val numpadText = listOf<String>("1","2","3","4","5","6","7","8","9","0")
    val firstLineText = listOf<String>("+","×","÷","=","/","￦","<",">","♡","☆")
    val secondLineText = listOf<String>("!","@","#","~","%","^","&","*","(",")")
    val thirdLineText = listOf<String>("\uD83D\uDE00","-","'","\"",":",";",",","?","DEL")
    val fourthLineText = listOf<String>("가","한/영",",","space",".","Enter")
    val myKeysText = ArrayList<List<String>>()
    val layoutLines = ArrayList<LinearLayout>()
    var downView:View? = null
    var animationMode:Int = 0
    var vibrate = 0
    var sound = 0
    var capsView:ImageView? = null

    fun init(){
        simbolsLayout = layoutInflater.inflate(R.layout.keyboard_simbols, null) as LinearLayout
        inputConnection = inputConnection
        keyboardInterationListener = keyboardInterationListener
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        context = context

        val config = context.getResources().configuration
        val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val height = sharedPreferences.getInt("keyboardHeight", 150)
        animationMode = sharedPreferences.getInt("theme", 0)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        val numpadLine = simbolsLayout.findViewById<LinearLayout>(
            R.id.numpad_line
        )
        val firstLine = simbolsLayout.findViewById<LinearLayout>(
            R.id.first_line
        )
        val secondLine = simbolsLayout.findViewById<LinearLayout>(
            R.id.second_line
        )
        val thirdLine = simbolsLayout.findViewById<LinearLayout>(
            R.id.third_line
        )
        val fourthLine = simbolsLayout.findViewById<LinearLayout>(
            R.id.fourth_line
        )

        if(config.orientation == Configuration.ORIENTATION_LANDSCAPE){
            firstLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height*0.7).toInt())
            secondLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height*0.7).toInt())
            thirdLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (height*0.7).toInt())
        }else{
            firstLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
            secondLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
            thirdLine.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height)
        }

        myKeysText.clear()
        myKeysText.add(numpadText)
        myKeysText.add(firstLineText)
        myKeysText.add(secondLineText)
        myKeysText.add(thirdLineText)
        myKeysText.add(fourthLineText)

        layoutLines.clear()
        layoutLines.add(numpadLine)
        layoutLines.add(firstLine)
        layoutLines.add(secondLine)
        layoutLines.add(thirdLine)
        layoutLines.add(fourthLine)

        setLayoutComponents()
    }

    fun getLayout():LinearLayout{
        return simbolsLayout
    }

    fun modeChange(){
        if(isCaps){
            isCaps = false
            for(button in buttons){
                button.setText(button.text.toString().toLowerCase())
            }
        }
        else{
            isCaps = true
            for(button in buttons){
                button.setText(button.text.toString().toUpperCase())
            }
        }
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
                inputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
            }
            playVibrate()
            val cursorcs:CharSequence? =  inputConnection?.getSelectedText(InputConnection.GET_TEXT_WITH_STYLES)
            if(cursorcs != null && cursorcs.length >= 2){

                val eventTime = SystemClock.uptimeMillis()
                inputConnection?.finishComposingText()
                inputConnection?.sendKeyEvent(KeyEvent(eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))
                inputConnection?.sendKeyEvent(KeyEvent(SystemClock.uptimeMillis(), eventTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))
            }

            when (actionButton.text.toString()) {
                "\uD83D\uDE00" -> {
                    keyboardInterationListener.modechange(3)
                }
                "한/영" -> {
                    keyboardInterationListener.modechange(1)
                }
                "가" -> {
                    keyboardInterationListener.modechange(1)
                }
                else -> {
                    playClick(
                        actionButton.text.toString().toCharArray().get(
                            0
                        ).toInt()
                    )
                    inputConnection?.commitText(actionButton.text.toString(), 1)
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
//            inputConnection.beginBatchEdit()
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
                        spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener))
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
                    "CAPS" -> {
                        spacialKey.setImageResource(R.drawable.ic_caps_unlock)
                        spacialKey.visibility = View.VISIBLE
                        actionButton.visibility = View.GONE
                        capsView = spacialKey
                        myOnClickListener = getCapsAction()
                        spacialKey.setOnClickListener(myOnClickListener)
                        spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener))
                        spacialKey.setBackgroundResource(R.drawable.key_background)
                    }
                    "Enter" -> {
                        spacialKey.setImageResource(R.drawable.ic_enter)
                        spacialKey.visibility = View.VISIBLE
                        actionButton.visibility = View.GONE
                        myOnClickListener = getEnterAction()
                        spacialKey.setOnClickListener(myOnClickListener)
                        spacialKey.setOnTouchListener(getOnTouchListener(myOnClickListener))
                        spacialKey.setBackgroundResource(R.drawable.key_background)
                    }
                    else -> {
                        actionButton.text = myText[item]
                        buttons.add(actionButton)
                        myOnClickListener = getMyClickListener(actionButton)
                        actionButton.setOnTouchListener(getOnTouchListener(myOnClickListener))
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
            inputConnection?.commitText(" ",1)
        }
    }

    fun getDeleteAction():View.OnClickListener{
        return View.OnClickListener{
            playVibrate()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                inputConnection?.deleteSurroundingTextInCodePoints(1, 0)
            }else{
                inputConnection?.deleteSurroundingText(1,0)
            }
        }
    }

    fun getCapsAction():View.OnClickListener{
        return View.OnClickListener{
            playVibrate()
            modeChange()
        }
    }

    fun getEnterAction():View.OnClickListener{
        return View.OnClickListener{
            playVibrate()
            val eventTime = SystemClock.uptimeMillis()
            inputConnection?.sendKeyEvent(KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD))
            inputConnection?.sendKeyEvent(KeyEvent(SystemClock.uptimeMillis(), eventTime,
                KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER, 0, 0, 0, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD))
        }
    }


}