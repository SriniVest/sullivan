package org.manatee;

/**
 * Created by HyunJun on 2016-04-24.
 */
public class NeuralNetwork {

    double[] inputLayer;
    double[] hiddenLayer;
    double[] outputLayer;

    double[] hiddenLayerWeight;
    double[] outputLayerWeight;

    public NeuralNetwork(int inputNodes, int hiddenNodes) {

        this.inputLayer = new double[inputNodes];
        this.hiddenLayer = new double[hiddenNodes];
        this.outputLayer = new double[1];

        this.hiddenLayerWeight = new double[inputNodes * hiddenNodes];
        this.outputLayerWeight = new double[hiddenNodes * 1];

        randomizeWeights();
    }

    /**
     * 각 노드 간 Weight 랜덤화
     */
    private void randomizeWeights() {

        for (int i = 0; i < hiddenLayerWeight.length; i++)
            hiddenLayerWeight[i] = Math.random() * 2 - 1;

        for (int i = 0; i < outputLayerWeight.length; i++)
            outputLayerWeight[i] = Math.random() * 2 - 1;
    }

    public double evaluate(double[] input) {

        if (input.length != inputLayer.length) {
            System.out.println("입력 파라미터 에러");
            return Double.NaN;
        }

        feedForward(input, inputLayer);
        feedForward(inputLayer, hiddenLayer, hiddenLayerWeight);
        feedForward(hiddenLayer, outputLayer, outputLayerWeight);

        return outputLayer[0];
    }

    public double train(double[] input, double[] desiredOutput, double trainRate) {
        evaluate(input);
        return backPropagate(desiredOutput, trainRate);
    }

    /**
     * 레이어에 초기값 먹이기
     *
     * @param backwardLayer
     * @param forwardLayer
     */
    private void feedForward(double[] backwardLayer, double[] forwardLayer) {
        for (int i = 0; i < forwardLayer.length; i++)
            forwardLayer[i] = backwardLayer[i];
    }

    /**
     * 이전 레이어에서 다음 레이어로~
     *
     * @param backwardLayer
     * @param forwardLayer
     * @param weight
     */
    private void feedForward(double[] backwardLayer, double[] forwardLayer, double[] weight) {

        for (int i = 0; i < forwardLayer.length; i++) {
            double summation = 0;

            for (int j = 0; j < backwardLayer.length; j++)
                summation += backwardLayer[i] * weight[i * forwardLayer.length + j];

            forwardLayer[i] = activate(summation);
        }
    }

    /**
     * Output Layer에 대해 Desired Input을 비교했을 때, delta를 구한다.
     *
     * @param backwardLayer
     * @param forwardLayer
     * @return
     */
    private double[] getBackwardLayerDelta(double[] backwardLayer, double[] forwardLayer) {

        double[] backwardLayerDelta = new double[backwardLayer.length];

        for (int i = 0; i < backwardLayer.length; i++) {
            double error = forwardLayer[i] - backwardLayer[i];
            backwardLayerDelta[i] = error * deactivate(backwardLayer[i]);
        }
        return backwardLayerDelta;
    }

    /**
     * 그 외의 Layer에 대해 앞 Layer의 Input과 비교했을 때 delta를 구한다.
     *
     * @param backwardLayer
     * @param forwardLayer
     * @param forwardLayerWeight
     * @param forwardLayerDelta
     * @return
     */
    private double[] getBackwardLayerDelta(double[] backwardLayer, double[] forwardLayer, double[] forwardLayerWeight, double[] forwardLayerDelta) {

        double[] backwardLayerDelta = new double[backwardLayer.length];

        for (int i = 0; i < backwardLayer.length; i++) {
            double error = 0;
            for (int j = 0; j < forwardLayer.length; j++)
                error += forwardLayerWeight[j * forwardLayer.length + i] * forwardLayerDelta[j];
            backwardLayerDelta[i] = error * deactivate(backwardLayer[i]);
        }
        return backwardLayerDelta;
    }

    /**
     * 계산한 delta값을 토대로 Layer의 Weight 값을 업데이트한다.
     *
     * @param backwardLayer
     * @param forwardLayer
     * @param forwardLayerWeight
     * @param forwardLayerDelta
     * @param trainRate
     */
    private void applyDelta(double[] backwardLayer, double[] forwardLayer, double[] forwardLayerWeight, double[] forwardLayerDelta, double trainRate) {
        for (int i = 0; i < forwardLayer.length; i++) {
            for (int j = 0; j < backwardLayer.length; j++) {
                double delta = - (forwardLayerDelta[i] * backwardLayer[j]) * trainRate;
                forwardLayerWeight[i * forwardLayer.length + j] += delta;
            }
        }
    }

    /**
     * 역전파방법으로 신경망을 학습시킨다.
     *
     * @param desiredOutput
     * @param trainRate
     * @return
     */
    private double backPropagate(double[] desiredOutput, double trainRate) {

        double[] outputLayerDelta = getBackwardLayerDelta(outputLayer, desiredOutput);
        applyDelta(hiddenLayer, outputLayer, outputLayerWeight, outputLayerDelta, trainRate);

        double[] hiddenLayerDelta = getBackwardLayerDelta(hiddenLayer, outputLayer, outputLayerWeight, outputLayerDelta);
        applyDelta(inputLayer, hiddenLayer, hiddenLayerWeight, hiddenLayerDelta, trainRate);

        // 에러를 리턴한다.
        double error = 0;
        for (int i  = 0; i < outputLayer.length; i++) {
            error += 0.5 * Math.pow(outputLayer[i] - desiredOutput[i], 2);
        }
        return error;
    }

    public double activate(double value) {
        return Math.tanh(value);
    }

    /**
     * activate 함수의 미분
     *
     * @param value
     * @return
     */
    public double deactivate(double value) {
        return 1 - Math.pow(value, 2);
    }


}
