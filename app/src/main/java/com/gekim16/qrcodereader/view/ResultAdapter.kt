package com.gekim16.qrcodereader.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.gekim16.qrcodereader.R
import com.gekim16.qrcodereader.model.Result
import kotlinx.android.synthetic.main.item.view.*
import java.util.*

class ResultAdapter(
    private val resultList: MutableList<Result>,
    private val clickListener: OnClickListener,
    private val context: Context
) : RecyclerView.Adapter<ResultAdapter.ViewHolder>(),
    Filterable {

    /**
     *  Filter 기능을 위해서 전달받은 리스트가 아닌 필더링된 리스트를 사용하여 화면에 보여줌
     */
    private var filteredItems = resultList

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, View.OnLongClickListener {
        private lateinit var result: Result
        private val itemLayout = itemView.item_linear_layout

        init {
            itemLayout.setOnClickListener(this)
            itemLayout.setOnLongClickListener(this)
        }

        fun bind(result: Result, position: Int) {
            this.result = result
            itemView.item_num.text = position.toString()
            itemView.item_url.text =
                context.getString(R.string.text_recycler_view, result.url, result.type)
        }

        /**
         *  MainActivity에서 구현되어 전달받은 clickListener를 사용하여 등록
         */
        override fun onClick(v: View?) {
            clickListener.onClick(this.result)
        }
        override fun onLongClick(v: View?): Boolean {
            return clickListener.onLongClick(this.result)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = filteredItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filteredItems[position], position + 1)
    }


    /**
     *  Filterable을 상속받아 getFilter 오버라이드
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()
                filteredItems =
                    if (charString.isEmpty()) {
                        resultList
                    } else {
                        val filteredList = ArrayList<Result>()
                        filteredItems.forEach {
                            if (it.url.toLowerCase(Locale.ROOT).contains(
                                    charString.toLowerCase(
                                        Locale.ROOT
                                    )
                                )
                            )
                                filteredList.add(it)
                        }

                        filteredList
                    }
                val result = FilterResults()
                result.values = filteredItems
                return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                filteredItems = results.values as MutableList<Result>
                notifyDataSetChanged()
            }

        }
    }

    fun addResult(result: Result) {
        resultList.add(result)
        notifyDataSetChanged()
    }

    fun deleteResult(result: Result) {
        resultList.remove(result)
        notifyDataSetChanged()
    }


    interface OnClickListener {
        fun onLongClick(result: Result): Boolean

        fun onClick(result: Result)
    }
}