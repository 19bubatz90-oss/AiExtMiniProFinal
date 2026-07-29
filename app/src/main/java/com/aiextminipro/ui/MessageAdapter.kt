package com.aiextminipro.ui
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aiextminipro.R
import com.aiextminipro.model.Message

class MessageAdapter : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    private var items: List<Message> = emptyList()
    fun submitList(list: List<Message>) { items = list; notifyDataSetChanged() }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false))
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = items[position]
        val params = holder.textView.layoutParams as? LinearLayout.LayoutParams
        if (msg.isUser) {
            holder.textView.setBackgroundResource(R.drawable.bubble_user)
            holder.textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            params?.gravity = android.view.Gravity.END
        } else {
            holder.textView.setBackgroundResource(R.drawable.bubble_assistant)
            holder.textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            params?.gravity = android.view.Gravity.START
        }
        holder.textView.layoutParams = params
        holder.textView.text = msg.text
    }
    override fun getItemCount() = items.size
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.messageTextView)
    }
}
