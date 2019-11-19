package com.myhome.rpgkeyboard.keyboardview

import android.content.Context
import android.content.SharedPreferences
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
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.myhome.rpgkeyboard.*
import java.lang.NumberFormatException

class KeyboardKorean constructor(var context:Context, var layoutInflater: LayoutInflater, var keyboardInterationListener: KeyboardInterationListener){

    lateinit var koreanLayout: LinearLayout
    var isCaps:Boolean = false
    var buttons:MutableList<Button> = mutableListOf<Button>()
    lateinit var hangulMaker: HangulMaker
    lateinit var vibrator: Vibrator
    lateinit var sharedPreferences:SharedPreferences
    var inputConnection:InputConnection? = null
        set(inputConnection){
            field = inputConnection
            hangulMaker = HangulMaker(inputConnection!!)
    }
    var sound = 0
    var vibrate = 0
    val numpadText = listOf<String>("1","2","3","4","5","6","7","8","9","0")
    val firstLineText = listOf<String>("ㅂ","ㅈ","ㄷ","ㄱ","ㅅ","ㅛ","ㅕ","ㅑ","ㅐ","ㅔ")
    val secondLineText = listOf<String>("ㅁ","ㄴ","ㅇ","ㄹ","ㅎ","ㅗ","ㅓ","ㅏ","ㅣ")
    val thirdLineText = listOf<String>("CAPS","ㅋ","ㅌ","ㅊ","ㅍ","ㅠ","ㅜ","ㅡ","DEL")
    val fourthLineText = listOf<String>("!#1","한/영",",","space",".","Enter")
    val firstLongClickText = listOf("!","@","#","$","%","^","&","*","(",")")
    val secondLongClickText = listOf<String>("~","+","-","×","♥",":",";","'","\"")
    val thirdLongClickText = listOf("","_","<",">","/",",","?")
    val myKeysText = ArrayList<List<String>>()
    val myLongClickKeysText = ArrayList<List<String>>()
    val layoutLines = ArrayList<LinearLayout>()
    var downView:View? = null
    var capsView:ImageView? = null

    fun init(){
        koreanLayout = layoutInflater.inflate(R.layout.keyboard_action, null) as LinearLayout
        keyboardInterationListener = keyboardInterationListener
        hangulMaker = HangulMaker(inputConnection!!)
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        context = context

        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)

        val height = sharedPreferences.getInt("keyboardHeight", 150)
        val config = context.getResources().configuration
        sound = sharedPreferences.getInt("keyboardSound", -1)
        vibrate = sharedPreferences.getInt("keyboardVibrate", -1)

        val numpadLine = koreanLayout.findViewById<LinearLayout>(
            R.id.numpad_line
        )
        val firstLine = koreanLayout.findViewById<LinearLayout>(
            R.id.first_line
        )
        val secondLine = koreanLayout.findViewById<LinearLayout>(
            R.id.second_line
        )
        val thirdLine = koreanLayout.findViewById<LinearLayout>(
            R.id.third_line
        )
        val fourthLine = koreanLayout.findViewById<LinearLayout>(
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
        return koreanLayout
    }


    fun modeChange(){
        if(isCaps){
            isCaps = false
            capsView?.setImageResource(R.drawable.ic_caps_unlock)
            for(button in buttons){
                when(button.text.toString()){
                    "ㅃ" -> {
                        button.text = "ㅂ"
                    }
                    "ㅉ" -> {
                        button.text = "ㅈ"
                    }
                    "ㄸ" -> {
                        button.text = "ㄷ"
                    }
                    "ㄲ" -> {
                        button.text = "ㄱ"
                    }
                    "ㅆ" -> {
                        button.text = "ㅅ"
                    }
                    "ㅒ" -> {
                        button.text = "ㅐ"
                    }
                    "ㅖ" -> {
                        button.text = "ㅔ"
                    }
                }
            }
        }
        else{
            isCaps = true
            capsView?.setImageResource(R.drawable.ic_caps_lock)
            for(button in buttons){
                when(button.text.toString()){
                    "ㅂ" -> {
                        button.text = "ㅃ"
                    }
                    "ㅈ" -> {
                        button.text = "ㅉ"
                    }
                    "ㄷ" -> {
                        button.text = "ㄸ"
                    }
                    "ㄱ" -> {
                        button.text = "ㄲ"
                    }
                    "ㅅ" -> {
                        button.text = "ㅆ"
                    }
                    "ㅐ" -> {
                        button.text = "ㅒ"
                    }
                    "ㅔ" -> {
                        button.text = "ㅖ"
                    }
                }
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

    private fun getMyLongClickListener(textView:TextView):View.OnLongClickListener{
        val longClickListener = object:View.OnLongClickListener{
            override fun onLongClick(p0: View?): Boolean {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    inputConnection!!.requestCursorUpdates(InputConnection.CURSOR_UPDATE_IMMEDIATE)
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
                    hangulMaker.clear()
                }
                when (textView.text.toString()) {
                    "한/영" -> {
                        keyboardInterationListener.modechange(0)
                        hangulMaker.clear()
                    }
                    "!#1" -> {
                        keyboardInterationListener.modechange(2)
                        hangulMaker.clear()
                    }
                    else -> {
                        playClick(textView.text.toString().toCharArray().get(0).toInt())
                        hangulMaker.directlyCommit()
                        inputConnection?.commitText(textView.text.toString(), 1)
                        if(isCaps){
                            modeChange()
                        }
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
                hangulMaker.clear()
            }
            when (actionButton.text.toString()) {
                "한/영" -> {
                    keyboardInterationListener.modechange(0)
                    hangulMaker.clear()
                }
                "!#1" -> {
                    keyboardInterationListener.modechange(2)
                    hangulMaker.clear()
                }
                else -> {
                    playClick(actionButton.text.toString().toCharArray().get(0).toInt())
                    try{
                        val myText = Integer.parseInt(actionButton.text.toString())
                        hangulMaker.directlyCommit()
                        inputConnection?.commitText(actionButton.text.toString(), 1)
                    }catch (e:NumberFormatException){
                        hangulMaker.commit(actionButton.text.toString().toCharArray().get(0))
                    }
                    if(isCaps){
                        modeChange()
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
                            if(!actionButton.text.equals("ㅋ")){
                                actionButton.setOnLongClickListener(getMyLongClickListener(longClickTextView))
                                longClickTextView.setOnLongClickListener(getMyLongClickListener(longClickTextView))
                            }
                            else{
                                actionButton.setOnTouchListener(getOnTouchListener(myOnClickListener))
                                longClickTextView.setOnTouchListener(getOnTouchListener(myOnClickListener))
                            }
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
            hangulMaker.commitSpace()
        }
    }

    fun getDeleteAction():View.OnClickListener{
        return View.OnClickListener{
            playVibrate()
            hangulMaker.delete()
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
            hangulMaker.directlyCommit()
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