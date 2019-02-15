# 悬浮窗实现点击手机屏幕底部位置

通过手机顶部区域模拟点击手机底部区域

# ![image](https://github.com/Mao-x-w/FloatWindow/blob/HEAD/introduce/1.gif)

前段时间手机摔了一下，手机底部区域点击没有反应了，换一块屏幕得好几百大洋，就打算先凑合用一段时间再换新手机了。
可是，底部区域不能点击用着实在蓝瘦。

首先，不能点返回键、不能点home键、不能点menu键，这个可以通过使用悬浮球来解决，一般手机都自带悬浮球功能。

其次，底部的一些应用没办法打开了（短信、电话、微信等），而且即使打开了某个应用，
应用底部的tab切换也不能点击，实在蓝瘦。买了个OTG数据线，通过鼠标操作了一段时间，也是费尽，不能总带着一个鼠标啊。
后来，突然想到既然返回键可以通过悬浮球实现，那么我可不可以通过悬浮一个顶部区域来操控底部区域。

说干就干，大体就分3步走

1.实现桌面悬浮窗口

本着不重复造轮子的原则，先在github逛了一圈，找到了一个还不错的开源库，我直接拿过来用了，有兴趣的可以去研究
https://github.com/zhaozepeng/FloatWindowPermission

2.通过顶部区域，控制底部的手势图标移动

这个就比较简单了，给顶部的手势图标加个触摸监听，同时控制顶部手势图标随着顶部移动而移动就可以，具体可看源码

3.通过底部手势图标的坐标，模拟点击

模拟点击实现起来有些费劲，百度了几种方式，都不好用，这里做一下记录：

1）通过一个view，模拟它的坐标，实现点击（偷梁换柱）

```
private void setSimulateClick(View view, float x, float y) {
    long downTime = SystemClock.uptimeMillis();
    final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
            MotionEvent.ACTION_DOWN, x, y, 0);
    downTime += 1000;
    final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
            MotionEvent.ACTION_UP, x, y, 0);
    view.onTouchEvent(downEvent);
    view.onTouchEvent(upEvent);
    downEvent.recycle();
    upEvent.recycle();
}

```
试了一下这种方式，只能在当前应用中使用，切换到桌面或其他应用中，不能用

2）AccessibilityService实现模拟点击

AccessibilityService是Android提供有某些障碍手机人群使用的辅助服务，可以帮助用户实现点击屏幕等一些帮助。

这种方式的实现原理是通过view的resource-id或text获取到view，然后调用performAction(AccessibilityNodeInfo.ACTION_CLICK)进行模拟点击

之前的微信抢红包软件就是通过这种方式实现的，但是我这里不能使用这种方式，因为我要点击的是不确定的

3）adb命令模拟点击

adb shell input tap x y

这种方式的一直连着电脑用adb控制，也不合适


最后在Stack Overflow上找到了一种解决方案

```

private void setSimulateClick2(View view, final float x, final float y) {
    new Thread(new Runnable() {
        @Override
        public void run() {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                String cmd = "/system/bin/input tap "+x+" "+y+" \n";
//                    String cmd = "/system/bin/input swipe "+x+" "+y+" "+x+100+" "+y+" "+ 100+"\n";
                os.writeBytes(cmd);
                os.writeBytes("exit\n");
                os.flush();
                os.close();
                process.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }).start();
}

```

这种解决方案，和使用电脑通过adb控制一样。但是需要Root权限，刚好我的手机Root了，使用这种方式解决了。























