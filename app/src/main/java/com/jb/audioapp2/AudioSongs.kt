package com.jb.audioapp2

import java.io.Serializable

class AudioSongs(
    var data: String,
    var title: String,
    var album: String,
    var artist: String
) : Serializable