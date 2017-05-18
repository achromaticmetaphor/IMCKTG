package us.achromaticmetaphor.imcktg;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class WAVETone {

  private static final int FOURCC_RIFF = 0x46464952;
  private static final int FOURCC_WAVE = 0x45564157;
  private static final int FOURCC_FMT = 0x20746d66;
  private static final int FOURCC_DATA = 0x61746164;

  private final FileChannel channel;
  private final MappedByteBuffer head;

  private int dataOffset = -1;
  private int samplesPerSecond;
  private ByteBuffer silentSample;

  public WAVETone(File wav) throws IOException {
    channel = new RandomAccessFile(wav, "rw").getChannel();
    MappedByteBuffer mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    mapped.order(ByteOrder.LITTLE_ENDIAN);

    try {
      if (mapped.getInt(0) != FOURCC_RIFF || mapped.getInt(8) != FOURCC_WAVE)
        throw new IllegalArgumentException("not WAVE");

      int fmtOffset = -1;

      for (int offset = 12; offset < mapped.capacity(); ) {
        int chunkId = mapped.getInt(offset);
        if (chunkId == FOURCC_FMT)
          fmtOffset = offset;
        if (chunkId == FOURCC_DATA)
          dataOffset = offset;
        offset += mapped.getInt(offset + 4) + 8;
        if ((offset & 1) != 0)
          offset++;
      }

      if (fmtOffset < 0)
        throw new IllegalArgumentException("no fmt  chunk");

      if (dataOffset < 0)
        throw new IllegalArgumentException("no data chunk");

      if (mapped.getShort(fmtOffset + 8) != 1)
        throw new IllegalArgumentException("not PCM");

      short nchannels = mapped.getShort(fmtOffset + 10);
      samplesPerSecond = mapped.getInt(fmtOffset + 12);
      short bitsPerSample = mapped.getShort(fmtOffset + 22);

      int bytesPerSample = nchannels * (bitsPerSample / 8) + ((bitsPerSample & 7) != 0 ? 1 : 0);
      silentSample = ByteBuffer.allocate(bytesPerSample);
      byte silentByte = (byte) (bitsPerSample < 9 ? (1 << (bitsPerSample - 1)) - 1 : 0);
      for (int i = 0; i < bytesPerSample; i++)
        silentSample.put(silentByte);
    }
      catch (IndexOutOfBoundsException ioobe) {
        throw new IllegalArgumentException("invalid WAVE", ioobe);
      }

    head = channel.map(FileChannel.MapMode.READ_WRITE, 0, dataOffset + 8);
    head.order(ByteOrder.LITTLE_ENDIAN);
  }

  private int dataSize() {
    return head.getInt(dataOffset + 4);
  }

  private MappedByteBuffer extendData(int bytes) throws IOException {
    int dataSize = dataSize();
    long dataEnd = dataSize + dataOffset + 8;
    if ((dataEnd & 1) != 0)
      dataEnd++;

    MappedByteBuffer tail = channel.map(FileChannel.MapMode.READ_ONLY, dataEnd, channel.size() - dataEnd);
    MappedByteBuffer newTail = channel.map(FileChannel.MapMode.READ_WRITE, dataEnd + bytes, channel.size() - dataEnd);
    newTail.put(tail);
    newTail.force();

    head.putInt(dataOffset + 4, dataSize + bytes);
    head.putInt(4, head.getInt(4) + bytes);
    head.force();

    return channel.map(FileChannel.MapMode.READ_WRITE, dataSize + dataOffset + 8, bytes);
  }

  public void appendSilence(int milliseconds) throws IOException {
    int outsamples = milliseconds * samplesPerSecond / 1000;
    MappedByteBuffer extended = extendData(outsamples * silentSample.capacity());
    for (int i = 0; i < outsamples; i++) {
      silentSample.rewind();
      extended.put(silentSample);
    }
    extended.force();
  }

  public void repeat(int count) throws IOException {
    int dataSize = dataSize();
    MappedByteBuffer samples = channel.map(FileChannel.MapMode.READ_ONLY, dataOffset + 8, dataSize);
    MappedByteBuffer extended = extendData(dataSize * count);
    for (int i = 0; i < count; i++) {
      samples.rewind();
      extended.put(samples);
    }
    extended.force();
  }

  public void close() throws IOException {
    channel.close();
  }

}
