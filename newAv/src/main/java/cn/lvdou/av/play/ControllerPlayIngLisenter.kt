package cn.lvdou.av.play

//播放进度回调监听
interface ControllerPlayIngLisenter {
    //position = 当前播放时间 单位S  total = 视频总时长 单位S
    fun onPlayIng(position: Int, total: Int)

    fun playPrepared()
}