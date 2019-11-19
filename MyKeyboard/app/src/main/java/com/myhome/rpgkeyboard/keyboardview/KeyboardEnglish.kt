package com.myhome.rpgkeyboard.keyboardview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.LinearLayout
import android.media.AudioManager
import android.content.Context.AUDIO_SERVICE
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.*
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.children
import com.myhome.rpgkeyboard.*

class KeyboardEnglish constructor(var context:Context, var layoutInflater: LayoutInflater, var keyboardInterationListener: KeyboardInterationListener) {
    lateinit var englishLayout: LinearLayout
    var inputConnection:InputConnection? = null
        set(inputConnection){
            field = inputConnection
        }
    lateinit var vibrator: Vibrator
    lateinit var sharedPreferences:SharedPreferences
    var isCaps:Boolean = false
    var buttons:MutableList<Button> = mutableListOf<Button>()

    val numpadText = listOf<String>("1","2","3","4","5","6","7","8","9","0")
    val firstLineText = listOf<String>("q","w","e","r","t","y","u","i","o","p")
    val secondLineText = listOf<String>("a","s","d","f","g","h","j","k","l")
    val thirdLineText = listOf<String>("CAPS","z","x","c","v","b","n","m","DEL")
    val fourthLineText = listOf<String>("!#1","한/영",",","space",".","Enter")
    val firstLongClickText = listOf("!","@","#","$","%","^","&","*","(",")")
    val secondLongClickText = listOf<String>("~","+","-","×","♥",":",";","'","\"")
    val thirdLongClickText = listOf("∞","_","<",">","/",",","?")
    val myKeysText = ArrayList<List<String>>()
    val myLongClickKeysText = ArrayList<List<String>>()
    val layoutLines = ArrayList<LinearLayout>()
    var downView:View? = null
    var sound = 0
    var vibrate = 0
    var capsView:ImageView? = null

    fun init() {
        context = context
        englishLayout = layoutInflater.inflate(R.layout.keyboard_action, null) as LinearLayout
        inputConnection = inputConnection
        keyboardInterationListener = keyboardInterationListener
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


        val config = context.getResources().configuration
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
        val height = sharedPreferences.getInt("keyboardHeight", 150)
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        val numpadLine = englishLayout.findViewById<LinearLayout>(
            R.id.numpad_line
        )
        val firstLine = englishLayout.findViewById<LinearLayout>(
            R.id.first_line
        )
        val secondLine = englishLayout.findViewById<LinearLayout>(
            R.id.second_line
        )
        val thirdLine = englishLayout.findViewById<LinearLayout>(
            R.id.third_line
        )
        val fourthLine = englishLayout.findViewById<LinearLayout>(
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

        myLongClickKeysText.clear()
        myLongClickKeysText.add(firstLongClickText)
        myLongClickKeysText.add(secondLongClickText)
        myLongClickKeysText.add(thirdLongClickText)

        layoutLines.clear()
        layoutLines.add(numpadLine)
        layoutLines.add(firstLine)
        layoutLines.add(secondLine)
        layoutLines.add(thirdLine)
        layoutLines.add(fourthLine)

        setLayoutComponents()
    }

    fun getLayout():LinearLayout{
        return englishLayout
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
        val am = context.getSystemService(AUDIO_SERVICE) as AudioManager?
        when (i) {
            32 -> am!!.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR)
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

    private fun getMyLongClickListener(textView: TextView):View.OnLongClickListener{
        val longClickListener = object:View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
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
                when (textView.text.toString()) {
                    "한/영" -> {
                        keyboardInterationListener.modechange(1)
                    }
                    "!#1" -> {
                        keyboardInterationListener.modechange(2)
                    }
                    else -> {
                        playClick(textView.text.toString().toCharArray().get(0).toInt())
                        inputConnection?.commitText(textView.text.toString(), 1)
                    }
                }
                return true
            }
        }
        return longClickListener
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
                inputConnection?.sendKeyEvent(KeyEvent(eventTime, eventTime,
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))
                inputConnection?.sendKeyEvent(KeyEvent(SystemClock.uptimeMillis(), eventTime,
                    KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT, 0, 0, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD))

            }
            else{
                when (actionButton.text.toString()) {
                    "한/영" -> {
                        keyboardInterationListener.modechange(1)
                    }
                    "!#1" -> {
                        keyboardInterationListener.modechange(2)
                    }
                    else -> {
                        playClick(
                            actionButton.text.toString().toCharArray().get(
                                0
                            ).toInt()
                        )
                        inputConnection?.commitText(actionButton.text,1)
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
            var longClickIndex = 0
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
                        val longClickTextView = children[item].findViewById<TextView>(R.id.text_long_click)
                        actionButton.text = myText[item]
                        buttons.add(actionButton)
                        myOnClickListener = getMyClickListener(actionButton)
                        if(line > 0 && line < 4){//특수기호가 삽입될 수 있는 라인
                            longClickTextView.setText(myLongClickKeysText[line - 1].get(longClickIndex++))
                            longClickTextView.bringToFront()
                            longClickTextView.setOnClickListener(myOnClickListener)
                            actionButton.setOnLongClickListener(getMyLongClickListener(longClickTextView))
                            longClickTextView.setOnLongClickListener(getMyLongClickListener(longClickTextView))
                        }
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