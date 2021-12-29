package com.foldright.examples.lb;

public interface Node {
    String id();

    int weight();

    boolean available();
}
