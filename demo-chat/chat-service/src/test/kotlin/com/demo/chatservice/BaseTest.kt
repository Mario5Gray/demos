package com.demo.chatservice

import org.mockito.Mockito

object TestUtil

fun <T> anyObject(): T {
    Mockito.anyObject<T>()
    return uninitialized()
}

fun <T> uninitialized(): T = null as T