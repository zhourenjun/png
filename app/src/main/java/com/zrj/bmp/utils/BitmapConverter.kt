package com.zrj.bmp.utils

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer

/**
 * 提供静态方法将Android位图对象转换为字节数组，该字节数组经*具体配置为以8位mod或24位mod格式表示.bmp文件，具体取决于配置
 *
 */
object BitmapConverter {
    // 将数据存储为bmp文件格式的缓冲区
    private lateinit var buffer: ByteBuffer

    // Different properties of bmp file format
    private var imageDataOffset = 0
    private var bytePerPixel = 0
    private var width = 0
    private var height = 0
    private var rowWidthInBytes = 0
    private var imageDataSize = 0
    private var fileSize = 0
    private lateinit var pixels: IntArray
    private lateinit var dummyBytesPerRow: ByteArray
    private var needPadding = false

    /**
     * 将Android Bitmap对象转换为bmp文件默认格式的字节数组
     */
    @JvmOverloads
    fun convert(
        bitmap: Bitmap,
        format: BitmapFormat = BitmapFormat.BITMAP_24_BIT_COLOR
    ): ByteArray {
        imageDataOffset = 0xE + 0x28  // 图像数据之前的偏移量   14  bmp文件头  // 40  bmp信息头
        bytePerPixel = format.value / 0x8

        // 图片尺寸
        width = bitmap.width
        height = bitmap.height

        // 一个从源图像接收像素的数组
        pixels = IntArray(width * height) //240*240 =57600

        // 行宽度（以字节为单位）
        rowWidthInBytes = bytePerPixel * width // 源图像宽度*编码一个像素的字节数。  2*240  3*240
        calculatePadding()

        // 文件中用于存储原始图像数据的字节数（不包括文件头）
        imageDataSize = (rowWidthInBytes + if (needPadding) dummyBytesPerRow.size else 0) * height
        // 文件的最终大小
        fileSize = imageDataSize + imageDataOffset

        // Android位图图像数据
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // 将包含位图数据的缓冲区
        buffer = ByteBuffer.allocate(fileSize)
        writeFileHeader() /* 位图文件头 */
        writeInfoHeader(format) /* 位图信息标题 */
        writeImageData(format) /* 图像数据 */
        return buffer.array()
    }


    /**
     * 每个图像行的字节数必须是4的倍数（bmp格式的要求）。 *如果图像行宽不是4个虚拟像素的倍数，则创建
     */
    private fun calculatePadding() {
        if (rowWidthInBytes % 4 != 0) {
            needPadding = true
            // 每行需要添加的虚拟字节
            dummyBytesPerRow = ByteArray(4 - rowWidthInBytes % 4)
            // 只需在数组中填充我们需要在每行末尾添加的虚拟字节
            for (i in dummyBytesPerRow.indices) {
                dummyBytesPerRow[i] = 0xFF.toByte()
            }
        }
    }

    /**
     * 将文件头写入缓冲区* 14字节 5个字段
     */
    private fun writeFileHeader() {
        // 位图特定签名（ASCII中的BM）
        buffer.put(0x42.toByte()) // B
        buffer.put(0x4D.toByte()) // M

        //最终文件的大小
        buffer.put(writeInt(fileSize))

        // 保留字节
        buffer.put(writeShort(0.toShort()))
        buffer.put(writeShort(0.toShort()))

        // 图像数据偏移
        buffer.put(writeInt(imageDataOffset))
    }

    /**
     *将Info标头写入缓冲区* 40字节 11个字段
     */
    private fun writeInfoHeader(format: BitmapFormat) {
        // 信息标题的大小
        buffer.put(writeInt(0x28))

        // 图像数据的宽度（行）和高度（列）
        buffer.put(writeInt(width + if (needPadding) if (dummyBytesPerRow.size == 3) 1 else 2 else 0))
        buffer.put(writeInt(height))

        //彩色平面->必须为1
        buffer.put(writeShort(1.toShort()))

        // 位计数（对应于不同的bmp文件格式）
        buffer.put(writeShort(format.value.toShort()))

        // 位压缩-> 0表示无
        buffer.put(writeInt(0))

        // 图像数据大小
        buffer.put(writeInt(imageDataSize))

        // 水平分辨率，以每米像素为单位
        buffer.put(writeInt(0x0B13))
        // 垂直分辨率，以每米像素为单位
        buffer.put(writeInt(0x0B13))

        //使用的颜色索引数
        buffer.put(writeInt(0x0))
        //重要颜色数-> 0表示全部
        buffer.put(writeInt(0x0))
    }

    /**
     * 将图像数据写入缓冲区*所有字节均从图像数据的末尾开始写入
     */
    private fun writeImageData(format: BitmapFormat) {
        var row = height
        val col = width
        var startPosition = (row - 1) * col
        var endPosition = row * col
        while (row > 0) {
            for (i in startPosition until endPosition) writeImageData(pixels[i], format)
            if (needPadding) buffer.put(dummyBytesPerRow)
            row--
            endPosition = startPosition
            startPosition -= col
        }
    }

    /**
     * 根据当前的BitmapFormat将灰度图像数据写入多个字节*不适用于少于8位的颜色格式
     */
    private fun writeImageData(pixel: Int, format: BitmapFormat) {
//        buffer.put((pixel and  0x000000FF).toByte())
//        buffer.put(((pixel and 0x0000FF00) shr 8).toByte())
//        buffer.put(((pixel and 0x00FF0000) shr 16).toByte())

        when (format.value / 8) {
            2 -> {

            }
            3 -> {
                buffer.put(Color.blue(pixel).toByte())
                buffer.put(Color.green(pixel).toByte())
                buffer.put(Color.red(pixel).toByte())
            }
        }
    }

    /**
     * Write int (16 bits) in a byte array (little-endian order)
     */
    private fun writeShort(value: Short): ByteArray {
        val b = ByteArray(2)
        b[0] = (value.toInt() and 0x00FF).toByte()
        b[1] = (value.toInt() and 0xFF00 shr 8).toByte()
        return b
    }

    /**
     * Write int (32 bits) in a byte array (little-endian order)
     */
    private fun writeInt(value: Int): ByteArray {
        val b = ByteArray(4)
        b[0] = (value and 0x000000FF).toByte()
        b[1] = (value and 0x0000FF00 shr 8).toByte()
        b[2] = (value and 0x00FF0000 shr 16).toByte()
        b[3] = (value and -0x1000000 shr 24).toByte()
        return b
    }
}


enum class BitmapFormat(val value: Int) {
    BITMAP_16_BIT_COLOR(16),
    BITMAP_24_BIT_COLOR(24);
}