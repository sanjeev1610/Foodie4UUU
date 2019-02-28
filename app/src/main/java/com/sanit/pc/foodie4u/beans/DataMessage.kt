package com.sanit.pc.foodie4u.beans

class DataMessage {
    lateinit var to: String
    lateinit var data: Map<String, String>

    constructor() {}

    constructor(to: String, data: Map<String, String>) {
        this.to = to
        this.data = data
    }
}
