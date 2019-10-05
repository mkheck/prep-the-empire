package com.thehecklers.theempire;

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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

        repo.deleteAll();

        for (x in 0..999)
            repo.save(
                Ship(
                    name = ships.get(rnd.nextInt(ships.size)),
                    captain = captains.get(rnd.nextInt(captains.size))
                )
            )

        repo.findAll().forEach { println(it) }
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

interface ShipRepository : CrudRepository<Ship, String> {
    fun findShipByCaptain(captain: String): Iterable<Ship>
}

@Document
data class Ship(@Id val id: String? = null, val name: String, val captain: String)