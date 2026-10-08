package cn.lvdou.av

import cn.lvdou.av.play.AvVideoView
import cn.lvdou.av.play.MyIjkPlayerFactory
import com.dueeeke.videoplayer.exo.ExoMediaPlayerFactory
import com.dueeeke.videoplayer.ijk.IjkPlayerFactory
import com.dueeeke.videoplayer.player.AndroidMediaPlayerFactory
import com.dueeeke.videoplayer.player.VideoViewConfig
import com.dueeeke.videoplayer.player.VideoViewManager
import java.lang.reflect.Field


class MyPlayerManager {

    companion object{
        //0为IJK,,1为EXO，，2为media
        open fun  checktFactory(kernel: String,videoView: AvVideoView) :AvVideoView{
            val factory = getCurrentPlayerFactory()
            val config = VideoViewManager.getConfig()
            val mPlayerFactoryField: Field = config.javaClass.getDeclaredField("mPlayerFactory")
            mPlayerFactoryField.isAccessible = true

            when(kernel){
                "1" -> if (factory !is IjkPlayerFactory ){
                    var playerFactory = MyIjkPlayerFactory.create()
                    mPlayerFactoryField.set(config, playerFactory);
                    videoView.setPlayerFactory(playerFactory)
                }
                "2" -> if (factory !is ExoMediaPlayerFactory){
                    var playerFactory = ExoMediaPlayerFactory.create()
                    mPlayerFactoryField.set(config, playerFactory);
                    videoView.setPlayerFactory(playerFactory)
                }
                "3" -> if (factory !is AndroidMediaPlayerFactory){
                    var playerFactory = AndroidMediaPlayerFactory.create()
                    mPlayerFactoryField.set(config, playerFactory);
                    videoView.setPlayerFactory(playerFactory)
                }

            }


            return videoView
        }
        open fun getCurrentPlayerFactory(): Any? {
            val config: VideoViewConfig = VideoViewManager.getConfig()
            var playerFactory: Any? = null
            try {
                val mPlayerFactoryField: Field = config.javaClass.getDeclaredField("mPlayerFactory")
                mPlayerFactoryField.setAccessible(true)
                playerFactory = mPlayerFactoryField.get(config)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return playerFactory
        }
    }


}