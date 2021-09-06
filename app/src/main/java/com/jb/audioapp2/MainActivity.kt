package com.jb.audioapp2

//See: https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.os.PersistableBundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileFilter
import java.util.*
import androidx.core.app.ActivityCompat

import android.content.pm.PackageManager

import androidx.core.content.ContextCompat

import android.os.Build
import android.content.DialogInterface
import android.content.ContentResolver










class MainActivity : AppCompatActivity() {

    private var player: MediaPlayerService? = null
    var serviceBound = false
    var audioList: ArrayList<AudioSongs>? = null
    var initialSongIndex = 0
    val Broadcast_PLAY_NEW_AUDIO = "com.jb.audioapp2.PlayNewAudio"

    //protected lateinit var rootView: View
    //lateinit var recyclerView: RecyclerView
    lateinit var mAdapter: MySongRecyclerViewAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        audioList = ArrayList<AudioSongs>()

        newLoadAudio()    //- works on my phone, but not in emulator 10
        //loadAudio() //used to work before google update 08/2021
        setUpAdapter()
        initRecyclerView()
    }

    private fun setUpAdapter() {
        mAdapter = MySongRecyclerViewAdapter()

        audioList?.let { mAdapter.addItems(it) }    //todo need this to use BaseRecyclerAdapter see prj Locations
        mAdapter.setOnItemClickListener(onItemClickListener = object : OnItemClickListener {
            override fun onItemClick(position: Int, view: View?) {
                var song = mAdapter.getItem(position)
                //Toast.makeText(this@MainActivity,"Play songs", Toast.LENGTH_LONG).show()
                Log.i("Testing info", "SetUpAdapter() - current position is $position")
                playAudio(position)
            }
        })
    }

    private fun initRecyclerView() {
        val recyclerView: RecyclerView? = findViewById<View>(R.id.recyclerview) as RecyclerView
        //recyclerView = rootView.findViewById(R.id.recyclerview)
        if (recyclerView != null) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.addItemDecoration(DividerItemDecoration(this@MainActivity, LinearLayoutManager.VERTICAL))
            recyclerView.adapter = mAdapter

            //newcode sept 2020 *****
            this.title = getString(R.string.app_name) + " Songs Found: " + audioList?.size
        }
    }

    //menu
    ///////////////////////////////////////////////////////////
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        if (id == R.id.action_shuffle) {

            if (audioList != null && audioList!!.size > 1) {

                //todo - stop service before changing list order
                if (serviceBound) {
                    unbindService(serviceConnection)
                    serviceBound = false
                    //service is active
                    player!!.stopSelf()
                }

                //Collections.shuffle(audioList)
                audioList!!.shuffle()

                // update RecyclerView
                mAdapter.clear()
                audioList?.let { mAdapter.addItems(it) }    //todo need this to use BaseRecyclerAdapter see prj Locations
                mAdapter.update()

            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //loadAudio
    //////////////////////////////////////////////////////////
    private fun newLoadAudio() {
        try {
            val count: Int = myNewGetAudioFileCount()
        } catch (e: Exception) {
            myShowErrorDlg("Error = " + e.message)
// Cannot use Toast in catch stmt - Toast.makeText(this, " Error = " + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAudio() {
        val contentResolver = contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = MediaStore.Audio.Media.DATA + " like ? "
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        val myProjection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST
        )
        val myMusic = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val myMusicDirName = myMusic.toString()

//https://stackoverflow.com/questions/11285288/android-mediastore-get-distinct-folders-of-music-files
        val rootSd = Environment.getExternalStorageDirectory().toString()
        //val root_sd = this.getExternalFilesDir(Environment.DIRECTORY_MUSIC).toString()


        Log.e("Root", rootSd)
        val state = Environment.getExternalStorageState()
        var list: Array<File>? = null
        val file = File(rootSd)
        //val file = File("mnt/sdcard/Music")


        list = file.listFiles(AudioFilter())
        //todo - catch list = null error. was related to MediaPlayerService - line 472 register_playNewAudio() see also line193 PlayAudio()
        // added android:requestLegacyExternalStorage="true" to Manifest which resoled list = null
        //See:https://stackoverflow.com/questions/57116335/environment-getexternalstoragedirectory-deprecated-in-api-level-29-java
//newcode
        try {
            Log.e("Size of list ", "" + list!!.size)
            for (i in list.indices) {
                val name = list[i].name
                val count: Int = myGetAudioFileCount(list[i].absolutePath)
                //val count: Int = myGetAudioFileCount(file.absolutePath)
                Log.e("Count : $count", list[i].absolutePath)
            }
        } catch (e: NullPointerException) {
            myShowErrorDlg("No Songs found. Error = " + e.message)
// Cannot use Toast in catch stmt
//            Toast.makeText( this, "No Songs found. Error = " + e.message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            myShowErrorDlg("Error = " + e.message)
// Cannot use Toast in catch stmt
//            Toast.makeText(this, " Error = " + e.message, Toast.LENGTH_SHORT).show()
//newcode - end
        }

    }

    private fun myNewGetAudioFileCount(): Int {

        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                null,
                null
        )
        if (cursor != null && cursor.count > 0) {
            //audioList = ArrayList<AudioSongs>()
            while (cursor.moveToNext()) {
                val data =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist =
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))


                // Save to audioList
                audioList!!.add(AudioSongs(data, title, album, artist))
            }
            //Collections.shuffle(audioList)
            audioList!!.shuffle()
        }
        val songCount = cursor!!.count
        cursor.close()
        return songCount
    }

    private fun myGetAudioFileCount(dirPath: String): Int {
        val selection = MediaStore.Audio.Media.DATA + " like ? "
        //val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val selectionArgs = arrayOf("$dirPath%")
        val cursor = applicationContext.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null && cursor.count > 0) {
            audioList = ArrayList<AudioSongs>()
            while (cursor.moveToNext()) {
                val data =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val title =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val album =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                val artist =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))


                // Save to audioList
                audioList!!.add(AudioSongs(data, title, album, artist))
            }
            //Collections.shuffle(audioList)
            audioList!!.shuffle()
        }
        //cursor.close();
        return cursor!!.count
    }

    // class to limit the choices shown when browsing to SD card to media files
    class AudioFilter : FileFilter {
        // only want to see the following audio file types
        private val extension = arrayOf(
            ".mp3",
            ".wav",
            ".mp4"
        )

        override fun accept(pathname: File): Boolean {

            // if we are looking at a directory/file that's not hidden we want to see it so return TRUE
            if ((pathname.isDirectory || pathname.isFile) && !pathname.isHidden) {
                return true
            }

            // loops through and determines the extension of all files in the directory
            // returns TRUE to only show the audio files defined in the String[] extension array
            for (ext in extension) {
                if (pathname.name.toLowerCase(Locale.ROOT).endsWith(ext)) {
                    return true
                }
            }
            return false
        }
    }

    private fun myShowErrorDlg(errMsg: String) {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)

        // set message of alert dialog
        dialogBuilder.setMessage("Populate Music Folder with MP3 songs and try again.")
            // if the dialog is cancelable
            .setCancelable(false)
            // positive button text and action
            .setPositiveButton("Close App", DialogInterface.OnClickListener {
                    dialog, id -> finish()
            })
        // negative button text and action
