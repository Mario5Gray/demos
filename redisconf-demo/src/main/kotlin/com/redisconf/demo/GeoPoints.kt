package com.redisconf.demo

import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands


val Mario = MessageGeo(Message("1", "Mario", "Its a me!"), 13.361389, 38.115556)
val Luigi = MessageGeo(Message("2", "Luigo", "Whoaaa"), 15.087269, 37.502669)
val Peach = MessageGeo(Message("3", "Peach", "Yipeee!"), 13.583333, 37.316667)

val RandomPlace = Point(13.583333, 37.316667)
val RandomDistance = Distance(100.0, RedisGeoCommands.DistanceUnit.KILOMETERS)