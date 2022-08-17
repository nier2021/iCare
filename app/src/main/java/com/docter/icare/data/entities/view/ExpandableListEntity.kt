package com.docter.icare.data.entities.view


data class ExpandableListEntity(
    val titleList: List<String>? = null,
    val dataList: HashMap<String, List<String>>? = null,
    val imgList : ArrayList<Int>? = null
)
