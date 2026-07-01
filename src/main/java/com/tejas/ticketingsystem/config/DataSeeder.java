package com.tejas.ticketingsystem.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tejas.ticketingsystem.entity.Category;
import com.tejas.ticketingsystem.repository.CategoryRepository;

@Component
public class DataSeeder implements CommandLineRunner{
    private final CategoryRepository categoryRepository;

    public DataSeeder(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            categoryRepository.save(new Category(null, "ACCOUNT", 24));
            categoryRepository.save(new Category(null, "BILLING", 12));
            categoryRepository.save(new Category(null, "TECHNICAL", 48));
            categoryRepository.save(new Category(null, "GENERAL", 72));
        }
    }
    
}
