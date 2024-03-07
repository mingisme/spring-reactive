package com.example.springreactive;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
public class EchoController {

    // curl localhost:8080/hello
    @GetMapping("/hello")
    public Mono<String> hello() {
        return Mono.just("Hello, InfoWorld!");
    }

    @GetMapping("echo/{str}")
    public Mono<String> echo(@PathVariable String str) {
        return Mono.just("Echo: " + str);
    }

    @GetMapping("echoquery")
    public Mono<String> echoQuery(@RequestParam("name") String name) {
        return Mono.just("Hello, " + name);
    }

    // curl -X POST -F "file=@./README.md" http://localhost:8080/writefile
    @PostMapping(value = "/writefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> writeFile(@RequestPart("file") Flux<FilePart> filePartFlux) {
        Path path = Path.of("/tmp/file.txt");

        // Delete the existing file if it already exists
        FileSystemUtils.deleteRecursively(path.toFile());

        // Save the file parts to the specified path
        return filePartFlux
                .flatMap(filePart -> filePart.transferTo(path))
                .then(Mono.just("File saved: " + path.toString()));
    }

    // curl localhost:8080/character/10
    @GetMapping("character/{id}")
    public Mono<String> getCharacterData(@PathVariable String id) {
        WebClient client = WebClient.create("https://swapi.dev/api/people/");
        return client.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> "Character data: " + response);
    }

    @GetMapping("future")
    public Mono<String> future(@RequestParam String name) throws ExecutionException, InterruptedException {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(10000);
                System.out.println("sleep 3000 ms");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello, " + name;
        });
//        System.out.println(future.get());
        return Mono.fromFuture(future);
    }

}
