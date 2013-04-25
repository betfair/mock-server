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




package com.betfair.utils.mockserver.domain;


import com.betfair.utils.mockserver.message.WeightedOutcomeRequest
import org.springframework.http.ResponseEntity

import javax.servlet.http.HttpServletResponse

public class WeightedOutcome implements Outcome {

    ArrayList<Outcome> outcomes
    ArrayList<Integer> weights
    Random rand = new Random()
    int weightsSum


    WeightedOutcome(Collection<WeightedOutcomeRequest> weightedOutcomes) {

        this.outcomes = new ArrayList<Outcome>(weightedOutcomes.collect {it.toResponse()})
        this.weights = new ArrayList<Integer>(weightedOutcomes.collect {it.weight})
        this.weightsSum = (int) this.weights.sum()
    }

    @Override
    ResponseEntity respondTo(HttpServletResponse response) throws IOException {
        if (outcomes.isEmpty()) {
            return null
        }
        int nextRand = rand.nextInt(weightsSum) // [0-weightsSum)
        int weightTally = 0

        for(int index = 0; index < weights.size(); ++index) {
            int weight = weights.get(index).intValue()

            if (nextRand < (weightTally + weight)) {
                Outcome outcome = outcomes.get(index)
                return outcome.respondTo(response)
           }

            weightTally += weight
        }

        assert false, "Shouldn't get here"
    }
}
