package it.unibo.collektive

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.consensus.boundedElection
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gradientCast

fun Aggregate<Int>.cityEntrypoint(env: EnvironmentVariables, collektiveDevice: CollektiveDevice<*>): Double {
    val globalDistances = with(collektiveDevice) { distances() }
    val isDistrictLeader = (boundedElection(bound = 50.0, metric = globalDistances) == localId)
        .also { env["districtLeader"] = it }
    val districtLeaderId = gradientCast(source = isDistrictLeader, local = localId, metric = globalDistances)
    return alignedOn(districtLeaderId) {
        val districtDistances = with(collektiveDevice) { distances() }
        val isBlockLeader = (boundedElection(bound = 15.0, metric = districtDistances) == localId)
            .also { env["blockLeader"] = it }
        val blockLeaderId = gradientCast(source = isBlockLeader, local = localId, metric = districtDistances)
        alignedOn(blockLeaderId) {
            val blockDistances = with(collektiveDevice) { distances() }
            val distToBlock = distanceTo(source = isBlockLeader, metric = blockDistances)
            when {
                isDistrictLeader -> -20.0
                isBlockLeader -> -10.0
                else -> distToBlock
            }
        }
    }
}