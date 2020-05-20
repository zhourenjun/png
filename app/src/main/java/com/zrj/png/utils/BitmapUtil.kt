package com.zrj.png.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

object BitmapUtil {



    /**
     * 将Bitmap存为 .bmp格式图片
     */
    fun saveBmp(bitmap: Bitmap?) {
        if (bitmap == null) return
        // 位图大小
        val nBmpWidth = bitmap.width
        val nBmpHeight = bitmap.height
        // 图像数据大小  3 = 每个像素的位数
        val bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4)
        try {
            // 存储文件名
            val filename = Environment.getExternalStorageDirectory().absolutePath + "/ble.bmp"
            val file = File(filename)
            if (!file.exists()) {
                file.createNewFile()
            }
            val fileos = FileOutputStream(filename)
            //42 4D
            //00 00 00 00   整个文件大小
            //00 00
            //00 00
            //36 00 00 00

            // bmp文件头  5个字段
            val bfType = 0x4d42  //1-2：(这里的数字代表的是"字",即两个字节，下同）图像文件头。0x4d42=’BM’，表示是Windows支持的BMP格式
            val bfSize = 14 + 40 + bufferSize.toLong()  //3-6：整个文件大小
            val bfReserved1 = 0     //7-8：保留，必须设置为0
            val bfReserved2 = 0     //9-10：保留，必须设置为0。
            val bfOffBits = 14 + 40.toLong()  //11-14：从文件开始到位图数据之间的偏移量
            // 保存bmp文件头
            writeWord(fileos, bfType)
            writeDword(fileos, bfSize)
            writeWord(fileos, bfReserved1)
            writeWord(fileos, bfReserved2)
            writeDword(fileos, bfOffBits)


            //28 00 00 00
            //05 00 00 00
            //05 00 00 00
            //01 00
            //18 00
            //00 00 00 00
            //00 00 00 00
            //00 00 00 00
            //00 00 00 00
            //00 00 00 00
            //00 00 00 00

            // bmp信息头  11个字段
            val biSize = 40L    // 15-18  位图图信息头长度
            val biWidth = nBmpWidth.toLong()  //19-22：位图宽度，以像素为单位
            val biHeight = nBmpHeight.toLong()  //23-26：位图高度，以像素为单位
            val biPlanes = 1                      //27-28：位图的位面数，该值总是1
            val biBitCount =24                  //29-30：每个像素的位数(颜色深度)。1（单色），4（16色），8（256色）
                                                // 16（64K色，高彩色），24（16M色，真彩色），32（4096M色，增强型真彩色）
            val biCompression = 0L               //31-34：压缩说明：0（不压缩），1（RLE 8，8位RLE压缩），
                                                // 2（RLE 4，4位RLE压缩，3（Bitfields，位域存放）。RLE简单地说是采用像素数+像素值的方式进行压缩。
            // T408采用的是位域存放方式，用两个字节表示一个像素，位域分配为r5b6g5。图中0300 0000为00000003h=3
            val biSizeImage = 0L                //35-38：用字节数表示的位图数据的大小，该数必须是4的倍数，数值上等于（≥位图宽度的最小的4的倍数）×位图高度×每个像素位数。0090 0000为00009000h=80×90×2h=36864。
            val biXpelsPerMeter = 0L            //39-42：用象素/米表示的水平分辨率
            val biYPelsPerMeter = 0L            //43-46：用象素/米表示的垂直分辨率
            val biClrUsed = 0L                   //47-50：位图使用的颜色索引数。设为0的话，则说明使用所有调色板项。
            val biClrImportant = 0L              //51-54：对图象显示有重要影响的颜色索引的数目。如果是0，表示都重要。
            // 保存bmp信息头
            writeDword(fileos, biSize)
            writeLong(fileos, biWidth)
            writeLong(fileos, biHeight)
            writeWord(fileos, biPlanes)
            writeWord(fileos, biBitCount)
            writeDword(fileos, biCompression)
            writeDword(fileos, biSizeImage)
            writeLong(fileos, biXpelsPerMeter)
            writeLong(fileos, biYPelsPerMeter)
            writeDword(fileos, biClrUsed)
            writeDword(fileos, biClrImportant)
            //bitmap-24bit-5-by-5-data  图像的所有像素都设为black 意味着像素的总值为0或0x00 0x00 0x00十六进制表示
            //像素数据中的每一行都需要被4整除
            //00 00 00   00 00 00   00 00 00   00 00 00   00 00 00   00
            //00 00 00   00 00 00   00 00 00   00 00 00   00 00 00   00
            //00 00 00   00 00 00   00 00 00   00 00 00   00 00 00   00
            //00 00 00   00 00 00   00 00 00   00 00 00   00 00 00   00
            //00 00 00   00 00 00   00 00 00   00 00 00   00 00 00   00
            // 像素扫描  左下为图像左上
            val bmpData = ByteArray(bufferSize)
            val wWidth = nBmpWidth * 3 + nBmpWidth % 4  // 3 = 每个像素的位数 每一行补足4的倍数
            var nCol = 0
            var nRealCol = nBmpHeight - 1
            while (nCol < nBmpHeight) {
                var wRow = 0  //行
                var wByteIdex = 0
                while (wRow < nBmpWidth) {
                    val clr = bitmap.getPixel(wRow, nCol)
                    //图像的位深度为24，因此我们有3个字节按顺序表示BGR颜色， 第一个字节用于蓝色，第二个用于绿色，第三个用于红色通道。
                    bmpData[nRealCol * wWidth + wByteIdex] = Color.blue(clr).toByte()
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = Color.green(clr).toByte()
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = Color.red(clr).toByte()
                    wRow++
                    wByteIdex += 3
                }
                ++nCol
                --nRealCol
            }
            fileos.write(bmpData)
            fileos.flush()
            fileos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    //构造16位位图图像
    //一个16位的位图图像都类似于一个24位的位图图像。但是，现在只有2位可以定义颜色值。 我们需要对文件元数据进行的唯一更改是BitsPerPixel值为16或0x10十六进制。其余所有字段值应相同
    //[0 XXXXX XXXXX XXXXX]
    //     ----- ----- -----
    //       RGB
    //红色0111110000000000 => 01111100 00000000 => 0x7C 0x00
    //绿色0000001111100000 => 00000011 11100000 => 0x03 0xE0
    //蓝色0000000000011111 => 00000000 00011111 => 0x00 0x1F
    //白色0111111111111111 => 01111111 11111111 => 0x7F 0xFF

    // 5X5分辨率的16位图像的位图数据
    //由于像素颜色值由2个字节表示，并且我们连续有5个像素，因此每行总共有10个字节。但是由于一行必须具有可被4字节整除的字节，因此我们需要在每行中添加2个填充字节
    //42 4D
    //00 00 00 00
    //00 00
    //00 00
    //36 00 00 00
    //
    //28 00 00 00
    //05 00 00 00
    //05 00 00 00
    //01 00
    //10 00
    //00 00 00 00
    //00 00 00 00
    //00 00 00 00
    //00 00 00 00
    //00 00 00 00
    //00 00 00 00
    //
    //FF 7F 00 00 00 00 00 00 E0 7F 00 00
    //00 00 00 00 00 00 00 00 00 00 00 00
    //00 00 00 00 E0 03 00 00 00 00 00 00
    //00 00 00 00 00 00 00 00 00 00 00 00
    //00 7C 00 00 00 00 00 00 1F 00 00 00
    @Throws(IOException::class)
    private fun writeWord(stream: FileOutputStream, value: Int) {
        val b = ByteArray(2)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        stream.write(b)
    }

    @Throws(IOException::class)
    private fun writeDword(stream: FileOutputStream, value: Long) {
        val b = ByteArray(4)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        b[2] = (value shr 16 and 0xff).toByte()
        b[3] = (value shr 24 and 0xff).toByte()
        stream.write(b)
    }

    @Throws(IOException::class)
    private fun writeLong(stream: FileOutputStream, value: Long) {
        val b = ByteArray(4)
        b[0] = (value and 0xff).toByte()
        b[1] = (value shr 8 and 0xff).toByte()
        b[2] = (value shr 16 and 0xff).toByte()
        b[3] = (value shr 24 and 0xff).toByte()
        stream.write(b)
    }
}


