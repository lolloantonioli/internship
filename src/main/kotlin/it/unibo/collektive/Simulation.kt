package it.unibo.collektive

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.accumulation.countDevices
import it.unibo.collektive.stdlib.consensus.boundedElection
import it.unibo.collektive.stdlib.spreading.gradientCast

fun Aggregate<Int>.entrypoint(env: EnvironmentVariables, collektiveDevice: CollektiveDevice<*>): Int {
    val distances = with(collektiveDevice) { distances() }

    // Elezione leader
    val isLeader = (boundedElection(bound = 20.0, metric = distances) == localId).also { env["leader"] = it }

    // Formazione regioni
    val closestLeader = electRegion(isLeader, distances)

    // conteggio nodi per regione
    val regionalCount = alignedOn(closestLeader) {
        countDevices(sink = isLeader)
    }

    return regionalCount
}

fun Aggregate<Int>.electRegion(isLeader: Boolean, distances: Field<Int, Double>): Int =
    gradientCast(source = isLeader, local = localId, metric = distances)
