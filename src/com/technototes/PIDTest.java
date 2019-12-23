package com.technototes;

// From a discussion with someone who's done this before:
// https://en.wikipedia.org/wiki/PID_controller
// Proportional: V = Kp * error
// Integral: V = Ki * sum of "recent" error over time
// Derivative: V = Kd * change of error over time

// V = P + I + D

public class PIDTest {

    float kp, ki, kd;

    int counts = 0;
    int whichHist = 0;

    // the amount of history to keep track of
    // for the integral portion of the value
    // (It's a circular array)
    float[] histErrFactor = new float[5];
    float prevTime = 0f;
    float prevError = 0f;
    float curTime = 0f;
    float curError = 0f;

    public PIDTest(float kPro, float kInt, float kDer) {
        kp = kPro;
        ki = kInt;
        kd = kDer;
        for (int i = 0; i < histErrFactor.length; i++) {
            histErrFactor[i] = 0f;
        }
    }

    // Record an observation point in time
    // for use by the P/I/D functions
    private void RecordPoint(float curErr, float curTim) {
        prevTime = curTime;
        prevError = curError;
        curTime = curTim;
        curError = curErr;
        histErrFactor[whichHist] = curError * (curTime - prevTime);
        whichHist = (whichHist + 1) % histErrFactor.length;
        counts = Math.max(histErrFactor.length, counts + 1);
    }

    // Proportional: V = Kp * error
    private float PFunc() {
        return kp * curError;
    }

    // Integral: V = Ki * approximate integral of error over time
    private float IFunc() {
        if (counts < histErrFactor.length) {
            return 0f;
        }
        float sum = 0f;
        for (int i = 0; i < histErrFactor.length; i++) {
            sum += histErrFactor[i];
        }
        return sum * ki;
    }

    // Derivative: V = Kd * change in error over time
    private float DFunc() {
        if (counts < 2) {
            return 0f;
        }
        float deltaE = curError - prevError;
        float deltaT = curTime - prevTime;
        return kd * deltaE / deltaT;
    }

    /**
     * Providing the current position, the current time, and the target position,
     * returns the speed to set the motors to.
     *
     * @param curErr  The current distance from target
     * @param curTime The current time, again in consistent units
     * @return the value using current data
     */
    public float getValue(float curErr, float curTime) {
        RecordPoint(curErr, curTime);
        return PFunc() + IFunc() + DFunc();
    }
}
