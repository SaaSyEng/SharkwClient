package com.sharkev.client.util;

import java.util.Random;

/**
 * Human-like randomization utilities.
 *
 * WHY THIS MATTERS:
 * Math.random() and new Random().nextInt() produce UNIFORM distributions.
 * Human behavior follows GAUSSIAN (bell curve) distributions.
 * ACs like Watchdog and AAC analyze CPS/timing distributions over time
 * and flag clients with suspiciously uniform patterns.
 *
 * This class provides Gaussian-distributed values for all timings.
 */
public class RandomUtil {

    private static final Random rand = new Random();

    // ---------------------------------------------------------------
    // Gaussian CPS delay
    // Mean ~= 1000/cps ms, std dev ~10% of mean (matches real players)
    // ---------------------------------------------------------------

    /**
     * Returns the next click delay in ms for a given CPS target.
     * Distribution is Gaussian, matching real player click patterns.
     */
    public static long nextClickDelay(float targetCPS) {
        double mean   = 1000.0 / targetCPS;
        double stdDev = mean * 0.10; // 10% std deviation
        double delay  = rand.nextGaussian() * stdDev + mean;
        // Clamp to sane range (never < 30ms or > 2x mean)
        return (long) Math.max(30, Math.min(mean * 2, delay));
    }

    /**
     * Returns a CPS value drawn from a Gaussian distribution
     * centered on (min+max)/2.
     */
    public static float nextCPS(float min, float max) {
        float mean   = (min + max) / 2f;
        float stdDev = (max - min) / 4f; // 95% of values fall in [min, max]
        float cps    = (float)(rand.nextGaussian() * stdDev + mean);
        return Math.max(min, Math.min(max, cps));
    }

    // ---------------------------------------------------------------
    // Gaussian rotation noise
    // Real mouse micro-corrections are Gaussian, not uniform
    // ---------------------------------------------------------------

    /**
     * Gaussian rotation noise.
     * sigma: standard deviation in degrees (0.1 = barely visible, 1.0 = noticeable)
     */
    public static float rotNoise(float sigma) {
        return (float)(rand.nextGaussian() * sigma);
    }

    // ---------------------------------------------------------------
    // Timing jitter
    // Human reactions have Gaussian jitter around a base delay
    // ---------------------------------------------------------------

    /**
     * Add Gaussian jitter to a base delay.
     * stdDevMs: standard deviation in milliseconds
     */
    public static long jitter(long baseMs, long stdDevMs) {
        return baseMs + (long)(rand.nextGaussian() * stdDevMs);
    }

    // ---------------------------------------------------------------
    // Uniform helpers (for ranges where Gaussian is overkill)
    // ---------------------------------------------------------------

    public static float uniform(float min, float max) {
        return min + rand.nextFloat() * (max - min);
    }

    public static int uniformInt(int min, int max) {
        return min + rand.nextInt(max - min + 1);
    }

    public static boolean chance(float probability) {
        return rand.nextFloat() < probability;
    }
}
