package com.myhome.rpgkeyboard.keyboardview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputConnection
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.myhome.rpgkeyboard.R

class EmojiRecyclerViewAdapter(val context:Context, val emojiList:ArrayList<String>, val inputConnection: InputConnection) :RecyclerView.Adapter<EmojiRecyclerViewAdapter.Holder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.emoji_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return emojiList.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(emojiList.get(position), context)
    }

    inner class Holder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val textView = itemView?.findViewById<TextView>(R.id.emoji_text)

        fun bind(emoji: String, context: Context) {
            textView?.setText(emoji)
            textView?.setOnClickListener(View.OnClickListener {
                inputConnection.commitText((it as TextView).text.toString(), 1)
            })
        }
    }
}