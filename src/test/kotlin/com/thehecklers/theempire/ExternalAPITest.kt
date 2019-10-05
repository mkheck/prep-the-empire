package com.thehecklers.theempire

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*

@WebFluxTest(ShipRouter::class)
internal class ExternalAPITest {
    @Autowired
    lateinit var client: WebTestClient

    @MockBean
    lateinit var repo: ShipRepository

    private val ship1 = Ship("000-SHIP-111", "Pagh", "Koloth")
    private val ship2 = Ship("000-SHIP-222", "P'Rang", "Martok")

    @BeforeEach
    fun setUp() {
        Mockito.`when`(repo.findAll()).thenReturn(Flux.just(ship1, ship2))
        Mockito.`when`(repo.findById(ship1.id!!)).thenReturn(Mono.just(ship1))
        Mockito.`when`(repo.findById(ship2.id!!)).thenReturn(Mono.just(ship2))
        Mockito.`when`(repo.findShipByCaptain(Optional.of(ship1.captain))).thenReturn(Flux.just(ship1))
        Mockito.`when`(repo.findShipByCaptain(Optional.of(ship2.captain))).thenReturn(Flux.just(ship2))
    }

    @Test
    fun `Get all 🚀s`() {
        StepVerifier.create(client.get()
            .uri("/ships")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .returnResult<Ship>()
            .responseBody)
            .expectNext(ship1)
            .expectNext(ship2)
            .verifyComplete()
    }

    @Test
    fun `Get 🚀 by 🆔`() {
        StepVerifier.create(client.get()
            .uri("/ships/{id}", ship2.id)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .returnResult<Ship>()
            .responseBody)
            .expectNext(ship2)
            .verifyComplete()
    }

    @Test
    fun `Get 🚀s with 👩‍✈️`() {
        StepVerifier.create(client.get()
            .uri("/search?captain={captain}", ship1.captain)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .returnResult<Ship>()
            .responseBody
            .take(1))
            .expectNext(ship1)
            .verifyComplete()
    }
}