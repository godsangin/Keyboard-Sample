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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.myhome.rpgkeyboard.*

class KeyboardEmoji{
    companion object{
        lateinit var emojiLayout: LinearLayout
        lateinit var inputConnection: InputConnection
        lateinit var keyboardInterationListener: KeyboardInterationListener
        lateinit var context:Context
        lateinit var vibrator: Vibrator

        lateinit var emojiRecyclerViewAdapter: EmojiRecyclerViewAdapter
        val fourthLineText = listOf<String>("한/영",getEmojiByUnicode(0x1F600), getEmojiByUnicode(0x1F466), getEmojiByUnicode(0x1F91A), getEmojiByUnicode(0x1F423),getEmojiByUnicode(0x1F331), getEmojiByUnicode(0x1F682),"DEL")
        var vibrate = 0
        var sound = 0

        fun newInstance(context:Context, layoutInflater: LayoutInflater, inputConnection: InputConnection, keyboardInterationListener: KeyboardInterationListener): LinearLayout {
            Companion.context = context
            emojiLayout = layoutInflater.inflate(R.layout.keyboard_emoji, null) as LinearLayout
            Companion.inputConnection = inputConnection
            Companion.keyboardInterationListener = keyboardInterationListener
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
            vibrate = sharedPreferences.getInt("vibrate", -1)
            sound = sharedPreferences.getInt("sound", -1)

            val fourthLine = emojiLayout.findViewById<LinearLayout>(
                R.id.fourth_line
            )
            val children = fourthLine.children.toList()
            for(item in children.indices){
                val actionButton = children[item].findViewById<Button>(R.id.key_button)
                val spacialKey = children[item].findViewById<ImageView>(R.id.spacial_key)
                if(fourthLineText[item].equals("DEL")){
                    actionButton.setBackgroundResource(R.drawable.del)
                    val myOnClickListener = getDeleteAction()
                    actionButton.setOnClickListener(myOnClickListener)
                }
                else{
                    actionButton.text = fourthLineText[item]
                    actionButton.setOnClickListener(View.OnClickListener {
                        when((it as Button).text){
                            "한/영" -> {
                                keyboardInterationListener.modechange(1)
                            }
                            getEmojiByUnicode(0x1F600) -> {
                                setLayoutComponents(0x1F600, 79)
                            }
                            getEmojiByUnicode(0x1F466) -> {
                                setLayoutComponents(0x1F466, 88)
                            }
                            getEmojiByUnicode(0x1F91A) -> {
                                setLayoutComponents(0x1F91A, 88)
                            }
                            getEmojiByUnicode(0x1F423) -> {
                                setLayoutComponents(0x1F423, 35)
                            }
                            getEmojiByUnicode(0x1F331) -> {
                                setLayoutComponents(0x1F331, 88)
                            }
                            getEmojiByUnicode(0x1F682) -> {
                                setLayoutComponents(0x1F682, 64)
                            }
                        }
                    })
                }
            }

            setLayoutComponents(0x1F600, 79)
            return emojiLayout
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

        private fun setLayoutComponents(unicode: Int, count:Int) {
            var recyclerView = emojiLayout.findViewById<RecyclerView>(R.id.emoji_recyclerview)
            val emojiList = ArrayList<String>()
            val config = context.getResources().configuration
            val sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE)
            val height = sharedPreferences.getInt("keyboardHeight", 150)

//            unicode = 0x1F600
//            val unicode = 0x1F48B
            for(i in 0..count){
                emojiList.add(getEmojiByUnicode(unicode + i))
//                emojiList.add(i.toString())
            }

            emojiRecyclerViewAdapter = EmojiRecyclerViewAdapter(context, emojiList, inputConnection)
            recyclerView.adapter = emojiRecyclerViewAdapter
            val gm = GridLayoutManager(context,8)
            gm.isItemPrefetchEnabled = true
            recyclerView.layoutManager = gm
            recyclerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height * 5)
        }

        fun getEmojiByUnicode(unicode: Int): String {
            return String(Character.toChars(unicode))
        }

        fun getDeleteAction():View.OnClickListener{
            return View.OnClickListener{
                playVibrate()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    inputConnection.deleteSurroundingTextInCodePoints(1, 0)
                }else{
                    inputConnection.deleteSurroundingText(1,0)
                }
            }
        }
    }
}