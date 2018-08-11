package com.mycompany.categorizerservice.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final String[] categories = new String[]{"Sport", "World", "Science", "Entertainment", "Health"};
    private final Random random = new Random();

    @Override
    public String categorize(String title, String text) {
        return categories[random.nextInt(categories.length)];
    }

}
