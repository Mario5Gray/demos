package com.demo.redisweb

import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands

val Gold = RingGeo(Ring("1", "Gold", "3+"), 13.361389, 38.115556)
val Silver = RingGeo(Ring("2", "Silver", "3+"), 15.087269, 37.502669)
val Bronze = RingGeo(Ring("3", "Bronze", "3+"), 13.583333, 37.316667)

val RandomPlace = Point(13.583333, 37.316667)
val RandomDistance = Distance(100.0, RedisGeoCommands.DistanceUnit.KILOMETERS)