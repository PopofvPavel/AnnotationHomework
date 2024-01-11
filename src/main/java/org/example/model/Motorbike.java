package org.example.model;

import org.example.annotations.Autowired;
import org.example.annotations.Component;

@Component("Motorbike")
public class Motorbike {
    @Autowired
    private Engine engine;
    @Autowired
    private Body body;

    @Override
    public String toString() {
        return "Motorbike{" +
                "body=" + body +
                ", engine=" + engine +
                '}';
    }
}
