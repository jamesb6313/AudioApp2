package com.jb.audioapp2

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import kotlinx.android.synthetic.main.fragment_song.view.*

class MySongRecyclerViewAdapter : BaseRecyclerViewAdapter<AudioSongs>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_song, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as? MyViewHolder

        //new ***** Nov 2020
        var mySelectedPosition: Boolean = false

        if (myHolder?.selectedPosition == position)
            mySelectedPosition = true;

        //    myHolder?.itemView.setBackgroundColor()//end
        myHolder?.setUpView(song = getItem(position), mySelectedPosition = mySelectedPosition)
    }



    inner class MyViewHolder(mView: View) : RecyclerView.ViewHolder(mView),
        View.OnClickListener {
        //new ***** Nov 2020
        var selectedPosition = -1 //end
        private val mTitleTextView: TextView = mView.title
        private val mArtistTextView: TextView = mView.artist
        private val mPlayedTextView: TextView = mView.played

        init {
            mView.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " 'Song title: " + mTitleTextView.text +
                    " 'Song artist: " + mArtistTextView.text +
                    "'"
        }

        @SuppressLint("SetTextI18n")
        fun setUpView(song: AudioSongs?, mySelectedPosition : Boolean) {
            mTitleTextView.text = song?.title
            mArtistTextView.text = song?.artist
            mPlayedTextView.text = "Not Played"
            if (mySelectedPosition) {
                mPlayedTextView.text = "Played"
                Log.i("Testing info", this.toString() + " Played")
            }
        }

        override fun onClick(v: View?) {
            itemClickListener.onItemClick(adapterPosition, v)
            //new ***** Nov 2020
            selectedPosition= (adapterPosition + 1)
            val positionClick : Int = (adapterPosition + 1)//end
            Log.i("Testing info", "onClick() - current position is $adapterPosition")
        }
    }
}