package com.thehecklers.theempire;

import lombok.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@SpringBootApplication
public class TheEmpireApplication {
    public static void main(String[] args) {
        SpringApplication.run(TheEmpireApplication.class, args);
    }
}

@Component
@AllArgsConstructor
class DataLoader {
    private final ShipRepository repo;

    @PostConstruct
    void loadShips() {
		List<String> ships = Arrays.asList("Enterprise", "Constitution", "Farragut", "Defiant", "Excalibur", "Exeter",
				"Lexington", "Hood", "Intrepid", "Voyager");
		List<String> captains = Arrays.asList("Adams", "Archer", "Decker", "Janeway", "Kirk", "Pike", "Sisko", "Spock",
				"Sulu", "Wesley");
		Random rnd = new Random();

    	repo.deleteAll();

		for (int x = 0; x < 1000; x++) {
			repo.save(new Ship(
				ships.get(rnd.nextInt(ships.size())),
				captains.get(rnd.nextInt(captains.size()))
			));
		}

		repo.findAll().forEach(System.out::println);
    }
}

@RestController
@AllArgsConstructor
class ShipController {
    private final ShipRepository repo;

    @GetMapping("/ships")
    Iterable<Ship> getAllShips() {
        return repo.findAll();
    }

    @GetMapping("/ships/{id}")
    Optional<Ship> getShipById(@PathVariable String id) {
        return repo.findById(id);
    }

    @GetMapping("/search")
    Iterable<Ship> getShipsCaptainedBy(@RequestParam(defaultValue = "Archer") String captain) {
        return repo.findShipByCaptain(captain);
    }
}

interface ShipRepository extends CrudRepository<Ship, String> {
    Iterable<Ship> findShipByCaptain(String captain);
}

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Document
class Ship {
    @Id
    private String id;
    @NonNull
    private String name, captain;
}