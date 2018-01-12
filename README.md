# RichTextView
TextView实现富文本，点击断句，讯飞语音合成播放。

## 使用

### 1、按照官方文档集成讯飞语音合成

### 2、程序启动，初始化讯飞
```
// 初始化讯飞
SpeechUtility.createUtility(MainActivity.this, SpeechConstant.APPID + "="+"自己申请的key");
```

### 3、传入html
```
tv_rich = findViewById(R.id.tv_rich);
tv_rich.fromHtml(text);
```

## 关键实现

### 1、讯飞集成，不再累赘
### 2、富文本处理：
  图片个数判断
  
  图片下载和展示
  
  富文本展示
  
  文章断句
  
  使用SpannableStringBuilder给每局设置点击
  
更多精彩内容，请关注我的微信公众号——Android机动车

![这里写图片描述](http://img.blog.csdn.net/20180110155733884?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamlhc2h1YWk5NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)	
