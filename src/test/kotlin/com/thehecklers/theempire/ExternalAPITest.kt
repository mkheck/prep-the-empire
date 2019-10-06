package com.thehecklers.theempire

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList

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
        Mockito.`when`(repo.findAll()).thenReturn(listOf(ship1, ship2).asFlow())

        Mockito.`when`(repo.findShipByCaptain(ship1.captain)).thenReturn(listOf(ship1).asFlow())
        Mockito.`when`(repo.findShipByCaptain(ship2.captain)).thenReturn(listOf(ship2).asFlow())

        GlobalScope.launch {
            Mockito.`when`(repo.findOne(ship1.id!!)).thenReturn(ship1)
            Mockito.`when`(repo.findOne(ship2.id!!)).thenReturn(ship2)
        }
    }

    @Test
    fun `Get all 🚀s`() {
        client.get()
            .uri("/ships")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList<Ship>()
            .contains(ship1, ship2)
    }

    @Test
    fun `Get 🚀 by 🆔`() {
        client.get()
            .uri("/ships/{id}", ship2.id)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody<Ship>()
            .isEqualTo(ship2)
    }

    @Test
    fun `Get 🚀s with 👩‍✈️`() {
        client.get()
            .uri("/search?captain={captain}", ship1.captain)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList<Ship>()
            .contains(ship1)
    }
}