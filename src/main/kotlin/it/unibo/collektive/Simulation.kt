package it.unibo.collektive

import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.neighborhood
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.aggregate.api.share
import it.unibo.collektive.aggregate.values
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.stdlib.accumulation.countDevices
import it.unibo.collektive.stdlib.collapse.fold
import it.unibo.collektive.stdlib.collapse.maxBy
import it.unibo.collektive.stdlib.consensus.boundedElection
import it.unibo.collektive.stdlib.spreading.distanceTo
import it.unibo.collektive.stdlib.spreading.gradientCast
import it.unibo.collektive.stdlib.iterables.FieldedCollectionsExtensions.max

fun Aggregate<Int>.entrypoint(env: EnvironmentVariables, collektiveDevice: CollektiveDevice<*>): Int {
    val distances = with(collektiveDevice) { distances() }

    // Elezione leader
    val isLeader = (boundedElection(bound = 20) == localId).also { env["leader"] = it }

    // Formazione regioni
    val closestLeader = electRegion(isLeader, distances)

    // conteggio nodi per regione
    val regionalCount = alignedOn(closestLeader) {
        countDevices(sink = isLeader)
    }

    // leader propaga il conteggio
    val regionalDecision = broadcastDecision(isLeader, regionalCount, distances)

    return regionalDecision
}

fun Aggregate<Int>.electRegion(isLeader: Boolean, distances: Field<Int, Double>): Int =
    gradientCast(source = isLeader, local = localId, metric = distances)

fun Aggregate<Int>.broadcastDecision(isLeader: Boolean, value: Int, distances: Field<Int, Double>): Int =
    gradientCast(source = isLeader, local = value, metric = distances)
