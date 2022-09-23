# ขั้นตอนการปรับใช้งาน Streaming
 - library ที่ใช้ streaming https://github.com/pedroSG94/rtmp-rtsp-stream-client-java
- เพิ่ม use permission นี้ใน AndroidManifest.xml
    ```        
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <!--Optional for play store-->
    <uses-feature android:name="android.hardware.camera" android:required="false" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
    ```
- เพิ่ม repositories นี้ใน project build.gradle
    ```
    allprojects {
      repositories {
        maven { url 'https://jitpack.io' }
      }
    }
    ```
- เพิ่ม lib ใน dependencies ในไฟล์ app build.gradle
    ```
    dependencies {
      implementation 'com.github.pedroSG94.rtmp-rtsp-stream-client-java:rtplibrary:2.1.9'
    }
    ```
# โดยแอปมีการทำงานคร่าวๆดังนี้
- เรียกใช้งาน lib ให้เชื่อมต่อกับ surfaceview เพื่อแสดงภาพ 
    ```
    rtspCamera1 = RtspCamera1(surfaceView, this)
    ```
- หลังจากทีการกดปุ่ม stream ให้เรียก 
    ```
    rtspCamera1!!.startStream(rtsp://url) 
    ```
- เพื่อทำการ stream ในที่นี้ให้ใช้ rtsp://5db335c6ace2c.streamlock.net:1935/claimdi/carid
- ภาพจากกล้องจะ stream ขึ้นสู่ server โดยสามารถเรียกดูได้ผ่าน https://5db335c6ace2c.streamlock.net/claimdi/carid/playlist.m3u8
- หลังจากกดปุ่ม stop stream ให้เรียกrtspCamera1?.stopStream()
- ส่วน stream ที่ถูกบันทึกไว้สามารถเรียกดูได้ผ่าน url ดังตัวอย่าง http://111.223.48.201:1935/claimdi_VOD/mp4:teststream_2022-09-22-22.46.03.676-ICT.mp4/playlist.m3u8
- โดยชื่อนี้ 
teststream_2022-09-22-22.46.03.676-ICT_0.mp4 คือ 
carid_ปี-เดือน-วัน-เวลาที่ทำการstream-timezone.mp4


