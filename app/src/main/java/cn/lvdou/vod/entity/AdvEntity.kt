package cn.lvdou.vod.entity

/**
 * 广告
 *  {
"ad_select":"html",
"html":{
"code":"<a href=\"http://www.shouzhuanmao.com/\" target=\"_blank\"><img src=\"https://storage.taifutj.com/admin/202001300958775new.jpg\" width=\"240px\" height=\"200px\" border=\"0\" /></a>",
"timeout":60
},
"vod":{
"url":""
}
}
 */

data class AdvEntity(
        var ad_select: String, //广告类型
        var html: Html,
        var video: Vod,
        var sdkad: Sdkad,
        var home_history: Boolean,
        var jiexi_key: String
)

data class Html(
        var code: String,
        var timeout: Int = 0
)

data class Sdkad(
        var code: String,
        var timeout: Int = 0
)

data class Vod(
        var url: String,
        var click_url: String
)

