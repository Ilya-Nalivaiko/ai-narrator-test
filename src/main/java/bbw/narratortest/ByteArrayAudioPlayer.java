package bbw.narratortest;

import com.sun.speech.freetts.audio.AudioPlayer;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;

public class ByteArrayAudioPlayer implements AudioPlayer {
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    public final AudioFileFormat.Type targetType;
    private AudioFormat format;
    private long startTime = -1;
    private float volume = 1.0f;

    public ByteArrayAudioPlayer(AudioFileFormat.Type targetType) {
        this.targetType = targetType;
    }

    // Core write methods (keep existing)
    @Override
    public boolean write(byte[] audioData) {
        outputStream.write(audioData, 0, audioData.length);
        return true;
    }

    @Override
    public boolean write(byte[] audioData, int offset, int length) {
        outputStream.write(audioData, offset, length);
        return true;
    }

    public byte[] getAudioBytes() {
        return outputStream.toByteArray();
    }

    // Minimal timing implementation
    @Override
    public void startFirstSampleTimer() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void resetTime() {
        startTime = -1;
    }

    @Override
    public long getTime() {
        return startTime > 0 ? System.currentTimeMillis() - startTime : 0;
    }

    // Required format methods
    @Override
    public AudioFormat getAudioFormat() {
        return format;
    }

    @Override
    public void setAudioFormat(AudioFormat format) {
        this.format = format;
    }

    // Volume stubs (required but unused for byte capture)
    @Override
    public void setVolume(float volume) {
        this.volume = volume;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    // Other required methods with minimal implementations
    @Override
    public void cancel() {
        outputStream.reset();
    }

    @Override
    public void close() {
        // No resources to release
    }

    @Override
    public boolean drain() {
        return true; // Already writing immediately
    }

    @Override
    public boolean end() {
        return true; // No special cleanup needed
    }

    // Unused methods can remain empty
    @Override public void begin(int samples) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void reset() {}
    @Override public void showMetrics() {}
}