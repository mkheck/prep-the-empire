package com.thehecklers.theempire;

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
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

        GlobalScope.launch {
            repo.deleteAll()

            for (x in 0..999) {
                repo.insert(
                    Ship(
                        name = ships.get(rnd.nextInt(ships.size)),
                        captain = captains.get(rnd.nextInt(captains.size))
                    )
                )
            }

            repo.findAll().collect { println(it) }
        }
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

    suspend fun getAllShips(req: ServerRequest) = ok().bodyAndAwait(repo.findAll())

    suspend fun getShipById(req: ServerRequest) = ok()
        .bodyValueAndAwait(repo.findOne(req.pathVariable("id")))

    suspend fun getShipByCaptain(req: ServerRequest) = ok()
        .bodyAndAwait((repo.findShipByCaptain(req.queryParam("captain").orElse("Martok"))))
}

@Component
class ShipRepository(private val mongo: ReactiveFluentMongoOperations, private val om: ObjectMapper) {
    suspend fun count() = mongo.query<Ship>()
        .awaitCount()

    fun findAll() = mongo.query<Ship>()
        .flow()

    fun findShipByCaptain(captain: String) = mongo.query<Ship>()
        .matching(query(where("captain").isEqualTo(captain)))
        .flow()

    suspend fun findOne(id: String) = mongo.query<Ship>()
        .matching(query(where("id").isEqualTo(id)))
        .awaitOne()

    suspend fun deleteAll() {
        mongo.remove<Ship>()
            .allAndAwait()
    }

    suspend fun insert(ship: Ship) = mongo.insert<Ship>()
        .oneAndAwait(ship)

    suspend fun update(ship: Ship) = mongo.update<Ship>()
        .replaceWith(ship).asType<Ship>()
        .findReplaceAndAwait()!!
}

@Document
data class Ship(@Id val id: String? = null, val name: String, val captain: String)