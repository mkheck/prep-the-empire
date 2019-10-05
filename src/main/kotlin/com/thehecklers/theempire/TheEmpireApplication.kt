package com.thehecklers.theempire;

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import javax.annotation.PostConstruct
import kotlin.random.Random

@SpringBootApplication
class TheEmpireApplication

fun main(args: Array<String>) {
    runApplication<TheEmpireApplication>(*args)
}

@Component
class DataLoader(private val repo: ShipRepository) {
    @PostConstruct
    fun loadShips() {
        val ships = listOf(
            "Ch'Tang",
            "Gr'oth",
            "Hegh'ta",
            "M'Char",
            "Maht-H'a",
            "Ning'tao",
            "Pagh",
            "T'Ong",
            "Vor'nak",
            "Ya'Vang"
        )
        val captains = listOf("Martok", "Koloth", "Kurn", "Kaybok", "Nu'Daq", "Lurkan", "Kargan", "K'Temoc", "Tanas")
        val rnd = Random

        repo.deleteAll().thenMany(
            (0..999).toFlux()
        )
            .map {
                Ship(
                    name = ships.get(rnd.nextInt(ships.size)),
                    captain = captains.get(rnd.nextInt(captains.size))
                )
            }
            .flatMap { repo.save(it) }
            .subscribe { println(it) }

    }
}

@RestController
class ShipController(private val repo: ShipRepository) {

    @GetMapping("/ships")
    fun getAllShips() = repo.findAll()

    @GetMapping("/ships/{id}")
    fun getShipById(@PathVariable id: String) = repo.findById(id)

    @GetMapping("/search")
    fun getShipsCaptainedBy(@RequestParam(defaultValue = "Martok") captain: String) = repo.findShipByCaptain(captain)
}

interface ShipRepository : ReactiveCrudRepository<Ship, String> {
    fun findShipByCaptain(captain: String): Flux<Ship>
}

@Document
data class Ship(@Id val id: String? = null, val name: String, val captain: String)