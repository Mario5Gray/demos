package com.demo.redisweb

data class Ring(val id: String, val alloy: String, val size: String)
data class RingGeo(val ring: Ring, val lat: Double, val lon: Double)

val InitRings = listOf(
        RingGeo(Ring("1", "Gold", "3+"), 13.361389, 38.115556),
        RingGeo(Ring("2", "Silver", "3+"), 15.087269, 37.502669),
        RingGeo(Ring("3", "Bronze", "3+"), 13.583333, 37.316667))
