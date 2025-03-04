/*
 * Copyright (c) 2020, 2024 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.examples.integrations.micronaut.data;

import javax.validation.constraints.Pattern;

import io.helidon.examples.integrations.micronaut.data.model.Pet;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.metrics.annotation.Timed;

/**
 * JAX-RS resource, and the MicroProfile entry point to manage pets.
 * This resource used Micronaut data beans (repositories) to query database, and
 * bean validation as implemented by Micronaut.
 */
@Path("/pets")
public class PetResource {
    private final DbPetRepository petRepository;

    /**
     * Create a new instance with pet repository.
     *
     * @param petRepo Pet repository from Micronaut data
     */
    @Inject
    public PetResource(DbPetRepository petRepo) {
        this.petRepository = petRepo;
    }

    /**
     * Gets all pets from the database.
     * @return all pets, using JSON-B to map them to JSON
     */
    @GET
    public Iterable<Pet> getAll() {
        return petRepository.findAll();
    }

    /**
     * Get a named pet from the database.
     *
     * @param name name of the pet to find, must be at least two characters long, may contain whitespace
     * @return a single pet
     * @throws jakarta.ws.rs.NotFoundException in case the pet is not in the database (to return 404 status)
     */
    @Path("/{name}")
    @GET
    @Timed
    public Pet pet(@PathParam("name") @Pattern(regexp = "\\w+[\\w+\\s?]*\\w") String name) {
        return petRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("Pet by name " + name + " does not exist"));
    }
}
