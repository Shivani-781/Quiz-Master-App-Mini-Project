// -- CATEGORY MODEL CLASS --

package com.example.quizmaster;

public class CategoryModel {

    // Declaring variables
    private String id;
    private String name;

    // Constructor and getter & setter methods
    public CategoryModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
