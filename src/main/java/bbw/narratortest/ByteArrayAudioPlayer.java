package bbw.narratortest;


import com.sun.speech.freetts.audio.AudioPlayer;
import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ByteArrayAudioPlayer implements AudioPlayer {
    private final ByteArrayOutputStream outputStream;
    private AudioFormat audioFormat;

    public ByteArrayAudioPlayer(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void setAudioFormat(AudioFormat format) {
        this.audioFormat = format;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    @Override
    public boolean write(byte[] audioData, int offset, int size) {
        outputStream.write(audioData, offset, size);
        return true;
    }

    @Override
    public void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void begin(int size) {
    }

    @Override
    public boolean drain() {
        return true;
    }

    @Override
    public void showMetrics() {
    }

    @Override
    public boolean end() {
        return true;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 100;
    }

    @Override
    public void pause() {
    }

    @Override
    public void resetTime() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void setVolume(float arg0) {
    }

    @Override
    public void startFirstSampleTimer() {
    }

    @Override
    public boolean write(byte[] arg0) {
        return true;
    }
}
