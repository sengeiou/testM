package com.qingmeng.mengmeng.entity

class StaticDataBean(var systemStatic: ArrayList<StaticBean>, var version: String) {

    class StaticBean(var id: Int, var title: String, var icon: String, var describe: String, var type: Int, var skipId: Int, var skipType: Int)
}