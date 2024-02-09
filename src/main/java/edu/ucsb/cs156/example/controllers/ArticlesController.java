package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Tag(name = "Articles")
@RequestMapping("/api/articles") // Corrected to match conventional REST paths
@RestController
@Slf4j
public class ArticlesController {

    @Autowired
    ArticlesRepository articlesRepository;

    @Operation(summary = "List all articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Articles> allArticles() {
        return articlesRepository.findAll();
    }

    @Operation(summary = "Create a new article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Articles postArticles(@RequestBody @Valid Articles articles) {
        // If dateAdded is not provided, use the current date and time
        articles.setDateAdded(articles.getDateAdded() != null ? articles.getDateAdded() : LocalDateTime.now());
        return articlesRepository.save(articles);
    }

    @Operation(summary = "Get an article by ID")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Articles getArticlesById(@Parameter(description="id") @RequestParam Long id) {
        return articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));
    }

    @Operation(summary = "Delete an article by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public void deleteArticles(@Parameter(description="id") @RequestParam Long id) {
        Articles articles = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));
        articlesRepository.delete(articles);
    }

    @Operation(summary = "Update an article by ID")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Articles updateArticles(@RequestBody @Valid Articles updatedArticles, @Parameter(description="id") @RequestParam Long id) {
        return articlesRepository.findById(id)
                .map(articles -> {
                    articles.setTitle(updatedArticles.getTitle());
                    articles.setUrl(updatedArticles.getUrl());
                    articles.setExplanation(updatedArticles.getExplanation());
                    articles.setEmail(updatedArticles.getEmail());
                    articles.setDateAdded(updatedArticles.getDateAdded() != null ? updatedArticles.getDateAdded() : articles.getDateAdded());
                    return articlesRepository.save(articles);
                }).orElseThrow(() -> new EntityNotFoundException(Articles.class, id));
    }
}
