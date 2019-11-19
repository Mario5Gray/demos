package com.streamy.orders.service

import java.util.*

data class Order(val id: UUID, val item: String, val count: Int)