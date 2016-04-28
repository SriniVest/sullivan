package org.sullivan;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PCM 데이터를 표현하는 클래스
 */
public class SLPcmData {

    /**
     * 음성 정보 데이터
     */
    public float[] data;

    /**
     * 인코딩 정보
     */
    public boolean isBigEndian = false;

    /**
     * 샘플 레이트
     */
    public int sampleRate;

    /**
     * 재생 시간
     */
    public float duration;

    public SLPcmData(float[] data, int sampleRate) {
        this.data = data;
        this.sampleRate = sampleRate;
        duration = data.length / sampleRate;
    }

    /**
     * 실시간 음성 처리가 가능한 AudioDispatcher를 반환한다.
     *
     * @param bufferSize
     * @param bufferOverlap
     * @return
     */
    public AudioDispatcher getAudioDispatcher(int bufferSize, int bufferOverlap) {
        try {
            AudioDispatcher dispatcher = AudioDispatcherFactory.fromFloatArray(data, sampleRate, bufferSize, bufferOverlap);
            return dispatcher;
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * .pronunciation 파일로 익스포트
     * 빠른 처리를 위해 압축은 하지 않는다.
     * <p>
     * 무조건 BIG ENDIAN으로 인코딩한다.
     */
    public static void export(SLPcmData pcmData, String path) {
        try (PrintWriter writer = new PrintWriter(path)) {

            writer.println(pcmData.sampleRate);

            if (pcmData.isBigEndian) {
                for (int i = 0; i < pcmData.data.length; i++) {
                    writer.println(pcmData.data[i]);
                }
            } else {
                for (int i = 0; i < pcmData.data.length; i++) {
                    writer.println(pcmData.data[pcmData.data.length - i - 1]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * PCM 데이터를 읽어온다
     *
     * @param source
     * @return
     */
    public static SLPcmData importPcm(File source) {
        try (BufferedReader br = new BufferedReader(new FileReader(source))) {
            int sampleRate = Integer.parseInt(br.readLine());
            List<Float> audioData = new ArrayList<>(4000);
            String line;
            while ((line = br.readLine()) != null) {
                audioData.add(Float.parseFloat(line));
            }
            float[] audioFloats = new float[audioData.size()];
            for (int i = 0; i < audioData.size(); i++) {
                audioFloats[i] = audioData.get(i);
            }

            return new SLPcmData(audioFloats, sampleRate);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 음성 데이터(Wave)를 PCM데이터로 변환한다.
     *
     * @param source
     * @return
     */
    public static SLPcmData importWav(File source) {

        AudioFormat format = null;

        TarsosDSPAudioFloatConverter converter = null;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(source);
            converter = TarsosDSPAudioFloatConverter.getConverter(JVMAudioInputStream.toTarsosDSPFormat(audioInputStream.getFormat()));

            format = audioInputStream.getFormat();

            int read;
            byte[] buffer = new byte[1024 * 1024];
            while ((read = audioInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();

        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }

        byte[] audioBytes = outputStream.toByteArray();
        byte[] audioBytesPadded = new byte[audioBytes.length * 4];

        System.arraycopy(audioBytes, 0, audioBytesPadded, 0, audioBytes.length);

        float[] audioFloats = new float[audioBytes.length];

        converter.toFloatArray(audioBytesPadded, 0, audioFloats, 0, audioFloats.length - 1);

        SLPcmData pcmData = new SLPcmData(audioFloats, (int) format.getSampleRate());
        pcmData.isBigEndian = format.isBigEndian();

        return pcmData;
    }

}
