package com.nacoda.mapkotlinfirebase.Retrofit

import com.google.gson.annotations.SerializedName
import com.project.dzakdzak.mapskotlinfirebase.Init.Leg
import com.project.dzakdzak.mapskotlinfirebase.Init.OverviewPolyline


class Route {

    @SerializedName("copyrights")
    var copyrights: String? = null
    @SerializedName("legs")
    var legs: List<Leg>? = null
    @SerializedName("overview_polyline")
    var overviewPolyline: OverviewPolyline? = null
    @SerializedName("summary")
    var summary: String? = null
    @SerializedName("warnings")
    var warnings: List<Any>? = null
    @SerializedName("waypoint_order")
    var waypointOrder: List<Any>? = null

}
