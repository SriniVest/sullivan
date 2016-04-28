package org.sullivan;

import java.util.Arrays;

/**
 * 음성 데이터 앞뒤의 여백을 제거한다.
 * 지금은 특정 threshold 값 이상이 어느 수준까지 유지되느냐를 기준으로 여백을 판단하지만,
 * 더 발달된 버전에서는 뉴럴넷을 사용하여 여백을 학습시킬 수도 있을 듯 하다.
 */
public class SLSilenceTruncator {

    public static float THRESHOLD = 0.05f;
    public static float MIN_THRESHOLD_PERSISTENCE = 1 / 20;

    /**
     * 음성 데이터에서 여백을 제거한다.
     *
     * @param pcmData
     */
    public static void truncate(SLPcmData pcmData) {

        float thresholdPersistence = pcmData.sampleRate * MIN_THRESHOLD_PERSISTENCE;

        int validStartIndex = 0;
        int validEndIndex = 0;
        int validCount = 0;

        // 왼쪽
        for (int i = 0; i < pcmData.data.length; i++) {
            if (pcmData.data[i] > THRESHOLD) {
                if (validCount < 1)
                    validStartIndex = i;
                validCount++;
            } else {
                validCount = 0;
            }
            if (validCount > thresholdPersistence)
                break;
        }

        // 오른쪽
        validCount = 0;
        for (int i = pcmData.data.length - 1; i > 1; i--) {
            if (pcmData.data[i] > THRESHOLD) {
                if (validCount < 1)
                    validEndIndex = i;
                validCount++;
            } else {
                validCount = 0;
            }
            if (validCount > thresholdPersistence)
                break;
        }

        pcmData.data =  Arrays.copyOfRange(pcmData.data, validEndIndex, validStartIndex);
    }


}
