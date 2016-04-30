package org.sullivan;


import java.util.List;

public interface SLFeatureExtractorListener {

    void onFeatureExtracted(List<float[]> featureMatrix);

}
