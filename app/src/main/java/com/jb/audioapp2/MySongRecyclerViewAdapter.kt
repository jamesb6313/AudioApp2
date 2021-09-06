package com.jb.audioapp2

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

//import kotlinx.android.synthetic.main.fragment_song.view.*

class MySongRecyclerViewAdapter : BaseRecyclerViewAdapter<AudioSongs>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_song, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val myHolder = holder as? MyViewHolder
        myHolder?.setUpView(song = getItem(position))
    }



    inner class MyViewHolder(mView: View) : RecyclerView.ViewHolder(mView),
        View.OnClickListener {
        private val mTitleTextView: TextView = mView.findViewById(R.id.title)
        private val mArtistTextView: TextView = mView.findViewById(R.id.artist)

        init {
            mView.setOnClickListener(this)
        }

        override fun toString(): String {
            return super.toString() + " 'Song title: " + mTitleTextView.text +
                    " 'Song artist: " + mArtistTextView.text +
                    "'"
        }

        fun setUpView(song: AudioSongs?) {
            mTitleTextView.text = song?.title
            mArtistTextView.text = song?.artist
        }

        override fun onClick(v: View?) {
            itemClickListener.onItemClick(absoluteAdapterPosition, v)
            Log.i("Testing info", "onClick() - current position is $absoluteAdapterPosition")
        }
    }
}