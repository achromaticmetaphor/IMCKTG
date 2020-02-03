package us.achromaticmetaphor.imcktg

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WAVETone(wav: File) {
    private val channel = RandomAccessFile(wav, "rw").channel
    private val head: MappedByteBuffer
    private var dataOffset = -1
    private var samplesPerSecond = 0
    private var silentSample: ByteBuffer? = null
    private fun dataSize(): Int {
        return head.getInt(dataOffset + 4)
    }

    @Throws(IOException::class)
    private fun extendData(bytes: Int): MappedByteBuffer {
        val dataSize = dataSize()
        var dataEnd = dataSize + dataOffset + 8.toLong()
        if (dataEnd and 1 != 0L) dataEnd++
        channel.map(FileChannel.MapMode.READ_WRITE, dataEnd + bytes, channel.size() - dataEnd).apply {
            put(channel.map(FileChannel.MapMode.READ_ONLY, dataEnd, channel.size() - dataEnd))
            force()
        }
        head.apply {
            putInt(dataOffset + 4, dataSize + bytes)
            putInt(4, head.getInt(4) + bytes)
            force()
        }
        return channel.map(FileChannel.MapMode.READ_WRITE, dataSize + dataOffset + 8.toLong(), bytes.toLong())
    }

    @Throws(IOException::class)
    fun appendSilence(milliseconds: Int) {
        val outsamples = milliseconds * samplesPerSecond / 1000
        val extended = extendData(outsamples * silentSample!!.capacity())
        for (i in 0 until outsamples) {
            silentSample!!.rewind()
            extended.put(silentSample!!)
        }
        extended.force()
    }

    @Throws(IOException::class)
    fun repeat(count: Int) {
        val dataSize = dataSize()
        val samples = channel.map(FileChannel.MapMode.READ_ONLY, dataOffset + 8.toLong(), dataSize.toLong())
        val extended = extendData(dataSize * count)
        for (i in 0 until count) {
            samples.rewind()
            extended.put(samples)
        }
        extended.force()
    }

    @Throws(IOException::class)
    fun close() {
        channel.close()
    }

    companion object {
        private const val FOURCC_RIFF = 0x46464952
        private const val FOURCC_WAVE = 0x45564157
        private const val FOURCC_FMT = 0x20746d66
        private const val FOURCC_DATA = 0x61746164
    }

    init {
        val mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
        mapped.order(ByteOrder.LITTLE_ENDIAN)
        try {
            require(!(mapped.getInt(0) != FOURCC_RIFF || mapped.getInt(8) != FOURCC_WAVE)) { "not WAVE" }
            var fmtOffset = -1
            var offset = 12
            while (offset < mapped.capacity()) {
                val chunkId = mapped.getInt(offset)
                if (chunkId == FOURCC_FMT) fmtOffset = offset
                if (chunkId == FOURCC_DATA) dataOffset = offset
                offset += mapped.getInt(offset + 4) + 8
                if (offset and 1 != 0) offset++
            }
            require(fmtOffset >= 0) { "no fmt  chunk" }
            require(dataOffset >= 0) { "no data chunk" }
            require(mapped.getShort(fmtOffset + 8).toInt() == 1) { "not PCM" }
            val nchannels = mapped.getShort(fmtOffset + 10)
            samplesPerSecond = mapped.getInt(fmtOffset + 12)
            val bitsPerSample = mapped.getShort(fmtOffset + 22)
            val bytesPerSample = nchannels * (bitsPerSample / 8) + if (bitsPerSample.toInt() and 7 != 0) 1 else 0
            silentSample = ByteBuffer.allocate(bytesPerSample)
            val silentByte = (if (bitsPerSample < 9) (1 shl bitsPerSample - 1) - 1 else 0).toByte()
            for (i in 0 until bytesPerSample) silentSample!!.put(silentByte)
        } catch (ioobe: IndexOutOfBoundsException) {
            throw IllegalArgumentException("invalid WAVE", ioobe)
        }
        head = channel.map(FileChannel.MapMode.READ_WRITE, 0, dataOffset + 8.toLong())
        head.order(ByteOrder.LITTLE_ENDIAN)
    }
}