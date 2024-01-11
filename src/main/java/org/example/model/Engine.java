package org.example.model;

import org.example.annotations.Autowired;
import org.example.annotations.Component;

@Component("Engine")
public class Engine {
    public void brr() {
        System.out.println("brrr");
    }
}