//            .setNegativeButton("Continue", DialogInterface.OnClickListener {
//                    dialog, id -> dialog.cancel()
//            })

        // create dialog box
        val alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle(errMsg)
        // show alert dialog
        alert.show()
    }


    //Service code
    /////////////////////////////////////////////////

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: MediaPlayerService.LocalBinder = service as MediaPlayerService.LocalBinder
            player = binder.service
            serviceBound = true
            Toast.makeText(this@MainActivity, "Service Bound", Toast.LENGTH_SHORT).show()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    private fun playAudio(audioIndex: Int) {
        try {


            //Check is service is active
            if (!serviceBound) {
                //newcode - Oct 2020 *****
                initialSongIndex = audioIndex
                Log.i("1 Song start index", "initialSongIndex = $initialSongIndex")
                //end

                //Store Serializable audioList to SharedPreferences
                val storage = StorageUtil(applicationContext)
                storage.storeAudio(audioList)
                storage.storeAudioIndex(audioIndex)
                val playerIntent = Intent(this, MediaPlayerService::class.java)
                playerIntent.putExtra("StartIndex", initialSongIndex) //newcode - Oct 2020 *****
                startService(playerIntent)
                bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
            } else {
                Log.i("2 Song start index", "initialSongIndex = $initialSongIndex")

                //Store the new audioIndex to SharedPreferences
                val storage = StorageUtil(applicationContext)
                storage.storeAudioIndex(audioIndex)

                //Service is active
                //Send a broadcast to the service -> PLAY_NEW_AUDIO
                val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
                //todo - get rid of hard code see MediaPlayerService
                //val broadcastIntent = Intent("com.jb.audioapp2.PlayNewAudio")
                sendBroadcast(broadcastIntent)
            }
        } catch (e: NullPointerException)
        {
            Log.e("Main Activity PlayAudio Error", "Main Activity PlayAudio Error = NullPointerException")
            myShowErrorDlg("Error = " + e.message)
        }
        //newcode - Oct 2020 *****
        this.title = "Playing song " + (audioIndex + 1) + " of " + audioList?.size
        //newcode - end
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putBoolean("serviceStatus", serviceBound)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("ServiceState")
    }

    override fun onDestroy() {
        super.onDestroy()
//        if (serviceBound) {
//            unbindService(serviceConnection)
//            //service is active
//            player!!.stopSelf()
//        }
    }
}