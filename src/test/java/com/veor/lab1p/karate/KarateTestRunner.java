package com.veor.lab1p.karate;

import com.intuit.karate.junit5.Karate;

class KarateTestRunner {

    @Karate.Test
    Karate testBankingAPI() {
        return Karate.run("bank").relativeTo(getClass());
    }

}
