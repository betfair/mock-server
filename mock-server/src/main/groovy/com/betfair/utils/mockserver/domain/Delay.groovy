/*
 * Copyright (c) 2012 The Sporting Exchange Limited
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1.	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2.	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3.	Neither the names of The Sporting Exchange Limited, Betfair Limited nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */




package com.betfair.utils.mockserver.domain

class Delay {
    // A fixed delay period in milliseconds
    long timeMillis = 0
    // With an optional (non-zero) jitter added to the fixed delay.
    // The jitter limits are plus or minus the absolute jitter value.
    long jitter = 0

    private rand = new Random()


    public Delay(long timeMillis, long jitter) {
        this.timeMillis = Math.abs(timeMillis)
        this.jitter = Math.abs(jitter)
    }

    public def nextDelay() {
        if(this.jitter == 0) {
            return this.timeMillis
        }

        long variation = generate_gaussian_jitter()
        return this.timeMillis + variation
    }

    // How should jitter be calculated?
    // Options include:
    // uniform value within the range - no correlation to other values
    // uniform with correlation percent %
    // Normal/guassian distribution - favouring the middle value
    // Other fancy - pareto, paretonormal

    private def generate_gaussian_jitter() {
        // e.g. 10ms jitter, we want +/- 10ms variation in delay
        // countJitterOrdinals = 20
        int countJitterOrdinals = this.jitter * 2  // +/- jitter

        // 99% of the time this will be within the range +/- 2.0
        // That's how a gaussian normally distributed number works
        // Which as integers could be seen as 4 buckets
        // [-2.0 to -1.0, -1.0 to 0.0, 0.0 to 1.0, 1.0 to 2.0]
        // Those 4 buckets can be scaled to however many integer buckets
        // the range of possible variation values covers
        double gaussian = Double.NaN
        while(gaussian == Double.NaN || (gaussian < -2.0) || (gaussian > 2.0) ) {
            gaussian = rand.nextGaussian()
        }
        // For +2.0
        // 20/2 + (20 * (2.0/4))
        // 10 + 10
        // 20
        // For -2.0
        // 10 + (- 10)
        // 0
        // So basically a range of [0-20], which we adjust to be +/- 10
        int ordinalIndex = (int)( (countJitterOrdinals / 2) + (countJitterOrdinals * (gaussian / 4)) )

        int variation = ordinalIndex - this.jitter
        variation
    }
}
