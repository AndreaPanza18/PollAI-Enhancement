package com.example.pollai.magazzino;

//@ nullable_by_default
public class Notifica {
    private String avvertimento;

    public void setAvvertimento(String avvertimento) {
        this.avvertimento = avvertimento;
    }

    public /*@ pure @*/ String getAvvertimento(){
        return avvertimento;
    }
}
