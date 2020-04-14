package test.io.smallrye.openapi.runtime.scanner.resources;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import test.io.smallrye.openapi.runtime.scanner.entities.Pet;

@RestController
@RequestMapping("/v2")
public class PetController {

    @RequestMapping(value = "/pet", produces = { "application/xml", "application/json" }, consumes = { "application/json",
            "application/xml" }, method = RequestMethod.POST)
    ResponseEntity<Void> addPet(@Valid @RequestBody Pet body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/pet/{petId}", produces = { "application/xml", "application/json" }, method = RequestMethod.DELETE)
    ResponseEntity<Void> deletePet(@PathVariable("petId") Long petId,
            @RequestHeader(value = "api_key", required = false) String apiKey) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/pet/findByStatus", produces = { "application/xml",
            "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<Pet>> findPetsByStatus(
            @NotNull @Valid @RequestParam(value = "status", required = true) List<String> status) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/pet/findByTags", produces = { "application/xml", "application/json" }, method = RequestMethod.GET)
    ResponseEntity<List<Pet>> findPetsByTags(@NotNull @Valid @RequestParam(value = "tags", required = true) List<String> tags) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/pet/{petId}", produces = { "application/xml", "application/json" }, method = RequestMethod.GET)
    ResponseEntity<Pet> getPetById(@PathVariable("petId") Long petId) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/pet", produces = { "application/xml", "application/json" }, consumes = { "application/json",
            "application/xml" }, method = RequestMethod.PUT)
    ResponseEntity<Void> updatePet(@Valid @RequestBody Pet body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/pet/{petId}", produces = { "application/xml", "application/json" }, consumes = {
            "application/x-www-form-urlencoded" }, method = RequestMethod.POST)
    ResponseEntity<Void> updatePetWithForm(@PathVariable("petId") Long petId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "status", required = false) String status) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @RequestMapping(value = "/pet/{petId}/uploadImage", produces = { "application/json" }, consumes = {
            "multipart/form-data" }, method = RequestMethod.POST)
    ResponseEntity<ModelApiResponse> uploadFile(@PathVariable("petId") Long petId,
            @RequestParam(value = "additionalMetadata", required = false) String additionalMetadata,
            @Valid @RequestPart(value = "file", required = false) MultipartFile file) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
}
