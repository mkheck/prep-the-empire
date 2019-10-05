package com.thehecklers.theempire;

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import java.util.*
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

@Component
class ShipRouter(private val repo: ShipRepository) {
    @Bean
    fun shipRouting() = router {
        GET("/ships") { req -> ok().body(repo.findAll()) }
        GET("/ships/{id}") { req -> ok().body(repo.findById(req.pathVariable("id"))) }
        // How to add @RequestParam(defaultValue = "Martok") to following handler ???
        GET("/search") { req -> ok().body(repo.findShipByCaptain(req.queryParam("captain"))) }
    }
}

interface ShipRepository : ReactiveCrudRepository<Ship, String> {
    fun findShipByCaptain(captain: Optional<String>): Flux<Ship>
}

@Document
data class Ship(@Id val id: String? = null, val name: String, val captain: String)