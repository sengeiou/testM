package com.qingmeng.mengmeng.utils.audio

import android.content.Context
import android.content.res.Resources
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment
import com.qingmeng.mengmeng.constant.IConstants
import java.io.*

class AudioRecordFunc {
    // 缓冲区字节大小
    private var bufferSizeInBytes = 0
    //AudioName裸音频数据文件 ，麦克风
    private var AudioName = ""
    //NewAudioName可播放的音频文件
    private var NewAudioName = ""
    private var audioRecord: AudioRecord? = null
    private var isRecord = false// 设置正在录制的状态

    //获取文件大小
    val recordFileSize: Long get() = AudioFileFunc.getFileSize(NewAudioName)

    //开始录音
    fun startRecordAndFile(): Int {
        //判断是否有外部存储设备sdcard
        if (AudioFileFunc.isSdcardExit) {
            if (isRecord) {
                return ErrorCode.E_STATE_RECODING
            } else {
                if (audioRecord == null)
                    creatAudioRecord()
                audioRecord!!.startRecording()
                // 让录制状态为true
                isRecord = true
                // 开启音频文件写入线程
                Thread(AudioRecordThread()).start()
                return ErrorCode.SUCCESS
            }
        } else {
            return ErrorCode.E_NOSDCARD
        }
    }

    //关闭录音
    fun stopRecordAndFile() {
        close()
    }

    //获取语言路径
    fun getNewAudioName(): String {
        return NewAudioName
    }

    private fun close() {
        if (audioRecord != null) {
            println("stopRecord")
            isRecord = false//停止文件写入
            audioRecord!!.stop()
            audioRecord!!.release()//释放资源
            audioRecord = null
        }
    }

    private fun creatAudioRecord() {
        // 获取音频文件路径
        AudioName = AudioFileFunc.rawFilePath
        NewAudioName = AudioFileFunc.wavFilePath
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AudioFileFunc.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        // 创建AudioRecord对象
        audioRecord = AudioRecord(AudioFileFunc.AUDIO_INPUT, AudioFileFunc.AUDIO_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes)
    }

    internal inner class AudioRecordThread : Runnable {
        override fun run() {
            writeDateTOFile()//往文件中写入裸数据
            copyWaveFile(AudioName, NewAudioName)//给裸数据加上头文件
        }
    }

    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */
    private fun writeDateTOFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        val audiodata = ByteArray(bufferSizeInBytes)
        var fos: FileOutputStream? = null
        var readsize = 0
        try {
            val file = File(AudioName)
            if (file.exists()) {
                file.delete()
            }
            fos = FileOutputStream(file)// 建立一个可存取字节的文件
        } catch (e: Exception) {
            e.printStackTrace()
        }
        while (isRecord) {
            readsize = audioRecord!!.read(audiodata, 0, bufferSizeInBytes)
            if (AudioRecord.ERROR_INVALID_OPERATION != readsize && fos != null) {
                try {
                    fos.write(audiodata)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        try {
            fos?.close()// 关闭写入流
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 这里得到可播放的音频文件
    private fun copyWaveFile(inFilename: String, outFilename: String) {
        var inPut: FileInputStream? = null
        var outPut: FileOutputStream? = null
        var totalAudioLen: Long = 0
        var totalDataLen = totalAudioLen + 36
        val longSampleRate = AudioFileFunc.AUDIO_SAMPLE_RATE.toLong()
        val channels = 2
        val byteRate = (16 * AudioFileFunc.AUDIO_SAMPLE_RATE * channels / 8).toLong()
        val data = ByteArray(bufferSizeInBytes)
        try {
            inPut = FileInputStream(inFilename)
            outPut = FileOutputStream(outFilename)
            totalAudioLen = inPut.channel.size()
            totalDataLen = totalAudioLen + 36
            WriteWaveFileHeader(outPut, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate)
            while (inPut.read(data) != -1) {
                outPut.write(data)
            }
            inPut.close()
            outPut.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    @Throws(IOException::class)
    private fun WriteWaveFileHeader(out: FileOutputStream, totalAudioLen: Long, totalDataLen: Long, longSampleRate: Long, channels: Int, byteRate: Long) {
        val header = ByteArray(44)
        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }

    object AudioFileFunc {
        //音频输入-麦克风
        internal val AUDIO_INPUT = MediaRecorder.AudioSource.MIC
        //采用频率
        //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
        internal val AUDIO_SAMPLE_RATE = 44100  //44.1KHz,普遍使用的频率
        //录音输出文件
        private val AUDIO_RAW_FILENAME = "RawAudio.raw"
        private val AUDIO_WAV_FILENAME = "FinalAudio.wav"
        internal val AUDIO_AMR_FILENAME = "FinalAudio.amr"

        /**
         * 判断是否有外部存储设备sdcard
         *
         * @return true | false
         */
        internal val isSdcardExit: Boolean get() = Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED

        /**
         * 获取麦克风输入的原始音频流文件路径
         *
         * @return
         */
        val rawFilePath: String
            get() {
                var mAudioRawPath = ""
                if (isSdcardExit) {
                    val fileBasePath = IConstants.DIR_AUDIO_STR
                    mAudioRawPath = "$fileBasePath/$AUDIO_RAW_FILENAME"
                }
                return mAudioRawPath
            }

        /**
         * 获取编码后的WAV格式音频文件路径
         *
         * @return
         */
        val wavFilePath: String
            get() {
                var mAudioWavPath = ""
                if (isSdcardExit) {
                    val fileBasePath = IConstants.DIR_AUDIO_STR
                    mAudioWavPath = "$fileBasePath/$AUDIO_WAV_FILENAME"
                }
                return mAudioWavPath
            }

        /**
         * 获取编码后的AMR格式音频文件路径
         *
         * @return
         */
        val amrFilePath: String
            get() {
                var mAudioAMRPath = ""
                if (isSdcardExit) {
                    val fileBasePath = IConstants.DIR_AUDIO_STR
                    mAudioAMRPath = "$fileBasePath/$AUDIO_AMR_FILENAME"
                }
                return mAudioAMRPath
            }

        /**
         * 获取文件大小
         *
         * @param path,文件的绝对路径
         * @return
         */
        internal fun getFileSize(path: String): Long {
            val mFile = File(path)
            return if (!mFile.exists()) -1 else mFile.length()
        }
    }

    object ErrorCode {
        internal val SUCCESS = 1000
        internal val E_NOSDCARD = 1001
        internal val E_STATE_RECODING = 1002
        internal val E_UNKOWN = 1003

        @Throws(Resources.NotFoundException::class)
        fun getErrorInfo(vContext: Context, vType: Int): String {
            when (vType) {
                SUCCESS -> return "SUCCESS"
                E_NOSDCARD -> return "E_NOSDCARD"
                E_STATE_RECODING -> return "E_STATE_RECODING"
                E_UNKOWN -> return "E_UNKOWN"
                else -> return "E_UNKOWN"
            }
        }
    }

    companion object {
        private var mInstance: AudioRecordFunc? = null

        val instance: AudioRecordFunc
            @Synchronized get() {
                if (mInstance == null)
                    mInstance = AudioRecordFunc()
                return mInstance!!
            }
    }
} 