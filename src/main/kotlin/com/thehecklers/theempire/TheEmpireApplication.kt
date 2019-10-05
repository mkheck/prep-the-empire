package com.thehecklers.theempire;

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.ok
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
    fun crouter() = coRouter {
        GET("/ships", ::getAllShips)
        GET("/ships/{id}", ::getShipById)
        GET("/search", ::getShipByCaptain)
    }

    suspend fun getAllShips(req: ServerRequest) = ok().body(repo.findAll()).awaitSingle()

    suspend fun getShipById(req: ServerRequest) = ok()
        .body(repo.findById(req.pathVariable("id"))).awaitSingle()

    // How to add @RequestParam(defaultValue = "Martok") to following handler ???
    suspend fun getShipByCaptain(req: ServerRequest) = ok()
        .body((repo.findShipByCaptain(req.queryParam("captain").orElse("Martok")))).awaitSingle()
}

interface ShipRepository : ReactiveCrudRepository<Ship, String> {
    fun findShipByCaptain(captain: String): Flux<Ship>
}

@Document
data class Ship(@Id val id: String? = null, val name: String, val captain: String)