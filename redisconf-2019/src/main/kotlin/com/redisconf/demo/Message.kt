package com.redisconf.demo

data class Message(val id: String, val name: String, val text: String)

data class MessageGeo(val ring: Message, val lat: Double, val lon: Double)
