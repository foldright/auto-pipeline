package com.foldright.example.lb;

public interface Node {
    String id();

    int weight();

    boolean available();
}
